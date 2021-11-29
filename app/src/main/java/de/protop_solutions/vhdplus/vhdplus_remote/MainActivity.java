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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.protop_solutions.vhdplus.vhdplus_remote.ElementRecyclerView.Element;
import de.protop_solutions.vhdplus.vhdplus_remote.ElementRecyclerView.ElementListAdapter;
import de.protop_solutions.vhdplus.vhdplus_remote.RecyclerViewCallbacks.DragAndDropCallback;
import de.protop_solutions.vhdplus.vhdplus_remote.RecyclerViewCallbacks.SwipeCallback;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

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

    SwipeCallback swipeToDeleteCallback;
    SwipeCallback swipeToEditCallback;
    private ConstraintLayout layout;

    private boolean edit = false;
    private int editPosition;
    private Element lastElement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        layout = findViewById(R.id.container);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            ip = extras.getString("IP").trim();
            if(extras.containsKey("Port")){
                String port = extras.getString("Port");
                if (port.length() > 0)
                    ip += ":" + port.trim();
            }
        }

        //Open AddActivity after "Add" button pressed
        findViewById(R.id.addButton).setOnClickListener(view -> {
            startAddActivity(null);
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
        adapter = new ElementListAdapter(this, elements, ip);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.getItemAnimator().setChangeDuration(0);
        recyclerView.setAdapter(adapter);

        //Add callbacks
        enableSwipeToDeleteAndUndo();
        enableSwipeToEdit();
        enableDragAndDrop();

        //Start listening for WiFi module
        adapter.onStart();
    }

    private void startAddActivity(Element item){
        //Saves elements in recycler view when activity stopped
        //For example when "Add" button pressed or application closed
        //If application reopened -> can restore elements
        try {
            saveElements();
        } catch (IOException e) {
            e.printStackTrace();
        }
        final Intent intent = new Intent(getApplicationContext() , AddActivity.class);
        intent.putExtra("edit", item);
        addActivityResultLauncher.launch(intent);
    }

    /**
     * Handles result of add activity and adds element
     * If element edited, inserts element in last position
     */
    ActivityResultLauncher<Intent> addActivityResultLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK || edit) {
                    Element element = new Element();
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        element.setType(data.getIntExtra("type", 1));
                        element.setHooks(data.getStringArrayListExtra("hooks"));
                        element.setNames(data.getStringArrayListExtra("names"));
                    } else element = lastElement;
                    if (edit) adapter.insertElement(element, editPosition);
                    else adapter.addElement(element);
                }
                edit = false;
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
     * Attach swipe left callback
     * Deletes elememt and shows snackbar to undo
     */
    private void enableSwipeToDeleteAndUndo() {
        swipeToDeleteCallback = new SwipeCallback(this, 0) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                final int position = viewHolder.getAdapterPosition();
                final Element item = adapter.getElements().get(position);

                adapter.removeElement(position);

                Snackbar snackbar = Snackbar.make(layout, "Element was removed", Snackbar.LENGTH_LONG);
                snackbar.setAction("Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        adapter.insertElement(item, position);
                        recyclerView.scrollToPosition(position);
                    }
                });

                snackbar.setActionTextColor(getResources().getColor(R.color.blue_500));
                snackbar.show();
            }
        };

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchhelper.attachToRecyclerView(recyclerView);
    }

    /**
     * Attach swipe right callback
     * Removes element to edit and saves removed item to restore if not edited
     * Start add activity with element to edit
     */
    private void enableSwipeToEdit() {
        swipeToEditCallback = new SwipeCallback(this, 1) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

                final int position = viewHolder.getAdapterPosition();
                final Element item = adapter.getElements().get(position);

                edit = true;
                editPosition = position;
                lastElement = item;
                adapter.removeElement(position);

                startAddActivity(item);

            }
        };

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToEditCallback);
        itemTouchhelper.attachToRecyclerView(recyclerView);
    }

    /**
     * Attach drag and drop callback
     */
    private void enableDragAndDrop() {
        DragAndDropCallback dragAndDropCallback = new DragAndDropCallback(this, adapter);
        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(dragAndDropCallback);
        itemTouchhelper.attachToRecyclerView(recyclerView);
    }

    /**
     * Starts listening to wifi module for new element data
     */
    @Override
    protected void onStart() {
        adapter.onStart();
        super.onStart();
    }

    /**
     * Stops listening to wifi module for new element data
     */
    @Override
    protected void onPause(){
        adapter.onStop();
        super.onPause();
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
        swipeToDeleteCallback.destroy();
        swipeToEditCallback.destroy();
        super.onDestroy();
    }
}