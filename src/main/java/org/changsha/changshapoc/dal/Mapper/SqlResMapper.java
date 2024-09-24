package org.changsha.changshapoc.dal.Mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.changsha.changshapoc.dal.Dao.SqlResDAO;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

@Repository
public interface SqlResMapper extends Mapper<SqlResDAO> {

}
