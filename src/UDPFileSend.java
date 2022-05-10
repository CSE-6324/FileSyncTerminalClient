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
    private final File fileToSend;

    public UDPFileSend(String hostName, int updPortNum, File fileToSend) {
        this.hostName = hostName;
        this.updPortNum = updPortNum;
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
        Message commMsg = new Message();
        try (DatagramSocket socket = new DatagramSocket();){
            InetAddress address = InetAddress.getByName(host);
            String fileName = fileToSend.getName();
            if (PrgUtility.isFileNameValid(fileName) && PrgUtility.hasFileExtension(fileName)) {
                byte[] fileNameBytes = fileName.getBytes();
                DatagramPacket fileStatPacket = new DatagramPacket(fileNameBytes, fileNameBytes.length, address, port);
                socket.send(fileStatPacket);
                byte[] fileByteArray = readFileToByteArray(fileToSend);
                sendFile(socket, fileByteArray, address, port);
            }

        } catch (Exception ex) {
            commMsg.setErrorMessage(TAG, METHOD_NAME, "Exception", ex.getMessage());
            commMsg.printToTerminal(commMsg.getMessage());
        }
    }

    public synchronized void sendFile(DatagramSocket socket, byte[] fileByteArray, InetAddress address, int port) throws IOException {
        final String METHOD_NAME = "sendFile";
        Message commMsg = new Message();
        commMsg.logMsgToFile("sending file");
        int seqNumber = 0; // for order
        boolean eofFlag; // to see if we got to the end of the file
        int ackSeq = 0; // to see if the datagram was received correctly
        int bytesUploaded = 0;
        PrgUtility.updateFileStatusBytesUpload(this.fileToSend.getName(), bytesUploaded, fileByteArray.length);
        int srcPos, destPost, len;
        for (int i = 0; i < fileByteArray.length; i = i + 1021) {
            seqNumber += 1;

            // create message
            byte[] message = new byte[1024]; //first two bytes of the data are for control (datagram integrity and order)
            message[0] = (byte) (seqNumber >> 8);
            message[1] = (byte) (seqNumber);

            if ((i + 1021) >= fileByteArray.length) { // have we reached the end of file
                eofFlag = true;
                message[2] = (byte) (1); // we reached the end of the file (last datagram to send)
            } else {
                eofFlag = false;
                message[2] = (byte) (0); // we haven't reached the end of the file, still sending datagrams
            }


            if (!eofFlag) {
                srcPos = i;
                destPost = 3;
                len = 1021;
                System.arraycopy(fileByteArray, srcPos, message, destPost, len);
            } else { // if it is the last datagram
                srcPos = i;
                destPost = 3;
                len = fileByteArray.length - i;
                System.arraycopy(fileByteArray, srcPos, message, destPost, len);
            }

            DatagramPacket sendPacket = new DatagramPacket(message, message.length, address, port);
            socket.send(sendPacket);
            commMsg.logMsgToFile("sent: seq num = " + seqNumber);

            boolean ackReceived; // was the datagram received?

            while (true) {
                byte[] ack = new byte[2];
                DatagramPacket ackPack = new DatagramPacket(ack, ack.length);

                try {
                    socket.setSoTimeout(50);
                    socket.receive(ackPack);
                    ackSeq = ((ack[0] & 0xff) << 8) + (ack[1] & 0xff); // figuring the seq num
                    ackReceived = true; // we received the ack
                } catch (SocketTimeoutException ex) {
                    commMsg.logMsgToFile("socked timed out waiting for ack");
                    ackReceived = false; // we did not receive the ack
                }

                // if the packet was received correctly next packet can be sent
                if ((ackSeq == seqNumber) && (ackReceived)) {
                    commMsg.logMsgToFile("ack received: seq num = " + ackSeq);
                    bytesUploaded += len;
                    PrgUtility.updateFileStatusBytesUpload(this.fileToSend.getName(), bytesUploaded, fileByteArray.length);
                    break;
                } else { // packet was not received, so we resend it
                    socket.send(sendPacket);
                    bytesUploaded -=  len;
                    PrgUtility.updateFileStatusBytesUpload(this.fileToSend.getName(), bytesUploaded, fileByteArray.length);
                    commMsg.logMsgToFile("resending: seq num = " + seqNumber);
                }
            }
        }
    }

    public byte[] readFileToByteArray(File file) {
        final String METHOD_NAME = "readFileToByteArray";
        Message commMsg = new Message();
        // creating a byte array using the length of the file
        // file.length returns long which is cast to int
        byte[] bArray = new byte[(int) file.length()];
        try (FileInputStream fis = new FileInputStream(file);){
            fis.read(bArray);
        } catch (IOException ex) {
            commMsg.setErrorMessage(TAG, METHOD_NAME, "IOException", ex.getMessage());
            commMsg.printToTerminal(commMsg.getMessage());
        }
        return bArray;
    }

    @Override
    public synchronized void run() {
        ready(this.updPortNum, this.hostName);
    }
}