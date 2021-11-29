/*
    Name: WiFiConnection
    Rev: 1.0
    Creator: Leon Beier
    Date: 27.11.2021
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
    Implements functions to check internet connection, send data to wifi module
    and request data from wifi module
*/

package de.protop_solutions.vhdplus.vhdplus_remote.WiFi;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import java.util.ArrayList;

import de.protop_solutions.vhdplus.vhdplus_remote.ElementRecyclerView.Element;

public class WiFiConnection {

    //Context of MainActivity
    private Context context;
    //Elements in recycler view
    private ArrayList<Element> elements;

    public WiFiConnection(Context context, ArrayList<Element> elements){
        this.context = context;
        this.elements = elements;
    }

    /**
     * Returns true if internet connection present
     * @return
     */
    public boolean checkConnection(){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null){
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetworkInfo.isConnected()) return true;
        }
        return false;
    }

    /**
     * Sends data to wifi module
     * E.g. that button pressed or slider value
     * type = b for button, s for slider...
     * data = hook ~ value
     * @param ipAddress
     * @param type
     * @param data
     */
    public void sendData(String ipAddress, String type, String data) {
        WiFiRequest wifiRequest = new WiFiRequest(this.context, ipAddress, "send", type, data, true);
        wifiRequest.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * Requests data for elements like LED or display in element list
     * @param ipAddress
     */
    public void requestData(String ipAddress) {
        String hooks = "";
        for (Element e: elements) {
            String type = "";
            switch(e.getType()){
                case Element.TYPE_LED:
                case Element.TYPE_LED10:
                    type = "l";
                    break;
                case Element.TYPE_RGBLED:
                case Element.TYPE_RGBLED10:
                    type = "r";
                    break;
                case Element.TYPE_DISPLAY:
                    type = "d";
                    break;
                case Element.TYPE_CONSOLE:
                    type = "c";
                    break;
            }
            if (isReceivingElement(e.getType())){
                for (String h: e.getHooks()) {
                    hooks = hooks + "~" + type + "_" + h;
                }
            }
        }
        if (hooks != ""){
            WiFiRequest connectionAsyncTask = new WiFiRequest(this.context, ipAddress, "read", "hooks", hooks, false);
            connectionAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    /**
     * Returns true if type is type of element that needs data from wifi module
     * @param type
     * @return
     */
    static boolean isReceivingElement(int type){
        switch(type){
            case Element.TYPE_LED:
            case Element.TYPE_LED10:
            case Element.TYPE_RGBLED:
            case Element.TYPE_RGBLED10:
            case Element.TYPE_DISPLAY:
            case Element.TYPE_CONSOLE:
                return true;
        }
        return false;
    }

    /**
     * Checks if at least one element in elements needs data from wifi module
     * @param elements
     * @return
     */
    public static boolean listHasReceivingElement(ArrayList<Element> elements){
        for (Element e : elements){
            if(isReceivingElement(e.getType())) return true;
        }
        return false;
    }
}
