import java.io.*;
import java.security.NoSuchAlgorithmException;

/**
 * @author sharif
 */

public class FileSyncClientApp {
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        if (args.length != 1) {
            usageError();
        }
        SyncClient syncClient = new SyncClient(args[0]);
        WatchClientDir clientDir = new WatchClientDir(syncClient.getClientType());
        clientDir.processEvents();
    }

    private static void usageError() {
        System.out.println("Usage: java FileSyncClientApp [w/m]");
        System.exit(-1);
    }
}
