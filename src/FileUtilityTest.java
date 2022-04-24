import org.junit.Test;

import java.io.File;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class FileUtilityTest {

    @org.junit.Before
    public void setUp() throws Exception {
    }

    @org.junit.After
    public void tearDown() throws Exception {
    }

    @org.junit.Test
    public void getFileInfo() {
    }

    @org.junit.Test
    public void getFileBlocks() {
        ArrayList<File> macFiles = SyncClientType.MAC.getLocalFiles();
        ArrayList<FileToSync> fileToSyncList = new ArrayList<>();
        Message returnMessage;
        for (File f: macFiles) {
            FileToSync fileToSync = new FileToSync(f);
            returnMessage = fileToSync.generateFileBlocks();
            if (!returnMessage.isMessageSuccess()) {
                System.out.println(returnMessage.getMessage());
                break;
            }
            returnMessage = fileToSync.generateFileBlockCheckSums();
            if (!returnMessage.isMessageSuccess()) {
                System.out.println(returnMessage.getMessage());
                break;
            }
            fileToSyncList.add(fileToSync);
        }

        for (FileToSync f2s: fileToSyncList) {
            System.out.println("File To Sync: " + f2s.getFileToSyncName());
            System.out.println("Total Blocks: " + f2s.getTotalBlocks());
            for (FileBlock fb: f2s.getFileBlockList()) {
                System.out.println("File Block Name: " + fb.getFileBlockName());
                System.out.println("File Block Number: " + fb.getFileBlockNumber());
                System.out.println("File Block Checksum: " + fb.getFileCheckSum());
                System.out.println("File Block Size: " + fb.getFileBlock().length());
            }
            System.out.println("-------------------------");
        }

        // will delete all file blocks after sent to server
        for (FileToSync f2s: fileToSyncList) {
            returnMessage = f2s.deleteAllFileBlocks();
            if (!returnMessage.isMessageSuccess()) {
                System.out.println(returnMessage.getMessage());
                break;
            }
        }
    }

    @org.junit.Test
    public void getServerFileNameList() {
        // I only have one file here for testing for now.
        // Need to change this if more files are added.
        ArrayList<String> serverFiles = FileUtility.getServerFileNameList();
//        for (String fileName: serverFiles) {
//            System.out.println(fileName);
//        }
        String expectedName = "milkyway.jpeg";
        String actualName = serverFiles.get(0);
        assertEquals(true, expectedName.equals(actualName));
    }

    @Test
    public void getFileNameFromFileBlockName() {
        String fileBlockName = "milkyway_001.jpeg";
        String expectedName = "milkyway.jpeg";
        String actualName = FileUtility.getFileNameFromFileBlockName(fileBlockName);
        assertEquals(true, expectedName.equals(actualName));
    }
}