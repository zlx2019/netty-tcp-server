package com.zero.nts;

import com.zero.nts.config.NettyProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(NettyProperties.class)
public class NettyTcpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(NettyTcpServerApplication.class, args);
    }

}
