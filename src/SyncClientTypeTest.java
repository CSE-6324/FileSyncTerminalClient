import org.junit.Test;

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
}