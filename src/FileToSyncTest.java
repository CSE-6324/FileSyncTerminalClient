import org.junit.Test;

import static org.junit.Assert.*;

public class FileToSyncTest {

    @Test
    public void generateFileBlocks() {
        FileToSync fileToSync = new FileToSync(SyncClientType.WINDOWS.getLocalFilePath());
        Message returnMsg = fileToSync.generateFileBlocks();
        assertEquals(false, returnMsg.isMessageSuccess());
    }

    @Test
    public void generateFileBlockCheckSums() {
    }

    @Test
    public void getTotalBlocks() {
    }

    @Test
    public void getFileToSyncName() {
    }

    @Test
    public void getFileBlockList() {
    }

    @Test
    public void deleteAllFileBlocks() {
    }

    @Test
    public void getFileBlocks() {
    }

    @Test
    public void getBlockName() {
    }
}