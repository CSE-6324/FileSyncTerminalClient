import javax.sound.midi.Soundbank;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

/**
 * @author sharif
 */

public class FileSyncClient {
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
//        if (args.length != 1) {
//            System.err.println("Usage: java FileSyncClient <w/m>");
//            System.exit(1);
//        }
//        SyncClient syncClient = new SyncClient(args[0], args[1], Integer.parseInt(args[2]));

        ArrayList<File> macFiles = FileUtility.getMacFiles();
        ArrayList<FileToSync> fileToSyncList = new ArrayList<>();
        Message returnMessage;
        for (File f: macFiles) {
            FileToSync fileToSync = new FileToSync(f);
            returnMessage = fileToSync.generateFileBlocks();
            if (!returnMessage.isMessageSuccess()) {
                System.out.println(returnMessage.getMessage());
                break;
            }
            returnMessage = fileToSync.generateFileBlockCheckSums();
            if (!returnMessage.isMessageSuccess()) {
                System.out.println(returnMessage.getMessage());
                break;
            }
            fileToSyncList.add(fileToSync);
        }

        for (FileToSync f2s: fileToSyncList) {
            System.out.println("File To Sync: " + f2s.getFileToSyncName());
            System.out.println("Total Blocks: " + f2s.getTotalBlocks());
            for (FileBlock fb: f2s.getFileBlockList()) {
                System.out.println("File Block Name: " + fb.getFileBlockName());
                System.out.println("File Block Number: " + fb.getFileBlockNumber());
                System.out.println("File Block Checksum: " + fb.getFileCheckSum());
                System.out.println("File Block Size: " + fb.getFileBlock().length());
            }
            System.out.println("-------------------------");
        }

        // will delete all file blocks after sent to server
        for (FileToSync f2s: fileToSyncList) {
            returnMessage = f2s.deleteAllFileBlocks();
            if (!returnMessage.isMessageSuccess()) {
                System.out.println(returnMessage.getMessage());
                break;
            }
        }
    }
}
