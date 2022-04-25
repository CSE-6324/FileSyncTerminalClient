/**
 * @author sharif
 */

public class Message {
    private final int DEFAULT_CODE = -1;
    private final boolean DEFAULT_SUCCESS = true;
    private String msg;
    private int msgCode;
    private boolean msgSuccess;

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
        setMessage("[Error] " + className + " @ " + methodName +"(): " + msg);
    }
}
