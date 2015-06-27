package com.drakeor.cryptio;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**
 * A library class that
 * handles most of the encryption related tasks.
 * We are deriving from an Activity class even though
 * this isn't actually an activity
 */
public class EncryptionManager {

    // Filepaths
    public String blobFilename;
    public String saltFilename;

    /**
     * Constructor
     */
    public EncryptionManager(File rootDirectory) {
        blobFilename = rootDirectory + "/cryptio.blob";
        saltFilename = rootDirectory + "/cryption_salt";
    }

    /* This function generates a new salt file for us to use.
     * It will return true if a new salt file was created and
     * false if the process failed at all.
     */
    public boolean generateNewSaltFile() {

        // Generate a secure salt and write it into our salt file.
        // Note that this function can be a bit expensive...
        File saltFile = new File(saltFilename);
        SecureRandom secureRandom = new SecureRandom();
        byte saltSeed[] = secureRandom.generateSeed(16);

        // Attempt to save our salt file
        try {
            FileOutputStream saltFileStream = new FileOutputStream(saltFile, false);
            saltFileStream.write(saltSeed);
            saltFileStream.close();
            return true;
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * This function returns our salt file.
     * It will return an empty array if it is unable to return the salt file
     */
    public byte[] getSalt() {
        try {
            File saltFile = new File(saltFilename);
            FileInputStream saltFileStream2 = new FileInputStream(saltFile);
            byte salt[] = new byte[(int)saltFile.length()];
            System.out.println(saltFile.length());
            saltFileStream2.read(salt);
            saltFileStream2.close();
            return salt;
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    /**
     * Byte to Hex (Courtesy of mkyong)
     * I haven't looked into this function yet, but I'm curious how it works
     */
    public String byteToHex(byte[] input) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < input.length; i++) {
            sb.append(Integer.toString((input[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

    /**
     * This function gets our password hash to use as our decryption key
     * for the blob file
     */
    public byte[] getPasswordHash(byte[] password) {

        // First, get our salt!
        byte[] passwordSalt = getSalt();
        if(passwordSalt.length < 1) {
            System.out.println("Could not get the password salt!");
            return new byte[0];
        }

        // Combine our salt and password
        byte[] finalPassword = new byte[passwordSalt.length + password.length];
        System.out.println(passwordSalt.length + password.length);
        System.arraycopy(passwordSalt, 0, finalPassword, 0, passwordSalt.length);
        System.arraycopy(password, 0, finalPassword, passwordSalt.length, password.length);

        // Built our final password hash
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(finalPassword);
            byte[] finalHash = md.digest();
            System.out.println("Password Hash: " + byteToHex(finalHash) );
            return finalHash;

        } catch(NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return new byte[0];
    }

    /**
     * THis function gets the blob file
     * @param passwordHash Hash returned by getPasswordHash
     * @return A null array if the decryption key was incorrect. Decrypted contents if it was correct.
     */
    public byte[] getBlobFile(byte[] passwordHash) {

        try {
            // Retrieve the raw data of the file
            File file = new File(blobFilename);
            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] bytes = new byte[(int)file.length()];
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
            bufferedInputStream.read(bytes);
            bufferedInputStream.close();

            // Decrypt the file
            SecretKeySpec secretKeySpec = new SecretKeySpec(passwordHash, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            byte[] decryptedData = cipher.doFinal(bytes);
            System.out.println("BAK: " + byteToHex(decryptedData));
            return decryptedData;

        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new byte[0];
    }

    public void saveBlobFile(byte[] passwordHash, byte[] newText) {

        // Combine our passwordhash and new text
        byte[] content = new byte[passwordHash.length + newText.length];
        System.arraycopy(passwordHash, 0, content, 0, passwordHash.length);
        System.arraycopy(newText, 0, content, passwordHash.length, newText.length);

        // Encrypt the file!
        SecretKeySpec secretKeySpec = new SecretKeySpec(passwordHash, "AES");
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] encryptedBlob = cipher.doFinal(content);

            // Write our new blob file!
            File blobFile = new File(blobFilename);
            FileOutputStream saltFileStream = new FileOutputStream(blobFile, false);
            saltFileStream.write(encryptedBlob);
            saltFileStream.close();

        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Creates a new encrypted blob file to store data!
     * @param passwordHash Hash returned by getPasswordHash
     */
    public void newBlobFile(byte[] passwordHash) {

        // Make some example text for our file!
        String exampleText = new String("Example text here!");
        byte[] exampleBytes = exampleText.getBytes();
        byte[] content = new byte[passwordHash.length + exampleBytes.length];
        System.arraycopy(passwordHash, 0, content, 0, passwordHash.length);
        System.arraycopy(exampleBytes, 0, content, passwordHash.length, exampleBytes.length);

        SecretKeySpec secretKeySpec = new SecretKeySpec(passwordHash, "AES");
        try {

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] encryptedBlob = cipher.doFinal(content);
            System.out.println("OLD: " + byteToHex(content));
            System.out.println("NEW: " + byteToHex(encryptedBlob));

           /* Cipher cipher2 = Cipher.getInstance("AES");
            cipher2.init(Cipher.DECRYPT_MODE, secretKeySpec);
            byte[] decryptedData = cipher2.doFinal(encryptedBlob);
            System.out.println("BAK: " + byteToHex(decryptedData));*/

            File blobFile = new File(blobFilename);
            FileOutputStream saltFileStream = new FileOutputStream(blobFile, false);
            saltFileStream.write(encryptedBlob);
            saltFileStream.close();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
