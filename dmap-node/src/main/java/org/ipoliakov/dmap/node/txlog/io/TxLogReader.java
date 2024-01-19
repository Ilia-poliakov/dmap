package org.ipoliakov.dmap.node.txlog.io;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;

import org.ipoliakov.dmap.node.txlog.exception.TxLogReadingException;
import org.ipoliakov.dmap.protocol.raft.Operation;

public interface TxLogReader {

    Stream<Operation> readAll() throws TxLogReadingException;

    Optional<Operation> read(int address) throws IOException;
}
