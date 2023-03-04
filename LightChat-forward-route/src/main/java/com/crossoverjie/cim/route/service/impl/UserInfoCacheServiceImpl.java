package com.crossoverjie.LightChat.route.service.impl;

import com.crossoverjie.LightChat.common.pojo.LightChatUserInfo;
import com.crossoverjie.LightChat.route.service.UserInfoCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.crossoverjie.LightChat.route.constant.Constant.ACCOUNT_PREFIX;
import static com.crossoverjie.LightChat.route.constant.Constant.LOGIN_STATUS_PREFIX;

/**
 * Function:
 *
 * @author crossoverJie
 *         Date: 2018/12/24 11:06
 * @since JDK 1.8
 */
@Service
public class UserInfoCacheServiceImpl implements UserInfoCacheService {

    /**
     * todo 本地缓存，为了防止内存撑爆，后期可换为 LRU。
     */
    private final static Map<Long,LightChatUserInfo> USER_INFO_MAP = new ConcurrentHashMap<>(64) ;

    @Autowired
    private RedisTemplate<String,String> redisTemplate ;

    @Override
    public LightChatUserInfo loadUserInfoByUserId(Long userId) {

        //优先从本地缓存获取
        LightChatUserInfo LightChatUserInfo = USER_INFO_MAP.get(userId);
        if (LightChatUserInfo != null){
            return LightChatUserInfo ;
        }

        //load redis
        String sendUserName = redisTemplate.opsForValue().get(ACCOUNT_PREFIX + userId);
        if (sendUserName != null){
            LightChatUserInfo = new LightChatUserInfo(userId,sendUserName) ;
            USER_INFO_MAP.put(userId,LightChatUserInfo) ;
        }

        return LightChatUserInfo;
    }

    @Override
    public boolean saveAndCheckUserLoginStatus(Long userId) throws Exception {

        Long add = redisTemplate.opsForSet().add(LOGIN_STATUS_PREFIX, userId.toString());
        if (add == 0){
            return false ;
        }else {
            return true ;
        }
    }

    @Override
    public void removeLoginStatus(Long userId) throws Exception {
        redisTemplate.opsForSet().remove(LOGIN_STATUS_PREFIX,userId.toString()) ;
    }

    @Override
    public Set<LightChatUserInfo> onlineUser() {
        Set<LightChatUserInfo> set = null ;
        // login-status is set, .members(key) 获取key中的值
        Set<String> members = redisTemplate.opsForSet().members(LOGIN_STATUS_PREFIX);
        for (String member : members) {
            if (set == null){
                set = new HashSet<>(64) ;
            }
            LightChatUserInfo LightChatUserInfo = loadUserInfoByUserId(Long.valueOf(member)) ;
            set.add(LightChatUserInfo) ;
        }

        return set;
    }

}
