import java.io.*;
import java.util.ArrayList;

/**
 * @author sharif
 */

public class FileUtility {
    private static final String TAG = "FileUtility";
    public static final String PROJECT_FILE_PATH = "/Users/sudiptasharif/repos/FileSyncer/project_files/";
    public static final String FOLDER_PATH_MAC = PROJECT_FILE_PATH + "mac_files/";
    public static final String FOLDER_PATH_WIN = PROJECT_FILE_PATH + "win_files/";
    public static final String FOLDER_PATH_SERVER = PROJECT_FILE_PATH + "server_files/";
    public static final int FILE_BLOCK_SIZE_4_MB = 1024 * 1024 * 4;

    private static ArrayList<File> getClientFiles(File fileFolder) {
        ArrayList<File> fileList = new ArrayList<>();
        for (File f: fileFolder.listFiles()) {
            if (!f.getName().startsWith("."))
                fileList.add(f);
        }
        return fileList;
    }

    public static ArrayList<File> getMacFiles() {
        File macFolder = new File(FOLDER_PATH_MAC);
        return getClientFiles(macFolder);
    }

    public static ArrayList<File> getWindowsFiles() {
        File winFolder = new File(FOLDER_PATH_WIN);
        return getClientFiles(winFolder);
    }

    public static String getFileInfo(File f) {
        String fileInfo = "File Name: " + f.getName() + System.lineSeparator();
        fileInfo += "File Length: " + f.length() + System.lineSeparator();
        fileInfo += "File Last Modified: " + f.lastModified() + System.lineSeparator();
        fileInfo += "---------------------";
        return fileInfo;
    }

    private static String getBlockName(String originalFileName, int blockNum) {
        String filePartName = "";
        String[] fileNameTokens = originalFileName.split("\\.");
        String fileNameWithoutExt = fileNameTokens[0];
        String extName = fileNameTokens[1];
        filePartName = String.format("%s_%03d", fileNameWithoutExt, blockNum);
        filePartName += "." + extName;
        return filePartName;
    }

    public static Message getFileBlocks(File file, ArrayList<FileBlock> fileBlockList) {
        final String METHOD_NAME = "getFileBlocks";
        Message returnMsg = new Message("");
        int blockNum = 0;
        int fileBlockSize = FILE_BLOCK_SIZE_4_MB;
        byte[] buffer = new byte[fileBlockSize];
        String fileName = file.getName();
        int bytesRead = -1;
        try(FileInputStream fileInputStream = new FileInputStream(file);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
        ) {
            bytesRead = bufferedInputStream.read(buffer);
            while (bytesRead > 0) {
                String fileBlockName = getBlockName(fileName, ++blockNum);
                File newFile = new File(file.getParent(), fileBlockName);
                try (FileOutputStream fileOutputStream = new FileOutputStream(newFile)) {
                    fileOutputStream.write(buffer, 0, bytesRead);
                }
                fileBlockList.add(new FileBlock(blockNum, newFile));
                bytesRead = bufferedInputStream.read(buffer);
            }
            returnMsg.setMessageSuccess(true);
        } catch (IOException e) {
            returnMsg.setMessageSuccess(false);
            returnMsg.setMessage(TAG, METHOD_NAME, e.getMessage());
        }
        return returnMsg;
    }

    public static ArrayList<File> getServerFiles() {
        ArrayList<File> fileList = new ArrayList<>();
        File serverFolder = new File(FOLDER_PATH_SERVER);
        for (File f: serverFolder.listFiles()) {
            if (!f.getName().startsWith("."))
                fileList.add(f);
        }
        return fileList;
    }
}
