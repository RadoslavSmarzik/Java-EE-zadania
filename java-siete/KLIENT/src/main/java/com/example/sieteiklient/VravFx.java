package com.example.sieteiklient;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.HashMap;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class VravFx extends Application implements Runnable {
    Button zmazHistoriu;
    Socket socket;
    TextArea myTextArea = new TextArea();
    HashMap<Integer, ClientFx> ostanyKomunikujuci = new HashMap<>();
    private static final int port = 2004;
    VBox root;
    Stage stage;
    OutputStream wr;
    InputStream rd;
    Stage primaryStage;
    String name;
    Canvas canvas;
    GraphicsContext graphicsContext;
    HBox menuPanel = new HBox();

    byte PRIHLASENIE = 0;
    byte ODHLASENIE = 1;
    byte SPRAVA = 2;
    byte KRESLENIE_STLACENIE = 3;
    byte KRESLENIE_TAHANIE = 4;
    byte ZMAZ_HISTORIU = 5;
    byte ZMAZ_PLOCHU = 6;
    byte OBRAZOK = 7;


    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        Prihlasenie prihlasenie = new Prihlasenie(primaryStage, this);
        prihlasenie.vykresliPlochu();
    }


    public void vykresliKomunikaciu(){
        root = new VBox();
        this.stage = primaryStage;
        vytvorCanvas();
        vytvorZmazPlochuButton();
        root.getChildren().add(menuPanel);
        vytvorEmojiMenu();
        vytvorZmazHistoriuButton();
        root.getChildren().add(myTextArea);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle(this.name);
        primaryStage.setScene(scene);
        primaryStage.show();

        myTextArea.setBorder(new Border(
                new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        myTextArea.requestFocus();
        stage.setOnCloseRequest((evt) -> {
            try {
                odhlasenie();
                socket.close();
            } catch (Exception e) { e.printStackTrace(); }
        });

        Thread th = new Thread(this);
        th.start();

    }

    void vytvorCanvas(){

        this.canvas = new Canvas(400,  200);
        root.getChildren().add(canvas);

        this.graphicsContext = canvas.getGraphicsContext2D();
        graphicsContext.setFill(Color.rgb(255,255,255));
        graphicsContext.fillRect(0,0,400,200);

        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED,
                new EventHandler<MouseEvent>(){

                    @Override
                    public void handle(MouseEvent event) {
                        posliSpravu(KRESLENIE_STLACENIE,event.getX() + "/"+event.getY());
                        graphicsContext.beginPath();
                        graphicsContext.moveTo(event.getX(), event.getY());
                        graphicsContext.stroke();
                    }
                });

        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED,
                new EventHandler<MouseEvent>(){

                    @Override
                    public void handle(MouseEvent event) {
                        posliSpravu(KRESLENIE_TAHANIE,event.getX() + "/"+event.getY());
                        graphicsContext.lineTo(event.getX(), event.getY());
                        graphicsContext.stroke();
                    }
                });

        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED,
                new EventHandler<MouseEvent>(){

                    @Override
                    public void handle(MouseEvent event) {

                    }
                });

        primaryStage.show();
    }


    public void mouse_pressed(double x, double y){
        graphicsContext.beginPath();
        graphicsContext.moveTo(x, y);
        graphicsContext.stroke();
    }

    public void mouse_dragged(double x, double y){
        graphicsContext.lineTo(x, y);
        graphicsContext.stroke();
    }

    public void mouse_released(){

    }

    public void run() {
        createConnection();
        prihlasenie();

        while(prijmiSpravu());
        System.out.println("Koniec rozhovoru.");
        try {
            socket.close();
        } catch (Exception e) { e.printStackTrace(); }
        System.exit(0);
    }

    private void createConnection() {
        try {
            socket = new Socket("localhost", port);
            wr = socket.getOutputStream();
            rd = socket.getInputStream();
            myTextArea.setOnKeyReleased(x -> {
                sprava(myTextArea.getText());
            });

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sprava(String message){
        posliSpravu(SPRAVA,message);
    }

    public void prihlasenie(){
        posliSpravu(PRIHLASENIE, this.name);
    }

    public void odhlasenie(){
        posliSpravu(ODHLASENIE,"");
    }


    public void posliSpravu(byte typ, String message) {
        try {
            wr.write(typ);
            if(message!=null) {
                byte bts[] = message.getBytes();
                wr.write(bts.length & 255);
                wr.write(bts.length >> 8);
                wr.write(bts, 0, bts.length);
            }
            wr.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean prijmiSpravu() {
        try {
            int idPrijate = (int) rd.read() + (rd.read() << 8);
            byte typSpravy = (byte) rd.read();
            int nbts = rd.read();
            int nbts2 = rd.read();
            if ((nbts < 0) || (nbts2 < 0)) return false;
            nbts = nbts + ( nbts2 << 8);
            byte bts[] = new byte[nbts];
            int i = 0; // how many bytes did we read so far
            do {
                int j = rd.read(bts, i, bts.length - i);
                if (j > 0) i += j;
                else break;
            } while (i < bts.length);
            handleSpravu(idPrijate,typSpravy,bts);
        } catch (IOException e) {
            return false;
        }
        return true;
    }


    public void vytvorNovehoKomunikujuceho(int id){
        ClientFx novyClient = new ClientFx(id+"");
        this.ostanyKomunikujuci.put(id, novyClient);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                root.getChildren().add(novyClient.hBox);
                root.getChildren().add(novyClient.textArea);
            }
        });
    }

    public void textAreaMusiExistovat(int id){
        if(!this.ostanyKomunikujuci.containsKey(id)){
            vytvorNovehoKomunikujuceho(id);
        }
    }


    public void handleSpravu(int idPrijate, byte typSpravy, byte[] sprava) throws IOException {
        if(typSpravy == PRIHLASENIE){
            vytvorNovehoKomunikujuceho(idPrijate);
            ostanyKomunikujuci.get(idPrijate).meno = new String(sprava);
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    ostanyKomunikujuci.get(idPrijate).label.setText(new String(sprava));
                }
            });
        }
        else if(typSpravy == SPRAVA){
            textAreaMusiExistovat(idPrijate);
            ostanyKomunikujuci.get(idPrijate).textArea.setText(new String(sprava));
        }
        else if(typSpravy == ODHLASENIE){
            if(!this.ostanyKomunikujuci.containsKey(idPrijate)){
                return;
            }
            TextArea deleteTextArea = this.ostanyKomunikujuci.get(idPrijate).textArea;
            HBox deleteHBox = this.ostanyKomunikujuci.get(idPrijate).hBox;
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    root.getChildren().remove(deleteHBox);
                    root.getChildren().remove(deleteTextArea);
                    //update application thread
                }
            });
        }

        else if(typSpravy == ZMAZ_HISTORIU){
            if(!this.ostanyKomunikujuci.containsKey(idPrijate)){
                return;
            }
            TextArea textArea = this.ostanyKomunikujuci.get(idPrijate).textArea;
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    textArea.setText("");
                    ostanyKomunikujuci.get(idPrijate).deleteImagesInHBox();
                }
            });
        }

        else if(typSpravy == KRESLENIE_STLACENIE){
            String[] parsedBod = new String(sprava).split("/");
            double x = Double.parseDouble(parsedBod[0]);
            double y = Double.parseDouble(parsedBod[1]);
            mouse_pressed(x,y);
        }

        else if(typSpravy == KRESLENIE_TAHANIE){
            String[] parsedBod = new String(sprava).split("/");
            double x = Double.parseDouble(parsedBod[0]);
            double y = Double.parseDouble(parsedBod[1]);
            mouse_dragged(x,y);
        }
        else if(typSpravy == ZMAZ_PLOCHU){
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    graphicsContext.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                    graphicsContext.setFill(Color.rgb(255,255,255));
                    graphicsContext.fillRect(0,0,400,200);
                }
            });

        }
        else if(typSpravy == OBRAZOK){
            handleObrazok(idPrijate, sprava);
        }


    }



    private void handleObrazok(int prijateId, byte[] bts){
        Image obrazok = new Image(new ByteArrayInputStream(bts));
        ImageView holder = new ImageView(obrazok);
        holder.setFitHeight(25);
        holder.setFitWidth(25);
        //HBox otherClientHbox = clientEmoticons.get(otherClientID);
        if(!ostanyKomunikujuci.containsKey(prijateId)){
            return;
        }
        ClientFx klient = ostanyKomunikujuci.get(prijateId);
        Platform.runLater(() -> {
                    klient.hBox.getChildren().add(holder);
                    //primaryStage.show();
                }
        );
    }


    public static void main(String[] args) {
        launch(args);
    }

    public void vytvorZmazHistoriuButton(){
        zmazHistoriu = new Button("ZMAZ HISTORIU");
        zmazHistoriu.setPrefHeight(30);
        zmazHistoriu.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                myTextArea.setText("");
                for (ClientFx client : ostanyKomunikujuci.values()) {
                    client.textArea.setText("");
                    client.deleteImagesInHBox();
                }
                posliSpravu(ZMAZ_HISTORIU, "");

            }
        });
        this.menuPanel.getChildren().add(zmazHistoriu);
    }

    public void vytvorZmazPlochuButton(){
        Button zmazPlochu = new Button("ZMAZ PLOCHU");
        zmazPlochu.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                graphicsContext.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                graphicsContext.setFill(Color.rgb(255,255,255));
                graphicsContext.fillRect(0,0,400,200);
                posliSpravu(ZMAZ_PLOCHU, "");

            }
        });
        this.root.getChildren().add(zmazPlochu);
    }

    public void vytvorEmojiMenu(){
        for(int i = 1; i < 4; i++) {
            Image img = new Image(new File("src\\emoji" + i + ".png").toURI().toString());
            ImageView view = new ImageView(img);
            view.setFitWidth(25);
            view.setFitHeight(25);
            view.setPreserveRatio(true);
            Button button = new Button();
            button.setPrefSize(25, 25);
            button.setGraphic(view);
            menuPanel.getChildren().add(button);

            int finalI = i;
            button.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent event) {
                    posliObrazok("src\\emoji" + finalI + ".png");

                }
            });

        }
    }

    public void posliObrazok(String cesta){

        try {

            BufferedImage image = ImageIO.read(new File(cesta));
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", byteArrayOutputStream);
            byte[] size = ByteBuffer.allocate(4).putInt(byteArrayOutputStream.size()).array();

            wr.write(OBRAZOK);

            wr.write(size);
            wr.write(byteArrayOutputStream.toByteArray());
            wr.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }





}