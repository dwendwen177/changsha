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
import org.changsha.changshapoc.entity.CmdAndHost;
import org.changsha.changshapoc.service.CmdService;
import org.changsha.changshapoc.service.FaultManageService;
import org.changsha.changshapoc.service.IntelligentDataService;
import org.changsha.changshapoc.service.MongoDBService;
import org.changsha.changshapoc.web.Common.ResponseResult;
import org.changsha.changshapoc.web.Common.SecurityAnalysisFullResponse;
import org.changsha.changshapoc.web.Common.SecurityAnalysisGroupResponse;
import org.changsha.changshapoc.web.Common.SecurityAnalysisResponse;
import org.changsha.changshapoc.web.Param.ExecSqlParam;
import org.changsha.changshapoc.web.Param.QueryMongoFullParam;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private static final String DATE_TIME_PATTERN = "^\\d{4}/\\d{2}/\\d{2}\\s*\\d{2}:\\d{2}:\\d{2}\\s*-\\s*\\d{4}/\\d{2}/\\d{2}\\s*\\d{2}:\\d{2}:\\d{2}$";

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    public static long[] parseToTimestamps(String dateTimeStr) {
        // 分割字符串以获取开始和结束时间
        String[] dateTimes = dateTimeStr.split(" - ");
        LocalDateTime startDate = LocalDateTime.parse(dateTimes[0].trim(), formatter);
        LocalDateTime endDate = LocalDateTime.parse(dateTimes[1].trim(), formatter);

        // 将LocalDateTime转换为Instant（考虑到时区，这里以系统默认时区为例）
        Instant startInstant = startDate.atZone(ZoneId.systemDefault()).toInstant();
        Instant endInstant = endDate.atZone(ZoneId.systemDefault()).toInstant();

        // 返回起始和结束时间的时间戳（到秒级别）
        return new long[]{startInstant.getEpochSecond(), endInstant.getEpochSecond()};
    }

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
    /**
     * 综合 queryMongoDB 和 queryMongoDBGroup两个接口
     * @param params.question_id 问题id
     * @param params.key_word 关键词
     *                  - 查询时间段场景: 2024/08/16 00:00:00 - 2024/08/17 23:59:59
     *                  - 查询关键词场景: 列名(type,remark,log_time等等)
     * @return
     */
    @RequestMapping(value = "/queryMongoDBFull", method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult queryMongoDBFull(@RequestBody QueryMongoFullParam params) throws IOException {
        String question_id = params.getQuestion_id();
        String key_word = params.getKey_word();
        log.info("[queryMongoDBFull] begin question_id: " + question_id + " key_word:", key_word);
        question_id = question_id.replaceAll("[^0-9]", "");
        key_word = key_word.replaceAll(" ", "");
        Pattern pattern = Pattern.compile(DATE_TIME_PATTERN);
        Matcher matcher = pattern.matcher(key_word.replaceAll("\\s+", ""));
        boolean bLogTime = matcher.matches();
        long begin_tp = 0;
        long end_tp = 0;

        if(bLogTime){
            begin_tp = parseToTimestamps(key_word)[0];
            end_tp = parseToTimestamps(key_word)[1];
        }
        log.info("[queryMongoDBFull] bLogTime:" + bLogTime + " begin_tp:" + begin_tp + " end_tp:"+ end_tp);

        SecurityAnalysisFullResponse resp = queryMongoDBFull(question_id, key_word, begin_tp, end_tp);

        log.info("[queryMongoDBFull] end:" + resp.toString());

        return ResponseResult.success(resp);
    }

    public SecurityAnalysisFullResponse queryMongoDBFull(String question_id, String key_word, long begin_tp, long end_tp) throws IOException {

        log.info("[queryMongoDBFull] internal");
        SecurityAnalysisFullResponse resp = new SecurityAnalysisFullResponse();
        boolean bLogTime = begin_tp != 0 || end_tp != 0;

        // 1. 查host
        JSONArray host = mongoDBService.getMongoDBData(hostCollection, hostLimit);
        JSONArray output = mongoDBService.getMongoDBData(outputCollection, outputLimit);
        SecurityAnalysisResponse securityAnalysisResponse = cmdService.handleCmd(output, host, question_id);

        if(bLogTime){
            List<CmdAndHost> cmdAndHosts = new ArrayList<>();
            for(CmdAndHost item: securityAnalysisResponse.getCmdAndHosts()){
                Long itemLogTimeSec = item.getLogTime().getTime() / 1000;
                if(itemLogTimeSec >= begin_tp && itemLogTimeSec <= end_tp){
                    cmdAndHosts.add(item);
                }
            }
            securityAnalysisResponse.setCmdAndHosts(cmdAndHosts);
        }
        resp.setSecurityAnalysisResponse(securityAnalysisResponse);
        log.info("[queryMongoDBFull] securityAnalysisResponse:" + securityAnalysisResponse);

        // 2. 查group
        List<Map<String, Object>> list;
        if(bLogTime){
            list = cmdResMapper.getCmdResByGroupWithTime(key_word, question_id, begin_tp, end_tp);
            key_word = "log_time";
        }else{
            list = cmdResMapper.getCmdResByGroup(key_word, question_id);
        }
        Map<String, Long> map = new HashMap<>();
        for (Map<String, Object> item : list) {
            if (item.get("key_value") == null) item.put("key_value", "unknown");
            map.put((String) item.get("key_value"), (Long) item.get("count_value"));
        }
        SecurityAnalysisGroupResponse securityAnalysisGroupResponse = new SecurityAnalysisGroupResponse();
        securityAnalysisGroupResponse.setMap(map);
        securityAnalysisGroupResponse.setGraphUrl("http://100.115.88.92:18090/high-risk-operation-group/" + question_id + "?type=" + key_word);
        resp.setSecurityAnalysisGroupResponse(securityAnalysisGroupResponse);
        log.info("[queryMongoDBFull] securityAnalysisGroupResponse:" + securityAnalysisGroupResponse);
        return resp;
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
        securityAnalysisGroupResponse.setGraphUrl("http://100.115.88.92:18090/high-risk-operation-group/" + numbersOnly + "?type=" + type);
        return ResponseResult.success(securityAnalysisGroupResponse);
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
        log.info("token1:" + token);
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        ActionTrace actionTrace = faultManageService.getFaultInfo(token);
        return ResponseResult.success(actionTrace);
        //return ResponseResult.success();
    }

}
