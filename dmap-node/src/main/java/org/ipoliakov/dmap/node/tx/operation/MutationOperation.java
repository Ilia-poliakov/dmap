package org.ipoliakov.dmap.node.tx.operation;

import org.ipoliakov.dmap.protocol.PayloadType;

import com.google.protobuf.MessageLite;

public interface MutationOperation<M extends MessageLite> {

    void execute(M message);

    PayloadType getPayloadType();
}
