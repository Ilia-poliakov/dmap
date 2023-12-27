package org.ipoliakov.dmap.common.network;

import java.util.Map;

import org.ipoliakov.dmap.protocol.DMapMessage;
import org.ipoliakov.dmap.protocol.PayloadType;
import org.ipoliakov.dmap.protocol.internal.raft.Operation;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;
import com.google.protobuf.Parser;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class ProtoMessageRegistry {

    private final Map<Class<?>, PayloadType> classPayloadTypeMap;
    private final Map<PayloadType, Parser<? extends MessageLite>> payloadTypeOnParser;

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
