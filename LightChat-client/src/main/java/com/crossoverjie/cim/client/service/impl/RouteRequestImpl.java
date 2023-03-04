package com.crossoverjie.LightChat.client.service.impl;

import com.alibaba.fastjson.JSON;
import com.crossoverjie.LightChat.client.config.AppConfiguration;
import com.crossoverjie.LightChat.client.service.EchoService;
import com.crossoverjie.LightChat.client.service.RouteRequest;
import com.crossoverjie.LightChat.client.thread.ContextHolder;
import com.crossoverjie.LightChat.client.vo.req.GroupReqVO;
import com.crossoverjie.LightChat.client.vo.req.LoginReqVO;
import com.crossoverjie.LightChat.client.vo.req.P2PReqVO;
import com.crossoverjie.LightChat.client.vo.res.LightChatServerResVO;
import com.crossoverjie.LightChat.client.vo.res.OnlineUsersResVO;
import com.crossoverjie.LightChat.common.core.proxy.ProxyManager;
import com.crossoverjie.LightChat.common.enums.StatusEnum;
import com.crossoverjie.LightChat.common.exception.LightChatException;
import com.crossoverjie.LightChat.common.res.BaseResponse;
import com.crossoverjie.LightChat.route.api.RouteApi;
import com.crossoverjie.LightChat.route.api.vo.req.ChatReqVO;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Function:
 *
 * @author crossoverJie
 *         Date: 2018/12/22 22:27
 * @since JDK 1.8
 */
@Service
public class RouteRequestImpl implements RouteRequest {

    private final static Logger LOGGER = LoggerFactory.getLogger(RouteRequestImpl.class);

    @Autowired
    private OkHttpClient okHttpClient ;

    @Value("${LightChat.route.url}")
    private String routeUrl ;

    @Autowired
    private EchoService echoService ;


    @Autowired
    private AppConfiguration appConfiguration ;

    @Override
    public void sendGroupMsg(GroupReqVO groupReqVO) throws Exception {
        RouteApi routeApi = new ProxyManager<>(RouteApi.class, routeUrl, okHttpClient).getInstance();
        ChatReqVO chatReqVO = new ChatReqVO(groupReqVO.getUserId(), groupReqVO.getMsg()) ;
        Response response = null;
        try {
            response = (Response)routeApi.groupRoute(chatReqVO);
        }catch (Exception e){
            LOGGER.error("exception",e);
        }finally {
            response.body().close();
        }
    }

    @Override
    public void sendP2PMsg(P2PReqVO p2PReqVO) throws Exception {
        RouteApi routeApi = new ProxyManager<>(RouteApi.class, routeUrl, okHttpClient).getInstance();
        com.crossoverjie.LightChat.route.api.vo.req.P2PReqVO vo = new com.crossoverjie.LightChat.route.api.vo.req.P2PReqVO() ;
        vo.setMsg(p2PReqVO.getMsg());​

        vo.setReceiveUserId(p2PReqVO.getReceiveUserId());
        vo.setUserId(p2PReqVO.getUserId());

        Response response = null;
        try {
            response = (Response) routeApi.p2pRoute(vo);
            String json = response.body().string() ;
            BaseResponse baseResponse = JSON.parseObject(json, BaseResponse.class);

            // account offline.
            if (baseResponse.getCode().equals(StatusEnum.OFF_LINE.getCode())){
                LOGGER.error(p2PReqVO.getReceiveUserId() + ":" + StatusEnum.OFF_LINE.getMessage());
            }

        }catch (Exception e){
            LOGGER.error("exception",e);
        }finally {
            response.body().close();
        }
    }

    @Override
    public LightChatServerResVO.ServerInfo getLightChatServer(LoginReqVO loginReqVO) throws Exception {

        RouteApi routeApi = new ProxyManager<>(RouteApi.class, routeUrl, okHttpClient).getInstance();
        com.crossoverjie.LightChat.route.api.vo.req.LoginReqVO vo = new com.crossoverjie.LightChat.route.api.vo.req.LoginReqVO() ;
        vo.setUserId(loginReqVO.getUserId());
        vo.setUserName(loginReqVO.getUserName());

        Response response = null;
        LightChatServerResVO LightChatServerResVO = null;
        try {
            response = (Response) routeApi.login(vo);
            String json = response.body().string();
            // 这里真的看吐了，有两个LightChatServerResVO，定义在不同project下面，这里的LightChatServerResVO和Response<>相同
            LightChatServerResVO = JSON.parseObject(json, LightChatServerResVO.class);

            //重复失败
            if (!LightChatServerResVO.getCode().equals(StatusEnum.SUCCESS.getCode())){
                echoService.echo(LightChatServerResVO.getMessage());

                // when client in reConnect state, could not exit.
                if (ContextHolder.getReconnect()){
                    echoService.echo("###{}###", StatusEnum.RECONNECT_FAIL.getMessage());
                    throw new LightChatException(StatusEnum.RECONNECT_FAIL);
                }

                System.exit(-1);
            }

        }catch (Exception e){
            LOGGER.error("exception",e);
        }finally {
            response.body().close();
        }
        //LightChatServerResVO.getDataBody()的类型是ServerInfo,结构和ip:LightChatServerPort:httpPort的那个LightChatServerResVO一样
        return LightChatServerResVO.getDataBody();
    }

    @Override
    public List<OnlineUsersResVO.DataBodyBean> onlineUsers() throws Exception{
        RouteApi routeApi = new ProxyManager<>(RouteApi.class, routeUrl, okHttpClient).getInstance();

        Response response = null;
        OnlineUsersResVO onlineUsersResVO = null;
        try {
            response = (Response) routeApi.onlineUser();
            // 我实在找不到response.body().string()返回值是什么的资料，我猜测返回的是{code:xxx,message:xxx,dataBody:{xx,xx}}这样的
            // BaseResponse<>和LightChatServerResVO的属性是一致的
            String json = response.body().string() ;
            onlineUsersResVO = JSON.parseObject(json, OnlineUsersResVO.class);

        }catch (Exception e){
            LOGGER.error("exception",e);
        }finally {
            response.body().close();
        }

        return onlineUsersResVO.getDataBody();
    }

    @Override
    public void offLine() {
        RouteApi routeApi = new ProxyManager<>(RouteApi.class, routeUrl, okHttpClient).getInstance();
        ChatReqVO vo = new ChatReqVO(appConfiguration.getUserId(), "offLine") ;
        Response response = null;
        try {
            response = (Response) routeApi.offLine(vo);
        } catch (Exception e) {
            LOGGER.error("exception",e);
        } finally {
            response.body().close();
        }
    }
}
