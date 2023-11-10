package org.ipoliakov.dmap.node.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ProtoUtilsTest {

    @Test
    void instanceShouldNotBeCreated() {
        Constructor<?> constructor = ProtoUtils.class.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        Assertions.assertThrows(InvocationTargetException.class, constructor::newInstance);
    }
}