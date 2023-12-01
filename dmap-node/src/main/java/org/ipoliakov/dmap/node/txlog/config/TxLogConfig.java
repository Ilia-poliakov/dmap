package org.ipoliakov.dmap.node.txlog.config;

import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.ipoliakov.dmap.datastructures.IntRingBuffer;
import org.ipoliakov.dmap.node.txlog.io.TxLogReader;
import org.ipoliakov.dmap.node.txlog.io.TxLogWriter;
import org.ipoliakov.dmap.node.txlog.io.file.TxLogFileReader;
import org.ipoliakov.dmap.node.txlog.io.file.TxLogFileWriter;
import org.ipoliakov.dmap.node.txlog.operation.MutationOperation;
import org.ipoliakov.dmap.protocol.PayloadType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TxLogConfig {

    @Value("${tx.log.file.growSize}")
    private Long growSize;

    @Bean
    public File txLogFile(@Value("${tx.log.dir}") String txDir) throws IOException {
        FileUtils.forceMkdir(new File(txDir));
        return new File(txDir + "/" + UUID.randomUUID());
    }

    @Bean
    public TxLogReader txLogReader(File txLogFile) {
        return new TxLogFileReader(txLogFile);
    }

    @Bean
    public TxLogWriter txLogWriter(File txLogFile) throws IOException {
        txLogFile.createNewFile();
        return new TxLogFileWriter(txLogFile, growSize);
    }

    @Bean
    public IntRingBuffer txLogFileIndex(@Value("${tx.log.index.capacity:4096}") int capacity) {
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
