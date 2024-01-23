package org.ipoliakov.dmap.node.internal.cluster.raft.log.io;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;

import org.ipoliakov.dmap.node.internal.cluster.raft.log.exception.RaftLogReadingException;
import org.ipoliakov.dmap.protocol.raft.Operation;

public interface RaftLogReader {

    Stream<Operation> readAll() throws RaftLogReadingException;

    Optional<Operation> read(int address) throws IOException;
}
