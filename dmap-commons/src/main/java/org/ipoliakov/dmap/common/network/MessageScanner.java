package org.ipoliakov.dmap.common.network;

import static org.reflections.scanners.Scanners.SubTypes;

import java.lang.reflect.Method;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.ipoliakov.dmap.protocol.PayloadType;
import org.reflections.Reflections;

import com.google.protobuf.MessageLite;
import com.google.protobuf.Parser;

public class MessageScanner {

    public static ProtoMessageRegistry scan(Class<? extends MessageLite>... packageToScan) {
        Map<PayloadType, Parser<? extends MessageLite>> payloadOnParserMap = new EnumMap<>(PayloadType.class);
        Map<Class<?>, PayloadType> classOnPayloadTypeMap = new IdentityHashMap<>();
        try {
            Set<MessageLite> defaultInstances = getProtoReqAndResDefaultInstances(packageToScan);
            Pattern toSnakeCasePattern = Pattern.compile("([a-z])([A-Z]+)");
            for (MessageLite messageLite : defaultInstances) {
                String name = messageLite.getClass().getSimpleName();
                name = toSnakeCasePattern.matcher(name).replaceAll("$1_$2").toUpperCase();
                PayloadType payloadType = PayloadType.valueOf(name);
                payloadOnParserMap.put(payloadType, messageLite.getParserForType());
                classOnPayloadTypeMap.put(messageLite.getClass(), payloadType);
            }

            return new ProtoMessageRegistry(classOnPayloadTypeMap, payloadOnParserMap);
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot initialize protocol messages", e);
        }
    }

    private static Set<MessageLite> getProtoReqAndResDefaultInstances(Class<? extends MessageLite>... packageToScan) throws Exception {
        Set<Class> allClasses = collectRequestsAndResponses(packageToScan);
        Set<MessageLite> instances = new HashSet<>();
        for (Class<?> clazz : allClasses) {
            Method factoryMethod = clazz.getDeclaredMethod("getDefaultInstance");
            MessageLite messageLite = (MessageLite) factoryMethod.invoke(clazz);
            instances.add(messageLite);
        }
        return instances;
    }

    private static Set<Class> collectRequestsAndResponses(Class<? extends MessageLite>... packagesToScan) {
        return new Reflections(packagesToScan)
                .get(
                        SubTypes.of(MessageLite.class)
                                .as(Class.class, packagesToScan[0].getClassLoader())
                                .filter(clazz -> clazz.getName().endsWith("Res") || clazz.getName().endsWith("Req"))
                );
    }
}
