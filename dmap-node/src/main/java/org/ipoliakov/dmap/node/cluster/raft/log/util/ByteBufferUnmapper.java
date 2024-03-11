package org.ipoliakov.dmap.node.cluster.raft.log.util;

import static java.lang.invoke.MethodType.methodType;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;

public class ByteBufferUnmapper {

    private static final MethodHandle UNMAP = lookupUnmapMethodHandle();

    private static MethodHandle lookupUnmapMethodHandle() {
        try {
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            Field f = unsafeClass.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            Object theUnsafe = f.get(null);
            return MethodHandles.lookup()
                    .findVirtual(unsafeClass, "invokeCleaner", methodType(void.class, ByteBuffer.class))
                    .bindTo(theUnsafe);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static void unmap(ByteBuffer mmap) {
        try {
            UNMAP.invokeExact(mmap);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
