package com.crossoverjie.LightChat.route.exception;

import com.crossoverjie.LightChat.common.exception.LightChatException;
import com.crossoverjie.LightChat.common.res.BaseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Function:
 *
 * @author crossoverJie
 * Date: 2020-04-12 22:13
 * @since JDK 1.8
 */
@ControllerAdvice
public class ExceptionHandlingController {

    private static Logger logger = LoggerFactory.getLogger(ExceptionHandlingController.class) ;

    @ExceptionHandler(LightChatException.class)
    @ResponseBody()
    public BaseResponse handleAllExceptions(LightChatException ex) {
        logger.error("exception", ex);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setCode(ex.getErrorCode());
        baseResponse.setMessage(ex.getMessage());
        return baseResponse ;
    }

}