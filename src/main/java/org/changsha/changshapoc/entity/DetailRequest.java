package org.changsha.changshapoc.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Valid
public class DetailRequest {

    // 必填 业务系统 ID.
    @NotNull(message = "bizSystemId cannot empty")
    private Long bizSystemId;

    // 必填 业务系统 ID.
    @NotNull(message = "traceId cannot empty")
    private String traceId;

    // 必填  选择的时长，单位为分钟
//    @NotNull(message = "timePeriod cannot empty")
//    private Long timePeriod;

    // 结束时间 yyyy-MM-dd HH:mm
//    private String endTime;
}
