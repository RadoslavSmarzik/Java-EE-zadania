package com.company;


import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.*;
import java.util.ArrayList;
import java.util.Arrays;

public class HandlerThread extends Thread{

    byte PRIHLASENIE = 0;
    byte ODHLASENIE = 1;
    byte SPRAVA = 2;

    ServerSecurity serverSecurity;
    int id;
    Socket socket = new Socket();
    OutputStream wr;
    InputStream rd;
    ArrayList<HandlerThread> handlers;
    String meno;
    String aktualnaSprava = "";

    int mojeCislovanieSpravy = 0;
    int ocakavamCisloSpravy = 1;

    HandlerThread(int id, Socket socket, ArrayList<HandlerThread> handlers, PrivateKey privateKey, PublicKey publicKey, String nameOfFileClientPublicKey) throws IOException, ClassNotFoundException {
        serverSecurity = new ServerSecurity(privateKey, publicKey, nameOfFileClientPublicKey);
        this.id = id;
        this.wr = socket.getOutputStream();
        this.rd = socket.getInputStream();
        this.handlers = handlers;
    }

    @Override
    public void run() {
        while(prijmiSpravu());
        System.out.println("Koniec rozhovoru.");
        try {
            ostatnyOdhlasenie();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.handlers.remove(this);
        try {
            socket.close();
        } catch (Exception e) { e.printStackTrace(); }
        //System.exit(0);
    }

    public void ostatnyOdhlasenie() throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, SignatureException, InvalidKeyException {
        posliOstatnym(ODHLASENIE, "".getBytes());
    }


    public void posliOstatnym(byte typSpravy, byte[] sprava) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, SignatureException, InvalidKeyException {
        for(int i = 0; i < handlers.size(); i++){
            if(handlers.get(i).socket != this.socket){
                handlers.get(i).posliSpravuSId(this.id, typSpravy, sprava);
            }
        }

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

    public void posliSpravuSId(int id, byte typSpravy, byte[] sprava) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, SignatureException {
        mojeCislovanieSpravy++;
        byte[] idCislovanie = intToBytes(this.mojeCislovanieSpravy);
        byte[] idBytes = intToBytes(id);
        byte[] typSpravyBytes = {typSpravy};

        byte[] cislovaniePlusId = spojPolia(idCislovanie, idBytes);
        byte[] idPlusTyp = spojPolia(cislovaniePlusId, typSpravyBytes);
        byte[] celaSprava = spojPolia(idPlusTyp, sprava);

        //byte[] sifrovanaCelaSprava = serverSecurity.cryptoSmall(celaSprava);
        byte[] sifrovanaCelaSprava = serverSecurity.cryptoMessage(celaSprava);

        byte[] signature = serverSecurity.signMessage(sifrovanaCelaSprava);

        try {
            if(sifrovanaCelaSprava != null) {
                wr.write(sifrovanaCelaSprava.length & 255);
                wr.write(sifrovanaCelaSprava.length >> 8);
                wr.write(sifrovanaCelaSprava, 0, sifrovanaCelaSprava.length);
            }
            if(signature != null) {
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

    public void spracujDecryptedMessage(byte[] message) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, SignatureException, InvalidKeyException {
        byte[] arr0 = {  message[0], message[1], message[2], message[3] };
        ByteBuffer wrapped0 = ByteBuffer.wrap(arr0);
        int cisloZoSpravy = wrapped0.getInt();
        skontrolujCislovanie(cisloZoSpravy);
        byte[] filteredMessage = Arrays.copyOfRange(message, 5, message.length);
        handlePrijataSprava(message[4],filteredMessage);

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

            if (serverSecurity.verifySignMessage(messageBytes, signature)) {

                //byte[] decryptedMessage = serverSecurity.decrypto(messageBytes);
                byte[] decryptedMessage = serverSecurity.decryptoLargeMessage(messageBytes);

                //System.out.println("USPESNE OVERENIE");
                spracujDecryptedMessage(decryptedMessage);
            } else {
                //System.out.println("neuspesne overenie");
            }


        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public void prihlasenieMojaInicializacia() throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, SignatureException, InvalidKeyException {
        for(int i = 0; i<this.handlers.size();i++){
            if(this.handlers.get(i).id != this.id) {
                //String sprava = this.handlers.get(i).id + "-prihlasenie-" + this.handlers.get(i).meno;
                posliSpravuSId(this.handlers.get(i).id,PRIHLASENIE, this.handlers.get(i).meno.getBytes());
                //String aktualnyText = this.handlers.get(i).id + "-sprava-" + this.handlers.get(i).aktualnaSprava;
                posliSpravuSId(this.handlers.get(i).id,SPRAVA, this.handlers.get(i).aktualnaSprava.getBytes());
            }
        }

    }

    public void handlePrijataSprava(byte typSpravy,byte[] sprava) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, SignatureException, InvalidKeyException {

        if(typSpravy == ODHLASENIE){
            this.handlers.remove(this);
        }
        else if(typSpravy == PRIHLASENIE){
            prihlasenieMojaInicializacia();
            this.meno = new String(sprava);
        }
        else if(typSpravy == SPRAVA){
            this.aktualnaSprava = new String(sprava);
        }
        posliOstatnym(typSpravy, sprava);
    }

}