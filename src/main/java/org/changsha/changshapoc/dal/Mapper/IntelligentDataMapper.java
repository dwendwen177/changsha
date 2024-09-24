package org.changsha.changshapoc.dal.Mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface IntelligentDataMapper {

    @Select("${sql}")
    List<Map<String, Object>> executeDynamicSql(@Param("sql") String sql);
}
