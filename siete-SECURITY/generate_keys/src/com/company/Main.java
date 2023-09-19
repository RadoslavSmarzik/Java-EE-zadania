package com.company;

import java.io.*;
import java.security.*;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
        /*Scanner s = new Scanner(System.in);
        System.out.println("Zadajte nazov:");
        String fileName = s.next();
        generateNewKeysToFiles(fileName);*/
        for(int i = 0; i < 11; i++){
            generateNewKeysToFiles(i+"");
        }
    }

    public static void generateNewKeysToFiles(String name) throws NoSuchAlgorithmException, IOException {
        KeyPair key = KeyPairGenerator.getInstance("RSA").generateKeyPair();

        FileOutputStream fileOutputStream = new FileOutputStream("src\\" + name + "_public.txt");
        ObjectOutputStream out = new ObjectOutputStream(fileOutputStream);
        out.writeObject(key.getPublic());
        fileOutputStream.close();
        out.close();

        FileOutputStream fileOutputStream2 = new FileOutputStream("src\\" + name + "_private.txt");
        ObjectOutputStream out2 = new ObjectOutputStream(fileOutputStream2);
        out2.writeObject(key.getPrivate());
        fileOutputStream2.close();
        out2.close();
    }

}

