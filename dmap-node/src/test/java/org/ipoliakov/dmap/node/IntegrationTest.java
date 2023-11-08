package org.ipoliakov.dmap.node;

import java.io.File;
import java.util.Map;

import org.ipoliakov.dmap.client.DMapClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.google.protobuf.ByteString;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { Main.class, TestConfig.class })
public abstract class IntegrationTest {

    @Autowired
    protected Map<ByteString, ByteString> dataStorage;
    @Autowired
    protected DMapClient<String, String> client;
    @Autowired
    private File txLogFile;

    @AfterEach
    void tearDown() {
        dataStorage.clear();
        txLogFile.deleteOnExit();
    }
}
