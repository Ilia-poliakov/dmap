package org.ipoliakov.dmap.client;

import java.io.Serializable;

import org.ipoliakov.dmap.client.internal.ClientPipelineInitializer;
import org.ipoliakov.dmap.common.network.ProtoMessageRegistry;
import org.ipoliakov.dmap.common.network.ResponseFutures;

import com.google.protobuf.ByteString;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Setter;

@Setter
public class ClientBuilder {

    private int threadCount = 1;
    private String host = "localhost";
    private int port = 9090;

    public <K extends Serializable, V extends Serializable> DMapClient<K, V> build(Serializer<K, ByteString> keySerializer,
                                                                                   Serializer<V, ByteString> valueSerializer) {
        EventLoopGroup group = singleThreadEventLoopGroup();
        ResponseFutures responseFutures = new ResponseFutures();
        ProtoMessageRegistry messageRegistry = new ProtoMessageRegistry();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(channel())
                    .handler(new ClientPipelineInitializer(responseFutures, messageRegistry));
            Channel c = bootstrap.connect(host, port).sync().channel();
            return new DMapClientImpl<>(c, responseFutures, messageRegistry, keySerializer, valueSerializer);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private EventLoopGroup singleThreadEventLoopGroup() {
        return Epoll.isAvailable() ? new EpollEventLoopGroup(threadCount) : new NioEventLoopGroup(threadCount);
    }

    private static Class<? extends SocketChannel> channel() {
        return Epoll.isAvailable() ? EpollSocketChannel.class : NioSocketChannel.class;
    }
}
