import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;

import static java.nio.file.StandardWatchEventKinds.*;

public class WatchServerDir implements Runnable {
    private static final String TAG = "WatchServerDir";
    private final WatchService dirWatcher;
    private final HashMap<WatchKey, Path> keys;
    private final SyncClientType syncClient;
    private final TCPClientSocket tcpClientSocketConn;
    private volatile boolean suspendAllOperation = false;

    public WatchServerDir(SyncClientType syncClient, TCPClientSocket tcpClientSocketConn) throws IOException {
        this.syncClient = syncClient;
        this.dirWatcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<>();
        this.tcpClientSocketConn = tcpClientSocketConn;
        registerServerDir(Paths.get(SyncServer.LOCALHOST.getServerFolderPath()));
    }

    @SuppressWarnings("unchecked")
    private static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

    private void registerServerDir(Path dir) throws IOException {
        WatchKey key = dir.register(dirWatcher, ENTRY_CREATE, ENTRY_DELETE);
        keys.put(key, dir);
    }

    private synchronized void processFileEventsInServerDir() throws IOException {
        final String METHOD_NAME = "processFileEventsInServerDir";
        Message msg = new Message();
        while (!suspendAllOperation) {
            // wait for key to be signalled
            WatchKey key;
            try {
                key = dirWatcher.take();
            } catch (InterruptedException e) {
                msg.setErrorMessage(TAG, METHOD_NAME, "InterruptedException", e.getMessage());
                msg.printToTerminal(msg.getMessage());
                return;
            }
            Path dir = keys.get(key);
            if (dir == null) {
                msg.logMsgToFile( "watchKey not recognized! continuing to next event");
                continue;
            }
            for (WatchEvent<?> event: key.pollEvents()) {
                WatchEvent<Path> ev = cast(event);
                Path name = ev.context();
                Path child = dir.resolve(name);

                if (child.getFileName().toString().charAt(0) == '.') {
                    continue; // skip hidden files
                }
                if (event.kind() == ENTRY_CREATE) {
                    msg.logMsgToFile("file created in server folder: " + child);
                    startFileDownloadTask(child.toFile());
                } else if (event.kind() == ENTRY_DELETE) {
                    msg.logMsgToFile("file deleted in server folder: " + child);
                    startFileDeleteTask(child.toFile());
                }
            }
            key.reset();
        }
    }

    @Override
    public synchronized void run() {
        final String METHOD_NAME = "run";
        Message msg = new Message();
        try {
            processFileEventsInServerDir();
        } catch (IOException e) {
            msg.setErrorMessage(TAG, METHOD_NAME, "IOException", e.getMessage());
            msg.printToTerminal(msg.getMessage());
        }
    }

    public synchronized void startFileDownloadTask(File fileToDownload) {
        final String METHOD_NAME = "startFileDownloadTask";
        Message msg = new Message();
        String fileName = PrgUtility.getFileNameFromFileBlockName(fileToDownload.getName());
        try {
            if (!(new File(syncClient.getLocalFilePath(), fileName).exists())) {
                    downloadFileBlockToClient(fileToDownload);
            }
        } catch (Exception e) {
            msg.setErrorMessage(TAG, METHOD_NAME, "UnableToDownloadFileToClient", msg.getMessage());
            msg.printToTerminal(msg.getMessage());
        }
    }

    private synchronized void downloadFileBlockToClient(File fileBlock) {
        final String METHOD_NAME = "downloadFileBlockToClient";
        Message msg = new Message();
        String serverResponse;
        String serverRequest;
        try {
            int udpPort = tcpClientSocketConn.getFreeLocalPort();
            UDPFileReceive udpFileReceive = new UDPFileReceive(udpPort, syncClient.getLocalFilePath());
            Thread fileReceiveThread = new Thread(udpFileReceive);
            fileReceiveThread.start();
            serverRequest = tcpClientSocketConn.tcpRequest(syncClient.getClientName(), "download", "udp_port", fileBlock.getName(), (udpPort + ""));
            msg.logMsgToFile(serverRequest);
            msg = tcpClientSocketConn.sendRequest(serverRequest);
            if (msg.isMessageSuccess()) {
                serverResponse = msg.getMessage();
                msg.printToTerminal("server response: " + serverResponse);
                if (serverResponse.equalsIgnoreCase("ok")) {
                    PrgUtility.updateFileStatusDownload(fileBlock.getName(), "done");
                } else {
                    msg.setErrorMessage(TAG, METHOD_NAME, "ServerDownloadRequestIsNotOK", msg.getMessage());
                    msg.printToTerminal(msg.getMessage());
                }
            } else {
                msg.setErrorMessage(TAG, METHOD_NAME, "UnableToSendTCPRequest",msg.getMessage());
                msg.printToTerminal(msg.getMessage());
            }
        } catch (Exception e) {
            msg.setErrorMessage(TAG, METHOD_NAME, "Exception", msg.getMessage());
            msg.printToTerminal(msg.getMessage());
        }
    }

    public void suspendAllOperation() throws IOException {
        suspendAllOperation = true;
    }

    public void resumeAllOperation() {
        suspendAllOperation = false;
    }

    public void startFileDeleteTask(File fileToDeleteFromServer) {
        final String METHOD_NAME = "startFileDeleteTask";
        Message msg = new Message();
        String fileName = PrgUtility.getFileNameFromFileBlockName(fileToDeleteFromServer.getName());
        try {
            File fileToDeleteInClient = new File(syncClient.getLocalFilePath(), fileName);
            if ((fileToDeleteInClient.exists())) {
                fileToDeleteInClient.delete();
                PrgUtility.updateFileStatusDelete(fileName, "done");
            }
        } catch (Exception e) {
            msg.setErrorMessage(TAG, METHOD_NAME, "Exception", e.getMessage());
            msg.logMsgToFile(msg.getMessage());
        }
    }
}
