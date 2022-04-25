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
}
