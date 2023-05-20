package org.ipoliakov.dmap.node.storage;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

import com.google.protobuf.ByteString;

@Component
public class SimpleMapStorage implements Storage {

    private final ConcurrentMap<ByteString, ByteString> map = new ConcurrentHashMap<>(1024);

    @Override
    public ByteString put(ByteString key, ByteString value) {
        return map.put(key, value);
    }

    @Override
    public ByteString get(ByteString key) {
        return map.get(key);
    }

    @Override
    public void clear() {
        map.clear();
    }
}
