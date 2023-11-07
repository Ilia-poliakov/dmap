package org.ipoliakov.dmap.node.txlog.io;

import java.io.IOException;

import org.ipoliakov.dmap.protocol.internal.Operation;

public interface TxLogWriter extends AutoCloseable {

    void write(Operation operation) throws IOException;

    void flush() throws IOException;
}
