package com.javarush.task.task30.task3008;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public class Message implements Serializable {
    private final MessageType type;
    private String data;

    public Message(MessageType type) {
        this.type = type;
        this.data = null;
    }


    public Message(MessageType type, String data) {
        this.type = type;
        this.data = data;
    }

    public String getData() {
        return data;
    }

    public MessageType getType() {
        return type;
    }

    public Message Shifr() {
        MessageDigest messageDigest = null;
        byte[] digest = new byte[0];
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(this.data.getBytes());
            digest = messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        BigInteger bigInt = new BigInteger(1, digest);
        String md5Hex = bigInt.toString(16);

        while( md5Hex.length() < 32 ){
            md5Hex = "0" + md5Hex;
        }
        this.data = md5Hex;
        return this;
    }

    public Message Rashifr() {
        //12345678
        String password = "";
        try (FileReader reader = new FileReader("password.txt")) {
            int c;
            while ((c = reader.read()) != -1) {

                password += (char) c;
                // System.out.print((char)c);
            }
        } catch (IOException ex) {

            System.out.println(ex.getMessage());
        }
        try {
            char[] pass = password.toCharArray();
            String line = this.getData();
            String line1 = line.substring(line.indexOf(':') + 2);
            byte[] keyBytes = password.getBytes("UTF-8");
            DESKeySpec keySpec = new DESKeySpec(keyBytes);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey key = keyFactory.generateSecret(keySpec);
            Cipher decryptCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            decryptCipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decryptedResponse = decryptCipher.doFinal(Base64.getDecoder().decode(line1));
            String decryptedResponseString = new String(decryptedResponse, "UTF-8");
            this.data = line.substring(0, line.indexOf(':') + 2) + decryptedResponseString;
        } catch (Exception e) {

        }
        return this;


    }
}