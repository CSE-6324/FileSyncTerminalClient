import java.io.*;
import java.util.ArrayList;

/**
 * @author sharif
 */

public class ServerFileUtility {
    private static final String TAG = "ServerFileUtility";
    public static final String FOLDER_PATH_SERVER = PrgUtility.PROJECT_FILE_PATH + "server_files/";


    public static ArrayList<String> getServerFileNameList() {
        ArrayList<String> fileList = new ArrayList<>();
        File serverFolder = new File(FOLDER_PATH_SERVER);
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

    public static String getFileNameFromFileBlockName(String fileBlockName) {
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
}
