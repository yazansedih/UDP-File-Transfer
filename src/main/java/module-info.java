module com.example.UDPFileTransfer {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.jfoenix;


    opens com.example.UDPFileTransfer to javafx.fxml;
    exports com.example.UDPFileTransfer;
    exports controllers;
    opens controllers to javafx.fxml;
    exports threads;
    opens threads to javafx.fxml;
}