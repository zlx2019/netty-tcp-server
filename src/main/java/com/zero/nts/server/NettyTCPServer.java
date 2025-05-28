package com.zero.nts.server;

import com.zero.nts.codec.EasyDecoder;
import com.zero.nts.codec.EasyEncoder;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
public class NettyTCPServer {
    @Value("${netty.tcp.port}")
    private int serverPort;
    @Autowired
    private EasyServerHandler serverHandler;

    private boolean keepAlive = true;
    private int backlog = 128;
    private int connectTimeout = 3000;

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
                .option(ChannelOption.SO_BACKLOG, backlog)
                // 连接超时时长
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout)
                // 启用TCP心跳机制
                .childOption(ChannelOption.SO_KEEPALIVE, keepAlive)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new EasyDecoder()) // 解码器
                                .addLast(new EasyEncoder()) // 编码器
                                // 添加空闲事件器，channel 空闲10秒后将发送 IdleStateEvent 事件
                                .addLast(new IdleStateHandler(0, 0, 10, TimeUnit.SECONDS))
                                .addLast(serverHandler)   // Channel 处理器
                                .addLast();
                    }
                });
        try {
            // 绑定地址
            ChannelFuture future = bootstrap.bind(serverPort).sync();
            future.addListener(cf -> {
                if (cf.isSuccess()){
                    log.info("[NettyTCPServer] Server started on port {}", serverPort);
                }
            });

            // 异步启动Netty服务
            serverChannel = future.channel();
            CompletableFuture.runAsync(()-> {
                try {
                    serverChannel.closeFuture().sync().addListener(cf-> {
                        log.info("[NettyTCPServer] Server closed");
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
