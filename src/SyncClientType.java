import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * @author sharif
 */

public enum SyncClientType {
    MAC ("mac", "/Users/sudiptasharif/repos/FileSyncer/project_files/mac_files/"),
    WINDOWS ("windows", "/Users/sudiptasharif/repos/FileSyncer/project_files/win_files/"),
    UNKNOWN ("unknown", "");

    private String clientName;
    private String localFilePath;
    private static final String TAG = "SyncClientType";

    SyncClientType (String clientName, String localFilePath) {
        this.clientName = clientName;
        this.localFilePath = localFilePath;
    }

    public String getClientName() {
        return this.clientName;
    }

    public String getLocalFilePath() {
        return this.localFilePath;
    }

    public ArrayList<File> getLocalFiles() {
        File fileFolder = new File(this.localFilePath);
        ArrayList<File> fileList = new ArrayList<>();
        for (File f: fileFolder.listFiles()) {
            if (!f.getName().startsWith("."))
                fileList.add(f);
        }
        return fileList;
    }

    public ArrayList<String> getLocalFileNames() {
        ArrayList<String> nameList = new ArrayList<>();
        ArrayList<File> localFiles = getLocalFiles();
        for (File f: localFiles) {
            nameList.add(f.getName());
        }
        return nameList;
    }

    public ArrayList<String> getNotSyncedFilesByClient() {
        ArrayList<String> currentServerFileList = SyncServer.LOCALHOST.getSyncedFileNames();
        ArrayList<String> notSyncedFileNameList = new ArrayList<>();
        for (String clientFileName: getLocalFileNames()) {
            if (!currentServerFileList.contains(clientFileName)) {
                notSyncedFileNameList.add(clientFileName);
            }
        }
        return notSyncedFileNameList;
    }

    public ArrayList<String> getFileNamesToDelete() {
        ArrayList<String> filesToDelete = new ArrayList<>();
        // notSyncedFileNameList : these are the files that are in the client file folder but not in the server
        ArrayList<String> notSyncedFileNameList = getNotSyncedFilesByClient();
        ArrayList<String> currentOtherClientFileList = getOtherClientFileNames();
        for (String fileName: notSyncedFileNameList) {
            if (!currentOtherClientFileList.contains(fileName)) {
                filesToDelete.add(fileName);
            }
        }
        return filesToDelete;
    }

    public ArrayList<String> getOtherClientFileNames() {
        ArrayList<String> otherClientFileNames = new ArrayList<>();
        for (SyncClientType clientType: SyncClientType.values()) {
            if (this != clientType && clientType != UNKNOWN) {
                for (String fileName: clientType.getLocalFileNames()) {
                    if (!otherClientFileNames.contains(fileName)) {
                        otherClientFileNames.add(fileName);
                    }
                }
            }
        }
        return otherClientFileNames;
    }

    public ArrayList<String> getFileNamesToUpload() {
        return getNotSyncedFilesByClient();
    }

    public ArrayList<String> getFileNamesToDownload() {
        ArrayList<String> currentServerFileList = SyncServer.LOCALHOST.getSyncedFileNames();
        ArrayList<String> localFileList = getLocalFileNames();
        ArrayList<String> filesToDownload = new ArrayList<>();
        for (String serverFileName: currentServerFileList) {
            if (!localFileList.contains(serverFileName)) {
                filesToDownload.add(serverFileName);
            }
        }
        return filesToDownload;
    }

    public ArrayList<String> getFileNamesToCheckForDeltaSync() {
        ArrayList<String> currentServerFileList = SyncServer.LOCALHOST.getSyncedFileNames();
        ArrayList<String> localFileList = getLocalFileNames();
        ArrayList<String> filesForDeltaSyncTest = new ArrayList<>();
        for (String clientFileName: localFileList) {
            if (currentServerFileList.contains(clientFileName)) {
                filesForDeltaSyncTest.add(clientFileName);
            }
        }
        return filesForDeltaSyncTest;
    }

    public Message getFilesToCheckForDeltaSync(ArrayList<FileToSync> filesForDeltaSyncCheck) {
        final String METHOD_NAME = "getFilesToCheckForDeltaSync";
        Message returnMsg = new Message();
        ArrayList<String> fileNamesForDeltaCheck = getFileNamesToCheckForDeltaSync();
        for (String fileName: fileNamesForDeltaCheck) {
            FileToSync fileToSync = new FileToSync(localFilePath + fileName);
            returnMsg = fileToSync.generateFileBlocksAndCheckSums();
            if (returnMsg.isMessageSuccess()) {
                filesForDeltaSyncCheck.add(fileToSync);
            } else {
                returnMsg.setErrorMessage(TAG, METHOD_NAME, returnMsg.getMessage());
                break;
            }
        }
        return returnMsg;
    }

    public Message getFilesToUpload(ArrayList<FileToSync> filesToUpload) {
        final String METHOD_NAME = "getFilesToUpload";
        Message returnMsg = new Message();
        ArrayList<String> fileNamesToUpload = getFileNamesToUpload();
        for (String fileName: fileNamesToUpload) {
            FileToSync fileToSync = new FileToSync(localFilePath + fileName);
            returnMsg = fileToSync.generateFileBlocksAndCheckSums();
            if (returnMsg.isMessageSuccess()) {
                filesToUpload.add(fileToSync);
            } else {
                returnMsg.setErrorMessage(TAG, METHOD_NAME, returnMsg.getMessage());
                break;
            }
        }
        return returnMsg;
    }

    public Message removeDeletedFilesByOtherClients() {
        final String METHOD_NAME = "deleteFiles";
        Message returnMsg = new Message();
        ArrayList<String> fileNamesToDelete = getFileNamesToDelete();
        for (String fileName: fileNamesToDelete) {
            try {
                File file = new File(localFilePath + fileName);
                if (!file.delete()) {
                    returnMsg.setMessageSuccess(false);
                    returnMsg.setErrorMessage(TAG, METHOD_NAME, "Unable to delete file: " + fileName);
                    break;
                }
            } catch (Exception e) {
                returnMsg.setMessageSuccess(false);
                returnMsg.setErrorMessage(TAG, METHOD_NAME, "Exception", e.getMessage());
                break;
            }
        }
        return returnMsg;
    }

    public Message getFileBlocksToUploadForDeltaSync(ArrayList<FileBlock> fileBlocksToUpload) {
        final String METHOD_NAME = "getFileBlocksToUpload";
        Message returnMsg = new Message();
        ArrayList<FileToSync> filesForDeltaSyncCheck = new ArrayList<>();
        returnMsg = getFilesToCheckForDeltaSync(filesForDeltaSyncCheck);
        if (returnMsg.isMessageSuccess()) {
            for (FileToSync clientFile: filesForDeltaSyncCheck) {
                // get all file flocks of a target file from server
                ArrayList<FileBlock> serverFileBlockList = new ArrayList<>();
                returnMsg = SyncServer.LOCALHOST.getAllFileBlocksByFileName(clientFile.getFileToSyncName(), serverFileBlockList);
                if (!returnMsg.isMessageSuccess()) {
                    returnMsg.setErrorMessage(TAG, METHOD_NAME, "UnableToGetFileBlocksFromServer",returnMsg.getMessage());
                    break;
                }
                for (FileBlock clientFileBlock: clientFile.getFileBlockList()) {
                    for (FileBlock serverFileBlock: serverFileBlockList) {
                        if (clientFileBlock.getFileBlockName().equals(serverFileBlock.getFileBlockName())) {
                            if (!clientFileBlock.getFileCheckSum().equals(serverFileBlock.getFileCheckSum())) {
                                fileBlocksToUpload.add(clientFileBlock);
                            }
                        }
                    }
                }
            }
        } else {
            returnMsg.setErrorMessage(TAG, METHOD_NAME, "UnableToGetFilesToCheckForDeltaSync", returnMsg.getMessage());
        }
        return returnMsg;
    }

    public String getFileNameFromFileBlockName(String fileBlockName) {
        // this will only work if the file block name has the
        // following format: fileName_block#.fileExt
        // eg: milkyway_001.jpeg
        String fileName = "";
        String[] fileBlockNameTokens = fileBlockName.split("\\.");
        String fileExt = fileBlockNameTokens[1];
        String fileNameWithBlockNum = fileBlockNameTokens[0];
        String[] fileNameTokens = fileNameWithBlockNum.split("_");
        fileName = fileNameTokens[0] + "." + fileExt;
        return fileName;
    }

    public ArrayList<String> getAllFileBlockNamesByFileName(String fileName) {
        ArrayList<String> fileBlocks = new ArrayList<>();
        File serverFolder = new File(localFilePath);
        for (File f: serverFolder.listFiles()) {
            String fileBlockName = f.getName();
            if (fileBlockName.startsWith(fileName.split("\\.")[0])) {
                fileBlocks.add(fileBlockName);
            }
        }
        return fileBlocks;
    }

    public void mergeFileBlocks(String fileName, ArrayList<String> fileBlockNameList) throws IOException {
        HashMap<Integer, File> orderedFileBlocks = new HashMap<>();
        Message msg = new Message();
        int keyIndx = 0;
        Collections.sort(fileBlockNameList);
        for (String fileBlockName: fileBlockNameList) {
            msg.printToTerminal(fileBlockName);
            orderedFileBlocks.put(keyIndx, new File(localFilePath + "/" + fileBlockName));
            keyIndx += 1;
        }
        File mergedFile = new File(localFilePath + "/" + fileName);
        FileMerger fileMerger = new FileMerger(mergedFile, orderedFileBlocks);
        fileMerger.mergeFiles();
        fileMerger.deleteMergedSources();
    }
}
