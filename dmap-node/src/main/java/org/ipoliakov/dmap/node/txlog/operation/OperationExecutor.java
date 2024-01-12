package org.ipoliakov.dmap.node.txlog.operation;

import java.util.EnumMap;

import org.ipoliakov.dmap.common.network.ProtoMessageRegistry;
import org.ipoliakov.dmap.protocol.PayloadType;
import org.ipoliakov.dmap.protocol.internal.Operation;
import org.springframework.stereotype.Component;

import com.google.protobuf.MessageLite;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OperationExecutor {

    private final ProtoMessageRegistry protoMessageRegistry;
    private final EnumMap<PayloadType, MutationOperation<MessageLite>> operationMap;

    public void execute(Operation operation) {
        MessageLite messageLite = protoMessageRegistry.parsePayload(operation);
        MutationOperation<MessageLite> op = operationMap.get(operation.getPayloadType());
        op.execute(messageLite);
    }
}