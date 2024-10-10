package org.changsha.changshapoc.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.changsha.changshapoc.entity.DetailRequest;
import org.changsha.changshapoc.entity.DetailResponse;
import org.changsha.changshapoc.entity.TraceRequest;
import org.changsha.changshapoc.entity.TraceResponse;
import org.changsha.changshapoc.service.TroubleEmergencyCopeService;
import org.changsha.changshapoc.web.Common.ResponseResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class TroubleEmergencyCopeServiceImpl implements TroubleEmergencyCopeService {

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
        String authStr = "api_key=" + apiKey + "&secret_key=" + secretKey + "&timestamp=" + timeMillis;
        String auth = DigestUtils.md5Hex(authStr);
        String apiUrl = tokenUrl + "/auth-api/auth/token?api_key=" + apiKey + "&auth=" + auth + "&timestamp=" + timeMillis;
        String response = callApi(apiUrl);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            log.info(jsonNode.toString());
            if (jsonNode.get("code").asInt() != 200) {
                log.error("Failed to get token, response code: {}, response message: {}.", jsonNode.get("code").asInt(), jsonNode.get("msg").asText());
                throw new RuntimeException("Failed to get token, response code: " + jsonNode.get("code").asInt());
            }
            return jsonNode.get("access_token").toString().replaceAll("\"", "");
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse response as JSON", e);
        }
    }

    @Override
    public ResponseResult<List<TraceResponse>> trace(TraceRequest request, String token) {
        String apiUrl = detailUrl + "/server-api/action/trace";
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        headers.put("Authorization", "Bearer " + token);
        HttpResponse<kong.unirest.JsonNode> response = Unirest.post(apiUrl)
                .headers(headers)
                .field("actionType", request.getActionType())
                .field("bizSystemId", String.valueOf(request.getBizSystemId()))
                .field("applicationId", String.valueOf(request.getApplicationId()))
                .field("actionId", String.valueOf(request.getActionId()))
                .field("timePeriod", "5")
                .field("endTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .field("pageNumber", request.getPageNumber() == null ? "1" : String.valueOf(request.getPageNumber()))
                .field("pageSize", request.getPageSize() == null ? "50" : String.valueOf(request.getPageSize()))
                .field("sortField", String.valueOf(request.getSortField()))
                .field("sortDirection", "DESC")
                .asJson();
        log.info("trace response: {}", response.getBody().toString());
        int statusCode = response.getStatus();
        if (!response.isSuccess()) {
            log.info("Failed to get trace, response code: {}", statusCode);
            throw new RuntimeException("Failed to get detail, response code: " + statusCode);
        }

        kong.unirest.JsonNode body = response.getBody();
        JSONObject jsonNode = body.getObject();

        if (jsonNode == null || !jsonNode.has("code") || jsonNode.getInt("code") != 200) {
            log.info("Failed to get trace, response code: {}", jsonNode.getInt("code"));
            throw new RuntimeException("Failed to get detail, response code: " + jsonNode.getInt("code"));
        }
        JSONObject dataNode = (JSONObject) jsonNode.get("data");
        JSONArray content = dataNode.getJSONArray("content");
        String string = content.toString();
        List<TraceResponse> traceResponses = com.alibaba.fastjson.JSONArray.parseArray(string, TraceResponse.class);

        return ResponseResult.success(traceResponses);
    }

    @Override
    public ResponseResult<DetailResponse> detail(DetailRequest request, String token) {
        String apiUrl = detailUrl + "/server-api/action/trace/detail";
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        headers.put("Authorization", "Bearer " + token);
        HttpResponse<kong.unirest.JsonNode> response = Unirest.post(apiUrl)
                .headers(headers)
                .field("bizSystemId", String.valueOf(request.getBizSystemId()))
                .field("traceId", request.getTraceId())
                .field("timePeriod", "5")
                .field("endTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .asJson();
        log.info("detail response: {}", response.getBody().toString());
        int statusCode = response.getStatus();
        if (!response.isSuccess()) {
            log.info("Failed to get detail, response code: {}", statusCode);
            throw new RuntimeException("Failed to get detail, response code: " + statusCode);
        }

        kong.unirest.JsonNode body = response.getBody();
        JSONObject jsonNode = body.getObject();

        if (jsonNode == null || !jsonNode.has("status") || jsonNode.getInt("status") != 200) {
            log.info("Failed to get detail, response code: {}", jsonNode.getInt("status"));
            throw new RuntimeException("Failed to get detail, response code: " + jsonNode.getInt("status"));
        }
        JSONObject dataNode = (JSONObject) jsonNode.get("data");
        String string = dataNode.toString();
        DetailResponse detailResponses = com.alibaba.fastjson.JSONObject.parseObject(string, DetailResponse.class);

        return ResponseResult.success(detailResponses);
    }

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
