import java.io.*;
import java.security.NoSuchAlgorithmException;

/**
 * @author sharif
 */

public class FileSyncClientApp {
    private static final String TAG = "FileSyncClientApp";
    private static String clientName;
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        if (args.length != 1) {
            usageError();
        } else {
            SyncClient syncClient = new SyncClient(args[0]);
            if (syncClient.getClientType() == SyncClientType.UNKNOWN) {
                usageError();
            } else {
                clientName = syncClient.getClientType().getClientName();
                startApp(syncClient);
            }
        }
    }

    private static void usageError() {
        System.out.println("> Usage: java FileSyncClientApp [win/w/mac/m]");
        System.exit(-1);
    }

    private static void printHelpPrompt() {
        System.out.println("> ");
        System.out.println("> " + clientName + " client app commands");
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

    private static void startApp(SyncClient syncClient) {
        final String METHOD_NAME = "startApp";
        Message msg = new Message();
        try (BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));) {
            startClientDirWatchService(syncClient);
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

    private static void startClientDirWatchService(SyncClient syncClient) throws IOException {
        WatchClientDir clientDir = new WatchClientDir(syncClient.getClientType());
        Thread clientWatcherThread = new Thread(clientDir);
        clientWatcherThread.start();
    }
}
