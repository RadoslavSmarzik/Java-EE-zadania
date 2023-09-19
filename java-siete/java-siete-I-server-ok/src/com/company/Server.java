package com.company;

import java.io.*;
import java.net.*;
import java.util.ArrayList;


public class Server {
    int port;
    int id = 0;
    ServerSocket serverSocket;
    ArrayList<Socket> clients = new ArrayList<>();
    ArrayList<HandlerThread> handlers = new ArrayList<>();

    Server(int portNumber){
        this.port = portNumber;
    }

    public void runServer() throws IOException {
        this.serverSocket = new ServerSocket(port);
        while(true){
            System.out.println("Cakam na klienta");
            Socket socket = serverSocket.accept();
            System.out.println("Nove spojenie");
            clients.add(socket);
            HandlerThread handler = new HandlerThread(this.id, socket, handlers);
            handlers.add(handler);
            id++;
            handler.start();
        }
    }



    public static void main(String[] args) throws IOException {
        Server server = new Server(2004);
        server.runServer();
    }
}