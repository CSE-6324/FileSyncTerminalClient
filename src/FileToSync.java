import java.io.*;
import java.util.ArrayList;

/**
 * @author sharif
 */

public class FileToSync {
    private static final String TAG = "FileToSync";
    private File fileToSync;
    private ArrayList<FileBlock> fileBlockList;

    public FileToSync (File fileToSync) {
        this.fileToSync = fileToSync;
        fileBlockList = new ArrayList<>();
    }

    public Message generateFileBlocks() {
        final String METHOD_NAME = "generateFileBlocks";
        Message returnMsg = ServerFileUtility.getFileBlocks(this.fileToSync, this.fileBlockList);
        if (!returnMsg.isMessageSuccess()) {
            returnMsg.setMessage(TAG, METHOD_NAME, returnMsg.getMessage());
        }
        return returnMsg;
    }

    public Message generateFileBlockCheckSums() {
        final String METHOD_NAME = "generateFileBlockCheckSums";
        Message returnMsg = new Message("", true);
        for (FileBlock fb : fileBlockList) {
            returnMsg = fb.generateCheckSum();
            if (!returnMsg.isMessageSuccess()) {
                returnMsg.setMessage(TAG, METHOD_NAME, returnMsg.getMessage());
                break;
            }
        }
        return returnMsg;
    }

    public int getTotalBlocks() {
        return this.fileBlockList.size();
    }

    public String getFileToSyncName() {
        return this.fileToSync.getName();
    }

    public ArrayList<FileBlock> getFileBlockList() {
        return this.fileBlockList;
    }

    public Message deleteAllFileBlocks() {
        final String METHOD_NAME = "deleteAllFileBlocks";
        Message returnMsg = new Message("", true);
        for (FileBlock fb: this.fileBlockList) {
            if (!fb.deleteFileBlock()) {
                returnMsg.setMessageSuccess(false);
                returnMsg.setMessage(TAG, METHOD_NAME, "Unable to delete file block: " + fb.getFileBlockName());
                break;
            }
        }
        return returnMsg;
    }
}
