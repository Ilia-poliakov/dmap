package org.ipoliakov.dmap.node.internal.cluster.raft.log.exception;

public class RaftLogReadingException extends RuntimeException {

    public RaftLogReadingException(Throwable cause) {
        super("Reading log error", cause);
    }
}
