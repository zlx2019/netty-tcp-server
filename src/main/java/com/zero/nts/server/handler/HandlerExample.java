package com.zero.nts.server.handler;

import com.zero.nts.message.EasyMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Netty 处理器示例
 *  ChannelHandler -> 顶层接口
 *       ↓
 *  ChannelInboundHandler -> 数据入站处理器接口
 *       ↓
 *  ChannelInboundHandlerAdapter 入站适配器扩展类
 *       ↓
 *  SimpleChannelInboundHandler<T> 更强大的入站处理器
 *      - 支持数据类型，结合编解码器自动实现数据的类型转换
 *      - 自动内存管理，会自动释放ByteBuf的内存，如果使用 ChannelInboundHandlerAdapter 需要手动释放 ReferenceCountUtil.release()
 *      - 如果有多个处理器，那么切记只能在最后一个处理器中开启自动释放，否则后续的处理器都无法获取到消息
 *
 *  执行链顺序：
 *      - handlerAdded()           // Handler 添加到 Pipeline
 *      - channelRegistered()      // Channel 注册到 EventLoop
 *      - channelActive()          // 连接激活
 *      - channelRead(msg1)        // 接收第一个数据包
 *      - channelRead(msg2)        // 接收第二个数据包（如果有）
 *      - channelRead(msgN)        // 接收第N个数据包
 *      - channelReadComplete()    // 一次读取操作完成
 *      - channelInactive()        // 连接断开
 *      - channelUnregistered()    // 从 EventLoop 注销
 *      - handlerRemoved()         // Handler 从 Pipeline 移除
 *
 *
 * @author Zero.
 * <p> Created on 2025/5/27 17:47 </p>
 */
public class HandlerExample extends SimpleChannelInboundHandler<EasyMessage> {

    /**
     * 接到数据后调用，并且解码序列化为对应的数据类型
     * @param ctx       上下文
     * @param message   数据
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, EasyMessage message) throws Exception {
    }

    /**
     * 消息过滤，可以过滤掉不需要处理的消息
     * @param msg   消息
     */
    @Override
    public boolean acceptInboundMessage(Object msg) throws Exception {
        return true;
    }
    /**
     * Handler 被添加到 ChannelPipeline 时执行
     * @param ctx       Channel 上下文
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
    }
    /**
     * Handler 从 ChannelPipeline 移除时执行
     * @param ctx   上下文
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);
    }
    /**
     * Handler 处理过程中发生异常时调用
     * @param ctx   上下文
     * @param cause 异常
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // 关闭连接
        ctx.close();
    }

    /**
     * Channel 注册到 EventLoop 时调用
     * @param ctx   上下文
     */
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
    }

    /**
     * Channel 从 EventLoop 中移除时调用
     * @param ctx 上下文
     */
    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
    }

    /**
     * Channel 连接建立成功后调用
     * @param ctx   上下文
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    /**
     * Channel 连接断开后调用
     * @param ctx   上下文
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    /**
     * 接收到数据包时调用
     * @param ctx   上下文
     * @param msg   接收到的数据，可能是{@link io.netty.buffer.ByteBuf}
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
    }


    /**
     * 一次读取操作完成时调用
     * @param ctx   上下文
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
    }

    /**
     * 用户自定义事件触发时调用
     * @param ctx   上下文
     * @param evt   触发的事件对象
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
    }

    /**
     * Channel的可写状态改变时调用
     * @param ctx   上下文
     */
    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        super.channelWritabilityChanged(ctx);
    }
}
