/*
    Name: WiFiRequest
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
    This code allows to do a request to the WiFi module.
    The wifi error broadcast returns if the request was successful.
    The wifi response broadcast returns the response of the WiFi module
*/

package de.protop_solutions.vhdplus.vhdplus_remote.WiFi;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

class WiFiRequest extends AsyncTask<Void, Void, String> {

    //Element type (e.g. "b" for button, "s" or "i") or "hooks" for request
    private final String type;
    //hook with value or hooks for request
    private final String data;
    //send (e.g. button) or read (e.g. led)
    private final String task;
    //Defined IP of wifi module
    private final String ipAddress;
    //True for send task
    private final boolean showError;
    //Context of MainActivity
    private final Context context;
    //Callback
    private OnTaskCompleted listener;

    //Message of Exception during execution
    private String errorMsg;

    public WiFiRequest(Context context, String ipAddress, String task, String type, String data, boolean showError, OnTaskCompleted listener) {
        this.context = context;
        this.ipAddress = ipAddress;
        this.task = task;
        this.type = type;
        this.data = data;
        this.showError = showError;
        this.listener = listener;
    }

    /**
     * Communication with wifi module
     * Builds url and requests response from wifi module
     * return response
     * @param voids
     * @return
     */
    @Override
    protected String doInBackground(Void... voids) {
        try {
            //Request
            String urlString = "http://" + ipAddress + "/" + task + "?" + type + "=" + data;
            URL url = new URL(urlString);
            URLConnection urlConnection = url.openConnection();
            urlConnection.setReadTimeout(4000);
            urlConnection.setConnectTimeout(4000);
            urlConnection.setDoOutput(true);

            //Response
            BufferedReader bufferedReader =
                    new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String response = "";
            for (String line; (line = bufferedReader.readLine()) != null;) response += line;
            return response;
        } catch (Exception e) {
            errorMsg = e.getMessage();
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Creates broadcast message with wifi module response
     * @param result
     */
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (errorMsg == null && result.trim().length() > 0) {
            listener.OnTaskCompleted(result, false);
        } else {
            if (errorMsg != null) {
                if (showError)
                    Toast.makeText(context, "WiFi Error: " + errorMsg,
                            Toast.LENGTH_LONG).show();
            }
            listener.OnTaskCompleted(result, true);
        }
    }


}
