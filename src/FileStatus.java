import java.util.HashMap;
import java.util.Map;

public class FileStatus {
    private HashMap<String, String> fileStatus;
    private static FileStatus uniqueFileStatus;
    private FileStatus() {
        fileStatus = new HashMap<>();
    }

    public static FileStatus getInstance() {
        if (uniqueFileStatus == null) {
            uniqueFileStatus = new FileStatus();
        }
        return uniqueFileStatus;
    }

    public void setFileStatus(String fileName, String status) {
        fileStatus.put(fileName, status);
    }

    public String getFileStatus() {
        String status = "";
        for (Map.Entry mapElement: fileStatus.entrySet()) {
            status+= mapElement.getKey() + " " + mapElement.getValue() + System.lineSeparator();
        }
        if (status.length() == 0) {
            status = "nothing to status";
        }
        return status;
    }
}
