package org.changsha.changshapoc.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.changsha.changshapoc.dal.Dao.SqlResDAO;
import org.changsha.changshapoc.dal.Mapper.Primary.IntelligentDataMapper;
import org.changsha.changshapoc.dal.Mapper.Secondary.SqlResMapper;
import org.changsha.changshapoc.service.IntelligentDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class IntelligentDataServiceImpl implements IntelligentDataService {

    @Autowired
    IntelligentDataMapper studentsMapper;
    @Autowired
    SqlResMapper sqlResMapper;

    @Override
    public JSONObject executeSql(String sql) {
        List<Map<String, Object>> maps = studentsMapper.executeDynamicSql(sql);
        UUID uuid = UUID.randomUUID();

        List<Long> numberList = new ArrayList<>();
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
            Iterator<Object> iterator = map.values().iterator();
            if (iterator.hasNext()) {
                stringList.add(String.valueOf(iterator.next()));
            }
            if (iterator.hasNext()) {
                numberList.add(Long.valueOf(String.valueOf(iterator.next())));
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
}
