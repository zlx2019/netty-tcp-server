package com.zero.nts.client.handler;

import com.zero.nts.message.EasyMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * @author Zero.
 * <p> Created on 2025/5/27 14:24 </p>
 */
@Slf4j
public class EasyClientHandler extends SimpleChannelInboundHandler<EasyMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, EasyMessage easyMessage) throws Exception {
        log.info("server: {}, payload: {}", easyMessage, new String(easyMessage.getData(), StandardCharsets.UTF_8));
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("[EasyClientHandler] connected to the server: {}", ctx.channel().remoteAddress());
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("[EasyClientHandler] disconnected from the server: {}", ctx.channel().remoteAddress());
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("[EasyClientHandler] exception caught: {}", cause.getMessage());
        ctx.close();
    }
}
