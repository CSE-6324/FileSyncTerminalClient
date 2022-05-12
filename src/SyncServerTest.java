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

    @Test
    public void getAllFileBlocksByFileName() {
        ArrayList<String> milkywayFileBlocks = SyncServer.LOCALHOST.getAllFileBlockNamesByFileName("milkyway.jpeg");
        for (String fileName: milkywayFileBlocks) {
            System.out.println(fileName);
        }
    }
}