/*
    Name: AddActivity
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
    In this code the "New Element" selector and the "Element Settings" List is managed.
    Also the communication to the MainActivity is implemented
*/

package de.protop_solutions.vhdplus.vhdplus_remote;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.protop_solutions.vhdplus.vhdplus_remote.ElementRecyclerView.Element;
import de.protop_solutions.vhdplus.vhdplus_remote.SettingRecyclerView.Setting;
import de.protop_solutions.vhdplus.vhdplus_remote.SettingRecyclerView.SettingListAdapter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddActivity extends AppCompatActivity {

    private Spinner typeSpinner;
    private RecyclerView recyclerView;
    private SettingListAdapter adapter;
    private ArrayList<Setting> settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        Button saveButton = (Button) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("type", typeSpinner.getSelectedItemPosition()+1);
                resultIntent.putExtra("names", adapter.getNames());
                resultIntent.putExtra("hooks", adapter.getHooks());
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });

        typeSpinner = (Spinner) findViewById(R.id.typeSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.element_array, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        typeSpinner.setAdapter(adapter);

        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                createList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                createList();
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.settingRecyclerView);
        createList();
    }

    /**
     * Loads settings depending on element from element_settings.xml
     * Saves settings in settings list
     * Initializes recycler view
     */
    private void createList() {
        settings = new ArrayList<>();
        List<String> s = new ArrayList<>();
        switch (typeSpinner.getSelectedItemPosition()+1) {
            case Element.TYPE_BUTTON:
                s = Arrays.asList(getResources().getStringArray(R.array.label_button_array));
                break;
            case Element.TYPE_BUTTON3:
                s = Arrays.asList(getResources().getStringArray(R.array.button3_array));
                break;
            case Element.TYPE_LED:
                s = Arrays.asList(getResources().getStringArray(R.array.label_led_array));
                break;
            case Element.TYPE_LED10:
                s = Arrays.asList(getResources().getStringArray(R.array.led10_array));
                break;
            case Element.TYPE_SWITCH:
                s = Arrays.asList(getResources().getStringArray(R.array.label_switch_array));
                break;
            case Element.TYPE_SWITCH3:
                s = Arrays.asList(getResources().getStringArray(R.array.switch3_array));
                break;
            case Element.TYPE_RGBLED:
                s = Arrays.asList(getResources().getStringArray(R.array.label_rgb_led_array));
                break;
            case Element.TYPE_RGBLED10:
                s = Arrays.asList(getResources().getStringArray(R.array.rgb_led10_array));
                break;
            case Element.TYPE_DISPLAY:
                s = Arrays.asList(getResources().getStringArray(R.array.value_display_array));
                break;
            case Element.TYPE_SLIDER:
                s = Arrays.asList(getResources().getStringArray(R.array.label_slider_array));
                break;
            case Element.TYPE_JOYSTICK:
                s = Arrays.asList(getResources().getStringArray(R.array.joystick_array));
                break;
            case Element.TYPE_CONSOLE:
                s = Arrays.asList(getResources().getStringArray(R.array.console_array));
                break;
        }
        addSettings(s);

        //Initialize recycler view adapter
        adapter = new SettingListAdapter(this, settings, typeSpinner.getSelectedItemPosition()+1);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getApplication()));
        recyclerView.setAdapter(adapter);
    }

    //Create setting list from string array from element_settings.xml
    private void addSettings(List<String> s){
        for (int i = 0; i < s.size() / 2; i++) {
            Setting setting = new Setting();
            setting.setName(s.get(i * 2));
            setting.setValue(s.get(i * 2 + 1));
            settings.add(setting);
        }
    }
}