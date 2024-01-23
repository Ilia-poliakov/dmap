package org.ipoliakov.dmap.node.cluster.raft.log.exception;

public class RaftLogReadingException extends RuntimeException {

    public RaftLogReadingException(Throwable cause) {
        super("Reading log error", cause);
    }
}
