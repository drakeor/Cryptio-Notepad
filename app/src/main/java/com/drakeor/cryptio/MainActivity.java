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
    public final static String EXTRA_PASSWORDHASH = "com.drakeor.cryptio.PASSWORD_HASH";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // FOR TESTING PURPOSES ONLY
       /* String filename = getFilesDir() + "/cryptio.blob";
        File file = new File(filename);
        if(file.exists())
            file.delete();*/
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
        File blobFile = new File(encryptionManager.blobFilename);

        if(blobFile.exists()) {
            // Grab our salt
            byte salt[] = encryptionManager.getSalt();

            // Mix in our password
            // Create our final password
            byte[] finalPasswordHash = encryptionManager.getPasswordHash(passwordText.getText().toString().getBytes());

            // Retrieve the raw data of the file
            byte[] bytes = encryptionManager.getBlobFile(finalPasswordHash);
            System.out.println("HASH: " + encryptionManager.byteToHex(bytes));

            if(bytes.length < 33) {
                passwordStatus.setText("The password was incorrect!");
            } else {
                byte[] passwordHash = Arrays.copyOfRange(bytes, 0, 32);
                // Lets just compare the passwords
                if(Arrays.equals(passwordHash, finalPasswordHash)) {
                    passwordStatus.setText("Your password was correct!");

                    // We want to clear out our password.. security things y'know.
                    passwordText.setText("");

                    // Redirect the user to the edit file page!
                    Intent intent = new Intent(this, NotepadActivity.class);
                    intent.putExtra(EXTRA_PASSWORDHASH, finalPasswordHash);
                    startActivity(intent);

                } else {
                    passwordStatus.setText("The password was incorrect!");
                }
            }
        } else {

            // We couldn't find the blob file!
            System.out.println("Blob file does not exist! Redirecting user to create new blob file.");

            // Redirect the user to the new file creation page!
            Intent intent = new Intent(this, NewBlobActivity.class);
            String requestedPassword = passwordText.getText().toString();
            intent.putExtra(EXTRA_PASSWORD, requestedPassword);
            startActivity(intent);

        }
    }
}
