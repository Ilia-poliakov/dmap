package org.ipoliakov.dmap.node.cluster.raft.log.config;

import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.ipoliakov.dmap.datastructures.IntRingBuffer;
import org.ipoliakov.dmap.node.cluster.raft.log.io.RaftLogReader;
import org.ipoliakov.dmap.node.cluster.raft.log.io.RaftLogWriter;
import org.ipoliakov.dmap.node.cluster.raft.log.io.file.RaftLogFileReader;
import org.ipoliakov.dmap.node.cluster.raft.log.io.file.RaftLogFileWriter;
import org.ipoliakov.dmap.node.cluster.raft.log.operation.MutationOperation;
import org.ipoliakov.dmap.protocol.PayloadType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RaftLogConfig {

    @Value("${raft.log.file.growSize}")
    private Long growSize;

    @Bean
    public File raftLogFile(@Value("${raft.log.dir}") String raftDir) {
        new File(raftDir).mkdirs();
        return new File(raftDir + "/" + UUID.randomUUID());
    }

    @Bean
    public RaftLogReader raftLogReader(File raftLogFile) {
        return new RaftLogFileReader(raftLogFile);
    }

    @Bean
    public RaftLogWriter raftLogWriter(File raftLogFile) throws IOException {
        raftLogFile.createNewFile();
        return new RaftLogFileWriter(raftLogFile, growSize);
    }

    @Bean
    public IntRingBuffer raftLogFileIndex(@Value("${raft.log.index.capacity:4096}") int capacity) {
        return new IntRingBuffer(capacity);
    }

    @Bean
    public EnumMap<PayloadType, MutationOperation> operationMap(List<MutationOperation> operations) {
        return operations.stream()
                .collect(Collectors.toMap(
                        MutationOperation::getPayloadType,
                        operation -> operation,
                        (k1, k2) -> { throw new IllegalArgumentException("Duplicate operations for payloadType " + k1.getClass() + " and " + k2.getClass()); },
                        () -> new EnumMap<>(PayloadType.class)
                ));
    }
}
