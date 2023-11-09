package org.ipoliakov.dmap.node.txlog.io;

import java.io.IOException;

import org.ipoliakov.dmap.protocol.internal.Operation;

public interface TxLogWriter extends AutoCloseable {

    int write(Operation operation) throws IOException;

    void flush() throws IOException;
}
