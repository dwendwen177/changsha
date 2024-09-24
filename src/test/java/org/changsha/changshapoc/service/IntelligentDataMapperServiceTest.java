package org.changsha.changshapoc.service;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
class IntelligentDataMapperServiceTest {

    @Autowired
    IntelligentDataService intelligentDataService;

    @Test
    public void conflictTime() {
        intelligentDataService.executeSql("select date,count(1) from students group by date","2");
    }
}