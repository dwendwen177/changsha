package org.changsha.changshapoc.service;

import org.changsha.changshapoc.entity.TraceRequest;
import org.changsha.changshapoc.entity.TraceResponse;
import org.changsha.changshapoc.web.Common.ResponseResult;

import javax.validation.Valid;
import java.util.List;

public interface TroubleEmergencyCopeService {

    String getToken();

    ResponseResult<List<TraceResponse>> trace(@Valid TraceRequest request, String token);
}
