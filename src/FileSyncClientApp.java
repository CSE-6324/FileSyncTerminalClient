import javax.swing.*;
import java.io.*;
import java.security.NoSuchAlgorithmException;

/**
 * @author sharif
 */

public class FileSyncClientApp {
    private static final String TAG = "FileSyncClientApp";
    private static WatchClientDir clientWatchDir;
    private static WatchServerDir serverWatchDir;

    public static void main(String[] args) {
        if (!validClientAppArgs(args)) {
            usageErrorInvalidArgLength();
            System.exit(-1);
        }
        SyncClientType syncClientType = getSyncClientType(args[0]);
        if (!validSyncClientType(syncClientType)) {
            usageErrorInvalidClientType();
            System.exit(-1);
        }
        TCPClientSocket tcpClientSocketConn = connectToServer(syncClientType);
        startApp(syncClientType, tcpClientSocketConn);
    }
    private static TCPClientSocket connectToServer(SyncClientType syncClientType) {
        TCPClientSocket tcpClientSocketConn = new TCPClientSocket(PrgUtility.HOST_NAME, PrgUtility.TCP_PORT_NUM, syncClientType);
        Message msg = tcpClientSocketConn.connectToServer();
        if (tcpClientSocketConn.isConnectedToServer()) {
            msg = tcpClientSocketConn.sendRequest(tcpClientSocketConn.tcpRequest(syncClientType.getClientName(),"handshake-client", syncClientType.getClientName()));
            if (msg.isMessageSuccess()) {
                System.out.println("> " + syncClientType.getClientName() + " client app connected to server");
                System.out.println("> server response: " + msg.getMessage());
            } else {
                System.out.println("> unable to make TCP connection with server");
                System.out.println("> client-server-handshake-failed");
                System.out.println("> error: " + msg.getMessage());
                System.exit(-1);
            }
        } else {
            System.out.println("> unable to make TCP connection with server");
            System.out.println("> " + msg.getMessage());
            System.exit(-1);
        }
        return tcpClientSocketConn;
    }

    private static boolean validClientAppArgs(String[] args) {
        return args.length == 1;
    }

    private static boolean validSyncClientType(SyncClientType syncClientType) {
        return SyncClientType.UNKNOWN != syncClientType;
    }

    private static void usageErrorInvalidArgLength() {
        System.out.println("> Usage: java FileSyncClientApp <client-type>");
    }

    private static void usageErrorInvalidClientType() {
        System.out.println("> Usage: java FileSyncClientApp <win/w/mac/m>");
    }

    private static void tcpConnectionError(Message msg) {
        msg.printToTerminal("Unable to connect to server. Please Try again later.");
        // TODO: dump this to log file later
        msg.printToTerminal(msg.getMessage());
    }

    private static void printHelpPrompt() {
        System.out.println("> ");
        System.out.println("> app commands");
        System.out.println("> ");
        System.out.println("> suspend - to stop syncing files");
        System.out.println("> resume - to start syncing files");
        System.out.println("> status - to display sync status");
        System.out.println("> help - to display app commands");
        System.out.println("> exit - to exit application");
        System.out.println("> ");
        System.out.print("> ");
    }

    private static void exitMsg() {
        System.out.println("> Good bye! Take care!\n");
    }

    private static void startApp(SyncClientType syncClientType, TCPClientSocket tcpClientSocketConn) {
        final String METHOD_NAME = "startApp";
        Message msg = new Message();
        try (BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));) {
            startClientDirWatchService(syncClientType, tcpClientSocketConn);
            startServerDirWatchService(syncClientType, tcpClientSocketConn);
            printHelpPrompt();
            String userInput = stdIn.readLine().trim();
            while (true) {
                if (userInput.equalsIgnoreCase("suspend")) {
                    msg.printToTerminal("all operation suspend");
                    clientWatchDir.suspendFileUpload();
                } else if (userInput.equalsIgnoreCase("resume")) {
                    msg.printToTerminal("all operation resumed");
                    clientWatchDir.resumeFileUpload();
                } else if (userInput.equalsIgnoreCase("status")) {
                    msg.printToTerminal("TODO: status");
                } else if (userInput.equalsIgnoreCase("exit")) {
                    exitMsg();
                    break;
                }  else {
                    printHelpPrompt();
                }
                userInput = stdIn.readLine().trim();
            }
        } catch (IOException e) {
            //TODO: replace this with dump to file as needed
            msg.setErrorMessage(TAG, METHOD_NAME, "IOException", e.getMessage());
            msg.printToTerminal(msg.getMessage());
        }
        System.exit(-1);
    }

    private static void startClientDirWatchService(SyncClientType syncClient, TCPClientSocket tcpClientSocketConn) throws IOException {
        clientWatchDir = new WatchClientDir(syncClient, tcpClientSocketConn);
        Thread clientWatcherThread = new Thread(clientWatchDir);
        clientWatcherThread.start();
    }

    private static SyncClientType getSyncClientType(String clientType) {
        if (clientType.equals("mac") || clientType.equals("m")) {
            return SyncClientType.MAC;
        } else if (clientType.equals("win") || clientType.equals("w")) {
            return SyncClientType.WINDOWS;
        } else {
            return SyncClientType.UNKNOWN;
        }
    }

    private static void startServerDirWatchService(SyncClientType syncClient, TCPClientSocket tcpClientSocketConn) throws IOException {
        serverWatchDir = new WatchServerDir(syncClient, tcpClientSocketConn);
        Thread serverWatcherThread = new Thread(serverWatchDir);
        serverWatcherThread.start();
    }
}
