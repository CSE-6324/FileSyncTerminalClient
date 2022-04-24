import java.io.File;
import java.util.ArrayList;

public enum SyncClientType {
    MAC ("mac", "/Users/sudiptasharif/repos/FileSyncer/project_files/mac_files/"),
    WINDOWS ("windows", "/Users/sudiptasharif/repos/FileSyncer/project_files/win_files/"),
    UNKNOWN ("unknown", "");

    private final String clientName;
    private final String localFilePath;

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
}
