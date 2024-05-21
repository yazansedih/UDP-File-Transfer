package controllers;

import com.example.UDPFileTransfer.Packet;
import com.example.UDPFileTransfer.UDPChecksum;
import com.example.UDPFileTransfer.UDPClient;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import com.jfoenix.controls.JFXButton;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ClientController implements Initializable {


    @FXML
    private JFXButton exit;

    @FXML
    private JFXButton clearbtn;

    @FXML
    private JFXTextArea fileName;

    @FXML
    private JFXButton uploadFileBtn;

    @FXML
    private JFXButton sendBtn;

    @FXML
    private JFXButton downloadBtn;

    @FXML
    private JFXTextField localIP;

    @FXML
    private JFXTextField localPort;

    @FXML
    private JFXTextField serverIP;

    @FXML
    private JFXTextField serverPort;

    @FXML
    private VBox container;

    public File selectedFile;

    public String[] dataFile;

    public int pckSend = 0;

    public int reTransPkt = 0;

    @FXML
    void FileChooser(ActionEvent event) {
        pckSend = 0;
        reTransPkt = 0;
        // Create a FileChooser instance
        FileChooser fileChooser = new FileChooser();

        // Set extension filters if needed (optional)
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));

        // Show the open file dialog
        selectedFile = fileChooser.showOpenDialog(uploadFileBtn.getScene().getWindow());

        // Check if a file was selected
        if (selectedFile != null) {
            // Set the selected file name in the JFXTextArea
            fileName.setText(selectedFile.getName());
            // Read all lines from the selected file and store in dataFile array
            try {
                String[] lines = Files.readAllLines(selectedFile.toPath()).toArray(new String[0]);
                dataFile = new String[lines.length + 2];
                dataFile[0] = fileName.getText();
                for (int i=0; i< lines.length; i++){
                    dataFile[i+1] = lines[i];
                }
                dataFile[lines.length+1] = String.valueOf(lines.length+1);

                for (int i=0; i<(dataFile.length-1) ; i++) {
                    generateMessages(dataFile[i] ,"1");
                }
                generateMessages("---------------------------------------------" ,"1");
                generateMessages(("Number of packets: " + String.valueOf(dataFile.length-1)) ,"1");
                generateMessages(("Original Size of the file: " + String.valueOf(selectedFile.length())) ,"1");
                generateMessages("---------------------------------------------" ,"1");
            } catch (IOException e) {
                e.printStackTrace();
                // Handle the error if reading the file fails
            }
        }
    }


    @FXML
    void SendFile(ActionEvent event) {
        if (serverIP.getText().isEmpty()) {
            generateMessages("ServerIP is empty, Please fill it ....", "3");
        } else if (serverPort.getText().isEmpty()) {
            generateMessages("ServerPort is empty, Please fill it ....", "3");
        } else if (fileName.getText().isEmpty()) {
            generateMessages("File is empty, Please upload file ....", "3");
        } else {
            String fn = fileName.getText();
            try (DatagramSocket s = new DatagramSocket()) {
                InetAddress ia = InetAddress.getByName(serverIP.getText());
                int port = Integer.parseInt(serverPort.getText()); // port 65535

                // Set the receive timeout before entering the while loop
                s.setSoTimeout(10); // 100 ms

                // send pkt0 nameFile
                String seqNum = null;
                for (int z = 0; z < dataFile.length; z++) {

                    seqNum = "pkt" + String.valueOf(z);
                    // Convert sentName to a byte array
                    byte[] sentName = dataFile[z].getBytes();
                    int checksum = UDPChecksum.calculateChecksum(sentName);
                    byte[] ACK = new byte[1];

                    Packet pkt = new Packet(seqNum, dataFile[z], checksum);
//                System.out.println(dataFile[z]);
                    // Serialize the pkt object to a byte array
                    while (true) {
                        byte[] pktData = null;
                        try {
                            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                            ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
                            objectStream.writeObject(pkt);
                            objectStream.flush();
                            pktData = byteStream.toByteArray();
                            objectStream.close();
                            byteStream.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        // Create a new DatagramPacket with the serialized pktData & Send the packet .
                        if (pktData != null){
                            if ( z == (dataFile.length-1)){
                                DatagramPacket p1 = new DatagramPacket(pktData, pktData.length, ia, port);
                                s.send(p1);
                            }
                            else {
                                generateMessages(("Send " + seqNum + ": " + dataFile[z]),"1");
                                DatagramPacket p1 = new DatagramPacket(pktData, pktData.length, ia, port);
                                s.send(p1);
                            }
                        }

                        try {
                            // Receiving pkt
                            DatagramPacket receivedPacket = new DatagramPacket(ACK, ACK.length);
                            s.receive(receivedPacket);

                            if (ACK[0] == 1) {
                                // print in screen ....
                                if (z == (dataFile.length-1)) break;

                                generateMessages(("ACK" + z),"2");
                                pckSend++;
                                break;
                            }
//                    System.out.println("Received pkt. " + ACK[0]);
                        } catch (SocketTimeoutException e) {
                            // Timeout occurred, handle the case where ACK is not received within the timeout period
                            if (z == (dataFile.length-1)) break;
                            Label l;
                            l = new Label("Timeout occurred, Resend the packet ....");
                            container.getChildren().add(l);
                            l.getStyleClass().add("l");
                            l.getStylesheets().add(String.valueOf(UDPClient.class.getResource("/CSS/Client/labelStyling.css")));
                            reTransPkt++;
                            continue;
                        }
                    }
                }
                generateMessages("---------------------------------------------" ,"1");
                generateMessages("Sending done.", "1");
                generateMessages("---------------------------------------------" ,"1");
                generateMessages(("Percentage of file transfer: " + String.valueOf((pckSend/(dataFile.length-1))*100 ) + "%"),"1");
                generateMessages(("Number of lost packets: " + String.valueOf(dataFile.length-pckSend-1)) ,"1");
                generateMessages(("Number of re-transmitted packets: " + String.valueOf(reTransPkt)) ,"1");
                generateMessages("---------------------------------------------" ,"1");
                generateMessages("Done." ,"1");
                generateMessages("\n" ,"1");
//            System.out.println(Packet.getPackets().size());
            } catch (IOException e) {
                e.printStackTrace();
                // Log the error message
                Label l;
                l = new Label("Error sending file: " + fn);
                container.getChildren().add(l);
                l.getStyleClass().add("l");
                l.getStylesheets().add(String.valueOf(UDPClient.class.getResource("/CSS/Client/labelStyling.css")));
            }
        }
    }


    @FXML
    void ClearMessage(ActionEvent event) {
        container.getChildren().clear();
        pckSend = 0;
        reTransPkt = 0;
    }

    @FXML
    void Close(ActionEvent event) {
        Stage stage = (Stage) exit.getScene().getWindow();
        stage.close();
    }


    public void generateMessages(String s ,String type){

        Label l1;
        Label l2;

        if (type.equals("1")){
            l1 = new Label(s);
            container.getChildren().add(l1);

            l1.getStyleClass().add("l1");
            l1.getStylesheets().add(String.valueOf(UDPClient.class.getResource("/CSS/Client/labelStyling.css")));
        } else if (type.equals("2")) {
            l2 = new Label(s);
            container.getChildren().add(l2);

            l2.getStyleClass().add("l2");
            l2.getStylesheets().add(String.valueOf(UDPClient.class.getResource("/CSS/Client/labelStyling.css")));
        } else {
            Label l;
            l = new Label(s);
            container.getChildren().add(l);
            l.getStyleClass().add("l");
            l.getStylesheets().add(String.valueOf(UDPClient.class.getResource("/CSS/Client/labelStyling.css")));
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
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        serverIP.setText("192.168.88.8");
        serverPort.setText("65535");

        try {
            InetAddress ia = InetAddress.getLocalHost();
            String []s = ia.toString().split("/");
            localIP.setText(s[1]);
            localPort.setText("65535");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
//        generateMessages();
    }


}

