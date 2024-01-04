package org.ipoliakov.dmap.util.concurrent;

public class QuorumUnreachableException extends RuntimeException {

    public QuorumUnreachableException(int reachable, int quorum) {
        super("Can't reach quorum reachable = " + reachable + ", required quorum = " + quorum);
    }
}
