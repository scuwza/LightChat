package com.crossoverjie.LightChat.common.exception;


import com.crossoverjie.LightChat.common.enums.StatusEnum;

/**
 * Function:
 *
 * @author crossoverJie
 *         Date: 2018/8/25 15:26
 * @since JDK 1.8
 */
public class LightChatException extends GenericException {


    public LightChatException(String errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public LightChatException(Exception e, String errorCode, String errorMessage) {
        super(e, errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public LightChatException(String message) {
        super(message);
        this.errorMessage = message;
    }

    public LightChatException(StatusEnum statusEnum) {
        super(statusEnum.getMessage());
        this.errorMessage = statusEnum.message();
        this.errorCode = statusEnum.getCode();
    }

    public LightChatException(StatusEnum statusEnum, String message) {
        super(message);
        this.errorMessage = message;
        this.errorCode = statusEnum.getCode();
    }

    public LightChatException(Exception oriEx) {
        super(oriEx);
    }

    public LightChatException(Throwable oriEx) {
        super(oriEx);
    }

    public LightChatException(String message, Exception oriEx) {
        super(message, oriEx);
        this.errorMessage = message;
    }

    public LightChatException(String message, Throwable oriEx) {
        super(message, oriEx);
        this.errorMessage = message;
    }


    public static boolean isResetByPeer(String msg) {
        if ("Connection reset by peer".equals(msg)) {
            return true;
        }
        return false;
    }

}
