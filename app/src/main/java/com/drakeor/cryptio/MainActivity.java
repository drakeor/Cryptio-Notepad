package com.drakeor.cryptio;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class MainActivity extends ActionBarActivity {

    public final static String EXTRA_PASSWORD = "com.drakeor.cryptio.PASSWORD";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        String filename = getFilesDir() + "/cryptio.blob";
        File file = new File(filename);

        try {

            // Retrieve the raw data of the file
            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] bytes = new byte[(int)file.length()];

            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
            bufferedInputStream.read(bytes,0,bytes.length);
            bufferedInputStream.close();

            // For now, just print the contents to console!
            // TODO: Pass to an encryption/decrption class
            String message = new String(bytes);
            System.out.println(message);

        } catch(FileNotFoundException e) {

            // We couldn't find the blob file!
            System.out.println("Blob file does not exist! Redirecting user to create new blob file.");

            // Redirect the user to the new file creation page!
            Intent intent = new Intent(this, NewBlobActivity.class);
            EditText passwordText = (EditText) findViewById(R.id.password_text);
            String requestedPassword = passwordText.getText().toString();
            intent.putExtra(EXTRA_PASSWORD, requestedPassword);
            startActivity(intent);

        } catch(IOException e) {
            e.printStackTrace();
        }

    }
}
