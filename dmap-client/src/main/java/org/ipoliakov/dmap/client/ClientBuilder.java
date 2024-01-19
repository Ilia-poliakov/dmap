package org.ipoliakov.dmap.client;

import java.io.Serializable;

import org.ipoliakov.dmap.client.internal.ClientPipelineInitializer;
import org.ipoliakov.dmap.client.serializer.StringSerializer;
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
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Setter
@SuppressWarnings("ClassDataAbstractionCoupling")
public class ClientBuilder {

    private final EventLoopGroup group = singleThreadEventLoopGroup();
    private final ResponseFutures responseFutures = new ResponseFutures();
    private final ProtoMessageRegistry messageRegistry = new ProtoMessageRegistry();

    private int threadCount = 1;
    private String host = "localhost";
    private int port = 9090;
    private IdGenerator idGenerator = new MonotonicallyIdGenerator();

    public ClientConfigurator connect() {
        try {
            Channel channel = new Bootstrap()
                    .group(group)
                    .channel(channel())
                    .handler(new ClientPipelineInitializer(responseFutures, messageRegistry))
                    .connect(host, port).sync().channel();
            return new ClientConfigurator(new MessageSender(channel, idGenerator, responseFutures, messageRegistry));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @RequiredArgsConstructor
    public static class ClientConfigurator {

        private final MessageSender messageSender;
        private final StringSerializer stringSerializer = new StringSerializer();

        public KeyValueStorageClientBuilder keyValueStorageBuilder() {
            return new KeyValueStorageClientBuilder();
        }

        public CrdtClientBuilder crdtClientBuilder() {
            return new CrdtClientBuilder();
        }

        public class KeyValueStorageClientBuilder {

            public KvStorageClient<String, String> build() {
                return this.build(stringSerializer, stringSerializer);
            }

            public <K extends Serializable, V extends Serializable> KvStorageClient<K, V> build(Serializer<K, ByteString> keySerializer,
                                                                                                Serializer<V, ByteString> valueSerializer) {
                return new KvStorageClientImpl<>(messageSender, keySerializer, valueSerializer);
            }
        }

        public class CrdtClientBuilder {

            public CrdtClient build() {
                return new CrdtClient(messageSender);
            }
        }
    }

    private EventLoopGroup singleThreadEventLoopGroup() {
        return Epoll.isAvailable() ? new EpollEventLoopGroup(threadCount) : new NioEventLoopGroup(threadCount);
    }

    private static Class<? extends SocketChannel> channel() {
        return Epoll.isAvailable() ? EpollSocketChannel.class : NioSocketChannel.class;
    }
}
