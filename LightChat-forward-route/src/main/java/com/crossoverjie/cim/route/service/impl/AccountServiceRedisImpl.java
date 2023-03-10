package com.crossoverjie.LightChat.route.service.impl;

import com.crossoverjie.LightChat.common.core.proxy.ProxyManager;
import com.crossoverjie.LightChat.common.enums.StatusEnum;
import com.crossoverjie.LightChat.common.exception.LightChatException;
import com.crossoverjie.LightChat.common.pojo.LightChatUserInfo;
import com.crossoverjie.LightChat.common.util.RouteInfoParseUtil;
import com.crossoverjie.LightChat.route.api.vo.req.ChatReqVO;
import com.crossoverjie.LightChat.route.api.vo.req.LoginReqVO;
import com.crossoverjie.LightChat.route.api.vo.res.LightChatServerResVO;
import com.crossoverjie.LightChat.route.api.vo.res.RegisterInfoResVO;
import com.crossoverjie.LightChat.route.service.AccountService;
import com.crossoverjie.LightChat.route.service.UserInfoCacheService;
import com.crossoverjie.LightChat.server.api.ServerApi;
import com.crossoverjie.LightChat.server.api.vo.req.SendMsgReqVO;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static com.crossoverjie.LightChat.common.enums.StatusEnum.OFF_LINE;
import static com.crossoverjie.LightChat.route.constant.Constant.ACCOUNT_PREFIX;
import static com.crossoverjie.LightChat.route.constant.Constant.ROUTE_PREFIX;

/**
 * Function:
 *
 * @author crossoverJie
 * Date: 2018/12/23 21:58
 * @since JDK 1.8
 */
@Service
public class AccountServiceRedisImpl implements AccountService {
    private final static Logger LOGGER = LoggerFactory.getLogger(AccountServiceRedisImpl.class);

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private UserInfoCacheService userInfoCacheService;

    @Autowired
    private OkHttpClient okHttpClient;

    @Override
    public RegisterInfoResVO  register(RegisterInfoResVO info) {
        // ACCOUNT_PREFIX = "LightChat-account:"
        String key = ACCOUNT_PREFIX + info.getUserId();

        String name = redisTemplate.opsForValue().get(info.getUserName());
        if (null == name) {
            //?????????????????????????????????
            redisTemplate.opsForValue().set(key, info.getUserName());
            redisTemplate.opsForValue().set(info.getUserName(), key);
        } else {
            long userId = Long.parseLong(name.split(":")[1]);
            info.setUserId(userId);
            info.setUserName(info.getUserName());
        }

        return info;
    }

    @Override
    public StatusEnum login(LoginReqVO loginReqVO) throws Exception {
        //??????Redis?????????
        String key = ACCOUNT_PREFIX + loginReqVO.getUserId();
        String userName = redisTemplate.opsForValue().get(key);
        if (null == userName) {
            return StatusEnum.ACCOUNT_NOT_MATCH;
        }

        if (!userName.equals(loginReqVO.getUserName())) {
            return StatusEnum.ACCOUNT_NOT_MATCH;
        }

        //?????????????????????????????????
        boolean status = userInfoCacheService.saveAndCheckUserLoginStatus(loginReqVO.getUserId());
        if (status == false) {
            //????????????
            return StatusEnum.REPEAT_LOGIN;
        }

        return StatusEnum.SUCCESS;
    }

    @Override
    public void saveRouteInfo(LoginReqVO loginReqVO, String msg) throws Exception {
        String key = ROUTE_PREFIX + loginReqVO.getUserId();
        redisTemplate.opsForValue().set(key, msg);
    }

    @Override
    public Map<Long, LightChatServerResVO> loadRouteRelated() {

        Map<Long, LightChatServerResVO> routes = new HashMap<>(64);


        RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
        // LightChat-route: {key:LightChat-route:userid  val:127.0.0.1:12112:8081}
        ScanOptions options = ScanOptions.scanOptions()
                .match(ROUTE_PREFIX + "*")
                .build();
        Cursor<byte[]> scan = connection.scan(options);

        while (scan.hasNext()) {
            byte[] next = scan.next();
            String key = new String(next, StandardCharsets.UTF_8);
            LOGGER.info("key={}", key);
            parseServerInfo(routes, key);

        }
        try {
            scan.close();
        } catch (IOException e) {
            LOGGER.error("IOException", e);
        }

        return routes;
    }

    @Override
    public LightChatServerResVO loadRouteRelatedByUserId(Long userId) {
        String value = redisTemplate.opsForValue().get(ROUTE_PREFIX + userId);

        if (value == null) {
            throw new LightChatException(OFF_LINE);
        }

        LightChatServerResVO LightChatServerResVO = new LightChatServerResVO(RouteInfoParseUtil.parse(value));
        return LightChatServerResVO;
    }

    private void parseServerInfo(Map<Long, LightChatServerResVO> routes, String key) {
        // key: LightChat-root:userId
        long userId = Long.valueOf(key.split(":")[1]);
        String value = redisTemplate.opsForValue().get(key);
        LightChatServerResVO LightChatServerResVO = new LightChatServerResVO(RouteInfoParseUtil.parse(value));
        routes.put(userId, LightChatServerResVO);
    }


    @Override
    public void pushMsg(LightChatServerResVO LightChatServerResVO, long sendUserId, ChatReqVO groupReqVO) throws Exception {
        LightChatUserInfo LightChatUserInfo = userInfoCacheService.loadUserInfoByUserId(sendUserId);

        String url = "http://" + LightChatServerResVO.getIp() + ":" + LightChatServerResVO.getHttpPort();
        ServerApi serverApi = new ProxyManager<>(ServerApi.class, url, okHttpClient).getInstance();
        SendMsgReqVO vo = new SendMsgReqVO(LightChatUserInfo.getUserName() + ":" + groupReqVO.getMsg(), groupReqVO.getUserId());
        Response response = null;
        try {
            response = (Response) serverApi.sendMsg(vo);
        } catch (Exception e) {
            LOGGER.error("Exception", e);
        } finally {
            response.body().close();
        }
    }

    @Override
    public void offLine(Long userId) throws Exception {

        // TODO: 2019-01-21 ????????????????????????????????????????????????

        //????????????
        redisTemplate.delete(ROUTE_PREFIX + userId);

        //??????????????????
        userInfoCacheService.removeLoginStatus(userId);
    }
}
