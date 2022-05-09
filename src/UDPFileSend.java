import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

/**
 * Credit: https://gist.github.com/absalomhr/ce11c2e43df517b2571b1dfc9bc9b487
 * I made changes as needed for this project
 */

public class UDPFileSend implements Runnable {
    private static final String TAG = "UDPFileSend";
    private final String hostName;
    private final int updPortNum;
    private File fileToSend;
    private volatile boolean suspendFileSend;

    public UDPFileSend(String hostName, int updPortNum, File fileToSend) {
        this.hostName = hostName;
        this.updPortNum = updPortNum;
        suspendFileSend = false;
        this.fileToSend = fileToSend;
    }

    /**
     * Method for setting a file (hardcode for now), and to send its name to the server using UDP
     *
     * @param port server port
     * @param host server name
     */
    public void ready (int port, String host) {
        final String METHOD_NAME = "ready";
        Message consoleMsg = new Message();
        try (DatagramSocket socket = new DatagramSocket();){
            InetAddress address = InetAddress.getByName(host);

            String fileName = this.fileToSend.getName();
            byte[] fileNameBytes = fileName.getBytes();

            DatagramPacket fileStatPacket = new DatagramPacket(fileNameBytes, fileNameBytes.length, address, port);
            socket.send(fileStatPacket);

            byte[] fileByteArray = readFileToByteArray(this.fileToSend);
            sendFile(socket, fileByteArray, address, port);
        } catch (Exception ex) {
            consoleMsg.setErrorMessage(TAG, METHOD_NAME, "Exception", ex.getMessage());
            consoleMsg.printToTerminal(consoleMsg.getMessage());
        }
    }

    public void sendFile(DatagramSocket socket, byte[] fileByteArray, InetAddress address, int port) throws IOException {
        final String METHOD_NAME = "sendFile";
        Message consoleMsg = new Message();
        consoleMsg.printToTerminal("sending file");
        int seqNumber = 0; // for order
        boolean eofFlag; // to see if we got to the end of the file
        int ackSeq = 0; // to see if the datagram was received correctly

        for (int i = 0; i < fileByteArray.length; i = i + 1021) {
            seqNumber += 1;

            // create message
            byte[] message = new byte[1024]; //first two bytes of the data are for control (datagram integrity and order)
            message[0] = (byte) (seqNumber >> 8);
            message[1] = (byte) (seqNumber);

            if ((i + 1021) >= fileByteArray.length) { // have we reached the end of file
                eofFlag = true;
                message[2] = (byte) (1); // we reached teh end of the file (last datagram to send)
            } else {
                eofFlag = false;
                message[2] = (byte) (0); // we haven't reached the end of the file, still sending datagrams
            }

            if (!eofFlag) {
                System.arraycopy(fileByteArray, i, message, 3,1021);
            } else { // if it is the last datagram
                System.arraycopy(fileByteArray, i, message, 3, fileByteArray.length - i);
            }

            DatagramPacket sendPacket = new DatagramPacket(message, message.length, address, port);
            socket.send(sendPacket);
            consoleMsg.printToTerminal("sent: seq num = " + seqNumber);

            boolean ackReceived; // was the datagram received?

            while (!suspendFileSend) {
                byte[] ack = new byte[2];
                DatagramPacket ackPack = new DatagramPacket(ack, ack.length);

                try {
                    socket.setSoTimeout(50);
                    socket.receive(ackPack);
                    ackSeq = ((ack[0] & 0xff) << 8) + (ack[1] & 0xff); // figuring the seq num
                    ackReceived = true; // we received the ack
                } catch (SocketTimeoutException e) {
                    consoleMsg.printToTerminal("socked timed out waiting for ack");
                    ackReceived = false; // we did not receive the ack
                }

                // if the packet was received correctly next packet can be sent
                if ((ackSeq == seqNumber) && (ackReceived)) {
                    consoleMsg.printToTerminal("ack received: seq num = " + ackSeq);
                    break;
                } else { // packet was not received, so we resend it
                    socket.send(sendPacket);
                    consoleMsg.printToTerminal("resending: seq num = " + seqNumber);
                }
            }
        }
    }

    public byte[] readFileToByteArray(File file) {
        final String METHOD_NAME = "readFileToByteArray";
        Message consoleMsg = new Message();
        // creating a byte array using the length of the file
        // file.length returns long which is cast to int
        int fileLength = (int) file.length();
        int numBytesReadIntoBuff;
        byte[] bArray = new byte[fileLength];
        try (FileInputStream fis = new FileInputStream(file);){
            numBytesReadIntoBuff = fis.read(bArray);
            if (numBytesReadIntoBuff == -1) {
                consoleMsg.printToTerminal("There is not more data because the end of the file has been reached");
            } else {
                consoleMsg.printToTerminal("Total number of bytes read into the buffer = " + numBytesReadIntoBuff);
            }
        } catch (IOException ex) {
            consoleMsg.setErrorMessage(TAG, METHOD_NAME, "IOException", ex.getMessage());
            consoleMsg.printToTerminal(consoleMsg.getMessage());
        }
        return bArray;
    }

    public void suspendFileSend() {
        this.suspendFileSend = true;
    }

    public void resumeFileSend() {
        this.suspendFileSend = false;
    }

    @Override
    public void run() {
        ready(this.updPortNum, this.hostName);
    }
}
