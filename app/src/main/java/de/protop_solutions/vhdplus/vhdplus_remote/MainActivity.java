/*
    Name: MainActivity
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
    In this code the main recycler view is managed, the wifi connection is controlled,
    the listener for the "Add Element" button is implemented and this file contains
    the communication between the AddElement Activity
*/

package de.protop_solutions.vhdplus.vhdplus_remote;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.protop_solutions.vhdplus.vhdplus_remote.ElementRecyclerView.Element;
import de.protop_solutions.vhdplus.vhdplus_remote.ElementRecyclerView.ElementListAdapter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    //IP from connect activity
    private String ip;
    //Recycler view with elements
    private RecyclerView recyclerView;
    //Adapter for background functions
    private ElementListAdapter adapter;
    //elements in recycler view
    private ArrayList<Element> elements;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            ip = extras.getString("IP");
        }

        //Open AddActivity after "Add" button pressed
        findViewById(R.id.addButton).setOnClickListener(view -> {
            //Saves elements in recycler view when activity stopped
            //For example when "Add" button pressed or application closed
            //If application reopened -> can restore elements
            try {
                saveElements();
            } catch (IOException e) {
                e.printStackTrace();
            }
            final Intent intent = new Intent(getApplicationContext() , AddActivity.class);
            addActivityResultLauncher.launch(intent);
        });

        //Load last elements in recycler view
        elements = new ArrayList<>();
        try {
            loadElements();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        //Initialize recycler view adapter
        recyclerView = findViewById(R.id.recyclerview);
        adapter = new ElementListAdapter(this, elements);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.getItemAnimator().setChangeDuration(0);
        recyclerView.setAdapter(adapter);
    }

    /**
     * Handles result of add activity and adds element
     */
    ActivityResultLauncher<Intent> addActivityResultLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Element element = new Element();
                    Intent data = result.getData();
                    element.setType(data.getIntExtra("type", 1));
                    element.setHooks(data.getStringArrayListExtra("hooks"));
                    element.setNames(data.getStringArrayListExtra("names"));

                    adapter.addElement(element);
                }
            }
        });

    /**
     * Saves elements of recycler view in Layout.txt
     * @throws IOException
     */
    private void saveElements() throws IOException {
        FileOutputStream outStream = new FileOutputStream(getFilesDir().getPath() + "/Layout.txt");
        ObjectOutputStream objectOutStream = new ObjectOutputStream(outStream);
        objectOutStream.writeInt(elements.size());
        for(Element e:elements) objectOutStream.writeObject(e);
        objectOutStream.close();
    }

    /**
     * Loads elements from Layout.txt and saves them in the elements list
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void loadElements() throws IOException, ClassNotFoundException {
        File file = new File(getFilesDir().getPath() + "/Layout.txt");
        if(!file.exists()) file.createNewFile();
        FileInputStream inStream = new FileInputStream(file);
        ObjectInputStream objectInStream = new ObjectInputStream(inStream);
        int count = objectInStream.readInt();
        elements = new ArrayList<>();
        for (int c=0; c < count; c++) elements.add((Element) objectInStream.readObject());
        objectInStream.close();
    }

    /**
     * Save elements when main activity destroyed
     */
    @Override
    protected void onDestroy()
    {
        try {
            saveElements();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
}