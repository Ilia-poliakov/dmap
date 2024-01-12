package org.ipoliakov.dmap.node.internal.cluster.raft;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.ipoliakov.dmap.common.network.MessageSender;
import org.ipoliakov.dmap.node.internal.cluster.Cluster;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ClusterTest {

    @Test
    void remove() {
        var raftCluster = new Cluster();
        int nodeCount = 6;
        for (int i = 1; i <= nodeCount; i++) {
            raftCluster.addMessageSender(i, Mockito.mock(MessageSender.class));
        }
        raftCluster.remove(2);
        assertEquals(3, raftCluster.getMajorityNodesCount());
    }

    @Test
    void getMajorityNodesCount() {
        var raftCluster = new Cluster();
        int nodeCount = 5;
        for (int i = 1; i <= nodeCount; i++) {
            raftCluster.addMessageSender(i, Mockito.mock(MessageSender.class));
        }
        assertEquals(3, raftCluster.getMajorityNodesCount());
    }
}