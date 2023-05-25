package org.ipoliakov.dmap.node;

import org.ipoliakov.dmap.node.network.MainPipelineInitializer;
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

    private static final int PORT = 9090;

    private final MainPipelineInitializer pipelineInitializer;

    public void start() {
        EventLoopGroup bossGroup = singleThreadEventLoopGroup();
        EventLoopGroup workerGroup = singleThreadEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(channel())
                    .childHandler(pipelineInitializer)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture f = b.bind(PORT).sync();
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.warn("Server thread has been interrupted", e);
        } finally {
            bossGroup.shutdownGracefully();
        }
    }

    private static Class<? extends ServerChannel> channel() {
        return Epoll.isAvailable() ? EpollServerSocketChannel.class : NioServerSocketChannel.class;
    }

    private static EventLoopGroup singleThreadEventLoopGroup() {
        return Epoll.isAvailable() ? new EpollEventLoopGroup(1) : new NioEventLoopGroup(1);
    }
}
