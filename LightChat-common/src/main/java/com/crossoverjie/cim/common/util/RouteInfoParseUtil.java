package com.crossoverjie.LightChat.common.util;

import com.crossoverjie.LightChat.common.exception.LightChatException;
import com.crossoverjie.LightChat.common.pojo.RouteInfo;

import static com.crossoverjie.LightChat.common.enums.StatusEnum.VALIDATION_FAIL;

/**
 * Function:
 *
 * @author crossoverJie
 * Date: 2020-04-12 20:42
 * @since JDK 1.8
 */
public class RouteInfoParseUtil {

    public static RouteInfo parse(String info){
        try {
            String[] serverInfo = info.split(":");
            RouteInfo routeInfo =  new RouteInfo(serverInfo[0], Integer.parseInt(serverInfo[1]),Integer.parseInt(serverInfo[2])) ;
            return routeInfo ;
        }catch (Exception e){
            throw new LightChatException(VALIDATION_FAIL) ;
        }
    }
}
