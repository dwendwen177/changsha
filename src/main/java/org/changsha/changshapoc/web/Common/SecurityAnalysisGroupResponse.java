package org.changsha.changshapoc.web.Common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SecurityAnalysisGroupResponse {
    private String graphUrl;
    private Map<String, Long> map;
}
