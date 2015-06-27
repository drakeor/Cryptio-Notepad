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

        if(password1.getText().toString().equals(password2.getText().toString())) {

            // Create our new encrypted file
            String filename = getFilesDir() + "/cryptio.blob";
            System.out.println(filename);
            File file = new File(filename);

            // Create our new salt file
            String salt_filename = getFilesDir() + "/cryptio_salt";
            File salt_file = new File(salt_filename);

            try {

                // Generate a secure salt and write it into our salt file.
                SecureRandom secureRandom = new SecureRandom();
                byte salt2[] = secureRandom.generateSeed(16);
                FileOutputStream saltFileStream = new FileOutputStream(salt_file, false);
                saltFileStream.write(salt2);
                saltFileStream.close();

                // Reopen our salt
                FileInputStream saltFileStream2 = new FileInputStream(salt_file);
                byte salt[] = new byte[(int)salt_filename.length()];
                System.out.println(salt_filename.length());
                saltFileStream2.read(salt);
                saltFileStream2.close();

                // Create our final password
                byte[] passwordInBytes = password1.getText().toString().getBytes();
                byte[] finalPassword = new byte[salt.length + passwordInBytes.length];
                System.out.println(salt.length + passwordInBytes.length);
                System.arraycopy(salt, 0, finalPassword, 0, salt.length);
                System.arraycopy(passwordInBytes, 0, finalPassword, salt.length, passwordInBytes.length);

                // Write into our file
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

                    // Write the hash to the file
                    FileOutputStream fileOutputStream = new FileOutputStream(file, false);
                    fileOutputStream.write(finalPasswordHash,0,32);
                    fileOutputStream.close();

                    // End our activity
                    finish();
                } catch(NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }


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
