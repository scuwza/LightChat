package com.crossoverjie.LightChat.route.api.vo.res;

import com.crossoverjie.LightChat.common.pojo.RouteInfo;

import java.io.Serializable;

/**
 * Function:
 *
 * @author crossoverJie
 *         Date: 2018/12/23 00:43
 * @since JDK 1.8
 */
public class LightChatServerResVO implements Serializable {

    private String ip ;
    private Integer LightChatServerPort;
    private Integer httpPort;

    public LightChatServerResVO(RouteInfo routeInfo) {
        this.ip = routeInfo.getIp();
        this.LightChatServerPort = routeInfo.getLightChatServerPort();
        this.httpPort = routeInfo.getHttpPort();
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getLightChatServerPort() {
        return LightChatServerPort;
    }

    public void setLightChatServerPort(Integer LightChatServerPort) {
        this.LightChatServerPort = LightChatServerPort;
    }

    public Integer getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(Integer httpPort) {
        this.httpPort = httpPort;
    }
}
