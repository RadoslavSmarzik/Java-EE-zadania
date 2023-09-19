package com.company;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.security.*;
import java.util.ArrayList;

public class ServerSecurity {
    private PrivateKey myPrivateKey;
    private PublicKey myPublicKey;
    private PublicKey clientPublicKey;

    public ServerSecurity(PrivateKey privateKey, PublicKey publicKey, String clientFileWithPublicKey) throws IOException, ClassNotFoundException {
        this.myPrivateKey = privateKey;
        this.myPublicKey = publicKey;
        this.clientPublicKey = getPublicKeyFromFile(clientFileWithPublicKey);
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

    void setClientPublicKeyFromFile(String fileName) throws IOException, ClassNotFoundException {
        this.clientPublicKey = getPublicKeyFromFile(fileName);
    }

    byte[] signMessage(byte[] message) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException {
        Signature podpis = Signature.getInstance("SHA1withRSA");
        podpis.initSign(myPrivateKey);
        podpis.update(message);
        return podpis.sign();
    }

    public boolean verifySignMessage(byte[] message, byte[] p) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException {
        Signature podpis = Signature.getInstance("SHA1withRSA");
        podpis.initVerify(clientPublicKey);
        podpis.update(message);
        return podpis.verify(p);
    }

    public byte[] decrypto(byte[] s) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher crypto = Cipher.getInstance("RSA");
        crypto.init(Cipher.DECRYPT_MODE, myPrivateKey);
        return crypto.doFinal(s);
    }

    byte[] cryptoSmall(byte[] s) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher crypto = Cipher.getInstance("RSA");
        crypto.init(Cipher.ENCRYPT_MODE, clientPublicKey);
        return crypto.doFinal(s);
    }

    public byte[] decryptoLargeMessage(byte[] msg) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
        ArrayList<byte[]> splittedMsg = split(msg, 256);
        byte[][] result = new byte[splittedMsg.size()][];
        for(int i = 0; i < splittedMsg.size(); i++){
            byte[] decryptedMsg = decrypto(splittedMsg.get(i));
            result[i] = decryptedMsg;
        }
        ByteBuffer byteBuffer = ByteBuffer.allocate(getSize(result));
        for(byte[] i : result){
            byteBuffer.put(i);
        }
        return byteBuffer.array();
    }

    public byte[] cryptoMessage(byte[] bts) throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException {
        if(bts.length < 245){
            return cryptoSmall(bts);
        }
        ArrayList<byte[]> splittedMsg = split(bts, 245);
        byte[][] result = new byte[splittedMsg.size()][];
        for(int i = 0; i < splittedMsg.size(); i++){
            byte[] encryptedMsg = cryptoSmall(splittedMsg.get(i));
            result[i] = encryptedMsg;
        }
        ByteBuffer byteBuffer = ByteBuffer.allocate(getSize(result));
        for(byte[] i : result){
            byteBuffer.put(i);
        }
        return byteBuffer.array();
    }


//POMOCNE FUNKCIE

    public int getSize(byte[][] array){
        int size = 0;
        for(byte[] i : array){
            size += i.length;
        }
        return size;
    }

    public ArrayList<byte[]> split(byte[] data, int maxLength){
        ArrayList<byte[]> result = new ArrayList<>();

        if(data.length > maxLength){
            ByteBuffer bb = ByteBuffer.wrap(data);

            int times = data.length / maxLength;
            int rest = data.length % maxLength;

            for(int i = 0; i < times; i++){
                byte[] temp = new byte[maxLength];
                bb.get(temp, 0, temp.length);
                result.add(temp);
            }
            if(rest != 0){
                byte[] temp = new byte[rest];
                bb.get(temp, 0, temp.length);
                result.add(temp);
            }
        } else {
            result.add(data);
        }
        return result;
    }


}
