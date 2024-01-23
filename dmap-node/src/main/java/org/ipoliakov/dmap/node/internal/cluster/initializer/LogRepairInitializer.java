package org.ipoliakov.dmap.node.internal.cluster.initializer;

import org.ipoliakov.dmap.node.Initializer;
import org.ipoliakov.dmap.node.internal.cluster.raft.log.repair.RepairManager;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogRepairInitializer implements Initializer {

    private final RepairManager repairManager;

    @Override
    public void initialize() {
        log.info("Repairing from log - start");

        repairManager.repairAll();

        log.info("Repairing from log - end");
    }
}
