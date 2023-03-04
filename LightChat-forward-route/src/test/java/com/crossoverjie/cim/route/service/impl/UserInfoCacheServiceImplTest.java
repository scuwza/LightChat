package com.crossoverjie.LightChat.route.service.impl;

import com.alibaba.fastjson.JSON;
import com.crossoverjie.LightChat.common.pojo.LightChatUserInfo;
import com.crossoverjie.LightChat.route.RouteApplication;
import com.crossoverjie.LightChat.route.service.UserInfoCacheService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Set;

@SpringBootTest(classes = RouteApplication.class)
@RunWith(SpringRunner.class)
public class UserInfoCacheServiceImplTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(UserInfoCacheServiceImplTest.class);


    @Autowired
    private UserInfoCacheService userInfoCacheService;

    @Test
    public void checkUserLoginStatus() throws Exception {
        boolean status = userInfoCacheService.saveAndCheckUserLoginStatus(2000L);
        LOGGER.info("status={}", status);
    }

    @Test
    public void removeLoginStatus() throws Exception {
        userInfoCacheService.removeLoginStatus(2000L);
    }

    @Test
    public void onlineUser(){
        Set<LightChatUserInfo> LightChatUserInfos = userInfoCacheService.onlineUser();
        LOGGER.info("LightChatUserInfos={}", JSON.toJSONString(LightChatUserInfos));
    }

}