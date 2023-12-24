package org.ipoliakov.dmap.node.txlog.repair;

import java.util.EnumMap;
import java.util.stream.Stream;

import org.ipoliakov.dmap.common.network.ProtoMessageRegistry;
import org.ipoliakov.dmap.node.txlog.io.TxLogReader;
import org.ipoliakov.dmap.node.txlog.operation.MutationOperation;
import org.ipoliakov.dmap.protocol.PayloadType;
import org.ipoliakov.dmap.protocol.internal.Operation;
import org.springframework.stereotype.Component;

import com.google.protobuf.MessageLite;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RepairManager {

    private final TxLogReader txLogReader;
    private final ProtoMessageRegistry protoMessageFactory;
    private final EnumMap<PayloadType, MutationOperation<MessageLite>> operationMap;

    public void repairAll() {
        log.info("Repairing start");

        try (Stream<Operation> operations = txLogReader.readAll()) {
            operations.forEach(operation -> {
                MessageLite messageLite = protoMessageFactory.parsePayload(operation);
                MutationOperation<MessageLite> op = operationMap.get(operation.getPayloadType());
                op.execute(messageLite);
            });
        }

        log.info("Repairing finished");
    }
}
