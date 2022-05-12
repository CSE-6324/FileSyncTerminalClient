import org.junit.Test;

import static org.junit.Assert.*;

public class WatchClientDirTest {

    @Test
    public void processEvents() {
        try {
            WatchClientDir macClient = new WatchClientDir(SyncClientType.MAC, null);
            Thread clientWatcherThread = new Thread(macClient);
            clientWatcherThread.start();
        } catch (Exception e) {
            System.out.println("Error: (Exception) " + e.getMessage());
        }

    }
}