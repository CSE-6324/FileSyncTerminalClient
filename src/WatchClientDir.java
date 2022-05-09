import java.io.File;
import java.nio.file.*;
import java.io.IOException;
import static java.nio.file.StandardWatchEventKinds.*;
import java.util.HashMap;

/**
 * @author sharif
 */

public class WatchClientDir implements Runnable {
    private static final String TAG = "WatchClientDir";
    private WatchService dirWatcher;
    private HashMap<WatchKey, Path> keys;
    private SyncClientType syncClient;
    private TCPClientSocket tcpClientSocketConn;

    public WatchClientDir(SyncClientType syncClient, TCPClientSocket tcpClientSocketConn) throws IOException {
        this.syncClient = syncClient;
        this.dirWatcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<>();
        this.tcpClientSocketConn = tcpClientSocketConn;
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
                msg.printToTerminal(TAG + " :: " + METHOD_NAME + " :: Error: (InterruptedException)" + e.getMessage());
                return;
            }

            Path dir = keys.get(key);
            if (dir == null) {
                msg.printToTerminal(TAG + " :: " + METHOD_NAME + " :: Error: WatchKey not recognized!! Continuing to next event.");
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
                    msg.printToTerminal("File to Upload: " + child);
                    msg = processFileCreateEvent(child.toFile());
                } else if (event.kind() == ENTRY_DELETE) {
                    msg.printToTerminal("File to Delete: " + child);
                } else if (event.kind() == ENTRY_MODIFY) {
                    msg.printToTerminal("File Modified: " + child);
                }
            }
            key.reset();
        }
    }

    @Override
    public void run() {
        final String METHOD_NAME = "run";
        Message msg = new Message();
        try {
            processEvents();
        } catch (IOException e) {
            msg.printToTerminal(TAG + " :: " + METHOD_NAME + " :: Error: (IOException)" + e.getMessage());
        }
    }

    public Message processFileCreateEvent(File newFile) {
        final String METHOD_NAME = "processFileCreateEvent";
        Message msg;
        String serverResponse;
        FileToSync fileToSync = new FileToSync(newFile);
        msg = fileToSync.generateFileBlocksAndCheckSums();
        if (msg.isMessageSuccess()) {
            msg.printToTerminal("File Blocks :- ");
            for (FileBlock fb: fileToSync.getFileBlockList()) {
                msg.printToTerminal(fb.getFileBlockName() + " :: " + fb.getFileCheckSum());
                msg = tcpClientSocketConn.sendRequest(tcpClientSocketConn.tcpRequest(syncClient.getClientName(),"upload", newFile.getName()));
                if (msg.isMessageSuccess()) {
                    serverResponse = msg.getMessage();
                    msg.printToTerminal("server response: " + serverResponse);

                    int udpPort = Integer.parseInt(serverResponse.split("=")[1]);
                    UDPFileSend udpFileSend = new UDPFileSend(SyncServer.LOCALHOST.getServerName(), udpPort, fb.getFileBlock());
                    Thread fileSendThread = new Thread(udpFileSend);
                    fileSendThread.start();
                } else {
                    msg.setErrorMessage(TAG, METHOD_NAME, msg.getMessage());
                    msg.printToTerminal(msg.getMessage());
                }
            }
            msg.printToTerminal("");
        } else {
            msg.setErrorMessage(TAG, METHOD_NAME, msg.getMessage());
            msg.printToTerminal(msg.getMessage());
        }
        return msg;
    }
}
