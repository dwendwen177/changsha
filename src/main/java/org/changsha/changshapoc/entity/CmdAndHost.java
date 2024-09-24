package org.changsha.changshapoc.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CmdAndHost {
    private String id;
    private String cmd;
    private Long logTime;
    private String loginUser;
    private String loginIp;
    private String agentConnectIp;
    private String remark;
    private Map hostTagMap;
    private String tagName;
}
