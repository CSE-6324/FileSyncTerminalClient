import org.junit.Test;

import static org.junit.Assert.*;

public class FileToSyncTest {

    @Test
    public void generateFileBlocks() {
        FileToSync fileToSync = new FileToSync(SyncClientType.WINDOWS.getLocalFilePath());
        Message returnMsg = fileToSync.generateFileBlocks();
        assertEquals(false, returnMsg.isMessageSuccess());

        fileToSync = new FileToSync(SyncClientType.WINDOWS.getLocalFilePath() + "stars.jpeg");
        returnMsg = fileToSync.generateFileBlocks();
        System.out.println(returnMsg.getMessage());
        assertEquals(true, returnMsg.isMessageSuccess());
        assertEquals(true, returnMsg.getMessage().equals(""));
    }

    @Test
    public void generateFileBlockCheckSums() {
        FileToSync fileToSync = new FileToSync(SyncClientType.MAC.getLocalFilePath() + "alpine.jpeg");
        Message returnMsg = fileToSync.generateFileBlocks();
        assertEquals(true, returnMsg.isMessageSuccess());
        returnMsg = fileToSync.generateFileBlockCheckSums();
        assertEquals(true, returnMsg.isMessageSuccess());
        assertEquals(true, returnMsg.getMessage().equals(""));
        for (FileBlock fb: fileToSync.getFileBlockList()) {
            System.out.println(fb.getFileCheckSum());
        }
    }

    @Test
    public void getTotalBlocks() {
        FileToSync fileToSync = new FileToSync(SyncClientType.MAC.getLocalFilePath() + "alpine.jpeg");
        Message returnMsg = fileToSync.generateFileBlocks();
        assertEquals(true, returnMsg.isMessageSuccess());
        assertEquals(4, fileToSync.getTotalBlocks());
    }

    @Test
    public void getFileBlockList() {
        FileToSync fileToSync = new FileToSync(SyncClientType.MAC.getLocalFilePath() + "alpine.jpeg");
        Message returnMsg = fileToSync.generateFileBlocks();
        assertEquals(true, returnMsg.isMessageSuccess());
        int count = 0;
        for (FileBlock fb: fileToSync.getFileBlockList()) {
            count++;
        }
        assertEquals(4, count);
    }

    @Test
    public void deleteAllFileBlocks() {
        FileToSync fileToSync = new FileToSync(SyncClientType.MAC.getLocalFilePath() + "alpine.jpeg");
        Message returnMsg = fileToSync.generateFileBlocks();
        assertEquals(true, returnMsg.isMessageSuccess());
        fileToSync.deleteAllFileBlocks();
        assertEquals(0, fileToSync.getTotalBlocks());

        fileToSync = new FileToSync(SyncClientType.WINDOWS.getLocalFilePath() + "stars.jpeg");
        returnMsg = fileToSync.generateFileBlocks();
        assertEquals(true, returnMsg.isMessageSuccess());
        fileToSync.deleteAllFileBlocks();
        assertEquals(0, fileToSync.getTotalBlocks());
    }

}