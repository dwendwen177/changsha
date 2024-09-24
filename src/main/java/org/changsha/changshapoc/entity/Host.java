package org.changsha.changshapoc.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Host {
    private String _id;
    private String agentConnectIp;
    private String remark;
    private Map hostTagMap;
}
