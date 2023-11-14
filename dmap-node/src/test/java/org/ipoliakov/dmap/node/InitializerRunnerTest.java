package org.ipoliakov.dmap.node;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class InitializerRunnerTest extends IntegrationTest {

    @Autowired
    private InitializerRunner initializerRunner;

    @Test
    void run() {
        initializerRunner.run();
    }
}