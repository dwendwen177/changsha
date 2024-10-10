package org.changsha.changshapoc.web.Common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SecurityAnalysisFullResponse {
    SecurityAnalysisResponse securityAnalysisResponse;
    SecurityAnalysisGroupResponse securityAnalysisGroupResponse;
}