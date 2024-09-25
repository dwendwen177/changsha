package org.changsha.changshapoc.dal.Mapper.Secondary;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.changsha.changshapoc.dal.Dao.CmdResDAO;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

@Repository
public interface CmdResMapper extends Mapper<CmdResDAO> {
    @Select("select ${type} as key_value, count(cmd) as count_value from cmd_res where question_id = ${questionId} group by ${type}")
    List<Map<String, Object>> getCmdResByGroup(@Param("type") String type, @Param("questionId") String questionId);
}
