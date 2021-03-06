import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class FileMerger {
    private static final String TAG = "FileMerger";
    private File destination;
    private HashMap<Integer, File> sources;

    public FileMerger(File destination, HashMap<Integer, File> sources) {
        this.destination = destination;
        this.sources = sources;
    }


    public void mergeFiles() throws IOException {
        FileOutputStream outputStream = new FileOutputStream(destination);
        for (int key = 1; key <= sources.size(); key++) {
            byte[] fileByteArray = readFileToByteArray(sources.get(key));
            String fileName = sources.get(key).getName();
            for (int idx = 0; idx < fileByteArray.length; idx++) {
                byte singleFileByte = fileByteArray[idx];
                if (PrgUtility.isExtensionTxt(fileName) && !(singleFileByte == (byte) 0)) {
                    outputStream.write(singleFileByte);
                } else if (!PrgUtility.isExtensionTxt(fileName)){
                    outputStream.write(singleFileByte);
                }
            }
        }
        outputStream.close();
    }

    public int getTotalFileSize() {
        int size = 0;
        for (int key = 0; key < sources.size(); key++) {
            size += (int) sources.get(key).length();
        }
        return size;
    }

    public byte[] readFileToByteArray(File file) {
        final String METHOD_NAME = "readFileToByteArray";
        Message consoleMsg = new Message();
        // creating a byte array using the length of the file
        // file.length returns long which is cast to int
        int fileLength = (int) file.length();
        byte[] bArray = new byte[fileLength];
        try (FileInputStream fis = new FileInputStream(file);){
            fis.read(bArray);
        } catch (IOException ex) {
            consoleMsg.setErrorMessage(TAG, METHOD_NAME, "IOException", ex.getMessage());
            consoleMsg.printToTerminal(consoleMsg.getMessage());
        }
        return bArray;
    }

    public void deleteMergedSources() {
        for (int key = 1; key <= sources.size(); key++) {
            sources.get(key).delete();
        }
    }
}
