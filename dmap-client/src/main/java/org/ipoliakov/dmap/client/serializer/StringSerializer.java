package org.ipoliakov.dmap.client.serializer;

import org.ipoliakov.dmap.client.Serializer;

import com.google.protobuf.ByteString;

public class StringSerializer implements Serializer<String, ByteString> {

    @Override
    public ByteString serialize(String src) {
        return ByteString.copyFromUtf8(src);
    }

    @Override
    public String dserialize(ByteString dst) {
        return dst.toStringUtf8();
    }
}
