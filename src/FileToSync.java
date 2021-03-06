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

    public FileToSync(String fileToSyncPath) {
        fileToSync = new File(fileToSyncPath);
        fileBlockList = new ArrayList<>();
    }

    public Message generateFileBlocks() {
        final String METHOD_NAME = "generateFileBlocks";
        Message returnMsg = getFileBlocks(this.fileToSync, this.fileBlockList);
        if (!returnMsg.isMessageSuccess()){
            returnMsg.setErrorMessage(TAG, METHOD_NAME, returnMsg.getMessage());
        }
        return returnMsg;
    }

    public Message generateFileBlockCheckSums() {
        final String METHOD_NAME = "generateFileBlockCheckSums";
        Message returnMsg = new Message();
        for (FileBlock fb : fileBlockList) {
            returnMsg = fb.generateCheckSum();
            if (!returnMsg.isMessageSuccess()) {
                returnMsg.setErrorMessage(TAG, METHOD_NAME, returnMsg.getMessage());
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
        Message returnMsg = new Message();
        for (FileBlock fb: this.fileBlockList) {
            if (!fb.deleteFileBlock()) {
                returnMsg.setMessageSuccess(false);
                returnMsg.setErrorMessage(TAG, METHOD_NAME, "Unable to delete file block: " + fb.getFileBlockName());
                break;
            }
        }
        this.fileBlockList.clear();
        return returnMsg;
    }

    private Message getFileBlocks(File file, ArrayList<FileBlock> fileBlockList) {
        final String METHOD_NAME = "getFileBlocks";
        Message returnMsg = new Message();
        int blockNum = 0;
//        int fileBlockSize = PrgUtility.FILE_BLOCK_SIZE_4_MB;
        int fileBlockSize = PrgUtility.FILE_BLOCK_SIZE_2_KB;
        byte[] buffer = new byte[fileBlockSize];
        String fileName = file.getName();
        int bytesRead;
        try(FileInputStream fileInputStream = new FileInputStream(file);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
        ) {
            bytesRead = bufferedInputStream.read(buffer);
            while (bytesRead > 0) {
                String fileBlockName = getBlockName(fileName, ++blockNum);
                File newFile = new File(PrgUtility.CLIENT_FILE_BLOCKS_PATH, fileBlockName);
                try (FileOutputStream fileOutputStream = new FileOutputStream(newFile)) {
                    if (bytesRead < buffer.length){
                        buffer = new byte[bytesRead+1];
                    }
                    fileOutputStream.write(buffer, 0, bytesRead);
                    fileOutputStream.flush();

                    bytesRead = bufferedInputStream.read(buffer);
                    fileBlockList.add(new FileBlock(blockNum, newFile));
                } catch (Exception e) {
                    returnMsg.setErrorMessage(TAG, METHOD_NAME, "Exception", e.getMessage());
                    returnMsg.printToTerminal(returnMsg.getMessage());
                }
            }
        } catch (IOException e) {
            returnMsg.setMessageSuccess(false);
            returnMsg.setErrorMessage(TAG, METHOD_NAME, "(IOException) " + e.getMessage());
        }
        return returnMsg;
    }

    private String getBlockName(String originalFileName, int blockNum) {
        String filePartName = "";
        String[] fileNameTokens = originalFileName.split("\\.");
        String fileNameWithoutExt = fileNameTokens[0];
        String extName = fileNameTokens[1];
        filePartName = String.format("%s_%03d", fileNameWithoutExt, blockNum);
        filePartName += "." + extName;
        return filePartName;
    }

    public Message generateFileBlocksAndCheckSums() {
        final String METHOD_NAME = "generateFileBlocksAndCheckSums";
        Message returnMsg = generateFileBlocks();
        if (returnMsg.isMessageSuccess()) {
            returnMsg = generateFileBlockCheckSums();
            if (!returnMsg.isMessageSuccess()) {
                returnMsg.setErrorMessage(TAG, METHOD_NAME, returnMsg.getMessage());
            }
        } else {
            returnMsg.setErrorMessage(TAG, METHOD_NAME, returnMsg.getMessage());
        }
        return returnMsg;
    }

    public void addFileBlock(FileBlock fileBlock) {
        this.fileBlockList.add(fileBlock);
    }
}
