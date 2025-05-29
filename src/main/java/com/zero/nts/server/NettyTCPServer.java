package com.zero.nts.server;

import com.zero.nts.codec.EasyDecoder;
import com.zero.nts.codec.EasyEncoder;
import com.zero.nts.config.NettyProperties;
import com.zero.nts.server.handler.EasyServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Netty TCP Server
 *
 * @author Zero.
 * <p> Created on 2025/5/26 14:31 </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NettyTCPServer {
    private final EasyServerHandler serverHandler;
    private final NettyProperties properties;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;

    @PostConstruct
    public void start() {
        // 创建事件循环
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                // 连接等待队列大小
                .option(ChannelOption.SO_BACKLOG, properties.getBacklog())
                // 连接超时时长
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) properties.getConnectTimeout().toMillis())
                // 启用TCP心跳机制
                .childOption(ChannelOption.SO_KEEPALIVE, properties.getKeepAlive())
                .childOption(ChannelOption.TCP_NODELAY, properties.getNoDelay())
                // 设置日志级别
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new EasyDecoder()) // 解码器
                                .addLast(new EasyEncoder()) // 编码器
                                // 添加空闲事件器，channel 空闲10秒后将发送 IdleStateEvent 事件
                                .addLast(new IdleStateHandler(0, 0, 10, TimeUnit.SECONDS))
                                .addLast(serverHandler);   // Channel 处理器
                    }
                });
        try {
            // 绑定地址并启动服务
            ChannelFuture server = bootstrap.bind(properties.getAddress()).sync().addListener(future -> {
                if (future.isSuccess()){
                    log.info("[NettyTCPServer] Server started on port {}", properties.getAddress());
                }
            });
            serverChannel = server.channel();
            // 异步启动Netty服务,避免阻塞程序启动
            CompletableFuture.runAsync(()-> {
                try {
                    serverChannel.closeFuture().sync().addListener(future-> {
                        log.info("[NettyTCPServer] server channel closed");
                    });
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (InterruptedException e) {
            stop();
            throw new BeanCreationException("[NettyTCPServer] Startup failed", e);
        }
    }


    @PreDestroy
    public void stop() {
        try {
            if (null != serverChannel) {
                serverChannel.close().sync();
            }
            if (bossGroup != null) {
                bossGroup.shutdownGracefully().sync().addListener(future -> {
                   if (future.isSuccess()) {
                       log.info("[NettyTCPServer] bossGroup shutdown success");
                   }
                });
            }
            if (workerGroup != null) {
                workerGroup.shutdownGracefully().sync().addListener(future -> {
                    if (future.isSuccess()) {
                        log.info("[NettyTCPServer] workerGroup shutdown success");
                    }
                });
            }
        } catch (Exception e) {
            log.error("[NettyTCPServer] server channel close failed", e);
        }
        log.info("[NettyTCPServer] stopped.");
    }
}
