package com.zero.nts.server.handler;

import com.zero.nts.message.EasyMessage;
import com.zero.nts.message.MessageType;
import com.zero.nts.message.MessageVersion;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * 服务端处理器
 *
 * @author Zero.
 * <p> Created on 2025/5/27 13:46 </p>
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class EasyServerHandler extends SimpleChannelInboundHandler<EasyMessage> {
    private final DefaultChannelGroup GROUP = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    /**
     * 接收到客户端消息处理
     * @param ctx       客户端上下文
     * @param message   数据消息
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, EasyMessage message) throws Exception {
        log.info("读取到消息: {}, payload: {}", message, new String(message.getData(), StandardCharsets.UTF_8));
        MessageType type = message.getType();

        // 响应消息
        EasyMessage respMsg = new EasyMessage();
        respMsg.setMagic('@');
        respMsg.setVersion(MessageVersion.V1);
        respMsg.setType(MessageType.NORMAL);
        byte[] bytes = "Response".getBytes(StandardCharsets.UTF_8);
        respMsg.setLength(bytes.length);
        respMsg.setData(bytes);
        ctx.channel().writeAndFlush(respMsg).addListener(future -> {
           if (future.isSuccess()) {
               log.info("Reply successfully");
           }
        });
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
    }

    /**
     * 客户端 Channel 注册到 EventLoop
     * @param ctx   客户端上下文
     */
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        log.info("[ServerHandler] channel register to event-loop");
    }

    /**
     * 客户端 Channel 从EventLoop 中移除
     * @param ctx   客户端上下文
     */
    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        log.info("[ServerHandler] client channel remove from the event-loop");
    }

    /**
     * 客户端 Channel 连接建立成功后
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.channel().id();
        log.info("[ServerHandler] client connected: {}", ctx.channel().id().asLongText());
        GROUP.add(ctx.channel());
        log.info("current number of clients: {}", GROUP.size());
        super.channelActive(ctx);
    }


    /**
     * 客户端 Channel 连接断开了
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("[ServerHandler] client disconnected: {}", ctx.channel().remoteAddress());
        GROUP.remove(ctx.channel());
        log.info("current number of clients: {}", GROUP.size());
        super.channelInactive(ctx);
    }

    /**
     * 产生异常
     * @param ctx   上下文
     * @param cause 异常
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("[ServerHandler] exceptionCaught: {}", ctx.channel().remoteAddress(), cause);
        this.channelInactive(ctx);
        ctx.close();
    }

    /**
     * 事件触发
     * @param ctx   上下文
     * @param evt   触发的事件
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        log.info("[ServerHandler] channel: {} event triggered: {}", ctx.channel().remoteAddress(), evt);
        super.userEventTriggered(ctx, evt);
    }


}
