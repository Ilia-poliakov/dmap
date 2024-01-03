package org.ipoliakov.dmap.client.internal;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

import org.ipoliakov.dmap.client.exception.LeaderNotAvailableException;
import org.ipoliakov.dmap.common.network.MessageSender;

import com.google.protobuf.MessageLite;

import io.netty.channel.Channel;
import lombok.Setter;

@Setter
public class ClientMessageSender {

    private final Random random = new Random();

    private final List<Channel> channels;
    private final MessageSender messageSender;

    private volatile Channel leaderChannel;

    public ClientMessageSender(MessageSender messageSender, List<Channel> channels) {
        this.channels = new CopyOnWriteArrayList<>(channels);
        this.messageSender = messageSender;
    }

    public <R extends MessageLite> CompletableFuture<R> send(MessageLite message, Class<R> responseType) {
        return messageSender.send(message, channels.get(random.nextInt(channels.size())), responseType);
    }

    public <R extends MessageLite> CompletableFuture<R> sendToLeader(MessageLite message, Class<R> responseType) {
        return messageSender.send(message, leaderChannel, responseType);
    }

    void refreshLeader(Endpoint endpoint) {
        channels.stream()
            .map(Channel::localAddress)
            .map(InetSocketAddress.class::cast)
            .filter(endpoint::isTheSame)
            .findFirst()
            .orElseThrow(() -> new LeaderNotAvailableException(endpoint));
    }
}
