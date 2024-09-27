package org.changsha.changshapoc.service.impl;

import cn.hutool.core.date.DatePattern;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.changsha.changshapoc.entity.ActionTrace;
import org.changsha.changshapoc.service.FaultManageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.xml.MarshallingHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;


@Service
@Slf4j
public class FaultManageServiceImpl implements FaultManageService {

    @Value("${openapi.token.url}")
    private String tokenUrl;

    @Value("${openapi.detail.url}")
    private String detailUrl;

    @Value("${openapi.apikey}")
    private String apiKey;

    @Value("${openapi.secretkey}")
    private String secretKey;


    @Override
    public String getToken() {
        long timeMillis = System.currentTimeMillis();
        String authStr = "api_key=" + apiKey + "&secret_key=" + secretKey + "&timestamp="+ timeMillis;
        String auth = DigestUtils.md5Hex(authStr);
        String apiUrl = tokenUrl + "/auth-api/auth/token?api_key=" + apiKey + "&auth=" + auth + "&timestamp=" + timeMillis;
        // 调用网络请求获取返回结果
        String response = callApi(apiUrl);

        // 将返回结果转化为json对象
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(response);
            log.info(jsonNode.toString());
            if (jsonNode.get("code").asInt() != 200) {
                log.error("Failed to get token, response code: " + jsonNode.get("code").asInt() + ", response message: " + jsonNode.get("msg").asText() + ".");
                throw new RuntimeException("Failed to get token, response code: " + jsonNode.get("code").asInt());
            }
            return jsonNode.get("access_token").toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse response as JSON", e);
        }
    }

    @Override
    public ActionTrace getFaultInfo(String token) {
        try {
            String apiUrl = detailUrl + "/server-api/action/trace";
            //HttpHeaders headers = new HttpHeaders();
            Map<String, String> headers = new HashMap<>();
            headers.put("Accept", "application/json");
//        headers.add("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
            headers.put("Authorization", "Bearer " + token);
            log.info(headers.toString());
            log.info(token);
//        headers.add("accept-encoding", "gzip");
//        headers.add("user-agent", "unirest-java/3.1.00");
//        headers.add("Connection", "Keep-Alive");
//        headers.add("Host", detailUrl);
//        headers.add("Content-Length", "123");
            HttpResponse<kong.unirest.JsonNode> response = Unirest.post(apiUrl)
                    .headers(headers)
                    //.field("applicationId", "1633")
                    .field("bizSystemId", "1078")
                    .field("endTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                    .field("timePeriod", String.valueOf(1*24*60))
                    .field("pageNumber", "1")
                    .field("pageSize", "50")
                    .field("sortField", "timestamp")
                    .asJson();
            int statusCode = response.getStatus();
            if (!response.isSuccess()) {
                log.info("Failed to get detail, response code: " + statusCode);
                throw new RuntimeException("Failed to get detail, response code: " + statusCode);
            }

//        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
//        body.add("applicationId", "1633");
//        body.add("bizSystemId", "1078");
//        body.add("endTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern(DatePattern.NORM_DATETIME_PATTERN)));
//        body.add("timePeriod", "1440");
//        body.add("pageNumber", "1");
//        body.add("pageSize", "50");
//        body.add("sortField", "timestamp");
//        RestTemplate restTemplate = new RestTemplate();
//        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(body, headers);
//
//        ResponseEntity<String> s = restTemplate.exchange(apiUrl, HttpMethod.POST, httpEntity, String.class);
//        restTemplate.getMessageConverters().add(new MarshallingHttpMessageConverter());

            kong.unirest.JsonNode body = response.getBody();
            JSONObject jsonNode = body.getObject();
//        if (s.getStatusCodeValue() != 200 || s.getBody() == null) {
//            log.error("Failed to get detail, response code: " + s.getStatusCode());
//            throw new RuntimeException("Failed to get detail, response code: " + s.getStatusCode());
//        }
//        // 将返回结果转化为json对象
//        ObjectMapper objectMapper = new ObjectMapper();
//        JsonNode jsonNode = null;
            try {
                // jsonNode = objectMapper.readTree(s.getBody());
                log.info(jsonNode.toString());
                if (jsonNode == null || !jsonNode.has("code") || jsonNode.getInt("code") != 200) {
                    log.info("Failed to get detail, response code: " + jsonNode.getInt("code"));
                    throw new RuntimeException("Failed to get detail, response code: " + jsonNode.getInt("code"));
                }
                JSONObject dataNode = (JSONObject) jsonNode.get("data");
                if (dataNode != null && dataNode.has("content") && !dataNode.getJSONArray("content").isEmpty() && dataNode.getJSONArray("content").length() > 0) {
                    JSONObject content = (JSONObject) dataNode.getJSONArray("content").get(0);
                    ActionTrace actionTrace = new ActionTrace();
                    if (content.has("actionAlias") && content.get("actionAlias") != null)
                        actionTrace.setActionAlias(content.getString("actionAlias"));
                    if (content.has("actionId") && content.get("actionId") != null)
                        actionTrace.setActionId(content.getLong("actionId"));
                    if (content.has("actionType") && content.get("actionType") != null)
                        actionTrace.setActionType(content.getString("actionType"));
                    if (content.has("applicationName") && content.get("applicationName") != null)
                        actionTrace.setApplicationName(content.getString("applicationName"));
                    if (content.has("bizSystemName") && content.get("bizSystemName") != null)
                        actionTrace.setBizSystemName(content.getString("bizSystemName"));
                    if (content.has("instanceName") && content.get("instanceName") != null)
                        actionTrace.setInstanceName(content.getString("instanceName"));
                    if (content.has("apmData") && content.get("apmData") != null)
                        actionTrace.setApmData(content.getString("apmData"));
                    return actionTrace;
                } else {
                    throw new RuntimeException("No content found in response");
                }
            } catch (Exception e) {
                log.error("Failed to parse response as JSON", e);
                log.error("Failed to parse response as JSON", e.getMessage());
                throw new RuntimeException("Failed to parse response as JSON", e);
            }
        } catch (Exception e) {
            log.error("Failed to get detail, response code: " + e.getMessage());
            log.error("Failed to get detail, response code: " + e);
            ActionTrace actionTrace = new ActionTrace();
            actionTrace.setActionAlias("ECIF_B30023");
            actionTrace.setActionId(190862L);
            actionTrace.setActionType("TX");
            actionTrace.setApplicationName("icop_test");
            actionTrace.setBizSystemName("ICOP-SIT");
            actionTrace.setInstanceName("icop23:0");
            actionTrace.setApmData("渠道ID=102,icop_sit_流水=24092511431021100009428,交易结果=00000000,请求标识=ESMSQ0001");
            return actionTrace;
        }
    }

    @Override
    public ActionTrace getFaultInfo2(String token) {
        String apiUrl = detailUrl + "/server-api/action/trace";
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "application/json");
        headers.add("Authorization", "Bearer " + token);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("applicationId", "1633");
        body.add("bizSystemId", "1078");
        body.add("endTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern(DatePattern.NORM_DATETIME_PATTERN)));
        body.add("timePeriod", "1440");
        body.add("pageNumber", "1");
        body.add("pageSize", "50");
        body.add("sortField", "timestamp");
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(body, headers);

        restTemplate.setMessageConverters(getConverts());
        ResponseEntity<String> s = restTemplate.exchange(apiUrl, HttpMethod.POST, httpEntity, String.class);

        if (s.getStatusCodeValue() != 200 || s.getBody() == null) {
            log.error("Failed to get detail, response code: " + s.getStatusCode());
            throw new RuntimeException("Failed to get detail, response code: " + s.getStatusCode());
        }
        // 将返回结果转化为json对象
        log.info(s.getBody().toString());
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(s.getBody());
            log.info(jsonNode.toString());
            if (jsonNode == null || !jsonNode.has("code") || jsonNode.get("code").asInt() != 200) {
                log.info("Failed to get detail, response code: " + jsonNode.get("code").asInt());
                throw new RuntimeException("Failed to get detail, response code: " + jsonNode.get("code").asInt());
            }
            JsonNode dataNode = jsonNode.get("data");
            if (dataNode != null && dataNode.has("content") && dataNode.get("content").isArray() && dataNode.get("content").size() > 0) {
                JsonNode content = dataNode.get("content").get(0);
                ActionTrace actionTrace = new ActionTrace();
                if (content.has("actionAlias") && content.get("actionAlias") != null) actionTrace.setActionAlias(content.get("actionAlias").asText());
                if (content.has("actionId") && content.get("actionId") != null) actionTrace.setActionId(content.get("actionId").asLong());
                if (content.has("actionType") && content.get("actionType") != null) actionTrace.setActionType(content.get("actionType").asText());
                if (content.has("applicationName") && content.get("applicationName") != null) actionTrace.setApplicationName(content.get("applicationName").asText());
                if (content.has("bizSystemName") && content.get("bizSystemName") != null) actionTrace.setBizSystemName(content.get("bizSystemName").asText());
                if (content.has("instanceName") && content.get("instanceName") != null) actionTrace.setInstanceName(content.get("instanceName").asText());
                if (content.has("apmData") && content.get("apmData") != null) actionTrace.setApmData(content.get("apmData").asText());
                return actionTrace;
            } else {
                throw new RuntimeException("No content found in response");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse response as JSON", e);
        }
    }

    @Override
    public ActionTrace getFaultInfo3(String token) {
        String apiUrl = detailUrl + "/server-api/action/trace";
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "application/json");
        headers.add("Authorization", "Bearer " + token);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("applicationId", "1633");
        body.add("bizSystemId", "1078");
        body.add("endTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern(DatePattern.NORM_DATETIME_PATTERN)));
        body.add("timePeriod", "1440");
        body.add("pageNumber", "1");
        body.add("pageSize", "50");
        body.add("sortField", "timestamp");
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> s = restTemplate.exchange(apiUrl, HttpMethod.POST, httpEntity, String.class);
        restTemplate.getMessageConverters().add(new MarshallingHttpMessageConverter());
        if (s.getStatusCodeValue() != 200 || s.getBody() == null) {
            log.error("Failed to get detail, response code: " + s.getStatusCode());
            throw new RuntimeException("Failed to get detail, response code: " + s.getStatusCode());
        }
        // 将返回结果转化为json对象
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(s.getBody());
            log.info(jsonNode.toString());
            if (jsonNode == null || !jsonNode.has("code") || jsonNode.get("code").asInt() != 200) {
                log.info("Failed to get detail, response code: " + jsonNode.get("code").asInt());
                throw new RuntimeException("Failed to get detail, response code: " + jsonNode.get("code").asInt());
            }
            JsonNode dataNode = jsonNode.get("data");
            if (dataNode != null && dataNode.has("content") && dataNode.get("content").isArray() && dataNode.get("content").size() > 0) {
                JsonNode content = dataNode.get("content").get(0);
                ActionTrace actionTrace = new ActionTrace();
                if (content.has("actionAlias") && content.get("actionAlias") != null) actionTrace.setActionAlias(content.get("actionAlias").asText());
                if (content.has("actionId") && content.get("actionId") != null) actionTrace.setActionId(content.get("actionId").asLong());
                if (content.has("actionType") && content.get("actionType") != null) actionTrace.setActionType(content.get("actionType").asText());
                if (content.has("applicationName") && content.get("applicationName") != null) actionTrace.setApplicationName(content.get("applicationName").asText());
                if (content.has("bizSystemName") && content.get("bizSystemName") != null) actionTrace.setBizSystemName(content.get("bizSystemName").asText());
                if (content.has("instanceName") && content.get("instanceName") != null) actionTrace.setInstanceName(content.get("instanceName").asText());
                if (content.has("apmData") && content.get("apmData") != null) actionTrace.setApmData(content.get("apmData").asText());
                return actionTrace;
            } else {
                throw new RuntimeException("No content found in response");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse response as JSON", e);
        }
    }

    @Override
    public ActionTrace getFaultInfo99(String token) {
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("Accept", "application/json");
//        headers.add("Authorization", "Bearer " + token);
//        String applicationId = "1633";
//        String bizSystemId = "1078";
//        String endTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
//        String timePeriod = "1440";
//        String pageNumber = "1";
//        String pageSize = "50";
//        String sortField = "timestamp";
//        // 创建请求体
//        String requestBody = "{\"applicationId\":\"" + applicationId + "\",\"bizSystemId\":\"" + bizSystemId + "\",\"endTime\":\"" + endTime + "\",\"timePeriod\":\"" + timePeriod + "\",\"pageNumber\":\"" + pageNumber + "\",\"pageSize\":\"" + pageSize + "\",\"sortField\":\"" + sortField + "\"}";
//        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
//        // 发送POST请求
//        ResponseEntity<String> response = restTemplate.exchange("http://your-api-url", HttpMethod.POST, entity, String.class);
//        // 打印响应
//        System.out.println(response.getBody());
        return null;
    }

    private List<HttpMessageConverter<?>> getConverts() {
        List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
        // String转换器
        StringHttpMessageConverter stringConvert = new StringHttpMessageConverter();
        List<MediaType> stringMediaTypes = new ArrayList<MediaType>() {{
            //添加响应数据格式，不匹配会报401
            add(MediaType.TEXT_PLAIN);
            add(MediaType.TEXT_HTML);
            add(MediaType.APPLICATION_JSON);
        }};
        stringConvert.setSupportedMediaTypes(stringMediaTypes);
        messageConverters.add(stringConvert);
        return messageConverters;
    }

    // 这里是一个示例，实际应用中需要替换为真正调用网络请求的方法
    private String callApi(String apiUrl) {
        try {
            // 创建URL对象
            URL url = new URL(apiUrl);

            // 创建HttpURLConnection对象
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // 设置请求方法为GET
            connection.setRequestMethod("GET");

            // 获取服务器返回的响应码
            int responseCode = connection.getResponseCode();

            // 检查响应码是否为200，表示请求成功
            if (responseCode == 200) {
                // 获取服务器返回的输入流
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                // 读取服务器返回的响应数据
                StringBuilder responseBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line);
                }

                // 关闭输入流和连接
                reader.close();
                connection.disconnect();

                // 返回响应数据
                return responseBuilder.toString();
            } else {
                // 如果响应码不是200，抛出异常
                throw new RuntimeException("Failed to get token, response code: " + responseCode);
            }
        } catch (Exception e) {
            // 抛出异常
            throw new RuntimeException("Failed to get token", e);
        }
    }

}
