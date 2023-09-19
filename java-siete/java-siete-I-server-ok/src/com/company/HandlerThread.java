package com.company;


import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class HandlerThread extends Thread{

    byte PRIHLASENIE = 0;
    byte ODHLASENIE = 1;
    byte SPRAVA = 2;
    byte KRESLENIE_STLACENIE = 3;
    byte KRESLENIE_TAHANIE = 4;
    byte ZMAZ_HISTORIU = 5;
    byte ZMAZ_PLOCHU = 6;
    byte OBRAZOK = 7;


    int id;
    Socket socket = new Socket();
    OutputStream wr;
    InputStream rd;
    ArrayList<HandlerThread> handlers;
    String meno;
    String aktualnaSprava = "";

    HandlerThread(int id, Socket socket, ArrayList<HandlerThread> handlers) throws IOException {
        this.id = id;
        this.wr = socket.getOutputStream();
        this.rd = socket.getInputStream();
        this.handlers = handlers;
    }

    @Override
    public void run() {
        while(prijmiSpravu());
        System.out.println("Koniec rozhovoru.");
        ostatnyOdhlasenie();
        this.handlers.remove(this);
        try {
            socket.close();
        } catch (Exception e) { e.printStackTrace(); }
        //System.exit(0);

    }

    public void ostatnyOdhlasenie(){
        posliOstatnym(ODHLASENIE, "".getBytes());
    }


    public void posliOstatnym(byte typSpravy, byte[] sprava){
        for(int i = 0; i < handlers.size(); i++){
            if(handlers.get(i).socket != this.socket){
                handlers.get(i).posliSpravuSId(this.id, typSpravy, sprava);
            }
        }

    }

    public void posliSpravuSId(int id, byte typSpravy, byte[] sprava){
        byte bts[] = sprava;
        try {
            wr.write(id & 255);
            wr.write(id >> 8);

            wr.write(typSpravy);
            if(sprava != null) {
                wr.write(bts.length & 255);
                wr.write(bts.length >> 8);
                wr.write(bts, 0, bts.length);
            }

            wr.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public void posliSpravu(String message) {
        byte bts[] = message.getBytes();
        try {
            wr.write(bts.length & 255);
            wr.write(bts.length >> 8);
            wr.write(bts, 0, bts.length);
            wr.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean prijmiSpravu() {
        try {
            byte typSpravy = (byte) rd.read();
            if(typSpravy == OBRAZOK){
                byte[] sizeAr = new byte[4];
                rd.read(sizeAr);
                int nbts = ByteBuffer.wrap(sizeAr).asIntBuffer().get();
                byte bts[] = new byte[nbts];
                int i = 0; // how many bytes did we read so far
                do {
                    int j = rd.read(bts, i, bts.length - i);
                    if (j > 0) i += j;
                    else break;
                } while (i < bts.length);
                handlePrijataSprava(typSpravy, bts);
                return true;
            }
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
            System.out.println(new String(bts));
            handlePrijataSprava(typSpravy,bts);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public void prihlasenieMojaInicializacia(){
        for(int i = 0; i<this.handlers.size();i++){
            if(this.handlers.get(i).id != this.id) {
                //String sprava = this.handlers.get(i).id + "-prihlasenie-" + this.handlers.get(i).meno;
                posliSpravuSId(this.handlers.get(i).id,PRIHLASENIE, this.handlers.get(i).meno.getBytes());
                //String aktualnyText = this.handlers.get(i).id + "-sprava-" + this.handlers.get(i).aktualnaSprava;
                posliSpravuSId(this.handlers.get(i).id,SPRAVA, this.handlers.get(i).aktualnaSprava.getBytes());
            }
        }

    }

    public void handlePrijataSprava(byte typSpravy,byte[] sprava){
        //String[] parsedSprava = sprava.split("-", 2);

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