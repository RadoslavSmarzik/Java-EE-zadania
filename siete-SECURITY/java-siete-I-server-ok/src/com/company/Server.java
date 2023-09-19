package com.company;

import java.io.*;
import java.net.*;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;


public class Server {
    int port;
    int id = 0;
    ServerSocket serverSocket;
    ArrayList<Socket> clients = new ArrayList<>();
    ArrayList<HandlerThread> handlers = new ArrayList<>();
    PrivateKey myPrivateKey;
    PublicKey myPublicKey;

    Server(int portNumber) throws IOException, ClassNotFoundException {
        this.port = portNumber;
        this.myPrivateKey = getPrivateKeyFromFile("src\\server_private.txt");
        this.myPublicKey = getPublicKeyFromFile("src\\server_public.txt");
    }

    public void runServer() throws IOException, ClassNotFoundException {
        this.serverSocket = new ServerSocket(port);
        while(true){
            System.out.println("Cakam na klienta");
            Socket socket = serverSocket.accept();
            System.out.println("Nove spojenie");
            clients.add(socket);
            //HandlerThread handler = new HandlerThread(this.id, socket, handlers, myPrivateKey, myPublicKey, "src\\client_public.txt");
            HandlerThread handler = new HandlerThread(this.id, socket, handlers, myPrivateKey, myPublicKey, "src\\keys\\"+id+"_public.txt");
            handlers.add(handler);
            id++;
            handler.start();
        }
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Server server = new Server(2004);
        server.runServer();
    }

    public java.security.PrivateKey getPrivateKeyFromFile(String file) throws IOException, ClassNotFoundException {
        FileInputStream fileInputStream = new FileInputStream(file);
        ObjectInputStream in = new ObjectInputStream(fileInputStream);
        try {
            PrivateKey privateKey = (PrivateKey) in.readObject();
            return privateKey;
        } catch (Exception e) {
            throw e;
        } finally {
            fileInputStream.close();
            in.close();
        }
    }

    public PublicKey getPublicKeyFromFile(String file) throws IOException, ClassNotFoundException {
        FileInputStream fileInputStream = new FileInputStream(file);
        ObjectInputStream in = new ObjectInputStream(fileInputStream);
        try {
            PublicKey publicKey = (PublicKey) in.readObject();
            return publicKey;
        } catch (Exception e) {
            throw e;
        } finally {
            fileInputStream.close();
            in.close();
        }
    }
}