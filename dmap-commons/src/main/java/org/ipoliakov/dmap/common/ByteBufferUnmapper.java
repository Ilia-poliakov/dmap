package org.ipoliakov.dmap.common;

import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodType.methodType;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;

public class ByteBufferUnmapper {

    private static final MethodHandle UNMAP = lookupUnmapMethodHandle();

    private static MethodHandle lookupUnmapMethodHandle() {
        MethodHandles.Lookup lookup = lookup();
        try {
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            MethodHandle unmapper = lookup.findVirtual(unsafeClass, "invokeCleaner", methodType(void.class, ByteBuffer.class));
            Field f = unsafeClass.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            Object theUnsafe = f.get(null);
            return unmapper.bindTo(theUnsafe);
        } catch (Exception e) {
            throw new RuntimeException(e);
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
