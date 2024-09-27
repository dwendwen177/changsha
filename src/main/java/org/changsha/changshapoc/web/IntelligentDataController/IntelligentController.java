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

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.changsha.changshapoc.dal.Dao.CmdResDAO;
import org.changsha.changshapoc.dal.Mapper.Secondary.CmdResMapper;
import org.changsha.changshapoc.entity.ActionTrace;
import org.changsha.changshapoc.service.CmdService;
import org.changsha.changshapoc.service.FaultManageService;
import org.changsha.changshapoc.service.IntelligentDataService;
import org.changsha.changshapoc.service.MongoDBService;
import org.changsha.changshapoc.web.Common.ResponseResult;
import org.changsha.changshapoc.web.Common.SecurityAnalysisGroupResponse;
import org.changsha.changshapoc.web.Common.SecurityAnalysisResponse;
import org.changsha.changshapoc.web.Param.ExecSqlParam;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
@Controller
@RequestMapping("/changsha/intelligentData")
@Slf4j
public class IntelligentController {

    @Value("${mongo.datasource.hostcollection}")
    private String hostCollection;

    @Value("${mongo.datasource.outputcollection}")
    private String outputCollection;

    @Value("${mongo.datasource.limit}")
    private Integer outputLimit;

    @Value("${mongo.datasource.limit}")
    private Integer hostLimit;

    @Value("${openapi.detail.delay}")
    private Long delay;

    @Autowired
    IntelligentDataService intelligentDataService;

    @Autowired
    private MongoDBService mongoDBService;

    @Autowired
    private CmdService cmdService;

    @Autowired
    private CmdResMapper cmdResMapper;

    @Autowired
    private FaultManageService faultManageService;

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

    @RequestMapping(value = "/queryMongoDB", method = RequestMethod.GET)
    @ResponseBody
    public ResponseResult queryMongoDB(@RequestParam(name = "query") String query) throws IOException {
        JSONArray host = mongoDBService.getMongoDBData(hostCollection, hostLimit);
        JSONArray output = mongoDBService.getMongoDBData(outputCollection, outputLimit);
        SecurityAnalysisResponse securityAnalysisResponse = cmdService.handleCmd(output, host, query);
        return ResponseResult.success(securityAnalysisResponse);
    }

    @RequestMapping(value = "/queryMongoDBGroup", method = RequestMethod.GET)
    @ResponseBody
    public ResponseResult queryMongoDBGroup(@RequestParam(name = "query") String query,
                                            @RequestParam(name = "type") String type) {
        String numbersOnly = query.replaceAll("[^0-9]", "");
        List<Map<String, Object>> list = cmdResMapper.getCmdResByGroup(type, numbersOnly);
        Map<String, Long> map = new HashMap<>();
        for (Map<String, Object> item : list) {
            if (item.get("key_value") == null) item.put("key_value", "unknown");
            map.put((String) item.get("key_value"), (Long) item.get("count_value"));
        }
        SecurityAnalysisGroupResponse securityAnalysisGroupResponse = new SecurityAnalysisGroupResponse();
        securityAnalysisGroupResponse.setMap(map);
        securityAnalysisGroupResponse.setGraphUrl("http://100.115.88.92:18090/high-risk-operation-group/" + numbersOnly);
        return ResponseResult.success(map);
        //return ResponseResult.success();
    }

    @RequestMapping(value = "/securityAnalysis", method = RequestMethod.GET)
    @ResponseBody
    public ResponseResult securityAnalysisList(@RequestParam(name = "query") String query) {
        String numbersOnly = query.replaceAll("[^0-9]", "");
        CmdResDAO cmdResDAO = new CmdResDAO();
        cmdResDAO.setQuestionId(numbersOnly);
        List<CmdResDAO> select = cmdResMapper.select(cmdResDAO);
        return ResponseResult.success(select);
    }

    @RequestMapping(value = "/securityAnalysisGroup", method = RequestMethod.GET)
    @ResponseBody
    public ResponseResult securityAnalysisGroup(@RequestParam(name = "query") String query,
                                            @RequestParam(name = "type") String type) {
        String numbersOnly = query.replaceAll("[^0-9]", "");
        List<Map<String, Object>> list = cmdResMapper.getCmdResByGroup(type, numbersOnly);
        Map<String, Long> map = new HashMap<>();
        for (Map<String, Object> item : list) {
            if (item.get("key_value") == null) item.put("key_value", "unknown");
            map.put((String) item.get("key_value"), (Long) item.get("count_value"));
        }
        return ResponseResult.success(map);
    }

    @RequestMapping(value = "/faultmanage/detail", method = RequestMethod.GET)
    @ResponseBody
    public ResponseResult queryDetail() {
        String token = faultManageService.getToken();
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        ActionTrace actionTrace = faultManageService.getFaultInfo(token);
        return ResponseResult.success(actionTrace);
    }

    @RequestMapping(value = "/faultmanage/detail2", method = RequestMethod.GET)
    @ResponseBody
    public ResponseResult queryDetail2() {
        String token = faultManageService.getToken();
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        ActionTrace actionTrace = faultManageService.getFaultInfo(token);
        return ResponseResult.success(actionTrace);
    }
}
