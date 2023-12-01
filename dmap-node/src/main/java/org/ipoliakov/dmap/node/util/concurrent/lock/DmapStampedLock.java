package org.ipoliakov.dmap.node.util.concurrent.lock;

import java.util.concurrent.locks.StampedLock;
import java.util.function.Supplier;

public class DmapStampedLock extends StampedLock {

    public <R> R optimisticRead(Supplier<R> readOperation) {
        long stamp = tryOptimisticRead();
        R result = readOperation.get();
        if (validate(stamp)) {
            return result;
        }
        return readLocked(readOperation);
    }

    public <R> R readLocked(Supplier<R> readOperation) {
        long stamp = readLock();
        try {
            return readOperation.get();
        } finally {
            unlock(stamp);
        }
    }

    public <R> R writeLocked(Supplier<R> writeOperation) {
        long stamp = writeLock();
        try {
            return writeOperation.get();
        } finally {
            unlockWrite(stamp);
        }
    }

    public long toWriteLock(long stamp) {
        long writeStamp = tryConvertToWriteLock(stamp);
        if (writeStamp == 0) {
            unlockRead(stamp);
            return writeLock();
        } else {
            return writeStamp;
        }
    }
}
