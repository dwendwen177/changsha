package org.changsha.changshapoc.service;

import org.json.JSONArray;

import java.io.IOException;

public interface CmdService {

    public void handleCmd(JSONArray outputJsonArray, JSONArray hostJsonArray) throws IOException;

}
