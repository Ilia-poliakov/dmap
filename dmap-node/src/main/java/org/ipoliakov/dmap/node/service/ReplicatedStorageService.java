package org.ipoliakov.dmap.node.service;

import org.ipoliakov.dmap.node.internal.cluster.raft.RaftReplicationService;
import org.ipoliakov.dmap.node.internal.cluster.raft.exception.ReplicationException;
import org.ipoliakov.dmap.protocol.storage.PutReq;
import org.ipoliakov.dmap.protocol.storage.RemoveReq;
import org.springframework.stereotype.Service;

import com.google.protobuf.ByteString;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReplicatedStorageService implements StorageMutationService {

    private final StorageMutationService storageService;
    private final RaftReplicationService replicationService;

    @Override
    public ByteString put(PutReq req) {
        return replicationService.replicate(req.getPayloadType(), req)
                .map(operation -> storageService.put(req))
                .orElseThrow(ReplicationException::new);
    }

    @Override
    public ByteString remove(RemoveReq req) {
        return replicationService.replicate(req.getPayloadType(), req)
                .map(operation -> storageService.remove(req))
                .orElseThrow(ReplicationException::new);
    }
}
