import java.io.File;
import java.util.ArrayList;

public enum SyncClientType {
    MAC ("mac", "/Users/sudiptasharif/repos/FileSyncer/project_files/mac_files/"),
    WINDOWS ("windows", "/Users/sudiptasharif/repos/FileSyncer/project_files/win_files/"),
    UNKNOWN ("unknown", "");

    private String clientName;
    private String localFilePath;

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

    public ArrayList<String> getFilesToDownload() {
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

    public ArrayList<FileToSync> getFilesToCheckForDeltaSync() {
        ArrayList<FileToSync> filesForDeltaSyncCheck = new ArrayList<>();
        ArrayList<String> fileNamesForDeltaCheck = getFileNamesToCheckForDeltaSync();
        for (String fileName: fileNamesForDeltaCheck) {
            FileToSync fileToSync = new FileToSync(fileName);
            fileToSync.generateFileBlocks();
            fileToSync.generateFileBlockCheckSums();
            filesForDeltaSyncCheck.add(fileToSync);
        }
        return filesForDeltaSyncCheck;
    }
}
