import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * @author sharif
 */

public class TCPClientSocket {
    private static final String TAG = "TCPClientSocket";
    private Socket socket;
    private String hostName;
    private int portNumber;
    private PrintWriter out;
    private BufferedReader in;
    private volatile boolean connectedToServer;
    private SyncClientType syncClientType;

    public TCPClientSocket(String hostName, int tcpPortNumber, SyncClientType syncClientType) {
        this.hostName = hostName;
        this.portNumber = tcpPortNumber;
        connectedToServer = false;
        this.syncClientType = syncClientType;
    }

    public Message connectToServer() {
        final String METHOD_NAME = "connectToServer";
        Message msg = new Message();
        try {
            socket = new Socket(hostName, portNumber);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            connectedToServer = isServerListening(in.readLine());
        } catch (UnknownHostException e) {
            msg.setErrorMessage(TAG, METHOD_NAME, "UnknownHostException" + e.getMessage());
        } catch (IOException e) {
            msg.setErrorMessage(TAG, METHOD_NAME, "IOException" + e.getMessage());
        } catch (Exception e) {
            msg.setErrorMessage(TAG, METHOD_NAME, "Exception" + e.getMessage());
        }
        return msg;
    }

    public Message sendRequest(String request) {
        final String METHOD_NAME = "sendRequest";
        Message msg = new Message();
        try {
            out.println(request);
            String response = in.readLine();
            msg.setMessage(response);
        } catch (Exception e) {
            msg.setErrorMessage(TAG, METHOD_NAME, "Exception", e.getMessage());
        }
        return msg;
    }

    public Message closeTCPConnection() {
        final String METHOD_NAME = "closeTCPConnection";
        Message returnMsg = new Message();
        try {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            returnMsg.setErrorMessage(TAG, METHOD_NAME, "IOException", e.getMessage());
        }
        return returnMsg;
    }

    public boolean isConnectedToServer() {
        return connectedToServer;
    }

    public boolean isServerListening(String listening) {
        return listening != null && listening.equalsIgnoreCase("yes");
    }

    public String tcpRequest(String clientName, String request, String requestValue) {
        return clientName + "=" + request + "=" + requestValue;
    }
}
