package org.ipoliakov.dmap.node.txlog.exception;

public class TxLogReadingException extends RuntimeException {

    public TxLogReadingException(Throwable cause) {
        super("Reading log error", cause);
    }
}
