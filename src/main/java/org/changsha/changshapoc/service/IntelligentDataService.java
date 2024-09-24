package org.changsha.changshapoc.service;

import java.util.List;
import java.util.Map;

public interface IntelligentDataService {

    public List<Map<String, Object>> executeSql(String sql);

}
