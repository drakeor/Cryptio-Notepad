package com.drakeor.cryptio;

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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;


public class MainActivity extends ActionBarActivity {

    public final static String EXTRA_PASSWORD = "com.drakeor.cryptio.PASSWORD";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // FOR TESTING PURPOSES ONLY
        String filename = getFilesDir() + "/cryptio.blob";
        File file = new File(filename);
        if(file.exists())
            file.delete();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Called whenever the user uses the connection button
     */
    public void decryptBlobFile(View view) {

        // Define some of our variables
        EditText passwordText = (EditText) findViewById(R.id.password_text);
        String filename = getFilesDir() + "/cryptio.blob";
        File file = new File(filename);
        String salt_filename = getFilesDir() + "/cryptio_salt";
        File salt_file = new File(salt_filename);

        // Status element!
        TextView passwordStatus = (TextView) findViewById(R.id.passwordStatus);

        try {

            // Grab our salt
            FileInputStream saltFileStream = new FileInputStream(salt_file);
            byte salt[] = new byte[(int)salt_filename.length()];
            System.out.println(salt_filename.length());
            saltFileStream.read(salt);
            saltFileStream.close();

            // Mix in our password
            // Create our final password
            byte[] passwordInBytes = passwordText.getText().toString().getBytes();
            byte[] finalPassword = new byte[salt.length + passwordInBytes.length];
            System.out.println(salt.length + passwordInBytes.length);
            System.arraycopy(salt, 0, finalPassword, 0, salt.length);
            System.arraycopy(passwordInBytes, 0, finalPassword, salt.length, passwordInBytes.length);
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                md.update(finalPassword);
                byte[] finalPasswordHash = md.digest();

                //Byte to Hex (Courtesy of mkyong)
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < finalPasswordHash.length; i++) {
                    sb.append(Integer.toString((finalPasswordHash[i] & 0xff) + 0x100, 16).substring(1));
                }

                System.out.println("Hash Size: " + Integer.toString(finalPasswordHash.length));
                System.out.println("Hex format : " + sb.toString());

                // Retrieve the raw data of the file
                FileInputStream fileInputStream = new FileInputStream(file);
                byte[] bytes = new byte[(int)file.length()];

                // Grab the first 32 bytes of the file for comparison
                BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                bufferedInputStream.read(bytes,0,32);
                bufferedInputStream.close();

                //Byte to Hex (Courtesy of mkyong)
                StringBuffer sb2 = new StringBuffer();
                for (int i = 0; i < bytes.length; i++) {
                    sb2.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
                }

                System.out.println("Hash Size: " + Integer.toString(finalPasswordHash.length));
                System.out.println("Hex format : " + sb2.toString());

                // For now, just print the contents to console!
                // TODO: Pass to an encryption/decryption class
                String message = new String(bytes);
                System.out.println(message);

                if(Arrays.equals(bytes,finalPasswordHash)) {
                    passwordStatus.setText("Your password was correct!");
                } else {
                    passwordStatus.setText("The password was incorrect!");
                }
            } catch(NoSuchAlgorithmException e) {
                e.printStackTrace();
            }


        } catch(FileNotFoundException e) {

            // We couldn't find the blob file!
            System.out.println("Blob file does not exist! Redirecting user to create new blob file.");

            // Redirect the user to the new file creation page!
            Intent intent = new Intent(this, NewBlobActivity.class);
            String requestedPassword = passwordText.getText().toString();
            intent.putExtra(EXTRA_PASSWORD, requestedPassword);
            startActivity(intent);

            e.printStackTrace();

        } catch(IOException e) {
            e.printStackTrace();
        }

    }
}
