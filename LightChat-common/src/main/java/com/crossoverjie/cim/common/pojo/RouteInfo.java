package com.crossoverjie.LightChat.common.pojo;

/**
 * Function:
 *
 * @author crossoverJie
 * Date: 2020-04-12 20:48
 * @since JDK 1.8
 */
public final class RouteInfo {

    private String ip ;
    private Integer LightChatServerPort;
    private Integer httpPort;

    public RouteInfo(String ip, Integer LightChatServerPort, Integer httpPort) {
        this.ip = ip;
        this.LightChatServerPort = LightChatServerPort;
        this.httpPort = httpPort;
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
