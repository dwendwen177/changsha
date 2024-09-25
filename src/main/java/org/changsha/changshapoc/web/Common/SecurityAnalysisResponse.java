package org.changsha.changshapoc.web.Common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.changsha.changshapoc.entity.CmdAndHost;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SecurityAnalysisResponse {
    List<CmdAndHost> cmdAndHosts;
    String graphUrl;
}
