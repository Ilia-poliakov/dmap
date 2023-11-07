package org.ipoliakov.dmap.node.txlog.io.file;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;

import org.ipoliakov.dmap.node.txlog.exception.TxLogReadingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TxLogFileReaderTest {

    @Test
    @DisplayName("readAll - translate FileNotFoundException to TxLogReadingException")
    void readAll_translateFileNotFoundException_To_TxLogReadingException() {
        TxLogFileReader reader = new TxLogFileReader(new File("non_existing_file"));
        assertThrows(TxLogReadingException.class, reader::readAll);
    }
}