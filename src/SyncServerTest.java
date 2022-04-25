import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class SyncServerTest {

    @Test
    public void getSyncedFileNames() {
        ArrayList<String> syncedFiles = SyncServer.LOCALHOST.getSyncedFileNames();
        for (String fileName: syncedFiles) {
            System.out.println(fileName);
        }
    }
}