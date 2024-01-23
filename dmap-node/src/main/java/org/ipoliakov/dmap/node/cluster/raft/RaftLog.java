package org.ipoliakov.dmap.node.cluster.raft;

import java.util.Optional;

import org.ipoliakov.dmap.node.cluster.raft.log.RaftLogService;
import org.ipoliakov.dmap.protocol.raft.Operation;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RaftLog {

    @Setter
    private volatile int lastTerm;
    @Setter
    private volatile long lastIndex;

    private final RaftLogService raftLogService;

    public void append(Operation operation) {
        raftLogService.append(operation);
        lastIndex = operation.getLogIndex();
        lastTerm = operation.getTerm();
    }

    public long getLastIndex() {
        long lastIndex = this.lastIndex;
        if (lastIndex > 0) {
            return lastIndex;
        }
        return readLast().getLogIndex();
    }

    public int getLastTerm() {
        int lastTerm = this.lastTerm;
        if (lastTerm > 0) {
            return lastTerm;
        }
        return readLast().getTerm();
    }

    public Optional<Operation> readByIndex(long index) {
        return raftLogService.readByLogIndex(index);
    }

    private Operation readLast() {
        Operation operation = raftLogService.readLastEntry()
                .orElseGet(Operation::getDefaultInstance);
        lastIndex = operation.getLogIndex();
        lastTerm = operation.getTerm();
        return operation;
    }
}