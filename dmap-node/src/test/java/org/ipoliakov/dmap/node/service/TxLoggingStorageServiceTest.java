package org.ipoliakov.dmap.node.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.UncheckedIOException;

import org.ipoliakov.dmap.node.txlog.io.TxLogWriter;
import org.ipoliakov.dmap.protocol.PutReq;
import org.ipoliakov.dmap.protocol.RemoveReq;
import org.ipoliakov.dmap.protocol.internal.Operation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.protobuf.ByteString;

@ExtendWith(MockitoExtension.class)
class TxLoggingStorageServiceTest {

    @Mock
    private TxLogWriter txLogWriter;
    @Mock
    private StorageMutationService storageService;

    @InjectMocks
    private TxLoggingStorageService txLoggingStorageService;

    @Test
    void put() throws IOException {
        PutReq putReq = PutReq.newBuilder()
                .setKey(ByteString.copyFromUtf8("key"))
                .setValue(ByteString.copyFromUtf8("val"))
                .build();
        txLoggingStorageService.put(putReq);

        verify(txLogWriter).write(any(Operation.class));
        verify(storageService).put(putReq);
    }

    @Test
    void remove() throws IOException {
        ByteString expectedRemovedValue = ByteString.copyFromUtf8("removed");
        when(storageService.remove(any(RemoveReq.class)))
                .thenReturn(expectedRemovedValue);

        RemoveReq req = RemoveReq.newBuilder()
                .setKey(ByteString.copyFromUtf8("key"))
                .setValue(ByteString.copyFromUtf8("val"))
                .build();
        ByteString actualRemovedValue = txLoggingStorageService.remove(req);

        assertEquals(expectedRemovedValue, actualRemovedValue);
        verify(txLogWriter).write(any(Operation.class));
        verify(storageService).remove(req);
    }

    @Test
    void put_RethrowAsUncheckedIOException() throws IOException {
        doThrow(IOException.class)
                .when(txLogWriter).write(any(Operation.class));

        assertThrows(UncheckedIOException.class, () -> txLoggingStorageService.put(PutReq.getDefaultInstance()));

    }
}