import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class FileSyncClient {
    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.err.println("Usage: java FileSyncClient <w/m/l> <host name> <port number>");
            System.exit(1);
        }
        String clientType = args[0];
        String hostName = args[1];
        int portNumber = Integer.parseInt(args[2]);

        try (
                Socket fileSyncSocket = new Socket(hostName, portNumber);
                PrintWriter out = new PrintWriter(fileSyncSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(fileSyncSocket.getInputStream()));
        ) {
            BufferedReader stdIn =
                    new BufferedReader (new InputStreamReader(System.in));
            String fromServer;
            String fromUser;

            while ((fromServer = in.readLine()) != null) {
                System.out.println("Server: " + fromServer);
                if(fromServer.equals("Bye."))
                    break;

                fromUser = stdIn.readLine();
                if (fromUser != null) {
                    System.out.println("Client: " + fromUser);
                    out.println(fromUser);
                }
            }
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " + hostName);
            System.exit(1);
        }
    }
}
