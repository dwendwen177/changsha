package org.changsha.changshapoc.entity;

import lombok.Data;

@Data
public class TraceResponse {

    private Long id;
    private Long bizSystemId;
    private String bizSystemName;
    private Long applicationId;
    private String applicationName;
    private Long instanceId;
    private String instanceName;
    private Long timestamp;
    private String traceGuid;
    private String actionGuid;
    private String actionType;
    private Long actionId;
    private String actionName;
    private String respTime;
    private Long callerApplicationId;
    private Long userId;
    private Long callerBizSystemId;
    private Long callerInstanceId;
    private Long errorCount;
    private Long errorFnNo;
    private Boolean isShowTrace;

}
