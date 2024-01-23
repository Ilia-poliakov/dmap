package org.ipoliakov.dmap.node.cluster.raft.log.io;

import java.io.IOException;

import org.ipoliakov.dmap.protocol.raft.Operation;

public interface RaftLogWriter extends AutoCloseable {

    int write(Operation operation) throws IOException;

    void flush() throws IOException;
}
