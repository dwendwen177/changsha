package org.changsha.changshapoc.service.impl;

import org.changsha.changshapoc.entity.Cmd;
import org.changsha.changshapoc.entity.CmdAndHost;
import org.changsha.changshapoc.entity.Host;
import org.changsha.changshapoc.service.CmdService;
import org.changsha.changshapoc.util.BeanCopyUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CmdServiceImpl implements CmdService {

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
    public void handleCmd(JSONArray outputJsonArray, JSONArray hostJsonArray) throws IOException {
        // 0. 从 mongoDB 获取 output.json, host.json 这 2 个文件
        // 1. 读取配置文件(output.json, host.json)
        // 读取文件内容
//        byte[] fileBytes = Files.readAllBytes(Paths.get("output.json"));
//        // 解析JSON数据
//        JSONObject jsonObject = new JSONObject(new String(fileBytes, "UTF-8"));
//        JSONArray outputJsonArray = jsonObject.getJSONArray("output");
//        JSONArray hostJsonArray = jsonObject.getJSONArray("host");

        // 2. 根据配置文件的内容，获取全部的 cmd 命令和 host 信息
        // 2.1 cmds
        List<Cmd> cmds = new ArrayList<>();
        for (int i = 0; i < outputJsonArray.length(); i++) {
            JSONObject cmdJson = outputJsonArray.getJSONObject(i);
            Cmd cmd = new Cmd();
            cmd.setId(cmdJson.getString("agentId"));
            cmd.setLogTime(cmdJson.getLong("logTime"));
            cmd.setCmd(cmdJson.getString("cmd"));
            cmd.setLoginUser(cmdJson.getString("loginUser"));
            cmd.setLoginIp(cmdJson.getString("loginIp"));
            cmds.add(cmd);
        }
        // 2.2 hosts
        List<Host> hosts = new ArrayList<>();
        for (int i = 0; i < hostJsonArray.length(); i++) {
            JSONObject hostJson = hostJsonArray.getJSONObject(i);
            Host host = new Host();
            host.set_id(hostJson.getString("_id"));
            host.setRemark(hostJson.getString("remark"));
            host.setAgentConnectIp(hostJson.getString("agentConnectIp"));
            host.setHostTagMap(hostJson.getJSONObject("hostTagMap").toMap());
        }

        // 3. 过滤匹配 gw_regex 模式的命令
        List<Cmd> filteredCmds = new ArrayList<>();
        for (int i = 0; i < cmds.size(); i++) {
            Cmd cmd = cmds.get(i);
            if (isCommandFiltered(cmd.getCmd())) filteredCmds.add(cmd);
        }

        // 4. 将 host.json 的内容和 output.json 的内容进行 join
        List<CmdAndHost> results =new ArrayList<>();
        for (int i = 0; i < filteredCmds.size(); i++) {
            for (int j = 0; j < hosts.size(); j++) {
                Host host = hosts.get(j);
                Cmd cmd = filteredCmds.get(i);
                if (host.get_id().equals(cmd.getId())) {
                    CmdAndHost cmdAndHost = BeanCopyUtils.copyObject(cmd, CmdAndHost.class);
                    cmdAndHost.setHostTagMap(host.getHostTagMap());
                    cmdAndHost.setRemark(host.getRemark());
                    cmdAndHost.setAgentConnectIp(host.getAgentConnectIp());
                    cmdAndHost.setTagName(host.getHostTagMap().get("tagName").toString());
                    results.add(cmdAndHost);
                }
            }
        }

        // 6. 将处理后的内容保存为 csv 文件
    }
}
