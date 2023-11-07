package org.ipoliakov.dmap.node.txlog.io;

import java.util.stream.Stream;

import org.ipoliakov.dmap.node.txlog.exception.TxLogReadingException;
import org.ipoliakov.dmap.protocol.internal.Operation;

public interface TxLogReader {

    Stream<Operation> readAll() throws TxLogReadingException;
}