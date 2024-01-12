package org.ipoliakov.dmap.node;

import java.io.File;
import java.util.Map;

import org.ipoliakov.dmap.client.DMapClient;
import org.ipoliakov.dmap.datastructures.IntRingBuffer;
import org.ipoliakov.dmap.node.internal.cluster.Cluster;
import org.ipoliakov.dmap.node.internal.cluster.raft.RaftLog;
import org.ipoliakov.dmap.node.internal.cluster.raft.election.ElectionService;
import org.ipoliakov.dmap.node.internal.cluster.raft.state.RaftState;
import org.ipoliakov.dmap.node.txlog.io.TxLogWriter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
    protected Cluster cluster;
    @Autowired
    protected RaftLog raftLog;
    @Autowired
    protected RaftState raftState;
    @Autowired
    protected IntRingBuffer txLogFileIndex;
    @Autowired
    protected ElectionService electionService;

    @BeforeEach
    void setUp() throws Exception {
        recreateLog();
    }

    @AfterEach
    void tearDown() throws Exception {
        resetState();
    }

    protected void resetState() throws Exception {
        dataStorage.clear();
        raftState.reset();
        raftLog.setLastTerm(0);
        raftLog.setLastIndex(0);
        txLogWriter.close();
        txLogFileIndex.clear();
        electionService.restartElectionTask();
    }

    private void recreateLog() throws Exception {
        txLogFile.delete();
        txLogFile.createNewFile();
    }
}
