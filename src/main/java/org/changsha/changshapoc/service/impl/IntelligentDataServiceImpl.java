package org.changsha.changshapoc.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.changsha.changshapoc.dal.Dao.SqlResDAO;
import org.changsha.changshapoc.dal.Mapper.IntelligentDataMapper;
import org.changsha.changshapoc.dal.Mapper.SqlResMapper;
import org.changsha.changshapoc.service.IntelligentDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class IntelligentDataServiceImpl implements IntelligentDataService {

    @Autowired
    IntelligentDataMapper studentsMapper;
    @Autowired
    SqlResMapper sqlResMapper;

    @Override
    public JSONObject executeSql(String sql,String seqId) {
        List<Map<String, Object>> maps = studentsMapper.executeDynamicSql(sql);

        List<Double> numberList = new ArrayList<>();
        List<String> stringList = new ArrayList<>();

        //不是序列不画图
        if(maps.size()==1){
            String jsonString = JSON.toJSONString(maps.get(0));
            SqlResDAO sqlResDAO = new SqlResDAO();
            sqlResDAO.setSeqid(seqId);
            sqlResDAO.setSqlContent(sql);
            sqlResDAO.setRes(jsonString);
            int insert = sqlResMapper.insert(sqlResDAO);
            assert insert > 0:"插入记录失败";

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("isGraph",false);
            jsonObject.put("value",JSON.parseObject(jsonString));
            jsonObject.put("graphUrl","/intelligent-quest/id="+seqId);
            return jsonObject;
        }

        for (Map<String, Object> map : maps) {
            for (Object value : map.values()) {
                // 检查值的类型并添加到相应的列表中
                if (value instanceof Number) {
                    numberList.add(((Number) value).doubleValue());
                } else if (value instanceof String) {
                    stringList.add((String) value);
                }
            }
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("X", stringList.toArray());
        jsonObject.put("Y", numberList.toArray());

        SqlResDAO sqlResDAO = new SqlResDAO();
        sqlResDAO.setSeqid(seqId);
        sqlResDAO.setSqlContent(sql);
        sqlResDAO.setRes(jsonObject.toJSONString());
        int insert = sqlResMapper.insert(sqlResDAO);
        assert insert > 0:"插入记录失败";

        jsonObject.put("isGraph",true);
        jsonObject.put("value",null);
        jsonObject.put("graphUrl","/intelligent-quest/id="+seqId);
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
