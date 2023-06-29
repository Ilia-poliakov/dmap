package org.ipoliakov.dmap.node.tx.repair;

import java.io.FileNotFoundException;
import java.util.EnumMap;
import java.util.stream.Stream;

import org.ipoliakov.dmap.common.network.ProtoMessageFactory;
import org.ipoliakov.dmap.node.tx.log.TxLogReader;
import org.ipoliakov.dmap.node.tx.operation.MutationOperation;
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
    private final ProtoMessageFactory protoMessageFactory;
    private final EnumMap<PayloadType, MutationOperation> operationMap;

    public void repairAll() {
        log.info("Repairing start");

        try (Stream<Operation> operations = txLogReader.read()) {
            operations.forEach(operation -> {
                MessageLite messageLite = protoMessageFactory.parsePayload(operation);
                MutationOperation op = operationMap.get(operation.getPayloadType());
                op.execute(messageLite);
            });
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("Transaction log file not found", e);
        }

        log.info("Repairing finished");
    }
}
