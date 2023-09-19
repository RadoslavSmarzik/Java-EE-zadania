module com.example.sieteiklient {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens com.example.sieteiklient to javafx.fxml;
    exports com.example.sieteiklient;
}