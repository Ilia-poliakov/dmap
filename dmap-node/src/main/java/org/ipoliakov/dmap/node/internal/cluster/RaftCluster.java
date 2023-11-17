package org.ipoliakov.dmap.node.internal.cluster;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.ipoliakov.dmap.common.network.MessageSender;
import org.springframework.stereotype.Component;

import com.google.protobuf.MessageLite;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RaftCluster {

    private final Map<Integer, MessageSender> messageSenders = new ConcurrentHashMap<>();

    public <R extends MessageLite> List<CompletableFuture<R>> sendToAll(MessageLite messages, Class<R> responseType) {
        return messageSenders.values()
                .stream()
                .map(sender -> sender.send(messages, responseType))
                .toList();
    }

    public void addMessageSender(int nodeId, MessageSender messageSender) {
        messageSenders.put(nodeId, messageSender);
    }

    public void remove(int id) {
        messageSenders.remove(id);
    }

    public int getMajorityNodesCount() {
        return messageSenders.size() / 2 + 1;
    }
}
