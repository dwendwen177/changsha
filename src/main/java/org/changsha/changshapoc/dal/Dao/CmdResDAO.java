package org.changsha.changshapoc.dal.Dao;

import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Data
@Table(name = "cmd_res")
public class CmdResDAO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long priId;
    private String questionId;
    private String id;
    private String cmd;
    private Date logTime;
    private String loginUser;
    private String loginIp;
    private String agentConnectIp;
    private String remark;
    private String tagName;
}
