module com.example.xmlmapa {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.xml;
    requires java.desktop;


    opens com.example.xmlmapa to javafx.fxml;
    exports com.example.xmlmapa;
}