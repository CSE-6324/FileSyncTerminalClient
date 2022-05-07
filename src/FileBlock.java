import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileBlock {
    private static final String TAG = "FileBlock";

    private int blockNumber;
    private String checkSum;
    private File fileBlock;

    public FileBlock(int blockNumber, File fileBlock) {
        this.blockNumber = blockNumber;
        this.fileBlock = fileBlock;
    }

    public FileBlock(String fileBlockNameWithPath) {
        this.fileBlock = new File(fileBlockNameWithPath);
    }
    
    public Message generateCheckSum()  {
        final String METHOD_NAME = "getCheckSum";
        StringBuilder fileCheckSum = new StringBuilder();
        Message returnMsg = new Message();
        try (InputStream inputStream = new FileInputStream(this.fileBlock)) {
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
            this.checkSum = fileCheckSum.toString();
        } catch (IOException e) {
            returnMsg.setMessageSuccess(false);
            returnMsg.setErrorMessage(TAG, METHOD_NAME, "(IOException) " + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            returnMsg.setMessageSuccess(false);
            returnMsg.setErrorMessage(TAG, METHOD_NAME, "(NoSuchAlgorithmException) " + e.getMessage());
        }
        return returnMsg;
    }

    public String getFileBlockName() {
        return this.fileBlock.getName();
    }

    public String getFileCheckSum() {
        return this.checkSum;
    }

    public File getFileBlock() {
        return this.fileBlock;
    }

    public int getFileBlockNumber() {
        return this.blockNumber;
    }

    public boolean deleteFileBlock() {
        return this.fileBlock.delete();
    }
}
