package org.ipoliakov.dmap.common;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MonotonicallyIdGeneratorTest {

    @Test
    @DisplayName("next - should start from zero by default")
    void next_startFromZeroByDefault() {
        assertEquals(0, new MonotonicallyIdGenerator().next());
    }

    @Test
    @DisplayName("next - should monotonically incrementing")
    void next_monotonicallyIncrement() {
        var generator = new MonotonicallyIdGenerator(1);
        assertAll(
            "monotonically increment from 1",
            () -> assertEquals(1, generator.next()),
            () -> assertEquals(2, generator.next()),
            () -> assertEquals(3, generator.next()),
            () -> assertEquals(4, generator.next()),
            () -> assertEquals(5, generator.next())
        );
    }
}