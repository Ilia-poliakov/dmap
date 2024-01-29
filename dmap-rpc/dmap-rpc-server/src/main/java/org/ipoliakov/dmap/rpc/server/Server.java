package org.ipoliakov.dmap.rpc.server;

import static org.ipoliakov.dmap.rpc.config.NetworkBaseConfig.threadEventLoopGroup;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class Server {

    @Value("${PORT:${server.port:9090}}")
    private final int port;
    @Value("${server.bossThreadNumber:1}")
    private final int bossThreadNumber;
    @Value("${server.workerThreadNumber:1}")
    private final int workerThreadNumber;
    @Value("${server.tcp.so_backlog:128}")
    private final Integer soBacklog;
    @Value("${server.tcp.tcp_nodelay:true}")
    private final Boolean tcpNodelay;
    @Value("${server.tcp.so_keepalive:true}")
    private final Boolean soKeepAlive;

    private final ServerPipelineInitializer pipelineInitializer;

    public void start() {
        EventLoopGroup bossGroup = threadEventLoopGroup(bossThreadNumber, "boss-");
        EventLoopGroup workerGroup = threadEventLoopGroup(workerThreadNumber, "worker-");
        try {
            new ServerBootstrap().group(bossGroup, workerGroup)
                .channel(channel())
                .childHandler(pipelineInitializer)
                .option(ChannelOption.SO_BACKLOG, soBacklog)
                .option(ChannelOption.TCP_NODELAY, tcpNodelay)
                .childOption(ChannelOption.SO_KEEPALIVE, soKeepAlive)
                .bind(port).sync()
                .channel()
                .closeFuture().sync();
        } catch (InterruptedException e) {
            log.warn("Server thread has been interrupted", e);
            Thread.currentThread().interrupt();
        } finally {
            bossGroup.shutdownGracefully();
        }
    }

    private static Class<? extends ServerChannel> channel() {
        return Epoll.isAvailable() ? EpollServerSocketChannel.class : NioServerSocketChannel.class;
    }
}