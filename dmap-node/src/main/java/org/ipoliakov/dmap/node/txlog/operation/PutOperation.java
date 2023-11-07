package org.ipoliakov.dmap.node.txlog.operation;

import org.ipoliakov.dmap.node.service.StorageMutationService;
import org.ipoliakov.dmap.protocol.PayloadType;
import org.ipoliakov.dmap.protocol.PutReq;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PutOperation implements MutationOperation<PutReq> {

    private final StorageMutationService storageService;

    @Override
    public void execute(PutReq message) {
        log.info("Repairing Put operation - start: message = {}", message);

        storageService.put(message);

        log.info("Repairing Put operation - end");
    }

    @Override
    public PayloadType getPayloadType() {
        return PayloadType.PUT_REQ;
    }
}
