package org.changsha.changshapoc.service;

import org.changsha.changshapoc.entity.ActionTrace;

public interface FaultManageService {

    public String getToken();

    public ActionTrace getFaultInfo(String token);

}
