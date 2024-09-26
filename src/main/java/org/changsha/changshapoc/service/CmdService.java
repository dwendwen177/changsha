package org.changsha.changshapoc.service;

import org.changsha.changshapoc.entity.CmdAndHost;
import org.changsha.changshapoc.web.Common.SecurityAnalysisResponse;
import org.json.JSONArray;

import java.io.IOException;
import java.util.List;

public interface CmdService {

    public SecurityAnalysisResponse handleCmd(JSONArray outputJsonArray, JSONArray hostJsonArray, String query) throws IOException;

}
