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
}