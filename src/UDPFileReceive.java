import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Credit: https://gist.github.com/absalomhr/ce11c2e43df517b2571b1dfc9bc9b487
 * I made changes as needed for this project
 */

public class UDPFileReceive implements Runnable {
    private static final String TAG = "UDPFileReceive";
    private final int udpPortNum;
    private String fileReceiveFolder;
    private volatile boolean suspendFileReceive;

    public UDPFileReceive(int udpPortNum, String fileReceiveFolder) {
        this.udpPortNum = udpPortNum;
        this.fileReceiveFolder = fileReceiveFolder;
        this.suspendFileReceive = false;
    }

    public void createFile () {
        final String METHOD_NAME = "createFile";
        Message msg = new Message();
        try (DatagramSocket socket = new DatagramSocket(this.udpPortNum);){
            byte[] receiveFileName = new byte[1024]; // where we store the data of datagram
            DatagramPacket receiveFileNamePacket = new DatagramPacket(receiveFileName, receiveFileName.length);
            socket.receive(receiveFileNamePacket);
            msg.printToTerminal("receiving file name: ");
            byte[] data = receiveFileNamePacket.getData();
            String fileName = new String(data, 0, receiveFileNamePacket.getLength());
            String[] fileNameTokens = fileName.split("/");
            fileName = fileNameTokens[fileNameTokens.length-1];

            msg.printToTerminal("creating file: " + fileName);
            File file = new File(this.fileReceiveFolder + "/" + fileName);
            FileOutputStream outToFile = new FileOutputStream(file);

            receiveFile(outToFile, socket);
        } catch (Exception ex) {
            msg.setErrorMessage(TAG, METHOD_NAME, "Exception",  ex.getMessage());
            msg.printToTerminal(msg.getMessage());
        }
    }

    private void receiveFile(FileOutputStream outToFile, DatagramSocket socket) throws IOException {
        final String METHOD_NAME = "receiveFile";
        Message consoleMsg = new Message();
        System.out.println("receiving file");
        boolean eofFile; // have we reached the end of  file
        int seqNum = 0; // order of seq
        int foundLast = 0; // last seq found

        while (!suspendFileReceive) {
            byte[] message = new byte[1024]; // where the data from the received datagram is stored
            byte[] fileByteArray = new byte[1021]; // where we store the data to be written to the file

            // receive packet and retrieve the data
            DatagramPacket receivedPacket = new DatagramPacket(message, message.length);
            socket.receive(receivedPacket);
            message = receivedPacket.getData();

            // get port and address for sending acknowledgement
            InetAddress address = receivedPacket.getAddress();
            int port = receivedPacket.getPort();

            // the 0xff is used to extract bits:
            // https://www.baeldung.com/java-and-0xff#:~:text=0xff%20is%20a%20number%20represented,0xff%20in%20binary%20is%2011111111
            // retrieve seq num
            seqNum = ((message[0] & 0xff) << 8) + (message[1] & 0xff);
            // check if we reached the last datagram (end of file)
            eofFile = (message[2] & 0xff) == 1;

            // if seq num is the last seen + 1, then it is correct
            // we get the data from the message and write the ack that it has been received correctly
            if (seqNum == (foundLast + 1)) {
                // set the last seq num to be the one we just received
                foundLast = seqNum;

                // retrieve data from message
                System.arraycopy(message, 3, fileByteArray, 0, 1021);

                // write the retrieved data to the file and print received data seq num
                synchronized (this){
                    outToFile.write(fileByteArray);
                }
                consoleMsg.printToTerminal("received: seq num = " + foundLast);

                // send acknowledgement
                sendAck(foundLast, socket, address, port);
            } else {
                consoleMsg.printToTerminal("expected seq num: " + (foundLast + 1) + " but received " + seqNum + ". DISCARDING");
                // resend the acknowledgement
                sendAck(foundLast, socket, address, port);
            }
            // check for last datagram
            if (eofFile) {
                outToFile.close();
                break;
            }
        }
    }

    public void sendAck(int foundLast, DatagramSocket socket, InetAddress address, int port) throws IOException {
        final String METHOD_NAME = "sendAck";
        Message consoleMsg = new Message();
        // send acknowledgement
        byte[] ackPacket = new byte[2];
        ackPacket[0] = (byte) (foundLast >> 8);
        ackPacket[1] = (byte) (foundLast);

        // the datagram packet to be sent
        DatagramPacket ack = new DatagramPacket(ackPacket, ackPacket.length, address, port);
        socket.send(ack);
        consoleMsg.printToTerminal("send ack: seq num = " + foundLast);
    }

    public void suspendFileReceive() {
        this.suspendFileReceive = true;
    }

    public void resumeFileReceive() {
        this.suspendFileReceive = false;
    }

    @Override
    public synchronized void run() {
        createFile();
    }
}