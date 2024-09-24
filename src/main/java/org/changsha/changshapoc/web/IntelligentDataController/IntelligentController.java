/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.changsha.changshapoc.web.IntelligentDataController;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.changsha.changshapoc.web.Common.ResponseResult;
import org.changsha.changshapoc.web.demo.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

/**
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
@Controller
@RequestMapping("changsha/intelligentData")
@Slf4j
public class IntelligentController {

    @RequestMapping(value = "/execSqlMock", method = RequestMethod.GET)
    @ResponseBody
    public ResponseResult execSqlMock(@RequestParam(name = "query") String query) {

        String[] xarray = {"2024-01", "2024-02", "2024-03","2024-04", "2024-05"};
        Double[] yarray = {1525671.0, 1920391.0, 721637.0,84784839.0,1524351.0};
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Xname", "日期");
        jsonObject.put("Yname", "数量");
        jsonObject.put("X", Arrays.asList(xarray));
        jsonObject.put("Y", Arrays.asList(yarray));
        return ResponseResult.success(jsonObject.toString());
    }
}
