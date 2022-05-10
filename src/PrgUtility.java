import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author sharif
 */

public class PrgUtility {
    public static final String PROJECT_FILE_PATH = "/Users/sudiptasharif/repos/FileSyncer/project_files/";
    public static final String CLIENT_FILE_BLOCKS_PATH = PrgUtility.PROJECT_FILE_PATH + "file_blocks/";
    public static final int FILE_BLOCK_SIZE_4_MB = 1024 * 1024 * 4;
    public static final int FILE_BLOCK_SIZE_2_KB = 1024 * 2;
    public static final int TCP_PORT_NUM = 5555;
    public static final String HOST_NAME = "localhost";
    public static final String CLIENT_LOG_FILE = PROJECT_FILE_PATH + "log_files/client_log.txt";

    public static String getFileExtension(String fileName) {
        String extension = "";
        int idx = fileName.lastIndexOf(".");
        if (idx > 0) {
            extension = fileName.substring(idx + 1);
        }
        return extension;
    }

    public static boolean hasFileExtension(String fileName) {
        String extension = getFileExtension(fileName);
        return extension.length() > 0;
    }

    public static boolean isFileNameValid(String fileName) {
        byte[] bArray = null;
        boolean answer = true;
        try {
            bArray = fileName.getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            answer = false;
        }
        return answer;
    }
}
