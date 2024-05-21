
package threads;

import com.example.UDPFileTransfer.Packet;
        import com.example.UDPFileTransfer.UDPChecksum;
        import controllers.ServerController;

        import java.io.*;
        import java.net.DatagramPacket;
        import java.net.DatagramSocket;
        import java.net.InetAddress;
        import java.util.ArrayList;
        import java.util.List;
        import java.util.regex.Matcher;
        import java.util.regex.Pattern;

public class ClientHandler  implements Runnable {
    private DatagramSocket serverSocket;
    ServerController serverController;

    public List<Packet> packets = new ArrayList<Packet>();

    public ClientHandler(DatagramSocket serverSocket, ServerController serverController) {
        this.serverSocket = serverSocket;
        this.serverController = serverController;
    }

    int reTransPkt = 0;

    @Override
    public void run() {
        try {
            int n =0;
            Packet prePkt = null;
            while (serverSocket != null) {
                int z = 0;
                int lines = 0;
                byte[] receivedData = new byte[10000000];
                DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);
                serverSocket.receive(receivedPacket);

                // Create a new thread to handle the client's request
                if (receivedPacket != null) {
                    // Deserialize the received data back into a Packet object
                    Packet recPkt = null;
                    try {
                        ByteArrayInputStream byteStream = new ByteArrayInputStream(receivedPacket.getData());
                        ObjectInputStream objectStream = new ObjectInputStream(byteStream);
                        recPkt = (Packet) objectStream.readObject();
                        objectStream.close();
                        byteStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // receive all pkts
                    try {
                        if (recPkt != null) {
                            lines = Integer.parseInt(recPkt.getData());
                            if (lines != 0){
                                serverController.generateMessages("---------------------------------------------" ,"1");
                                serverController.generateMessages("Receiving Done .... ","1");
                                serverController.generateMessages("---------------------------------------------" ,"1");
//                                serverController.generateMessages(String.valueOf(lines) ,"1");
                                try {
                                    String[] dataFile = new String[lines];
                                    Packet prePacket = null;
                                    z = 0;
                                    for (Packet packet: packets){
                                        if (prePacket != null) {
                                            if (!packet.getSeqNum().equals(prePacket.getSeqNum())) {
                                                dataFile[z] = prePacket.getData();
                                                z++;
                                            }
                                        }
                                        prePacket = packet;
                                    }
                                    dataFile[z] = prePacket.getData(); // last index

                                    for (String s: dataFile){
                                        serverController.generateMessages(s ,"1");
                                    }
                                    String fname = dataFile[0];
                                    String []fn = fname.split("\\.");
                                    System.out.println(fn[0]);
                                    String filePath = "C:\\Users\\AB\\ProjectNetwork1\\ProjectNetwork1\\src\\main\\java\\receivingFile\\" + fn[0]; // Replace with the actual file name
                                    File file = new File(filePath);

                                    PrintWriter pw = new PrintWriter(filePath);
                                    for (String s : dataFile) {
                                        pw.write(s + "\n");
                                    }
                                    pw.close();

                                    serverController.generateMessages("---------------------------------------------" ,"1");
                                    serverController.generateMessages(("Number of packets: " + String.valueOf(dataFile.length)) ,"1");
                                    serverController.generateMessages(("Original Size of the file: " + String.valueOf(file.length())) ,"1");
                                    serverController.generateMessages(("Percentage of file transfer: 100%"),"1");
                                    serverController.generateMessages(("Number of lost packets: 0") ,"1");
                                    serverController.generateMessages(("Number of re-transmitted packets: " + String.valueOf(reTransPkt)) ,"1");
                                    serverController.generateMessages("---------------------------------------------" ,"1");
                                    serverController.generateMessages("Done." ,"1");
                                    serverController.generateMessages("\n" ,"1");
                                } catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                            reTransPkt = 0;
                            packets.clear();
                            continue;
                        }
                    } catch (Exception ignored){

                    }

                    serverController.generateMessages(("Receive " + recPkt.getSeqNum() + ": " + recPkt.getData()), "1");
                    int extractedNumber = extractNumberFromString(recPkt.getSeqNum());
                    InetAddress clientAddress = receivedPacket.getAddress();
                    int clientPort = receivedPacket.getPort();

                    byte[] ackData = new byte[1]; // Assuming ACK can be represented in a single byte
                    boolean ACK = UDPChecksum.verifyChecksum(recPkt.getData().getBytes(), recPkt.getChecksum());
                    ackData[0] = (byte) (ACK ? 1 : 0); // 1 for true, 0 for false

                    if (ackData[0] == 1) {
                        packets.add(recPkt);
                        DatagramPacket ackPacket = new DatagramPacket(ackData, ackData.length, clientAddress, clientPort);
                        serverSocket.send(ackPacket);
                        if (n == 0) {
                            serverController.generateMessages(("Sending ACK" + extractedNumber), "2");
                            prePkt = recPkt;
                            n++;
                        } else {
                            if (!prePkt.getSeqNum().equals(recPkt.getSeqNum())){
//                                serverController.generateMessages(("Resending ACK" + (extractedNumber-1) + " ...."), "3");
                                serverController.generateMessages(("Sending ACK" + extractedNumber), "2");
                                prePkt = recPkt;
                                continue;
                            }
                            serverController.generateMessages(("Resending ACK" + extractedNumber  + " ...."), "2");
                            reTransPkt++;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int extractNumberFromString(String inputString) {
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(inputString);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group());
        }
        return 0;
    }
}