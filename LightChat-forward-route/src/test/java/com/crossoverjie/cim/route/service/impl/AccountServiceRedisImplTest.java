package com.crossoverjie.LightChat.route.service.impl;

import com.alibaba.fastjson.JSON;
import com.crossoverjie.LightChat.route.RouteApplication;
import com.crossoverjie.LightChat.route.api.vo.res.LightChatServerResVO;
import com.crossoverjie.LightChat.route.service.AccountService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@SpringBootTest(classes = RouteApplication.class)
@RunWith(SpringRunner.class)
public class AccountServiceRedisImplTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(AccountServiceRedisImplTest.class);

    @Autowired
    private AccountService accountService ;

    @Test
    public void loadRouteRelated() throws Exception {
        for (int i = 0; i < 100; i++) {

            Map<Long, LightChatServerResVO> longLightChatServerResVOMap = accountService.loadRouteRelated();
            LOGGER.info("longLightChatServerResVOMap={},cun={}" , JSON.toJSONString(longLightChatServerResVOMap),i);
        }
        TimeUnit.SECONDS.sleep(10);
    }

}