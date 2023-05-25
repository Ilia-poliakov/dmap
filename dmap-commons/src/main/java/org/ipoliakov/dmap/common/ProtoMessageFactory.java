package org.ipoliakov.dmap.common;

import java.lang.reflect.Method;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.ipoliakov.dmap.protocol.DMapMessage;
import org.ipoliakov.dmap.protocol.PayloadType;
import org.reflections.Reflections;

import com.google.protobuf.Descriptors;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.MessageLite;
import com.google.protobuf.Parser;

public class ProtoMessageFactory {

    private static final Map<Class<?>, PayloadType> classPayloadTypeMap;
    private static final Map<PayloadType, Parser<? extends MessageLite>> payloadTypeOnParser;

    static {
        Map<PayloadType, Parser<? extends MessageLite>> payloadParser = new EnumMap<>(PayloadType.class);
        Map<Class<?>, PayloadType> classOnPayloadType = new IdentityHashMap<>();
        try {
            Set<Class<? extends MessageLite>> allClasses = collectRequestsAndResponses();
            Set<MessageLite> defaultInstances = getProtoClassesDefaultInstances(allClasses);
            Pattern toSnakeCasePattern = Pattern.compile("([a-z])([A-Z]+)");
            for (MessageLite messageLite : defaultInstances) {
                if (messageLite instanceof Message message) {
                    Descriptors.FieldDescriptor payloadType = message.getDescriptorForType().findFieldByName("payloadType");
                    if (payloadType != null) {
                        String name = message.getClass().getSimpleName();
                        name = toSnakeCasePattern.matcher(name).replaceAll("$1_$2").toUpperCase();
                        PayloadType pt = PayloadType.valueOf(name);
                        payloadParser.put(pt, message.getParserForType());
                        classOnPayloadType.put(message.getClass(), pt);
                    }
                }
            }
            payloadTypeOnParser = payloadParser;
            classPayloadTypeMap = classOnPayloadType;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Set<Class<? extends MessageLite>> collectRequestsAndResponses() {
        Reflections reflections = new Reflections(DMapMessage.class.getPackageName());
        Set<Class<? extends MessageLite>> allClasses = reflections.getSubTypesOf(MessageLite.class);
        allClasses.removeIf(clazz -> !(clazz.getName().endsWith("Res") || clazz.getName().endsWith("Req")));
        return allClasses;
    }

    private static Set<MessageLite> getProtoClassesDefaultInstances(Set<Class<? extends MessageLite>> protoClasses) throws Exception {
        Set<MessageLite> instances = new HashSet<>();
        for (Class<?> clazz : protoClasses) {
            Method factoryMethod = clazz.getDeclaredMethod("getDefaultInstance");
            MessageLite messageLite = (MessageLite) factoryMethod.invoke(clazz);
            instances.add(messageLite);
        }
        return instances;
    }

    public MessageLite parsePayload(DMapMessage protoMessage) throws InvalidProtocolBufferException {
        PayloadType payloadType = protoMessage.getPayloadType();
        Parser<? extends MessageLite> parser = payloadTypeOnParser.get(payloadType);
        return parser.parseFrom(protoMessage.getPayload());
    }

    public PayloadType getPayloadType(Class<? extends MessageLite> type) {
        return classPayloadTypeMap.get(type);
    }
}
