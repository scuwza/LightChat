package com.crossoverjie.LightChat.route.service;

import com.crossoverjie.LightChat.common.enums.StatusEnum;
import com.crossoverjie.LightChat.route.api.vo.req.ChatReqVO;
import com.crossoverjie.LightChat.route.api.vo.req.LoginReqVO;
import com.crossoverjie.LightChat.route.api.vo.res.LightChatServerResVO;
import com.crossoverjie.LightChat.route.api.vo.res.RegisterInfoResVO;

import java.util.Map;

/**
 * Function: 账户服务
 *
 * @author crossoverJie
 *         Date: 2018/12/23 21:57
 * @since JDK 1.8
 */
public interface AccountService {

    /**
     * 注册用户
     * @param info 用户信息
     * @return
     * @throws Exception
     */
    RegisterInfoResVO register(RegisterInfoResVO info) throws Exception;

    /**
     * 登录服务
     * @param loginReqVO 登录信息
     * @return true 成功 false 失败
     * @throws Exception
     */
    StatusEnum login(LoginReqVO loginReqVO) throws Exception ;

    /**
     * 保存路由信息
     * @param msg 服务器信息
     * @param loginReqVO 用户信息
     * @throws Exception
     */
    void saveRouteInfo(LoginReqVO loginReqVO ,String msg) throws Exception ;

    /**
     * 加载所有用户的路有关系
     * @return 所有的路由关系
     */
    Map<Long, LightChatServerResVO> loadRouteRelated() ;

    /**
     * 获取某个用户的路有关系
     * @param userId
     * @return 获取某个用户的路有关系
     */
    LightChatServerResVO loadRouteRelatedByUserId(Long userId) ;


    /**
     * 推送消息
     * @param LightChatServerResVO
     * @param groupReqVO 消息
     * @param sendUserId 发送者的ID
     * @throws Exception
     */
    void pushMsg(LightChatServerResVO LightChatServerResVO, long sendUserId , ChatReqVO groupReqVO) throws Exception;

    /**
     * 用户下线
     * @param userId 下线用户ID
     * @throws Exception
     */
    void offLine(Long userId) throws Exception;
}
