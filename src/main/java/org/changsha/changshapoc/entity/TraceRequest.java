package org.changsha.changshapoc.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Valid
public class TraceRequest {

    // 必填 action类型，允许值，TX,IF
    @NotBlank(message = "actionType cannot empty")
    @Pattern(regexp = "TX|IF", message = "actionType must be TX or IF")
    private String actionType;

    // 必填 业务系统 ID.
    @NotNull(message = "bizSystemId cannot empty")
    private Long bizSystemId;

    // 应用 id
    @NotNull(message = "applicationId cannot empty")
    private Long applicationId;

    // 业务/事务接口 ID
    private Long actionId;

    // 必填  选择的时长，单位为分钟
    @NotNull(message = "timePeriod cannot empty")
    private Long timePeriod;

    // 结束时间 yyyy-MM-dd HH:mm
//    private String endTime;

    // 必填
    private Long pageNumber;
    // 必填
    private Long pageSize;
    // 必填
    @NotBlank(message = "sortField cannot empty")
    private String sortField;
    // 必填 ASC DESC
//    @NotBlank(message = "sortDirection cannot empty")
//    @Pattern(regexp = "ASC|DESC", message = "actionType must be ASC or DESC")
//    private String sortDirection;
}
