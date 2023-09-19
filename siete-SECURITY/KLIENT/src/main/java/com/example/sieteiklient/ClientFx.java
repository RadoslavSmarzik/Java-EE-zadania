package com.example.sieteiklient;

import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;

public class ClientFx {
    TextArea textArea;
    Label label;
    String meno;
    HBox hBox;

    ClientFx(String meno){
        this.meno = meno;
        textArea = new TextArea();
        textArea.setEditable(false);
        label = new Label(meno);
        label.setMinHeight(25);
        hBox = new HBox();
        hBox.getChildren().add(label);
    }

    public void deleteImagesInHBox(){
        hBox.getChildren().clear();
        hBox.getChildren().add(label);
    }
}
