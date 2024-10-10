package org.changsha.changshapoc.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Valid
public class DetailResponse {

    private Long bizSystemId;
    private String bizSystemName;
    private Long applicationId;
    private String applicationName;
    private Long instanceId;
    private String instanceName;
    private Long actionId;
    private String actionName;
    private String traceGuid;
    private String actionGuid;
    private String actionType;
    private Long timestamp;
    private Long respTime;
    private Long userId;
    private Long errorCount;
    private Long errorFnNo;
    private String uri;// 事务跳转链接
    private TimeLine timeLine;
    private Topology topology;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    static class TimeLine {
        private String actionGuid;
        private String className;// class
        private String method;
        private String metricType;
        private List<String> metricName;
        private Long exclusiveTime;
        private BigDecimal exclusiveRatio;
        private Long offset;
        private Long seq;
        private List<String> stackTraces;
        private List<TimeLine> subTimeLines;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    static class Topology {
        private List<Node> nodes;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        static class Node{
            private String name;
        }

    }
}
