package org.ipoliakov.dmap.util.concurrent;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@Timeout(10)
class LockFreeSnowflakeIdGeneratorTest {

    private static final long TIMESTAMP = 1703354186594L;
    private static final long EXPECTED_ID = 7144385278248001536L;
    private static final Clock CLOCK = Clock.fixed(Instant.ofEpochMilli(TIMESTAMP), ZoneId.systemDefault());

    @Test
    @DisplayName("nodeId must be between 0 and 1023")
    void maxNodeId() {
        assertThrows(IllegalArgumentException.class, () -> new LockFreeSnowflakeIdGenerator(CLOCK, 1024));
        assertThrows(IllegalArgumentException.class, () -> new LockFreeSnowflakeIdGenerator(CLOCK, -1));
    }

    @Test
    @DisplayName("next - should increment sequence when same timestamp and node id")
    void next() {
        var idGenerator = new LockFreeSnowflakeIdGenerator(CLOCK, 10);
        assertAll(
            "Different id on same timestamp",
            () -> assertEquals(EXPECTED_ID, idGenerator.next()),
            () -> assertEquals(EXPECTED_ID + 1, idGenerator.next()),
            () -> assertEquals(EXPECTED_ID + 2, idGenerator.next())
        );
    }

    @Test
    @DisplayName("next - should wait next timestamp when max sequence overflow")
    void shouldWait_whenMaxSequenceOverflow() throws InterruptedException {
        int maxSequence = 4095;
        ClockAdapter clock = new ClockAdapter() {

            int currentIteration;

            @Override
            public Instant instant() {
                if (currentIteration <= maxSequence + 1) {
                    currentIteration++;
                    return Instant.ofEpochMilli(time.get());
                }
                return Instant.ofEpochMilli(time.incrementAndGet());
            }
        };

        var idGenerator = new LockFreeSnowflakeIdGenerator(clock, 10);
        for (int i = 0; i <= maxSequence; i++) {
            idGenerator.next();
        }
        AtomicLong generatedId = new AtomicLong();
        Thread thread = new Thread(() -> generatedId.set(idGenerator.next()));
        thread.setDaemon(true);
        thread.start();
        thread.join();

        assertEquals(423665664L, generatedId.get());
    }

    @Test
    @DisplayName("next - should throw IllegalStateException when time gone backwards")
    void next_timeGoneBackwards() {
        Clock backwardClock = new ClockAdapter() {

            @Override
            public Instant instant() {
                return Instant.ofEpochMilli(time.getAndDecrement());
            }
        };
        var generator = new LockFreeSnowflakeIdGenerator(backwardClock, 10);
        assertEquals(419471360L, generator.next());
        assertThrows(IllegalStateException.class, generator::next);
    }

    private static abstract class ClockAdapter extends Clock {

        AtomicLong time = new AtomicLong(100);

        @Override
        public ZoneId getZone() {
            return ZoneId.systemDefault();
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }
    }
}