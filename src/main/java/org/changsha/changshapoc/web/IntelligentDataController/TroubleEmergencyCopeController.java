package org.changsha.changshapoc.web.IntelligentDataController;

import org.changsha.changshapoc.entity.TraceRequest;
import org.changsha.changshapoc.entity.TraceResponse;
import org.changsha.changshapoc.service.TroubleEmergencyCopeService;
import org.changsha.changshapoc.web.Common.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController("trouble/emergency/cope")
public class TroubleEmergencyCopeController {

    @Autowired
    private TroubleEmergencyCopeService troubleEmergencyCopeService;

    @PostMapping("trace")
    public ResponseResult<List<TraceResponse>> trace(@RequestBody @Valid TraceRequest request) {
        String token = troubleEmergencyCopeService.getToken();
        return troubleEmergencyCopeService.trace(request, token);
    }


}
