package org.ipoliakov.dmap.node.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.ipoliakov.dmap.node.storage.Storage;
import org.ipoliakov.dmap.protocol.PutReq;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.protobuf.ByteString;

@ExtendWith(MockitoExtension.class)
class StorageServiceImplTest {

    private static final ByteString KEY = ByteString.copyFromUtf8("key");
    private static final ByteString VALUE = ByteString.copyFromUtf8("value");

    @Mock
    private Storage storage;

    @InjectMocks
    private StorageServiceImpl storageService;

    @AfterEach
    void tearDown() {
        Mockito.reset(storage);
    }

    @Test
    @DisplayName("put - return empty byteString when adding new value")
    void put_newValue() {
        ByteString prevValue = storageService.put(PutReq.newBuilder()
                .setKey(KEY)
                .setValue(VALUE)
                .build());
        assertEquals(ByteString.EMPTY, prevValue);
    }

    @Test
    @DisplayName("put - return previous value when another value already associated with this key")
    void put_replaceValue() {
        ByteString newValue = ByteString.copyFromUtf8("new value");
        when(storage.put(KEY, newValue)).thenReturn(VALUE);
        ByteString prevValue = storageService.put(PutReq.newBuilder()
                .setKey(KEY)
                .setValue(newValue)
                .build());
        assertEquals(VALUE, prevValue);
    }

    @Test
    @DisplayName("get - return value when exists")
    void get() {
        when(storage.get(KEY)).thenReturn(VALUE);
        ByteString actualValue = storageService.get(KEY);
        assertEquals(VALUE, actualValue);
    }

    @Test
    @DisplayName("get - return empty byte string when non exists")
    void get_nonExists() {
        ByteString actualValue = storageService.get(KEY);
        assertEquals(ByteString.EMPTY, actualValue);
    }

}