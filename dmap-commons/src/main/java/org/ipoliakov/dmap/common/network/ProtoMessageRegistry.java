package org.ipoliakov.dmap.common.network;

import static org.reflections.scanners.Scanners.SubTypes;

import java.lang.reflect.Method;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.ipoliakov.dmap.protocol.DMapMessage;
import org.ipoliakov.dmap.protocol.PayloadType;
import org.ipoliakov.dmap.protocol.raft.Operation;
import org.reflections.Reflections;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;
import com.google.protobuf.Parser;

public class ProtoMessageRegistry {

    private static final Map<Class<?>, PayloadType> classPayloadTypeMap;
    private static final Map<PayloadType, Parser<? extends MessageLite>> payloadTypeOnParser;

    static {
        Map<PayloadType, Parser<? extends MessageLite>> payloadParser = new EnumMap<>(PayloadType.class);
        Map<Class<?>, PayloadType> classOnPayloadType = new IdentityHashMap<>();
        try {
            Set<MessageLite> defaultInstances = getProtoReqAndResDefaultInstances();
            Pattern toSnakeCasePattern = Pattern.compile("([a-z])([A-Z]+)");
            for (MessageLite messageLite : defaultInstances) {
                String name = messageLite.getClass().getSimpleName();
                name = toSnakeCasePattern.matcher(name).replaceAll("$1_$2").toUpperCase();
                PayloadType pt = PayloadType.valueOf(name);
                payloadParser.put(pt, messageLite.getParserForType());
                classOnPayloadType.put(messageLite.getClass(), pt);
            }
            payloadTypeOnParser = payloadParser;
            classPayloadTypeMap = classOnPayloadType;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Set<MessageLite> getProtoReqAndResDefaultInstances() throws Exception {
        Set<Class> allClasses = collectRequestsAndResponses();
        Set<MessageLite> instances = new HashSet<>();
        for (Class<?> clazz : allClasses) {
            Method factoryMethod = clazz.getDeclaredMethod("getDefaultInstance");
            MessageLite messageLite = (MessageLite) factoryMethod.invoke(clazz);
            instances.add(messageLite);
        }
        return instances;
    }

    private static Set<Class> collectRequestsAndResponses() {
        return new Reflections(DMapMessage.class.getPackageName())
            .get(
                SubTypes.of(MessageLite.class)
                    .as(Class.class, DMapMessage.class.getClassLoader())
                    .filter(clazz -> clazz.getName().endsWith("Res") || clazz.getName().endsWith("Req"))
            );
    }

    public MessageLite parsePayload(DMapMessage protoMessage) {
        PayloadType payloadType = protoMessage.getPayloadType();
        return parsePayload(payloadType, protoMessage.getPayload());
    }

    public MessageLite parsePayload(Operation operation) {
        PayloadType payloadType = operation.getPayloadType();
        return parsePayload(payloadType, operation.getMessage());
    }

    private MessageLite parsePayload(PayloadType payloadType, ByteString payload) {
        try {
            Parser<? extends MessageLite> parser = payloadTypeOnParser.get(payloadType);
            return parser.parseFrom(payload);
        } catch (InvalidProtocolBufferException e) {
            throw new IllegalArgumentException("Cannot parse proto message", e);
        }
    }

    public PayloadType getPayloadType(Class<? extends MessageLite> type) {
        return classPayloadTypeMap.get(type);
    }
}
