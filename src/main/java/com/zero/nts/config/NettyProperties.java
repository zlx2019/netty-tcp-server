package com.zero.nts.config;

import io.netty.handler.logging.LogLevel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.time.Duration;

/**
 * @author Zero.
 * <p> Created on 2025/5/28 13:46 </p>
 */
@Data
@ConfigurationProperties(prefix = "netty")
public class NettyProperties {
    /**
     * 服务地址
     */
    private String host;
    /**
     * 服务端口
     */
    private int port;
    /**
     * 连接等待队列上限
     */
    private int backlog = 1024;
    /**
     * 日志级别
     */
    private LogLevel level = LogLevel.DEBUG;
    /**
     * 客户端连接超时时间
     */
    private Duration connectTimeout;
    /**
     * 是否开启TCP keep-alive，每隔2小时探测客户端是否存活，关闭即可，自己实现心跳机制
     */
    private Boolean keepAlive = false;
    /**
     * TCP Nagle 是否开启
     */
    private Boolean noDelay = false;

    public SocketAddress getAddress() {
        return new InetSocketAddress(host, port);
    }
}
