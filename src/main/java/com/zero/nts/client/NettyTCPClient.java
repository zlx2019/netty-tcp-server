package com.zero.nts.client;

import com.zero.nts.client.handler.EasyClientHandler;
import com.zero.nts.codec.EasyDecoder;
import com.zero.nts.codec.EasyEncoder;
import com.zero.nts.message.EasyMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * TCP 客户端
 *
 * @author Zero.
 * <p> Created on 2025/5/27 14:16 </p>
 */
@Slf4j
public class NettyTCPClient {
    public static void main(String[] args) throws InterruptedException {
        // 客户端仅需要一个事件循环
        NioEventLoopGroup works = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(works)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new EasyDecoder())
                                    .addLast(new EasyEncoder())
                                    .addLast(new EasyClientHandler());
                        }
                    });
            ChannelFuture future = bootstrap.connect("127.0.0.1", 9879).sync();
            if (future.isSuccess()) {
                log.info("Connected to Server successfully");
            }
            Channel channel = future.channel();
            startPayer(channel);

            // 等待连接关闭
            channel.closeFuture().sync().addListener(cf -> {
                log.info("Closing Channel");
            });
        } finally {
            works.shutdownGracefully();
        }
    }

    private static void startPayer(Channel channel) {
        Thread payer = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (channel.isActive()){
//                System.out.print("> ");
                String line = scanner.nextLine();
                // 客户端主动退出
                if ("quit".equalsIgnoreCase(line.trim())) {
                    log.info("Quit");
                    channel.close();
                    break;
                }

                // 发送消息
                sendMessage(channel, line);
            }

        });
        payer.setDaemon(true);
        payer.start();
    }

    private static void sendMessage(Channel channel, String content) {
        if (!channel.isActive()) {
            log.error("Channel is not active");
            return;
        }
        EasyMessage message = new EasyMessage();
        message.setVersion((byte) 1);
        message.setType((byte) 1);
        byte[] payload = content.getBytes(StandardCharsets.UTF_8);
        message.setLength(payload.length);
        message.setData(payload);
        channel.writeAndFlush(message).addListener(future -> {
            if (future.isSuccess()) {
                log.info("Message sent successfully");
            }else {
                log.error("Message sent failed:", future.cause());
            }
        });
    }
}
