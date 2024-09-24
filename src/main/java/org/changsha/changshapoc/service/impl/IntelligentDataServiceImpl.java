package org.changsha.changshapoc.service.impl;

import org.changsha.changshapoc.dal.Mapper.IntelligentDataMapper;
import org.changsha.changshapoc.service.IntelligentDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class IntelligentDataServiceImpl implements IntelligentDataService {

    @Autowired
    IntelligentDataMapper studentsMapper;

    @Override
    public List<Map<String, Object>> executeSql(String sql) {
        List<Map<String, Object>> maps = studentsMapper.executeDynamicSql(sql);

        List<Double> numberList = new ArrayList<>();
        List<String> stringList = new ArrayList<>();

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
        System.out.println(numberList);
        System.out.println(stringList);
        return null;
    }
}
