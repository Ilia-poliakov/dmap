package org.ipoliakov.dmap.node.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.ipoliakov.dmap.node.internal.cluster.raft.RaftReplicationService;
import org.ipoliakov.dmap.node.internal.cluster.raft.exception.ReplicationException;
import org.ipoliakov.dmap.protocol.raft.Operation;
import org.ipoliakov.dmap.util.ProtoMessages;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.protobuf.ByteString;

@ExtendWith(MockitoExtension.class)
class ReplicatedStorageServiceTest {

    @Mock
    private StorageMutationService storageService;
    @Mock
    private RaftReplicationService replicationService;

    @InjectMocks
    private ReplicatedStorageService replicatedStorageService;

    @Test
    @DisplayName("Put - should commit in storage only after successful replication")
    void put_OnlyAfterReplication() {
        var putReq = ProtoMessages.putReq()
                .setKey(ByteString.copyFromUtf8("key"))
                .setValue(ByteString.copyFromUtf8("value"))
                .build();
        when(storageService.put(putReq)).thenReturn(ByteString.EMPTY);
        when(replicationService.replicate(putReq.getPayloadType(), putReq))
                .thenReturn(Optional.of(
                        Operation.newBuilder()
                                .setPayloadType(putReq.getPayloadType())
                                .setMessage(putReq.toByteString())
                                .build()
                        )
                );

        replicatedStorageService.put(putReq);
        verify(replicationService).replicate(putReq.getPayloadType(), putReq);
        verify(storageService).put(putReq);
    }

    @Test
    @DisplayName("Put - should throw when replication failed")
    void put_throwIfReplicationFailed() {
        var putReq = ProtoMessages.putReq()
                .setKey(ByteString.copyFromUtf8("key"))
                .setValue(ByteString.copyFromUtf8("value"))
                .build();
        assertThrows(ReplicationException.class, () -> replicatedStorageService.put(putReq));
        verifyNoInteractions(storageService);
    }

    @Test
    @DisplayName("Remove - should commit in storage only after successful replication")
    void remove_OnlyAfterReplication() {
        var removeReq = ProtoMessages.removeReq(ByteString.copyFromUtf8("key"));
        when(storageService.remove(removeReq)).thenReturn(ByteString.EMPTY);
        when(replicationService.replicate(removeReq.getPayloadType(), removeReq))
                .thenReturn(Optional.of(
                                Operation.newBuilder()
                                        .setPayloadType(removeReq.getPayloadType())
                                        .setMessage(removeReq.toByteString())
                                        .build()
                        )
                );

        replicatedStorageService.remove(removeReq);
        verify(replicationService).replicate(removeReq.getPayloadType(), removeReq);
        verify(storageService).remove(removeReq);
    }

    @Test
    @DisplayName("Remove - should throw when replication failed")
    void remove_throwIfReplicationFailed() {
        var removeReq = ProtoMessages.removeReq(ByteString.copyFromUtf8("key"));
        assertThrows(ReplicationException.class, () -> replicatedStorageService.remove(removeReq));
        verifyNoInteractions(storageService);
    }
}