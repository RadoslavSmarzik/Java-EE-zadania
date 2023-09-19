package com.example.sieteiklient;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.security.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


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

    MySecurity mySecurity;

    int mojeCislovanieSpravy = 0;
    int ocakavamCisloSpravy = 1;


    String nameOfMyKeys;


    @Override
    public void start(Stage primaryStage) {
        Scanner s = new Scanner(System.in);
        System.out.println("ZADAJTE PORADIE KLIENTA (0 az 10):");
        nameOfMyKeys = s.next();
        System.out.println("nastavene");

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
                        try {
                            posliSpravu(KRESLENIE_STLACENIE,event.getX() + "/"+event.getY());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        graphicsContext.beginPath();
                        graphicsContext.moveTo(event.getX(), event.getY());
                        graphicsContext.stroke();
                    }
                });

        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED,
                new EventHandler<MouseEvent>(){

                    @Override
                    public void handle(MouseEvent event) {
                        try {
                            posliSpravu(KRESLENIE_TAHANIE,event.getX() + "/"+event.getY());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
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
        try {
            if(nameOfMyKeys != null){
                this.mySecurity = new MySecurity("src\\keys\\"+nameOfMyKeys+"_private.txt",
                        "src\\keys\\"+nameOfMyKeys+"_public.txt", "src\\server_public.txt");
            }
            else {
                this.mySecurity = new MySecurity("src\\client_private.txt",
                        "src\\client_public.txt", "src\\server_public.txt");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        createConnection();
        try {
            prihlasenie();
        } catch (Exception e) {
            e.printStackTrace();
        }

        while(prijmiSpravu());
        System.out.println("Koniec rozhovoru.");
        try {
            socket.close();
        } catch (Exception e) { e.printStackTrace(); }
        System.exit(0);
    }

    private void createConnection(){
        try {
            socket = new Socket("localhost", port);
            System.out.println("Vytvoreny socket pre odosielanie");
            wr = socket.getOutputStream();
            rd = socket.getInputStream();
            myTextArea.setOnKeyReleased(x -> {
                try {
                    sprava(myTextArea.getText());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sprava(String message) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        posliSpravu(SPRAVA,message);
    }

    public void prihlasenie() throws NoSuchAlgorithmException, SignatureException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        posliSpravu(PRIHLASENIE, this.name);
    }

    public void odhlasenie() throws NoSuchAlgorithmException, SignatureException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException {
        posliSpravu(ODHLASENIE,"");
    }

    public byte[] intToBytes(int value){
        byte[] bytes = new byte[Integer.BYTES];
        int length = bytes.length;
        for (int i = 0; i < length; i++) {
            bytes[length - i - 1] = (byte) (value & 0xFF);
            value >>= 8;
        }
        return bytes;
    }

    byte[] spojPolia(byte[] type, byte[] message){
        byte[] c = new byte[type.length + message.length];
        System.arraycopy(type, 0, c, 0, type.length);
        System.arraycopy(message, 0, c, type.length, message.length);
        return c;
    }

    public void posliSpravu(byte typ, String message) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        mojeCislovanieSpravy++;
        byte[] idCislovanie = intToBytes(this.mojeCislovanieSpravy);

        byte[] type = {typ};
        byte msg[] = message.getBytes();
        byte[] cislovaniePlusType = spojPolia(idCislovanie, type);
        byte[] messgaeToSend = spojPolia(cislovaniePlusType, msg);

        //byte[] cryptedMessageToSend = mySecurity.cryptoSmall(messgaeToSend);

        byte[] cryptedMessageToSend = mySecurity.cryptoMessage(messgaeToSend);

        byte[] signature = mySecurity.signMessage(cryptedMessageToSend);

        try {
            if(messgaeToSend!=null) {
                wr.write(cryptedMessageToSend.length & 255);
                wr.write(cryptedMessageToSend.length >> 8);
                wr.write(cryptedMessageToSend, 0, cryptedMessageToSend.length);
            }
            if(signature!=null) {
                wr.write(signature.length & 255);
                wr.write(signature.length >> 8);
                wr.write(signature, 0, signature.length);
            }
            wr.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void skontrolujCislovanie(int aktualneCisloZoSpravy){
        System.out.println("OCAKAVANIE: " + ocakavamCisloSpravy + " PRISLO: " + aktualneCisloZoSpravy);
        if(aktualneCisloZoSpravy != ocakavamCisloSpravy){
            System.out.println("PROBLEM: ZLE CISLOVANIE");
        }
           ocakavamCisloSpravy++;
    }

    public void spracujDecryptedMessage(byte[] message) throws IOException {
        byte[] arr0 = {  message[0], message[1], message[2], message[3] };
        ByteBuffer wrapped0 = ByteBuffer.wrap(arr0);
        int cisloZoSpravy = wrapped0.getInt();
        skontrolujCislovanie(cisloZoSpravy);

        byte[] arr = {  message[4], message[5], message[6], message[7] };
        ByteBuffer wrapped = ByteBuffer.wrap(arr);
        int id = wrapped.getInt();
        byte[] filteredMessage = Arrays.copyOfRange(message, 9, message.length);
        handleSpravu(id,message[8],filteredMessage);
    }

    public boolean prijmiSpravu() {
        try {
            int nbts = rd.read();
            int nbts2 = rd.read();
            if ((nbts < 0) || (nbts2 < 0)) return false;
            nbts = nbts + (nbts2 << 8);
            byte bts[] = new byte[nbts];
            int i = 0; // how many bytes did we read so far
            do {
                int j = rd.read(bts, i, bts.length - i);
                if (j > 0) i += j;
                else break;
            } while (i < bts.length);

            int nbtsx = rd.read();
            int nbts2x = rd.read();
            if ((nbtsx < 0) || (nbts2x < 0)) return false;
            nbtsx = nbtsx + (nbts2x << 8);
            byte btsx[] = new byte[nbtsx];
            int ix = 0; // how many bytes did we read so far
            do {
                int jx = rd.read(btsx, ix, btsx.length - ix);
                if (jx > 0) ix += jx;
                else break;
            } while (ix < btsx.length);

            byte[] messageBytes = bts;
            byte[] signature = btsx;

            if (mySecurity.verifySignMessage(messageBytes, signature)) {
                //byte[] decryptedMessage = mySecurity.decrypto(messageBytes);
                byte[] decryptedMessage = mySecurity.decryptoLargeMessage(messageBytes);
                System.out.println("USPESNE OVERENIE");
                spracujDecryptedMessage(decryptedMessage);
            } else {
                System.out.println("neuspesne overenie");
            }


        } catch (Exception e) {
            //e.printStackTrace();
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
        //System.out.println(idPrijate + " " +  typSpravy);
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
                try {
                    posliSpravu(ZMAZ_HISTORIU, "");
                } catch (Exception e) {
                    e.printStackTrace();
                }

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
                try {
                    posliSpravu(ZMAZ_PLOCHU, "");
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        this.root.getChildren().add(zmazPlochu);
    }


}