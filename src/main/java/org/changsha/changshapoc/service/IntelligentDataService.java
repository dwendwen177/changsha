package org.changsha.changshapoc.service;

import com.alibaba.fastjson.JSONObject;

public interface IntelligentDataService {

    public JSONObject executeSql(String sql);

    public JSONObject getRes(String seqId);
}
