package org.ipoliakov.dmap.node.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;

import org.ipoliakov.dmap.protocol.PutReq;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.protobuf.ByteString;

class StorageServiceImplTest {

    private static final ByteString KEY = ByteString.copyFromUtf8("key");
    private static final ByteString VALUE = ByteString.copyFromUtf8("value");

    private StorageServiceImpl storageService;

    @BeforeEach
    void setUp() {
        storageService = new StorageServiceImpl(new HashMap<>());
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
        storageService.put(PutReq.newBuilder()
                .setKey(KEY)
                .setValue(VALUE)
                .build());
        ByteString prevValue = storageService.put(PutReq.newBuilder()
                .setKey(KEY)
                .setValue(newValue)
                .build());
        assertEquals(VALUE, prevValue);
    }

    @Test
    @DisplayName("get - return value when exists")
    void get() {
        storageService.put(PutReq.newBuilder()
                .setKey(KEY)
                .setValue(VALUE)
                .build());
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