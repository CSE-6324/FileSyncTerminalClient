import java.io.File;
import java.nio.file.*;
import java.io.IOException;
import static java.nio.file.StandardWatchEventKinds.*;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author sharif
 */

public class WatchClientDir implements Runnable {
    private static final String TAG = "WatchClientDir";
    private final WatchService dirWatcher;
    private final HashMap<WatchKey, Path> keys;
    private final SyncClientType syncClient;
    private final TCPClientSocket tcpClientSocketConn;
    private volatile boolean suspendAllOperation = false;

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

    private synchronized void processFileEventsInClientDir() throws IOException {
        final String METHOD_NAME = "processFileEventsInClientDir";
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
                    // skip hidden files
                    continue;
                }
                if (event.kind() == ENTRY_CREATE) {
                    msg.logMsgToFile("file created in client folder: " + child);
                    String fileName = child.toFile().getName();
                    if (!(PrgUtility.isFileABlockFile(fileName)) && !(syncClient.isFileInOtherClients(fileName))) {
                        startFileUploadTask(child.toFile());
                    } else if (PrgUtility.isFileABlockFile(fileName)) {
                        fileName = PrgUtility.getFileNameFromFileBlockName(fileName);
                        ArrayList<String> fileBlockNameListInServer = SyncServer.LOCALHOST.getAllFileBlockNamesByFileName(fileName);
                        ArrayList<String> fileBlockNameListInClient = syncClient.getAllFileBlockNamesByFileName(fileName);

                        if (fileBlockNameListInClient.size() == fileBlockNameListInServer.size()) {
                            syncClient.mergeFileBlocks(fileName, fileBlockNameListInClient);
                            msg.printToTerminal("finished merging of file blocks of file: " + fileName);
                        }
                    }
                } else if (event.kind() == ENTRY_DELETE) {
                    msg.logMsgToFile("file deleted in client folder: " + child);
                    startFileDeleteTask(child.toFile());
                } else if (event.kind() == ENTRY_MODIFY) {
                    msg.logMsgToFile("file modified in client folder: " + child);
                    startDeltaSyncTask(child.toFile());
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
            processFileEventsInClientDir();
        } catch (IOException e) {
            msg.setErrorMessage(TAG, METHOD_NAME, "IOException", e.getMessage());
            msg.printToTerminal(msg.getMessage());
        }
    }

    public synchronized void startFileUploadTask(File newFile) {
        final String METHOD_NAME = "startFileUploadTask";
        Message msg = new Message();
        String fileBlocksUploaded = "file blocks uploaded to server" + System.lineSeparator() + "> ";
        FileToSync fileToSync = new FileToSync(newFile);
        int blockUploadedCount = 1;
        try {
            msg = fileToSync.generateFileBlocksAndCheckSums();
            if (msg.isMessageSuccess()) {
                for (FileBlock fb: fileToSync.getFileBlockList()) {
                    uploadFileBlockToServer(fb);
                    fileBlocksUploaded += fb.getFileBlockName() + " :: " + fb.getFileCheckSum() + System.lineSeparator() + "> ";
                    PrgUtility.updateFileStatusBlockUpload(newFile.getName(), blockUploadedCount, fileToSync.getFileBlockList().size());
                    blockUploadedCount++;
                }
                PrgUtility.updateFileStatusBlockCheckSum(newFile.getName() + "-checksums" + System.lineSeparator(), fileBlocksUploaded);
            } else {
                msg.setErrorMessage(TAG, METHOD_NAME, "UnableToGenerateFileBlocksAndCheckSum", msg.getMessage());
                msg.printToTerminal(msg.getMessage());
            }
        } catch (Exception e) {
            msg.setErrorMessage(TAG, METHOD_NAME, "Exception", e.getMessage());
            msg.printToTerminal(msg.getMessage());
        }
    }

    private synchronized void uploadFileBlockToServer(FileBlock fileBlock) {
        final String METHOD_NAME = "uploadFileBlockToServer";
        Message msg = new Message();
        String serverResponse;
        String serverRequest = tcpClientSocketConn.tcpRequest(syncClient.getClientName(),"upload", fileBlock.getFileBlockName());
        msg.logMsgToFile(serverRequest);
        msg = tcpClientSocketConn.sendRequest(serverRequest);
        if (msg.isMessageSuccess()) {
            serverResponse = msg.getMessage();
            msg.printToTerminal("server response: " + serverResponse);

            int udpPort = Integer.parseInt(serverResponse.split("=")[1]);
            UDPFileSend udpFileSend = new UDPFileSend(SyncServer.LOCALHOST.getServerName(), udpPort, fileBlock.getFile());
            Thread fileSendThread = new Thread(udpFileSend);
            try {
                fileSendThread.start();
                PrgUtility.updateFileStatusUpload(fileBlock.getFileBlockName(), "done");
            } catch (Exception e) {
                msg.setErrorMessage(TAG, METHOD_NAME, "UnableToSendFileToServer", e.getMessage());
                msg.printToTerminal(msg.getMessage());
            }
        } else {
            msg.setErrorMessage(TAG, METHOD_NAME, "UnableToSendTCPRequest",msg.getMessage());
            msg.printToTerminal(msg.getMessage());
        }
    }

    public void suspendAllOperation() throws IOException {
        suspendAllOperation = true;
    }

    public void resumeAllOperation() {
        suspendAllOperation = false;
    }

    public void startFileDeleteTask(File fileToDelete) {
        final String METHOD_NAME = "startFileDeleteTask";
        Message msg = new Message();
        String serverResponse;
        if (!PrgUtility.isFileABlockFile(fileToDelete.getName())) {
            String serverRequest = tcpClientSocketConn.tcpRequest(syncClient.getClientName(),"delete", fileToDelete.getName());
            msg.logMsgToFile(serverRequest);
            msg = tcpClientSocketConn.sendRequest(serverRequest);
            if (msg.isMessageSuccess()) {
                serverResponse = msg.getMessage();
                msg.printToTerminal("server response: " + serverResponse);
                if (serverResponse.equalsIgnoreCase("ok")) {
                    String fileName = fileToDelete.getName();
                    fileToDelete.delete();
                    PrgUtility.updateFileStatusDelete(fileName, "done");
                } else {
                    msg.setErrorMessage(TAG, METHOD_NAME, "ServerResponseNotOk", msg.getMessage());
                    msg.printToTerminal(msg.getMessage());
                }
            }else {
                msg.setErrorMessage(TAG, METHOD_NAME, "UnableToSendTCPRequest",msg.getMessage());
                msg.printToTerminal(msg.getMessage());
            }
        }
    }

    public void startDeltaSyncTask(File fileToDeltaSync) {
        final String METHOD_NAME = "startFileEditTask";
        Message msg;
        String serverResponse;
        ArrayList<FileBlock> deltaSyncFileBlocks = new ArrayList<>();
        msg = syncClient.getFileBlocksToUploadForDeltaSync(deltaSyncFileBlocks);
        if (msg.isMessageSuccess()) {
            for (FileBlock fileBlock: deltaSyncFileBlocks) {
                msg.printToTerminal("start delta sync: " + fileBlock.getFileBlockName());
                uploadFileBlockToServer(fileBlock);
                PrgUtility.updateFileStatusDeltaSync(fileBlock.getFileBlockName(), "done");
            }
        } else {
            msg.setErrorMessage(TAG, METHOD_NAME, "UnableToGetFileBlocksForDeltaSync");
            msg.printToTerminal(msg.getMessage());
        }
    }
}
