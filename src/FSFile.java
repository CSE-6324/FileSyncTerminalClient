import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

/**
 * @author sharif
 */

public class FSFile {
    private static final String TAG = "FSFile";
    private final String PROJECT_FILE_PATH = "/Users/sudiptasharif/repos/FileSyncer/project_files/";
    private final String FOLDER_PATH_MAC = PROJECT_FILE_PATH + "mac_files/";
    private final String FOLDER_PATH_WIN = PROJECT_FILE_PATH + "win_files/";
    private final String FOLDER_PATH_SERVER = PROJECT_FILE_PATH + "server_files/";

    public FSFile() {}

    private ArrayList<File> getFiles(File fileFolder) {
        ArrayList<File> fileList = new ArrayList<>();
        for (File f: fileFolder.listFiles()) {
            if (!f.getName().startsWith("."))
                fileList.add(f);
        }
        return fileList;
    }

    public ArrayList<File> getMacFiles() {
        File macFolder = new File(FOLDER_PATH_MAC);
        return getFiles(macFolder);
    }

    public ArrayList<File> getWindowsFiles() {
        File winFolder = new File(FOLDER_PATH_WIN);
        return getFiles(winFolder);
    }

    public Message getFileBlocks(File file, ArrayList<File> fileBlockList) {
        final String METHOD_NAME = "getFileBlocks";
        Message returnMsg = new Message("");
        int blockNum = 0;
        int fileBlockSize = FSUtility.FILE_BLOCK_SIZE_4_MB;
        byte[] buffer = new byte[fileBlockSize];
        String fileName = file.getName();
        int bytesRead;
        try(FileInputStream fileInputStream = new FileInputStream(file);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
        ) {
            bytesRead = bufferedInputStream.read(buffer);
            while (bytesRead > 0) {
                String fileBlockName = getBlockName(fileName, ++blockNum);
                File fileBlock = new File(file.getParent(), fileBlockName);
                try (FileOutputStream fileOutputStream = new FileOutputStream(fileBlock)) {
                    fileOutputStream.write(buffer, 0, bytesRead);
                }
                fileBlockList.add(fileBlock);
                bytesRead = bufferedInputStream.read(buffer);
            }
            returnMsg.setMessageSuccess(true);
        } catch (IOException e) {
            returnMsg.setMessageSuccess(false);
            returnMsg.setMessage(TAG, METHOD_NAME, e.getMessage());
        }
        return returnMsg;
    }

    public Message getCheckSum(File file, StringBuilder fileCheckSum)  {
        final String METHOD_NAME = "getCheckSum";
        Message returnMsg = new Message("");
        try (InputStream inputStream = new FileInputStream(file)) {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[1024];
            int bytesRead = inputStream.read(buffer);
            while (bytesRead > 0) {
                md.update(buffer, 0, bytesRead);
                bytesRead = inputStream.read(buffer);
            }
            for (byte b: md.digest()) {
                fileCheckSum.append(String.format("%02x", b));
            }
            returnMsg.setMessageSuccess(true);
        } catch (IOException e) {
            returnMsg.setMessageSuccess(false);
            returnMsg.setMessage(TAG, METHOD_NAME, e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            returnMsg.setMessageSuccess(false);
            returnMsg.setMessage(TAG, METHOD_NAME, e.getMessage());
        }
        return returnMsg;
    }

    public String getFileInfo(File f) {
        String fileInfo = "File Name: " + f.getName() + System.lineSeparator();
        fileInfo += "File Length: " + f.length() + System.lineSeparator();
        fileInfo += "File Last Modified: " + f.lastModified() + System.lineSeparator();
        fileInfo += "---------------------";
        return fileInfo;
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
}
