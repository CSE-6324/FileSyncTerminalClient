import java.awt.event.MouseWheelEvent;
import java.nio.file.*;
import java.io.IOException;

import static java.nio.file.StandardWatchEventKinds.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WatchClientDir implements Runnable {
    private static final String TAG = "WatchClientDir";
    private WatchService dirWatcher;
    private HashMap<WatchKey, Path> keys;
    private SyncClientType syncClient;

    public WatchClientDir(SyncClientType syncClient) throws IOException {
        this.syncClient = syncClient;
        this.dirWatcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<>();
        registerClientDir(Paths.get(this.syncClient.getLocalFilePath()));
    }

    @SuppressWarnings("unchecked")
    private static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

    private void registerClientDir(Path dir) throws IOException {
        WatchKey key = dir.register(dirWatcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        keys.put(key, dir);
    }

    private void processEvents() throws IOException {
        final String METHOD_NAME = "processEvents";
        Message msg = new Message();
        for (;;) {
            // wait for key to be signalled
            WatchKey key;
            try {
                key = dirWatcher.take();
            } catch (InterruptedException e) {
                // TODO: Need to dump this to a file after all main feature dev work. Display a user-friendly msg later.
                System.out.println(TAG + " :: " + METHOD_NAME + " :: Error: (InterruptedException)" + e.getMessage());
                return;
            }

            Path dir = keys.get(key);
            if (dir == null) {
                // TODO: Need to dump this to a file after all main feature dev work. Display a user-friendly msg later.
                System.out.println();
                System.out.println(TAG + " :: " + METHOD_NAME + " :: Error: WatchKey not recognized!! Continuing to next event.");
                continue;
            }

            for (WatchEvent<?> event: key.pollEvents()) {
                WatchEvent<Path> ev = cast(event);
                Path name = ev.context();
                Path child = dir.resolve(name);

                if (child.getFileName().toString().charAt(0) == '.') {
                    // skip hidden files
                    continue;
                }
                if (event.kind() == ENTRY_CREATE) {
                    msg.printToTerminalFollowWithUserPrompt("File to Upload: " + child);
                } else if (event.kind() == ENTRY_DELETE) {
                    msg.printToTerminalFollowWithUserPrompt("File to Delete: " + child);
                } else if (event.kind() == ENTRY_MODIFY) {
                    msg.printToTerminalFollowWithUserPrompt("File Modified: " + child);
                }
            }
            key.reset();
        }
    }

    @Override
    public void run() {
        final String METHOD_NAME = "run";
        try {
            processEvents();
        } catch (IOException e) {
            // TODO: Need to dump this to a file after all main feature dev work. Display a user-friendly msg later.
            System.out.println(TAG + " :: " + METHOD_NAME + " :: Error: (IOException)" + e.getMessage());
        }
    }
}