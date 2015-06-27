package com.drakeor.cryptio;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;


public class NewBlobActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // By calling this first, we avoid the null pointer error caused by
        // trying to get the edit box before calling this
        setContentView(R.layout.activity_new_blob);

        // Retrieve our message from the intent
        Intent intent = getIntent();
        String passedPassword = intent.getStringExtra(MainActivity.EXTRA_PASSWORD);

        // Replace the password field with the password passed to us!
        EditText editBox = (EditText) findViewById(R.id.password_create);
        editBox.setText(passedPassword);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_blob, menu);
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

    public void createNewAccount(View view) {

        // Get both of our passwords
        EditText password1 = (EditText) findViewById(R.id.password_create);
        EditText password2 = (EditText) findViewById(R.id.password_verify);
        EncryptionManager encryptionManager = new EncryptionManager(getFilesDir());

        if(password1.getText().toString().equals(password2.getText().toString())) {
            try {
                // Generate a secure salt and write it into our salt file.
                encryptionManager.generateNewSaltFile();
                byte[] salt = encryptionManager.getSalt();

                // Create our final password
                byte[] finalPasswordHash = encryptionManager.getPasswordHash(password1.getText().toString().getBytes());

                // Write the hash to the file
                File file = new File(encryptionManager.blobFilename);
                FileOutputStream fileOutputStream = new FileOutputStream(file, false);
                fileOutputStream.write(finalPasswordHash,0,32);
                fileOutputStream.close();

                // End our activity
                finish();

            // Handle errors
            } catch(FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            // Display an error message to the user
            TextView textView = (TextView) findViewById(R.id.newBlobInfoText);
            textView.setText("Les mots de passe ne correspondent pas.");
        }
    }
}
