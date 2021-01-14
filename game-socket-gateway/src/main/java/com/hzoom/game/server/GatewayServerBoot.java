package com.hzoom.game.server;

import com.hzoom.game.cloud.PlayerServiceInstanceManager;
import com.hzoom.game.stream.TopicService;
import com.hzoom.game.handler.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class GatewayServerBoot {
    @Autowired
    private GatewayServerProperties gatewayServerProperties;
    @Autowired
    private PlayerServiceInstanceManager playerServiceInstanceManager;
    @Autowired
    private PlayerChannelManager playerChannelManager;
    @Autowired
    private TopicService topicService;
    @Autowired
    private RequestRateLimiterHandler requestRateLimiterHandler;
    @Autowired
    private DispatchHandler dispatchHandler;

    private NioEventLoopGroup bossGroup;
    private NioEventLoopGroup workGroup;

    public void startServer(){
        bossGroup = new NioEventLoopGroup(gatewayServerProperties.getBossThreadCount());
        workGroup = new NioEventLoopGroup(gatewayServerProperties.getWorkThreadCount());
        int port = gatewayServerProperties.getPort();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup,workGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG,128)
                    .childOption(ChannelOption.SO_KEEPALIVE,true)
                    .childOption(ChannelOption.TCP_NODELAY,true)
                    .childHandler(createChannelInitializer());
            log.info("启动服务 端口：{}",port);
            ChannelFuture bindFuture = bootstrap.bind(port).sync();
            bindFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }

    private ChannelInitializer<Channel> createChannelInitializer() {
        ChannelInitializer<Channel> channelInitializer = new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast("encoder",new ServerEncodeHandler(gatewayServerProperties))
                        .addLast("decode",new ServerDecodeHandler())
                        .addLast("confirm",new ConfirmHandler(playerServiceInstanceManager,playerChannelManager,topicService,gatewayServerProperties))
                        .addLast("requestLimit",requestRateLimiterHandler)
                        .addLast(new IdleStateHandler(gatewayServerProperties.getReaderIdleTimeSeconds(), gatewayServerProperties.getWriterIdleTimeSeconds(), gatewayServerProperties.getAllIdleTimeSeconds()))
                        .addLast("heartbeat",new HeartbeatHandler())
                        .addLast("dispatch",dispatchHandler);
            }
        };
        return channelInitializer;
    }

    public void stop() {// 优雅的关闭服务
        int quietPeriod = 5;
        int timeout = 30;
        TimeUnit timeUnit = TimeUnit.SECONDS;
        bossGroup.shutdownGracefully(quietPeriod, timeout, timeUnit);
        workGroup.shutdownGracefully(quietPeriod, timeout, timeUnit);
    }
}
