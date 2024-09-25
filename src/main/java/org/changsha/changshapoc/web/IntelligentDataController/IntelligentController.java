/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.changsha.changshapoc.web.IntelligentDataController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.changsha.changshapoc.service.IntelligentDataService;
import org.changsha.changshapoc.entity.CmdAndHost;
import org.changsha.changshapoc.web.Common.ResponseResult;
import org.changsha.changshapoc.web.Param.ExecSqlParam;
import org.changsha.changshapoc.web.demo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
@Controller
@RequestMapping("/changsha/intelligentData")
@Slf4j
public class IntelligentController {

    @Autowired
    IntelligentDataService intelligentDataService;

    @RequestMapping(value = "/execSqlMock", method = RequestMethod.GET)
    @ResponseBody
    public ResponseResult execSqlMock(@RequestParam(name = "query") String query) {
        String[] xarray = {"2024-01", "2024-02", "2024-03","2024-04", "2024-05"};
        Double[] yarray = {1525671.0, 1920391.0, 721637.0,84784839.0,1524351.0};
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("X", xarray);
        jsonObject.put("Y", yarray);
        return ResponseResult.success(jsonObject);
    }

    @RequestMapping(value = "/execSql", method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult execSql(@RequestBody ExecSqlParam execSqlParam) {
        JSONObject jsonObject = intelligentDataService.executeSql(execSqlParam.getSql());
        return ResponseResult.success(jsonObject);
    }

    @RequestMapping(value = "/execSql", method = RequestMethod.GET)
    @ResponseBody
    public ResponseResult getRes(@RequestParam(name = "id") String id) {
        JSONObject jsonObject = intelligentDataService.getRes(id);
        return ResponseResult.success(jsonObject);
    }

    @RequestMapping(value = "/securityAnalysis", method = RequestMethod.GET)
    @ResponseBody
    public ResponseResult queryMongoDB(@RequestParam(name = "query", required = false) String query) {
        List<CmdAndHost> cmdAndHostList = new ArrayList<>();
        CmdAndHost cmdAndHost = new CmdAndHost();
        cmdAndHost.setId("1");
        cmdAndHost.setCmd("ls -l");
        cmdAndHost.setLogTime(new Date(System.currentTimeMillis()));
        cmdAndHost.setLoginUser("admin");
        cmdAndHost.setLoginIp("192.168.0.1");
        cmdAndHost.setAgentConnectIp("192.168.0.2");
        cmdAndHost.setRemark("测试数据");
        cmdAndHost.setTagName("tag1");
        cmdAndHost.setHostTagMap(new HashMap<>());
        cmdAndHostList.add(cmdAndHost);
        return ResponseResult.success(cmdAndHostList);
    }

    @RequestMapping(value = "/securityAnalysisGroup", method = RequestMethod.GET)
    @ResponseBody
    public ResponseResult queryMongoDBGroup(@RequestParam(name = "query", required = false) String query,
                                            @RequestParam(name = "type", required = false) String type) {

        Map<String, Integer> map = new HashMap<>();
        map.put("tag1", 10);
        map.put("tag2", 20);
        map.put("tag3", 30);
        return ResponseResult.success(map);
    }
}
