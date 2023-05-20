package org.ipoliakov.dmap.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Method;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.reflections.Reflections;

import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;
import com.google.protobuf.MessageLite;

public class ProtocolTest {

    @Test
    void payloadTypePositionTest() throws Exception {
        Reflections reflections = new Reflections(DMapMessage.class.getPackageName());
        Set<Class<? extends MessageLite>> allClasses = reflections.getSubTypesOf(MessageLite.class);
        allClasses.removeIf(clazz -> !(clazz.getName().endsWith("Res") || clazz.getName().endsWith("Req")));

        for (Class<? extends MessageLite> clazz : allClasses) {
            Method factoryMethod = clazz.getDeclaredMethod("getDefaultInstance");
            MessageLite messageLite = (MessageLite) factoryMethod.invoke(clazz);
            Message message = (Message) messageLite.getDefaultInstanceForType();
            Descriptors.FieldDescriptor payloadType = message.getDescriptorForType().findFieldByName("payloadType");

            assertNotNull(payloadType, "payloadType is required for *Req or *Res messages, but not specified for message " + clazz);
            assertEquals(1, payloadType.getNumber(), "payloadType must be first field in message " + clazz);
        }
    }
}
