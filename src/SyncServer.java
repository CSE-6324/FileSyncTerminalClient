import java.io.File;
import java.util.ArrayList;

public enum SyncServer {
    LOCALHOST ("localhost", "/Users/sudiptasharif/repos/FileSyncer/project_files/server_files/");

    private String serverName;
    private String serverFolderPath;

    SyncServer (String serverName, String serverLocalPath) {
        this.serverName = serverName;
        this.serverFolderPath = serverLocalPath;
    }

    public ArrayList<String> getSyncedFileNames() {
        ArrayList<String> fileList = new ArrayList<>();
        File serverFolder = new File(serverFolderPath);
        for (File f: serverFolder.listFiles()) {
            String fullFileName = f.getName();
            if (!fullFileName.startsWith(".") ) {
                String fileName = getFileNameFromFileBlockName(fullFileName);
                if (!fileList.contains(fileName))
                    fileList.add(fileName);
            }
        }
        return fileList;
    }

    private String getFileNameFromFileBlockName(String fileBlockName) {
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

    public String getServerFolderPath() {
        return serverFolderPath;
    }

    public String getServerName() {
        return serverName;
    }

    public ArrayList<String> getAllFileBlockNamesByFileName(String fileName) {
        ArrayList<String> fileBlocks = new ArrayList<>();
        File serverFolder = new File(serverFolderPath);
        for (File f: serverFolder.listFiles()) {
            String fileBlockName = f.getName();
            if (fileBlockName.startsWith(fileName.split("\\.")[0])) {
                fileBlocks.add(fileBlockName);
            }
        }
        return fileBlocks;
    }

    public Message getAllFileBlocksByFileName(String fileName, ArrayList<FileBlock> fileBlockList) {
        final String METHOD_NAME = "getAllFileBlocksByFileName";
        Message returnMsg = new Message();
        ArrayList<String> allFileBlocksOfTargetFile = getAllFileBlockNamesByFileName(fileName);
        for (String fileBlockName: allFileBlocksOfTargetFile) {
            FileBlock fileBlock = new FileBlock(serverFolderPath + fileBlockName);
            returnMsg = fileBlock.generateCheckSum();
            if (returnMsg.isMessageSuccess()) {
                fileBlockList.add(fileBlock);
            } else {
                returnMsg.setErrorMessage("SyncServer", METHOD_NAME, "GenerateCheckSumError", returnMsg.getMessage());
                break;
            }
        }
        return returnMsg;
    }
}
