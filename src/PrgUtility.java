import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PrgUtility {
    public static final String PROJECT_FILE_PATH = "/Users/sudiptasharif/repos/FileSyncer/project_files/";
    public static final String CLIENT_FILE_BLOCKS_PATH = PrgUtility.PROJECT_FILE_PATH + "file_blocks/";
    public static final int FILE_BLOCK_SIZE_4_MB = 1024 * 1024 * 4;
    public static final int FILE_BLOCK_SIZE_2_KB = 1024 * 2;
    public static final int TCP_PORT_NUM = 5555;
    public static final int UDP_PORT_NUM = 7777;
    public static final String HOST_NAME = "localhost";
}
