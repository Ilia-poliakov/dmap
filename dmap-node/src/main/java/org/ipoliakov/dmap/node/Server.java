package org.ipoliakov.dmap.node;

import org.ipoliakov.dmap.node.network.MainPipelineInitializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class Server {

    @Value("${server.port}")
    private final int port;
    @Value("${server.bossThreadNumber}")
    private final int bossThreadNumber;
    @Value("${server.workerThreadNumber}")
    private final int workerThreadNumber;
    @Value("${server.tcp.so_backlog}")
    private final Integer soBacklog;
    @Value("${server.tcp.tcp_nodelay}")
    private final Boolean tcpNodelay;
    @Value("${server.tcp.so_keepalive}")
    private final Boolean soKeepAlive;

    private final MainPipelineInitializer pipelineInitializer;

    public void start() {
        EventLoopGroup bossGroup = threadEventLoopGroup(bossThreadNumber);
        EventLoopGroup workerGroup = threadEventLoopGroup(workerThreadNumber);
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(channel())
                    .childHandler(pipelineInitializer)
                    .option(ChannelOption.SO_BACKLOG, soBacklog)
                    .option(ChannelOption.TCP_NODELAY, tcpNodelay)
                    .childOption(ChannelOption.SO_KEEPALIVE, soKeepAlive);

            ChannelFuture f = b.bind(port).sync();
            f.channel().closeFuture().sync();
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

    private static EventLoopGroup threadEventLoopGroup(int threadNumber) {
        return Epoll.isAvailable() ? new EpollEventLoopGroup(threadNumber) : new NioEventLoopGroup(threadNumber);
    }
}
