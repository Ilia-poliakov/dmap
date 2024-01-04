package org.ipoliakov.dmap.node.internal.cluster.raft.exception;

public class ReplicationException extends RuntimeException {

    public ReplicationException() {
        super("Can't replicate operation to quorum");
    }
}
