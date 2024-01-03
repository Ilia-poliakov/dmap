package org.ipoliakov.dmap.client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.ipoliakov.dmap.client.internal.ClientMessageSender;
import org.ipoliakov.dmap.client.internal.ClientResponseHandler;
import org.ipoliakov.dmap.client.internal.Endpoint;
import org.ipoliakov.dmap.common.IdGenerator;
import org.ipoliakov.dmap.common.MonotonicallyIdGenerator;
import org.ipoliakov.dmap.common.network.MessageSender;
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
@SuppressWarnings("checkstyle:classdataabstractioncoupling")
public class ClientBuilder {

    private int threadCount = 1;
    private IdGenerator idGenerator = new MonotonicallyIdGenerator();
    private List<Endpoint> nodes = new ArrayList<>();

    public <K extends Serializable, V extends Serializable> DMapClient<K, V> build(Serializer<K, ByteString> keySerializer,
                                                                                   Serializer<V, ByteString> valueSerializer) {
        var responseFutures = new ResponseFutures();
        var messageRegistry = new ProtoMessageRegistry();
        var messageSender = new MessageSender(idGenerator, responseFutures, messageRegistry);
        try {
            var bootstrap = bootstrap();
            var responseHandler = new ClientResponseHandler(bootstrap, messageSender, responseFutures, messageRegistry);
            bootstrap.handler(responseHandler);
            List<Channel> channels = connectToAll(bootstrap);
            var clientMessageSender = new ClientMessageSender(messageSender, channels);
            var client = new DMapClientImpl<>(clientMessageSender, keySerializer, valueSerializer);
            client.refreshLeader();
            return client;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Channel> connectToAll(Bootstrap bootstrap) throws InterruptedException {
        List<Channel> channels = new ArrayList<>(nodes.size());
        for (Endpoint node : nodes) {
            channels.add(bootstrap.connect(node.host(), node.port()).sync().channel());
        }
        return channels;
    }

    public ClientBuilder addNode(String host, int port) {
        this.nodes.add(new Endpoint(host, port));
        return this;
    }

    private Bootstrap bootstrap() {
        return new Bootstrap()
                .group(eventLoopGroup())
                .channel(channel());
    }

    private EventLoopGroup eventLoopGroup() {
        return Epoll.isAvailable() ? new EpollEventLoopGroup(threadCount) : new NioEventLoopGroup(threadCount);
    }

    private static Class<? extends SocketChannel> channel() {
        return Epoll.isAvailable() ? EpollSocketChannel.class : NioSocketChannel.class;
    }

}
