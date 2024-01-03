package org.ipoliakov.dmap.node.txlog.repair;

import java.util.stream.Stream;

import org.ipoliakov.dmap.node.internal.cluster.raft.RaftLog;
import org.ipoliakov.dmap.node.txlog.io.TxLogReader;
import org.ipoliakov.dmap.node.txlog.operation.OperationExecutor;
import org.ipoliakov.dmap.protocol.internal.Operation;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RepairManager {

    private final RaftLog raftLog;
    private final TxLogReader txLogReader;
    private final OperationExecutor operationExecutor;

    public void repairAll() {
        log.info("Repairing start");

        try (Stream<Operation> operations = txLogReader.readAll()) {
            operations.forEach(operation -> {
                operationExecutor.execute(operation);
                raftLog.setLastTerm(operation.getTerm());
                raftLog.setLastIndex(operation.getLogIndex());
            });
        }

        log.info("Repairing finished");
    }
}
