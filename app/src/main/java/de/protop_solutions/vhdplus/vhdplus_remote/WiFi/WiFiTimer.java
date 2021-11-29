/*
    Name: WiFiTimer
    Rev: 1.0
    Creator: Leon Beier
    Date: 28.11.2021
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
    This code implements a timer that updates the elements with values from the wifi module
*/

package de.protop_solutions.vhdplus.vhdplus_remote.WiFi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;
import android.widget.Adapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import de.protop_solutions.vhdplus.vhdplus_remote.ElementRecyclerView.Element;
import de.protop_solutions.vhdplus.vhdplus_remote.ElementRecyclerView.ElementListAdapter;

public class WiFiTimer {

    //Context of MainActivity
    private Context context;
    //Used to get and change element list
    ElementListAdapter adapter;

    //Object that handles requests to WiFi module
    private WiFiConnection wifi;
    //Defined IP of wifi module
    private String ip;

    //Timer to update elements
    Timer timer;
    //Task that requests element data
    TimerTask timerTask;
    //Used for timer task
    final Handler handler = new Handler();

    //Receives response from wifi module
    BroadcastReceiver wifiReceiver;

    //Time after task is executed when connection is present
    int defaultDelayLength;
    //Time after task is executed
    int delayLength;

    //Used to check for connection problem
    long lastRequestTime;
    long lastResponseTime;

    //Used to check if connection problem changed
    boolean noResponse;
    boolean noInternet;

    public WiFiTimer(Context context, ElementListAdapter adapter, WiFiConnection wifi, String ip, int delayLength){
        this.context = context;
        this.adapter = adapter;
        this.wifi = wifi;
        this.delayLength = delayLength;
        this.defaultDelayLength = delayLength;
        this.ip = ip;

        lastResponseTime = System.currentTimeMillis();
        noResponse = false;
        noInternet = false;
    }

    /**
     * Starts timer and receiver for response
     */
    public void startWiFiConnection(){
        if (timer == null) {
            timer = new Timer();
            initializeTimerTask();
            timer.schedule(timerTask, delayLength, delayLength);

            wifiReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    boolean error = intent.getBooleanExtra("error", true);
                    String response = "";
                    if (!error) response = intent.getStringExtra("response");
                    responseHandler(error, response);
                }
            };
            LocalBroadcastManager.getInstance(context).registerReceiver(wifiReceiver, new IntentFilter("wifi"));
        }
    }

    /**
     * Stops timer and receiver for response
     */
    public void stopWiFiConnection(){
        if (timer != null) {
            timer.cancel();
            timer = null;

            LocalBroadcastManager.getInstance(context).unregisterReceiver(wifiReceiver);
        }
    }

    /**
     * Restarts timer with new delayLength
     */
    public void updateDelayLength(){
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        timer = new Timer();
        initializeTimerTask();
        timer.schedule(timerTask, delayLength, delayLength);
    }

    /**
     * Initializes task that requests for data from wifi module
     */
    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        if (WiFiConnection.listHasReceivingElement(adapter.getElements())) {
                            if (wifi.checkConnection()) {
                                if (noInternet)
                                    Toast.makeText(context, "Connected With Internet!", Toast.LENGTH_LONG).show();
                                noInternet = false;
                                if (lastRequestTime - lastResponseTime > delayLength) {
                                    handleReceiveError();
                                }
                                lastRequestTime = System.currentTimeMillis();
                                wifi.requestData(ip);
                            } else if (!noInternet) {
                                Toast.makeText(context, "No Internet Connection!", Toast.LENGTH_LONG).show();
                                noInternet = true;
                            }
                        }
                    }
                });
            }
        };
    }

    /**
     * Increases delay length after connection error
     * Displays error message after 4 errors
     */
    void handleReceiveError(){
        if (delayLength < 10*defaultDelayLength){
            delayLength += defaultDelayLength;
            updateDelayLength();
        }

        if (delayLength == 4*defaultDelayLength){
            Toast.makeText(context, "No Response From WiFi Module!", Toast.LENGTH_LONG).show();
            noResponse = true;
        }
    }

    /**
     * Handles response message and updates elements
     * @param error
     * @param response
     */
    private void responseHandler(boolean error, String response){
        if (error) {
            handleReceiveError();
        }
        else{
            if(noResponse)
                Toast.makeText(context, "WiFi Module Connected!", Toast.LENGTH_LONG).show();

            noResponse = false;
            lastResponseTime = System.currentTimeMillis();

            if (delayLength > defaultDelayLength){
                delayLength = defaultDelayLength;
                updateDelayLength();
            }

            String[] r = response.split("~");
            if (r.length > 0) {
                if (r[0].contains("R")) {
                    int i = 1;
                    ArrayList<String> v;
                    for (Element e : adapter.getElements()) {
                        if (i >= r.length) {
                            break;
                        }
                        switch (e.getType()) {
                            case Element.TYPE_LED:
                            case Element.TYPE_RGBLED:
                            case Element.TYPE_DISPLAY:
                                //case Element.TYPE_CONSOLE:
                                v = e.getValues();
                                if (v == null || v.size() == 0 || !v.get(0).equals(r[i])) {
                                    v = new ArrayList<>();
                                    v.add(r[i]);
                                    e.setValues(v);
                                    adapter.notifyItemChanged(adapter.getElements().indexOf(e));
                                }
                                i++;
                                break;
                            case Element.TYPE_LED10:
                            case Element.TYPE_RGBLED10:
                                v = e.getValues();
                                boolean update = false;
                                if (v == null) v = new ArrayList<>();
                                for (int j = 0; j < 10; j++) {
                                    if (i >= r.length)
                                        break;
                                    if (v.size() > j) {
                                        if (update == false && !v.get(j).equals(r[i]))
                                            update = true;
                                        v.set(j, r[i]);
                                    } else {
                                        v.add(r[i]);
                                        update = true;
                                    }
                                    i++;
                                }
                                e.setValues(v);
                                adapter.notifyItemChanged(adapter.getElements().indexOf(e));
                                break;
                        }
                    }
                }
                else if (r[0].contains("C")){
                    Element e = adapter.getElements().get(adapter.getLastConsolePosition());
                    ArrayList<String> v = e.getValues();
                    if (v == null || v.size() == 0 || !v.get(0).equals(r[1])) {
                        v = new ArrayList<>();
                        v.add(r[1]);
                        e.setValues(v);
                        adapter.notifyItemChanged(adapter.getElements().indexOf(e));
                    }
                }
            }
        }
    }
}
