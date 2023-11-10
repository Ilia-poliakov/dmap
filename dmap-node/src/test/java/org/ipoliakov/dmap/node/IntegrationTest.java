package org.ipoliakov.dmap.node;

import java.io.File;
import java.util.Map;

import org.ipoliakov.dmap.client.DMapClient;
import org.ipoliakov.dmap.node.internal.cluster.RaftCluster;
import org.ipoliakov.dmap.node.internal.cluster.raft.RaftLog;
import org.ipoliakov.dmap.node.internal.cluster.raft.RaftState;
import org.ipoliakov.dmap.node.txlog.io.TxLogWriter;
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
    @Autowired
    private TxLogWriter txLogWriter;
    @Autowired
    protected RaftCluster raftCluster;
    @Autowired
    protected RaftLog raftLog;
    @Autowired
    protected RaftState raftState;

    @AfterEach
    void tearDown() throws Exception {
        dataStorage.clear();
        raftState.reset();
        raftLog.setLastTerm(1);
        raftLog.setLastIndex(1);
        txLogWriter.close();
        txLogFile.delete();
    }
}
