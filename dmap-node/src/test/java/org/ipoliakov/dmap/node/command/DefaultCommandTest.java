package org.ipoliakov.dmap.node.command;

import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class DefaultCommandTest {

    @Test
    void getPayloadType_alwaysNull() {
        var defaultCommand = new DefaultCommand();
        assertNull(defaultCommand.getPayloadType());
    }
}