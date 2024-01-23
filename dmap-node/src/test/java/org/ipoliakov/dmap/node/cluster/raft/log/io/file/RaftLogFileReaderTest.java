package org.ipoliakov.dmap.node.cluster.raft.log.io.file;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;

import org.ipoliakov.dmap.node.cluster.raft.log.exception.RaftLogReadingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RaftLogFileReaderTest {

    @Test
    @DisplayName("readAll - translate FileNotFoundException to RaftLogReadingException")
    void readAll_translateFileNotFoundException_To_RaftLogReadingException() {
        RaftLogFileReader reader = new RaftLogFileReader(new File("non_existing_file"));
        assertThrows(RaftLogReadingException.class, reader::readAll);
    }
}