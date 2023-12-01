package org.ipoliakov.dmap.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.ipoliakov.dmap.protocol.PayloadType;
import org.ipoliakov.dmap.protocol.ValueRes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.protobuf.ByteString;

class ProtoUtilsTest {

    @Test
    void instanceShouldNotBeCreated() {
        Constructor<?> constructor = ProtoUtils.class.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        Assertions.assertThrows(InvocationTargetException.class, constructor::newInstance);
    }

    @Test
    void valueRes() {
        assertEquals(
                ValueRes.newBuilder()
                        .setValue(ByteString.copyFromUtf8("value"))
                        .setPayloadType(PayloadType.VALUE_RES)
                        .build(),
                ProtoUtils.valueRes(ByteString.copyFromUtf8("value"))
        );
    }
}