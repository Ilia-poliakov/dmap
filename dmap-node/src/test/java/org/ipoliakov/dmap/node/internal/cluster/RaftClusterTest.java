package org.ipoliakov.dmap.node.internal.cluster;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.ipoliakov.dmap.common.network.MessageSender;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class RaftClusterTest {

    @Test
    void remove() {
        var raftCluster = new RaftCluster();
        int nodeCount = 6;
        for (int i = 1; i <= nodeCount; i++) {
            raftCluster.addMessageSender(i, Mockito.mock(MessageSender.class));
        }
        raftCluster.remove(2);
        assertEquals(3, raftCluster.getMajorityNodesCount());
    }

    @Test
    void getMajorityNodesCount() {
        var raftCluster = new RaftCluster();
        int nodeCount = 5;
        for (int i = 1; i <= nodeCount; i++) {
            raftCluster.addMessageSender(i, Mockito.mock(MessageSender.class));
        }
        assertEquals(3, raftCluster.getMajorityNodesCount());
    }
}