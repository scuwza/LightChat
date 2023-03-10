package com.crossoverjie.LightChat.client.handle;

import com.crossoverjie.LightChat.client.service.EchoService;
import com.crossoverjie.LightChat.client.service.ReConnectManager;
import com.crossoverjie.LightChat.client.service.ShutDownMsg;
import com.crossoverjie.LightChat.client.service.impl.EchoServiceImpl;
import com.crossoverjie.LightChat.client.util.SpringBeanFactory;
import com.crossoverjie.LightChat.common.constant.Constants;
import com.crossoverjie.LightChat.common.protocol.LightChatRequestProto;
import com.crossoverjie.LightChat.common.protocol.LightChatResponseProto;
import com.crossoverjie.LightChat.common.util.NettyAttrUtil;
import com.vdurmont.emoji.EmojiParser;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Function:
 *
 * @author crossoverJie
 *         Date: 16/02/2018 18:09
 * @since JDK 1.8
 */
@ChannelHandler.Sharable
public class LightChatClientHandle extends SimpleChannelInboundHandler<LightChatResponseProto.LightChatResProtocol> {

    private final static Logger LOGGER = LoggerFactory.getLogger(LightChatClientHandle.class);

    private MsgHandleCaller caller ;

    private ThreadPoolExecutor threadPoolExecutor ;

    private ScheduledExecutorService scheduledExecutorService ;

    private ReConnectManager reConnectManager ;

    private ShutDownMsg shutDownMsg ;

    private EchoService echoService ;


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        if (evt instanceof IdleStateEvent){
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt ;

            if (idleStateEvent.state() == IdleState.WRITER_IDLE){
                LightChatRequestProto.LightChatReqProtocol heartBeat = SpringBeanFactory.getBean("heartBeat",
                        LightChatRequestProto.LightChatReqProtocol.class);
                ctx.writeAndFlush(heartBeat).addListeners((ChannelFutureListener) future -> {
                    if (!future.isSuccess()) {
                        LOGGER.error("IO error,close Channel");
                        future.channel().close();
                    }
                }) ;
            }

        }

        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //??????????????????????????????????????????
        LOGGER.info("LightChat server connect success!");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

        if (shutDownMsg == null){
            shutDownMsg = SpringBeanFactory.getBean(ShutDownMsg.class) ;
        }

        //??????????????????????????????????????????
        if (shutDownMsg.checkStatus()){
            return;
        }

        if (scheduledExecutorService == null){
            scheduledExecutorService = SpringBeanFactory.getBean("scheduledTask",ScheduledExecutorService.class) ;
            reConnectManager = SpringBeanFactory.getBean(ReConnectManager.class) ;
        }
        LOGGER.info("????????????????????????????????????");
        reConnectManager.reConnect(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LightChatResponseProto.LightChatResProtocol msg) throws Exception {
        if (echoService == null){
            echoService = SpringBeanFactory.getBean(EchoServiceImpl.class) ;
        }


        //??????????????????
        if (msg.getType() == Constants.CommandType.PING){
            //LOGGER.info("??????????????????????????????");
            NettyAttrUtil.updateReaderTime(ctx.channel(),System.currentTimeMillis());
        }

        if (msg.getType() != Constants.CommandType.PING) {
            //????????????
            callBackMsg(msg.getResMsg());

            //??????????????? emoji ?????????????????? Unicode ?????????????????????????????????
            String response = EmojiParser.parseToUnicode(msg.getResMsg());
            echoService.echo(response);
        }





    }

    /**
     * ????????????
     * @param msg
     */
    private void callBackMsg(String msg) {
        threadPoolExecutor = SpringBeanFactory.getBean("callBackThreadPool",ThreadPoolExecutor.class) ;
        threadPoolExecutor.execute(() -> {
            caller = SpringBeanFactory.getBean(MsgHandleCaller.class) ;
            caller.getMsgHandleListener().handle(msg);
        });

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //?????????????????????
        cause.printStackTrace() ;
        ctx.close() ;
    }
}
