/*
    Name: ConnectActivity
    Rev: 1.0
    Creator: Leon Beier
    Date: 08.11.2021
    Copyright (c) 2021 Protop Solutions UG. All right reserved.

    Permission is hereby granted, free of charge, to any person obtaining a copy of
    this java code and associated documentation files (the "Java Code"), to deal in the
    Java Code without restriction, including without limitation the rights to use,
    copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
    Java Code, and to permit persons to whom the Java Code is furnished to do so,
    subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Java Code.

    THE Java Code IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
    FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
    COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
    AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
    WITH THE Java Code OR THE USE OR OTHER DEALINGS IN THE Java Code.

    Description:
    In this code the last IP and/or Port settings are loaded and the new settings are saved
*/

package de.protop_solutions.vhdplus.vhdplus_remote;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ConnectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        //Open MainActivity after "Connect" button pressed
        //Adds IP Address to intent extras
        findViewById(R.id.connectButton).setOnClickListener(view -> {
            final Intent intent = new Intent(getApplicationContext() , MainActivity.class);
            intent.putExtra("IP", ((EditText) findViewById(R.id.editTextIP)).getText().toString());
            startActivity(intent);
        });

        //Set editTextIP text to last IP address
        try {
            loadLastIP();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop(){
        super.onStop();
        //Saves current editTextIP text when activity stopped
        //For example when "Connect" button pressed or application closed
        //If application reopened -> can restore text
        try {
            saveLastIP();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves IP address from editTextIP in IP.txt
     * @throws IOException
     */
    private void saveLastIP() throws IOException {
        File file = new File(getFilesDir().getPath() + "/IP.txt");
        if(!file.exists()) file.createNewFile();
        FileOutputStream stream = new FileOutputStream(file);
        try {
            stream.write(((EditText)findViewById(R.id.editTextIP)).getText().toString().getBytes());
        } finally {
            stream.close();
        }
    }

    /**
     * Loads IP address from IP.txt and sets editTextIP text
     * @throws IOException
     */
    private void loadLastIP() throws IOException {
        File file = new File(getFilesDir().getPath() + "/IP.txt");
        if(file.exists()) {
            FileInputStream stream = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            stream.read(data);
            stream.close();
            ((EditText)findViewById(R.id.editTextIP)).setText(new String(data, StandardCharsets.UTF_8));
        }
    }
}