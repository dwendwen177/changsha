package org.changsha.changshapoc.service;

import org.changsha.changshapoc.entity.ActionTrace;

public interface FaultManageService {

    public String getToken();

    public ActionTrace getFaultInfo(String token);

    public ActionTrace getFaultInfo2(String token);

    public ActionTrace getFaultInfo3(String token);

    public ActionTrace getFaultInfo99(String token);


}
