import org.junit.Test;

import java.io.File;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class FileUtilityTest {

    @org.junit.Before
    public void setUp() throws Exception {
    }

    @org.junit.After
    public void tearDown() throws Exception {
    }

    @org.junit.Test
    public void getMacFiles() {
        // I only have one file here for testing for now.
        // Need to change this if more files are added.
        String expectedFile = "milkyway.jpeg";
        ArrayList<File> files = FileUtility.getMacFiles();
        String actualFile = files.get(0).getName();
        assertEquals(true, expectedFile.equals(actualFile));
    }

    @org.junit.Test
    public void getWindowsFiles() {
        // I only have one file here for testing for now.
        // Need to change this if more files are added.
        String expectedFile = "alpine.jpeg";
        ArrayList<File> files = FileUtility.getWindowsFiles();
        String actualFile = files.get(0).getName();
        assertEquals(true, expectedFile.equals(actualFile));
    }

    @org.junit.Test
    public void getFileInfo() {
    }

    @org.junit.Test
    public void getFileBlocks() {
    }

    @org.junit.Test
    public void getServerFiles() {
        ArrayList<File> serverFiles = FileUtility.getServerFiles();
        for (File f: serverFiles) {
            System.out.println(f.getName());
        }
    }

    @Test
    public void getFileNameFromFileBlockName() {
        String fileBlockName = "milkyway_001.jpeg";
        String expectedName = "milkyway.jpeg";
        String actualName = FileUtility.getFileNameFromFileBlockName(fileBlockName);
        assertEquals(true, expectedName.equals(actualName));
    }
}