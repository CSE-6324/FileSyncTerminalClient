import javax.annotation.processing.SupportedSourceVersion;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

/**
 * @author sharif
 */

public class Message {
    private final int DEFAULT_CODE = -1;
    private final boolean DEFAULT_SUCCESS = true;
    private String msg;
    private int msgCode;
    private boolean msgSuccess;
    private final File logFile = new File(PrgUtility.CLIENT_LOG_FILE);
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public Message() {
        this("");
    }
    public Message(String msg) {
        this.msg = msg;
        this.msgCode = DEFAULT_CODE;
        this.msgSuccess = DEFAULT_SUCCESS;
    }
    public Message(String msg, int msgCode, boolean msgSuccess) {
        this.msg = msg;
        this.msgCode = msgCode;
        this.msgSuccess = msgSuccess;
    }

    public Message(String msg, boolean msgSuccess) {
        this.msg = msg;
        this.msgCode = DEFAULT_CODE;
        this.msgSuccess = msgSuccess;
    }

    public Message(String msg, int msgCode) {
        this.msg = msg;
        this.msgCode = msgCode;
        this.msgSuccess = DEFAULT_SUCCESS;
    }

    public String getMessage() {
        return msg;
    }

    public int getMessageCode() {
        return msgCode;
    }

    public boolean isMessageSuccess() {
        return msgSuccess;
    }

    public void setMessageCode(int msgCode) {
        this.msgCode = msgCode;
    }

    public void setMessageSuccess(boolean msgSuccess) {
        this.msgSuccess = msgSuccess;
    }

    public void setMessage(String msg) {
        this.msg = msg;
    }

    public void setErrorMessage(String className, String methodName, String msg) {
        this.msgSuccess = false;
        setMessage("[Error] " + className + " @ " + methodName +"(): " + msg);
    }

    public void setErrorMessage(String className, String methodName, String errorName, String msg) {
        this.msgSuccess = false;
        setMessage("[Error] " + className + " @ " + methodName +"(): " + "(" + errorName + ") " + msg);
    }

    private void printToTerminalUserPrompt() {
        System.out.print("> ");
    }

    public void printToTerminal(String msg) {
        if (!msgSuccess) {
            msg = logMsgToFile(msg);
        }
        System.out.println(msg);
        printToTerminalUserPrompt();
    }

    public String logMsgToFile(String msg) {
        String userFriendlyMsg = "";
        try (Writer filWriter = new FileWriter(logFile, true);){
            String logMsg = String.format("[ %s ] %s", simpleDateFormat.format(System.currentTimeMillis()), msg);
            filWriter.write(logMsg + System.lineSeparator());
            filWriter.flush();
            userFriendlyMsg = "the app has encountered an issue. its logged and will be fixed asap. thank you.";
        } catch (Exception e) {
            userFriendlyMsg = String.format("an internal error (%s) has occurred. we have noted it, and are working on it. thank you.", e.getMessage());
        }
        return userFriendlyMsg;
    }
}
