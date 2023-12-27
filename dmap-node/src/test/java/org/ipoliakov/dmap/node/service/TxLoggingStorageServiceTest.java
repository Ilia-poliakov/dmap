package org.ipoliakov.dmap.node.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.ipoliakov.dmap.node.internal.cluster.raft.RaftLog;
import org.ipoliakov.dmap.node.internal.cluster.raft.state.RaftState;
import org.ipoliakov.dmap.protocol.client.PutReq;
import org.ipoliakov.dmap.protocol.client.RemoveReq;
import org.ipoliakov.dmap.protocol.internal.raft.Operation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.protobuf.ByteString;

@ExtendWith(MockitoExtension.class)
class TxLoggingStorageServiceTest {

    @Mock
    private RaftLog raftLog;
    @Mock
    private RaftState raftState;
    @Mock
    private StorageMutationService storageService;

    @InjectMocks
    private TxLoggingStorageService txLoggingStorageService;

    @Test
    void put() {
        PutReq putReq = PutReq.newBuilder()
                .setKey(ByteString.copyFromUtf8("key"))
                .setValue(ByteString.copyFromUtf8("val"))
                .build();
        txLoggingStorageService.put(putReq);

        verify(raftLog).append(any(Operation.class));
        verify(storageService).put(putReq);
    }

    @Test
    void remove() {
        ByteString expectedRemovedValue = ByteString.copyFromUtf8("removed");
        when(storageService.remove(any(RemoveReq.class)))
                .thenReturn(expectedRemovedValue);

        RemoveReq req = RemoveReq.newBuilder()
                .setKey(ByteString.copyFromUtf8("key"))
                .build();
        ByteString actualRemovedValue = txLoggingStorageService.remove(req);

        assertEquals(expectedRemovedValue, actualRemovedValue);
        verify(raftLog).append(any(Operation.class));
        verify(storageService).remove(req);
    }
}