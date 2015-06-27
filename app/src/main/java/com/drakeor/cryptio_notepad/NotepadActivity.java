package com.drakeor.cryptio_notepad;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import java.util.Arrays;


public class NotepadActivity extends ActionBarActivity {

    public byte[] passwordHash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notepad);

        // Receive our message from the intent
        Intent intent = getIntent();
        passwordHash = intent.getByteArrayExtra(MainActivity.EXTRA_PASSWORDHASH);

        // Replace the text
        EncryptionManager encryptionManager = new EncryptionManager(getFilesDir());
        byte[] bytes = encryptionManager.getBlobFile(passwordHash);
        System.out.println("HASH: " + encryptionManager.byteToHex(bytes));
        byte[] message = Arrays.copyOfRange(bytes, 32, bytes.length);
        String messageString = new String(message);

        // Add it to our notepad field
        EditText editBox = (EditText) findViewById(R.id.mainText);
        editBox.setText(messageString);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_notepad, menu);
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

    public void saveAndExit(View view) {
        EncryptionManager encryptionManager = new EncryptionManager(getFilesDir());
        EditText editBox = (EditText) findViewById(R.id.mainText);
        byte[] newContent = editBox.getText().toString().getBytes();
        encryptionManager.saveBlobFile(passwordHash, newContent);
        finish();
    }
}
