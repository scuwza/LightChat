package com.crossoverjie.LightChat.route.service;

import com.crossoverjie.LightChat.common.enums.StatusEnum;
import com.crossoverjie.LightChat.common.exception.LightChatException;
import com.crossoverjie.LightChat.common.pojo.RouteInfo;
import com.crossoverjie.LightChat.route.cache.ServerCache;
import com.crossoverjie.LightChat.route.kit.NetAddressIsReachable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Function:
 *
 * @author crossoverJie
 * Date: 2020-04-12 21:40
 * @since JDK 1.8
 */
@Component
public class CommonBizService {
    private static Logger logger = LoggerFactory.getLogger(CommonBizService.class) ;


    @Autowired
    private ServerCache serverCache ;

    /**
     * check ip and port
     * @param routeInfo
     */
    public void checkServerAvailable(RouteInfo routeInfo){
        boolean reachable = NetAddressIsReachable.checkAddressReachable(routeInfo.getIp(), routeInfo.getLightChatServerPort(), 1000);
        if (!reachable) {
            logger.error("ip={}, port={} are not available", routeInfo.getIp(), routeInfo.getLightChatServerPort());

            // rebuild cache
            serverCache.rebuildCacheList();

            throw new LightChatException(StatusEnum.SERVER_NOT_AVAILABLE) ;
        }

    }
}
