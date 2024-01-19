package org.ipoliakov.dmap.node.txlog.operation;

import org.ipoliakov.dmap.node.service.StorageMutationService;
import org.ipoliakov.dmap.protocol.PayloadType;
import org.ipoliakov.dmap.protocol.storage.RemoveReq;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveOperation implements MutationOperation<RemoveReq> {

    private final StorageMutationService storageService;

    @Override
    public void execute(RemoveReq req) {
        log.info("Repairing Remove operation - start: message = {}", req);

        storageService.remove(req);

        log.info("Repairing Remove operation - end");
    }

    @Override
    public PayloadType getPayloadType() {
        return PayloadType.REMOVE_REQ;
    }
}
