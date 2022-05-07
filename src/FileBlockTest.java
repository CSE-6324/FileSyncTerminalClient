import org.junit.Test;

import static org.junit.Assert.*;

public class FileBlockTest {

    @Test
    public void generateCheckSum() {
        String fileName = "file_test_001.txt";
        FileBlock testFileBlock = new FileBlock(SyncServer.LOCALHOST.getServerFolderPath() + fileName);
        Message returnMsg = testFileBlock.generateCheckSum();
        System.out.println(returnMsg.getMessage());
        System.out.println(testFileBlock.getFileBlockName() + "::" + testFileBlock.getFileCheckSum());
    }
}