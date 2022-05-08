import javax.swing.*;
import java.io.*;
import java.security.NoSuchAlgorithmException;

/**
 * @author sharif
 */

public class FileSyncClientApp {
    private static final String TAG = "FileSyncClientApp";

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
        connectToServerAndStartApp(syncClientType);
    }
    private static void connectToServerAndStartApp(SyncClientType syncClientType) {
        TCPClientSocket tcpClientSocketConn = new TCPClientSocket(PrgUtility.HOST_NAME, PrgUtility.TCP_PORT_NUM, syncClientType);
        Message msg = tcpClientSocketConn.connectToServer();
        if (tcpClientSocketConn.isConnectedToServer()) {
            msg = tcpClientSocketConn.sendRequest(tcpClientSocketConn.tcpRequest("handshake-client", syncClientType.getClientName()));
            if (msg.isMessageSuccess()) {
                System.out.println("> " + syncClientType.getClientName() + " client app connected to server");
                System.out.println("> server response: " + msg.getMessage());
                startApp(syncClientType);
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

    private static void startApp(SyncClientType syncClientType) {
        final String METHOD_NAME = "startApp";
        Message msg = new Message();
        try (BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));) {
            startClientDirWatchService(syncClientType);
            printHelpPrompt();
            String userInput = stdIn.readLine().trim();
            while (true) {
                if (userInput.equalsIgnoreCase("suspend")) {
                    msg.printToTerminal("TODO: suspend");
                } else if (userInput.equalsIgnoreCase("resume")) {
                    msg.printToTerminal("TODO: resume");
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

    private static void startClientDirWatchService(SyncClientType syncClient) throws IOException {
        WatchClientDir clientDir = new WatchClientDir(syncClient);
        Thread clientWatcherThread = new Thread(clientDir);
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
}
