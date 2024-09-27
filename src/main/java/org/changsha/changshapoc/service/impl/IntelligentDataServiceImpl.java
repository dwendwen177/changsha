package org.changsha.changshapoc.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.changsha.changshapoc.dal.Dao.SqlResDAO;
import org.changsha.changshapoc.dal.Mapper.Primary.IntelligentDataMapper;
import org.changsha.changshapoc.dal.Mapper.Secondary.SqlResMapper;
import org.changsha.changshapoc.service.IntelligentDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class IntelligentDataServiceImpl implements IntelligentDataService {

    @Autowired
    IntelligentDataMapper studentsMapper;
    @Autowired
    SqlResMapper sqlResMapper;

    @Override
    public JSONObject executeSql(String sql) {

        //解析查询字段
        List<String> fields = extractFields(sql);
        System.out.println("Extracted Fields:");
        for (String field : fields) {
            log.info(field);
        }


        List<Map<String, Object>> maps = studentsMapper.executeDynamicSql(sql);
        UUID uuid = UUID.randomUUID();

        List<String> numberList = new ArrayList<>();
        List<String> stringList = new ArrayList<>();

        if(maps.get(0).entrySet().size() > 2){
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("value","poc只支持查询结果小于等于2列的数据");
            return jsonObject;
        }

        //不是序列不画图
        if(maps.size()==1){
            String jsonString = JSON.toJSONString(maps.get(0));
            SqlResDAO sqlResDAO = new SqlResDAO();
            sqlResDAO.setSeqid(uuid.toString());
            sqlResDAO.setSqlContent(sql);
            sqlResDAO.setRes(jsonString);
            int insert = sqlResMapper.insert(sqlResDAO);
            assert insert > 0:"插入记录失败";

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("isGraph",false);
            jsonObject.put("value",JSON.parseObject(jsonString));
            jsonObject.put("graphUrl","http://100.115.88.92:18090/intelligent-quest/"+uuid.toString());
            return jsonObject;
        }


        for (Map<String, Object> map : maps) {

            if(map.get(fields.get(0))==null){
                stringList.add("-");
            }else {
                stringList.add(map.get(fields.get(0)).toString());
            }

            if(map.get(fields.get(1))==null){
                numberList.add("0");
            }else {
                numberList.add(map.get(fields.get(1)).toString());
            }


//            for (Object value : map.values()) {
//                // 检查值的类型并添加到相应的列表中
//                if (value instanceof Number) {
//                    numberList.add(((Number) value).doubleValue());
//                } else if (value instanceof String) {
//                    stringList.add((String) value);
//                }
//            }
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("X", stringList.toArray());
        jsonObject.put("Y", numberList.toArray());

        SqlResDAO sqlResDAO = new SqlResDAO();
        sqlResDAO.setSeqid(uuid.toString());
        sqlResDAO.setSqlContent(sql);
        sqlResDAO.setRes(jsonObject.toJSONString());
        int insert = sqlResMapper.insert(sqlResDAO);
        assert insert > 0:"插入记录失败";

        jsonObject.put("isGraph",true);
        jsonObject.put("value",null);
        jsonObject.put("graphUrl","http://100.115.88.92:18090/intelligent-quest/"+uuid.toString());
        return jsonObject;
    }

    @Override
    public JSONObject getRes(String seqId) {
        SqlResDAO sqlResDAO = new SqlResDAO();
        sqlResDAO.setSeqid(seqId);
        SqlResDAO res = sqlResMapper.select(sqlResDAO).get(0);
        JSONObject jsonObject = JSON.parseObject(res.getRes());
        return jsonObject;
    }

    public List<String> extractFields(String sql) {
        List<String> fields = new ArrayList<>();
        // 匹配 SELECT 语句中的字段，包括函数形式的字段
        String regex = "(?i)select\\s+(.*?)\\s+from"; // Regex to match fields between SELECT and FROM
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(sql);
        if (matcher.find()) {
            String fieldList = matcher.group(1);
            String[] fieldArray = fieldList.split(",\\s*"); // 分割字段
            for (String field : fieldArray) {
                // 提取字段名并处理 AS
                String[] parts = field.split("\\s+as\\s+", 2); // 分割 AS
                if (parts.length == 2) {
                    // 如果有 AS，则取 AS 之后的部分作为字段名
                    fields.add(parts[1].trim());
                } else {
                    // 如果没有 AS，则取整个字段作为字段名
                    fields.add(field.trim());
                }
            }
        }
        return fields;
    }
}
