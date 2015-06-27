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
        TextView passwordStatus = (TextView) findViewById(R.id.passwordStatus);
        EncryptionManager encryptionManager = new EncryptionManager(getFilesDir());

        try {
            // Grab our salt
            byte salt[] = encryptionManager.getSalt();

            // Mix in our password
            // Create our final password
            byte[] finalPasswordHash = encryptionManager.getPasswordHash(passwordText.getText().toString().getBytes());

            // Retrieve the raw data of the file
            File file = new File(encryptionManager.blobFilename);
            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] bytes = new byte[(int)file.length()];

            // Grab the first 32 bytes of the file for comparison
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
            bufferedInputStream.read(bytes,0,32);
            bufferedInputStream.close();
            System.out.println("HASH: " + encryptionManager.byteToHex(bytes));

            // Lets just compare the passwords
            if(Arrays.equals(bytes, finalPasswordHash)) {
                passwordStatus.setText("Your password was correct!");
            } else {
                passwordStatus.setText("The password was incorrect!");
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
