import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
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

    public static boolean isFileABlockFile(String fileName) {
        boolean answer = false;
        Message msg = new Message();
        if (fileName != null && fileName.length() > 0) {
            String[] fileBlockNameTokens = fileName.split("\\.");
            if (fileBlockNameTokens.length == 2) {
                String[] fileBlockTokens = fileBlockNameTokens[0].split("_");
                if (fileBlockTokens.length == 2) {
                    try {
                        int blockNum = Integer.parseInt(fileBlockTokens[1]);
                        answer = true;
                    } catch (Exception e) {
                        msg.logMsgToFile(fileName + "is not a valid file block name");
                    }
                }
            }
        }
        return answer;
    }

    public static String getFileNameFromFileBlockName(String fileBlockName) {
        // this will only work if the file block name has the
        // following format: fileName_block#.fileExt
        // eg: milkyway_001.jpeg
        String fileName = "";
        if (fileBlockName!= null && fileBlockName.length() > 0) {
            String[] fileBlockNameTokens = fileBlockName.split("\\.");
            if (fileBlockNameTokens.length == 2) {
                String fileExt = fileBlockNameTokens[1];
                String fileNameWithBlockNum = fileBlockNameTokens[0];
                String[] fileNameTokens = fileNameWithBlockNum.split("_");
                fileName = fileNameTokens[0] + "." + fileExt;
            }
        }
        return fileName;
    }

    public static void updateFileStatusBytesUpload(String fileName, int bytesUpload, int totalBytes) {
        FileSyncClientApp.fileStatus.setFileStatus(fileName, String.format("bytes uploaded: %d/%d", bytesUpload, totalBytes));
    }

    public static void updateFileStatusBlockUpload(String fileName, int fileBlock, int totalFileBlock) {
        FileSyncClientApp.fileStatus.setFileStatus(fileName, String.format("blocks uploaded: %d/%d", fileBlock, totalFileBlock));
    }

    public static void updateFileStatusDelete(String fileName, String status) {
        FileSyncClientApp.fileStatus.setFileStatus(fileName, "delete " + status);
    }

    public static void updateFileStatusDownload(String fileName, String status) {
        FileSyncClientApp.fileStatus.setFileStatus(fileName, "download " + status);
    }

    public static void updateFileStatusBlockCheckSum(String fileName, String status) {
        FileSyncClientApp.fileStatus.setFileStatus(fileName, status);
    }

    public static void updateFileStatusUpload(String fileName, String status) {
        FileSyncClientApp.fileStatus.setFileStatus(fileName, "delete " + status);
    }

    public static boolean hasValidUTFChars(byte[] bArray) {
        CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
        try {
            decoder.decode(ByteBuffer.wrap(bArray));
        } catch (CharacterCodingException ex) {
            return false;
        }
        return true;
    }
}
