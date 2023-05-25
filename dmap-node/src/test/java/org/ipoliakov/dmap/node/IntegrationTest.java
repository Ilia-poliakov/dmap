package org.ipoliakov.dmap.node;

import org.ipoliakov.dmap.client.DMapClient;
import org.ipoliakov.dmap.node.storage.SimpleMapStorage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { Main.class, TestConfig.class })
public abstract class IntegrationTest {

    @Autowired
    protected SimpleMapStorage storage;
    @Autowired
    protected DMapClient<String, String> client;

    @AfterEach
    void tearDown() {
        storage.clear();
    }
}
