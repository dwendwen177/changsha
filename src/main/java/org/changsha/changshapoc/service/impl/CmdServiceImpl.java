package org.changsha.changshapoc.service.impl;

import org.changsha.changshapoc.dal.Dao.CmdResDAO;
import org.changsha.changshapoc.dal.Mapper.Secondary.CmdResMapper;
import org.changsha.changshapoc.entity.Cmd;
import org.changsha.changshapoc.entity.CmdAndHost;
import org.changsha.changshapoc.entity.Host;
import org.changsha.changshapoc.service.CmdService;
import org.changsha.changshapoc.util.BeanCopyUtils;
import org.changsha.changshapoc.web.Common.SecurityAnalysisResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CmdServiceImpl implements CmdService {

    @Autowired
    CmdResMapper cmdResMapper;

    private static boolean isCommandFiltered(String cmd) {
        List<Pattern> gwRegex = Arrays.asList(
                Pattern.compile("reboot.*"),
                Pattern.compile("shutdown.*"),
                Pattern.compile("init 0.*"),
                Pattern.compile("init 6.*"),
                Pattern.compile("halt.*"),
                Pattern.compile("\\s*kill -9\\s+\\S*"),
                Pattern.compile("rm *"),
                Pattern.compile("rm -rf *"),
                Pattern.compile("\\s*service\\s+\\S+\\s+stop\\s*"),
                Pattern.compile("\\s*service\\s+\\S+\\s+start\\s*"),
                Pattern.compile("\\s*service\\s+\\S+\\s+restart\\s*")
        );
        for (Pattern pattern : gwRegex) {
            Matcher matcher = pattern.matcher(cmd);
            if (matcher.matches()) {
                return true;
            }
        }
        return false;
    }

//    public static List<JsonObject> loadConfig(String filePath) throws IOException {
//        ObjectMapper objectMapper = new ObjectMapper();
//        File file = new File(filePath);
//        List<JsonObject> output = objectMapper.readValue(file, TypeFactory.defaultInstance().constructCollectionType(List.class, JsonObject.class));
//        return output;
//    }

    @Override
    public SecurityAnalysisResponse handleCmd(JSONArray outputJsonArray, JSONArray hostJsonArray, String query) throws IOException {
        // 0. 从 mongoDB 获取 output.json, host.json 这 2 个文件
        // 1. 读取配置文件(output.json, host.json)
        // 读取文件内容
//        byte[] fileBytes = Files.readAllBytes(Paths.get("output.json"));
//        // 解析JSON数据
//        JSONObject jsonObject = new JSONObject(new String(fileBytes, "UTF-8"));
//        JSONArray outputJsonArray = jsonObject.getJSONArray("output");
//        JSONArray hostJsonArray = jsonObject.getJSONArray("host");
        String numbersOnly = query.replaceAll("[^0-9]", "");
        CmdResDAO testCmdResDAO = new CmdResDAO();
        testCmdResDAO.setQuestionId(numbersOnly);
        List<CmdResDAO> cmdResDAO1 = cmdResMapper.select(testCmdResDAO);
        boolean flag = false;
        if (cmdResDAO1 == null || cmdResDAO1.size() == 0) {
            flag = true;
        }

        // 2. 根据配置文件的内容，获取全部的 cmd 命令和 host 信息
        // 2.1 cmds
        List<Cmd> cmds = new ArrayList<>();
        for (int i = 0; i < outputJsonArray.length(); i++) {
            JSONObject cmdJson = outputJsonArray.getJSONObject(i);
            Cmd cmd = new Cmd();
            if (cmdJson.has("agentId") && !cmdJson.isNull("agentId")) cmd.setId(cmdJson.getString("agentId"));
            if (cmdJson.has("logTime") && !cmdJson.isNull("logTime")) cmd.setLogTime((Date) cmdJson.get("logTime"));
            if (cmdJson.has("cmd") && !cmdJson.isNull("cmd")) cmd.setCmd(cmdJson.getString("cmd"));
            if (cmdJson.has("loginUser") && !cmdJson.isNull("loginUser")) cmd.setLoginUser(cmdJson.getString("loginUser"));
            if (cmdJson.has("loginIp") && !cmdJson.isNull("loginIp")) cmd.setLoginIp(cmdJson.getString("loginIp"));
            cmds.add(cmd);
        }
        // 2.2 hosts
        List<Host> hosts = new ArrayList<>();
        for (int i = 0; i < hostJsonArray.length(); i++) {
            JSONObject hostJson = hostJsonArray.getJSONObject(i);
            Host host = new Host();
            if (hostJson.get("_id") != null) {
                if (hostJson.get("_id") instanceof String) host.set_id(hostJson.getString("_id"));
                else host.set_id(hostJson.getJSONObject("_id").getString("$oid"));
            }
            if (hostJson.has("remark") && !hostJson.isNull("remark")) host.setRemark(hostJson.getString("remark"));
            if (hostJson.has("agentConnectIp") && !hostJson.isNull("agentConnectIp")) host.setAgentConnectIp(hostJson.getString("agentConnectIp"));
            if (hostJson.has("hostTagMap") && !hostJson.isNull("hostTagMap")) host.setHostTagMap(hostJson.getJSONObject("hostTagMap").toMap());
            hosts.add(host);
        }

        // 3. 过滤匹配 gw_regex 模式的命令
        List<Cmd> filteredCmds = new ArrayList<>();
        for (int i = 0; i < cmds.size(); i++) {
            Cmd cmd = cmds.get(i);
            if (isCommandFiltered(cmd.getCmd())) filteredCmds.add(cmd);
        }

        // 4. 将 host.json 的内容和 output.json 的内容进行 join
        List<CmdAndHost> results =new ArrayList<>();
//        String questionId = UUID.randomUUID().toString();
//        questionId = questionId.replaceAll("-", "");
//        Random random = new Random();
//        int randomInt = random.nextInt(1000000000); // 生成0到999999999之间的随机整数
        String questionId = numbersOnly;
        for (int i = 0; i < filteredCmds.size(); i++) {
            for (int j = 0; j < hosts.size(); j++) {
                Host host = hosts.get(j);
                Cmd cmd = filteredCmds.get(i);
                if (host.get_id().equals(cmd.getId())) {
                    CmdAndHost cmdAndHost = BeanCopyUtils.copyObject(cmd, CmdAndHost.class);
                    cmdAndHost.setHostTagMap(host.getHostTagMap());
                    cmdAndHost.setRemark(host.getRemark());
                    cmdAndHost.setAgentConnectIp(host.getAgentConnectIp());
                    if (host.getHostTagMap() != null && host.getHostTagMap().containsKey("tagName") && host.getHostTagMap().get("tagName") != null) cmdAndHost.setTagName(host.getHostTagMap().get("tagName").toString());
                    CmdResDAO cmdResDAO = BeanCopyUtils.copyObject(cmdAndHost, CmdResDAO.class);
                    cmdResDAO.setQuestionId(questionId);
                    if (flag) cmdResMapper.insert(cmdResDAO);
                    results.add(cmdAndHost);
                }
            }
        }
        SecurityAnalysisResponse securityAnalysisResponse = new SecurityAnalysisResponse();
        securityAnalysisResponse.setCmdAndHosts(results);
        securityAnalysisResponse.setGraphUrl("http://100.115.88.92:18090/high-risk-operation/" + questionId);

        // 6. 将处理后的内容存到数据库，并返回 json 对象

        return securityAnalysisResponse;
    }
}
