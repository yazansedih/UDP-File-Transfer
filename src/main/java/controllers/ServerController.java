package controllers;

import com.example.UDPFileTransfer.Packet;
import com.example.UDPFileTransfer.UDPClient;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import threads.ClientHandler;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ServerController extends Thread implements Initializable  {

    @FXML
    private JFXTextField clinetPort;

    @FXML
    private JFXButton clearbtn;

    @FXML
    private JFXTextField clinetIP;

    @FXML
    private VBox container;

    @FXML
    private JFXButton downbtn;

    @FXML
    private JFXButton exit;

    @FXML
    private JFXTextField localIP;

    @FXML
    private JFXTextField localPort;

    @FXML
    private JFXButton startbtn;

    public List<Packet> packets = new ArrayList<Packet>();
    public static List<Packet> tempPkt = new ArrayList<Packet>();

    @FXML
    void StartReciver(ActionEvent event) {
        if (clinetIP.getText().isEmpty()){
            generateMessages("ClientIP is empty, Please fill it ....", "3");
        } else if(clinetPort.getText().isEmpty()){
            generateMessages("ClientPort is empty, Please fill it ....", "3");
        } else {
            startbtn.setDisable(true);
            startbtn.getStyleClass().add("start-btn");
            startbtn.getStylesheets().add(String.valueOf(UDPClient.class.getResource("/CSS/Server/labelStyling.css")));

            try {
                generateMessages("Server Started ....", "2");
                int port = Integer.parseInt(clinetPort.getText());
                DatagramSocket serverSocket = new DatagramSocket(port);
                Thread clientHandlerThread = new Thread(new ClientHandler(serverSocket,this));
                clientHandlerThread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    @FXML
    void DownloadBtn(ActionEvent event) {

    }
    @FXML
    void ClearMessage(ActionEvent event) {
        container.getChildren().clear();
    }

    @FXML
    void Close(ActionEvent event) {
        Stage stage = (Stage) exit.getScene().getWindow();
        stage.close();
    }

public void generateMessages(String s, String type) {
    Label l1;
    Label l2;

    if (type.equals("1")) {
        l1 = new Label(s);
        Platform.runLater(() -> container.getChildren().add(l1));
        l1.getStyleClass().add("l1");
        l1.getStylesheets().add(String.valueOf(UDPClient.class.getResource("/CSS/Client/labelStyling.css")));
    } else if (type.equals("2")) {
        l2 = new Label(s);
        Platform.runLater(() -> container.getChildren().add(l2));
        l2.getStyleClass().add("l2");
        l2.getStylesheets().add(String.valueOf(UDPClient.class.getResource("/CSS/Client/labelStyling.css")));
    } else {
        Label l;
        l = new Label(s);
        Platform.runLater(() -> container.getChildren().add(l));
        l.getStyleClass().add("l");
        l.getStylesheets().add(String.valueOf(UDPClient.class.getResource("/CSS/Client/labelStyling.css")));
    }
}


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        clinetIP.setText("192.168.88.8");
        clinetPort.setText("65535");

        try {
            InetAddress ia = InetAddress.getLocalHost();
            String []s = ia.toString().split("/");
            localIP.setText(s[1]);
            localPort.setText("65535");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
}
