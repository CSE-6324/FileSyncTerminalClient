import org.junit.Test;

import java.io.File;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class SyncClientTypeTest {

    @Test
    public void getClientName() {
        assertEquals(true, SyncClientType.MAC.getClientName().equals("mac"));
        assertEquals(true, SyncClientType.WINDOWS.getClientName().equals("windows"));
    }

    @Test
    public void values() {
        for (SyncClientType clientType: SyncClientType.values()) {
            System.out.println(clientType.getClientName());
        }
    }

    @Test
    public void getLocalFilePath() {
        assertEquals(true, SyncClientType.MAC.getLocalFilePath().equals("/Users/sudiptasharif/repos/FileSyncer/project_files/mac_files/"));
        assertEquals(true, SyncClientType.WINDOWS.getLocalFilePath().equals("/Users/sudiptasharif/repos/FileSyncer/project_files/win_files/"));
    }

    @Test
    public void getMacLocalFiles() {
        ArrayList<File> files = SyncClientType.MAC.getLocalFiles();
        for (File f: files) {
            System.out.println(f.getName());
        }
    }

    @Test
    public void getWindowsLocalFiles() {
        ArrayList<File> files = SyncClientType.WINDOWS.getLocalFiles();
        for (File f: files) {
            System.out.println(f.getName());
        }
    }

    @Test
    public void getLocalMacFileNames() {
        ArrayList<String> fileNames = SyncClientType.MAC.getLocalFileNames();
        for (String fileName: fileNames) {
            System.out.println(fileName);
        }
    }

    @Test
    public void getLocalWindowsFileNames() {
        ArrayList<String> fileNames = SyncClientType.WINDOWS.getLocalFileNames();
        for (String fileName: fileNames) {
            System.out.println(fileName);
        }
    }

    @Test
    public void getNotsyncedFilesByClient() {
        ArrayList<String> fileNames = SyncClientType.MAC.getNotSyncedFilesByClient();
        System.out.println("Mac Client Not Synced File List");
        for (String fileName: fileNames) {
            System.out.println(fileName);
        }

        fileNames = SyncClientType.WINDOWS.getNotSyncedFilesByClient();
        System.out.println("Windows Client Not Synced File List");
        for (String fileName: fileNames) {
            System.out.println(fileName);
        }
    }

    @Test
    public void getFileNamesToDeleteMac() {
        System.out.println("getFileNamesToDeleteMac");
        ArrayList<String> fileNames = SyncClientType.MAC.getFileNamesToDelete();
        for (String fileName: fileNames) {
            System.out.println(fileName);
        }
    }

    @Test
    public void getFileNamesToDeleteWindows() {
        System.out.println("getFileNamesToDeleteWindows");
        ArrayList<String> fileNames = SyncClientType.WINDOWS.getFileNamesToDelete();
        for (String fileName: fileNames) {
            System.out.println(fileName);
        }
    }

    @Test
    public void getOtherClientFileNames() {
        ArrayList<String> otherClientFileNames = SyncClientType.MAC.getOtherClientFileNames();
        System.out.println("When this client is mac");
        for (String fileName: otherClientFileNames) {
            System.out.println(fileName);
        }

        otherClientFileNames = SyncClientType.WINDOWS.getOtherClientFileNames();
        System.out.println("When this client is windows");
        for (String fileName: otherClientFileNames) {
            System.out.println(fileName);
        }
    }

    @Test
    public void getFileNamesToUploadMac() {
        System.out.println("getFileNamesToUploadMac");
        ArrayList<String> fileNames = SyncClientType.MAC.getFileNamesToUpload();
        for (String fileName: fileNames) {
            System.out.println(fileName);
        }
    }

    @Test
    public void getFileNamesToUploadWindows() {
        System.out.println("getFileNamesToUploadWindows");
        ArrayList<String> fileNames = SyncClientType.WINDOWS.getFileNamesToUpload();
        for (String fileName: fileNames) {
            System.out.println(fileName);
        }
    }

    @Test
    public void getFileNamesToDownloadMac() {
        System.out.println("getFilesToDownloadMac");
        ArrayList<String> filesToDownload = SyncClientType.MAC.getFileNamesToDownload();
        for (String fileName: filesToDownload) {
            System.out.println(fileName);
        }
    }

    @Test
    public void getFileNamesToDownloadWindows() {
        System.out.println("getFilesToDownloadWindows");
        ArrayList<String> filesToDownload = SyncClientType.WINDOWS.getFileNamesToDownload();
        for (String fileName: filesToDownload) {
            System.out.println(fileName);
        }
    }

    @Test
    public void getFileNamesToCheckForDeltaSyncMac() {
        ArrayList<String> filesForDeltaSyncTest = SyncClientType.MAC.getFileNamesToCheckForDeltaSync();
        for (String fileName: filesForDeltaSyncTest) {
            System.out.println(fileName);
        }
    }

    @Test
    public void getFileNamesToCheckForDeltaSyncWindows() {
        ArrayList<String> filesForDeltaSyncTest = SyncClientType.WINDOWS.getFileNamesToCheckForDeltaSync();
        for (String fileName: filesForDeltaSyncTest) {
            System.out.println(fileName);
        }
    }

    @Test
    public void getFilesToCheckForDeltaSyncMac() {
        ArrayList<FileToSync> filesForDeltaSyncCheck = new ArrayList<>();
        Message returnMsg = SyncClientType.MAC.getFilesToCheckForDeltaSync(filesForDeltaSyncCheck);
        System.out.println(returnMsg.getMessage());
        assertEquals(true, returnMsg.isMessageSuccess());
        for (FileToSync f2s: filesForDeltaSyncCheck) {
            for (FileBlock fb: f2s.getFileBlockList()) {
                System.out.println(fb.getFileBlockName() + " :: " + fb.getFileCheckSum());
            }
        }
    }

    @Test
    public void getFilesToCheckForDeltaSyncWindows() {
        ArrayList<FileToSync> filesForDeltaSyncCheck = new ArrayList<>();
        Message returnMsg = SyncClientType.WINDOWS.getFilesToCheckForDeltaSync(filesForDeltaSyncCheck);
        System.out.println(returnMsg.getMessage());
        assertEquals(true, returnMsg.isMessageSuccess());
        for (FileToSync f2s: filesForDeltaSyncCheck) {
            for (FileBlock fb: f2s.getFileBlockList()) {
                System.out.println(fb.getFileBlockName() + " :: " + fb.getFileCheckSum());
            }
        }
    }

    @Test
    public void getFilesToUpload() {
        ArrayList<FileToSync> filesToUpload = new ArrayList<>();
        Message returnMsg = SyncClientType.WINDOWS.getFilesToUpload(filesToUpload);
        System.out.println(returnMsg.getMessage());
        assertEquals(true, returnMsg.isMessageSuccess());
        for (FileToSync f2s: filesToUpload) {
            for (FileBlock fb: f2s.getFileBlockList()) {
                System.out.println(fb.getFileBlockName() + " :: " + fb.getFileCheckSum());
            }
        }
    }

    @Test
    public void removeDeletedFilesByOtherClientsMac() {
        Message retuMessage = SyncClientType.MAC.removeDeletedFilesByOtherClients();
        assertEquals(true, retuMessage.isMessageSuccess());
    }

    @Test
    public void removeDeletedFilesByOtherClientsWindows() {
        Message retuMessage = SyncClientType.WINDOWS.removeDeletedFilesByOtherClients();
        assertEquals(true, retuMessage.isMessageSuccess());
    }

    @Test
    public void getFileBlocksToUploadForDeltaSync() {
        // Mac
        ArrayList<FileBlock> fileBlockToUpload = new ArrayList<>();
        Message returnMsg = SyncClientType.MAC.getFileBlocksToUploadForDeltaSync(fileBlockToUpload);
        System.out.println(returnMsg.getMessage());
        assertEquals(true, returnMsg.isMessageSuccess());
        for (FileBlock fileBlock: fileBlockToUpload) {
            System.out.println(fileBlock.getFileBlockName() + " :: " + fileBlock.getFileCheckSum());
        }
    }
}