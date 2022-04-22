/**
 * @author sharif
 */

public class SyncClient {
    private String clientType;

    public SyncClient(String clientType, String hostName, int portNumber) {
        this.clientType = clientType;
    }
}

//        try (
//                Socket fileSyncSocket = new Socket(hostName, portNumber);
//                PrintWriter out = new PrintWriter(fileSyncSocket.getOutputStream(), true);
//                BufferedReader in = new BufferedReader(
//                new InputStreamReader(fileSyncSocket.getInputStream()));
//                ) {
//                BufferedReader stdIn =
//                new BufferedReader (new InputStreamReader(System.in));
//                String fromServer;
//                String fromUser;
//
//                while ((fromServer = in.readLine()) != null) {
//                System.out.println("Server: " + fromServer);
//                if(fromServer.equals("Bye."))
//                break;
//
//                fromUser = stdIn.readLine();
//                if (fromUser != null) {
//                System.out.println("Client: " + fromUser);
//                out.println(fromUser);
//                }
//                }
//                } catch (UnknownHostException e) {
//                System.err.println("Don't know about host " + hostName);
//                System.exit(1);
//                } catch (IOException e) {
//                System.err.println("Couldn't get I/O for the connection to " + hostName);
//                System.exit(1);
//                }
