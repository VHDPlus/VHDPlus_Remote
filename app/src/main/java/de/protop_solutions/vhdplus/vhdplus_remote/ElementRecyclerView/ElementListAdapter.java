/*
    Name: ElementListAdapter
    Rev: 1.0
    Creator: Leon Beier
    Date: 15.11.2021
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
    Recycler View adapter for element list
    Handles different types of elements in the recycler view
*/

package de.protop_solutions.vhdplus.vhdplus_remote.ElementRecyclerView;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;
import de.protop_solutions.vhdplus.vhdplus_remote.R;
import de.protop_solutions.vhdplus.vhdplus_remote.SettingRecyclerView.Setting;
import de.protop_solutions.vhdplus.vhdplus_remote.WiFi.WiFiConnection;
import de.protop_solutions.vhdplus.vhdplus_remote.WiFi.WiFiTimer;

public class ElementListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    //Context of MainActivity
    private Context context;
    //Elements in recycler view
    private ArrayList<Element> elements;

    //Object that handles requests to WiFi module
    private WiFiConnection wifi;
    //Timer that checks for new LED, Console, Display... data every half second
    private WiFiTimer wifiTimer;
    //Defined IP of wifi module
    private String ip;

    //Last position in elements of console where user pressed send button
    int lastConsolePosition = 0;

    //Constructor
    public ElementListAdapter(Context context, ArrayList<Element> elements, String ip){
        this.context = context;
        this.elements = elements;
        wifi = new WiFiConnection(context, elements);
        wifiTimer = new WiFiTimer(context, this, wifi, ip, 500);
        this.ip = ip;
    }

    /**
     * Starts listening to wifi module for new element data
     */
    public void onStart(){
        wifiTimer.startWiFiConnection();
    }

    /**
     * Stops listening to wifi module for new element data
     */
    public void onStop(){
        wifiTimer.stopWiFiConnection();
    }

    /**
     * Returns view holder depending on element type
     * Gets layout of element and creates view holder for element
     * @param parent
     * @param viewType
     * @return
     */
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType){
            case Element.TYPE_BUTTON:
                view = LayoutInflater.from(context).inflate(R.layout.button_layout, parent, false);
                return new ButtonViewHolder(view);
            case Element.TYPE_BUTTON3:
                view = LayoutInflater.from(context).inflate(R.layout.button3_layout, parent, false);
                return new Button3ViewHolder(view);
            case Element.TYPE_LED:
                view = LayoutInflater.from(context).inflate(R.layout.led_layout, parent, false);
                return new LEDViewHolder(view);
            case Element.TYPE_LED10:
                view = LayoutInflater.from(context).inflate(R.layout.led10_layout, parent, false);
                return new LED10ViewHolder(view);
            case Element.TYPE_SWITCH:
                view = LayoutInflater.from(context).inflate(R.layout.switch_layout, parent, false);
                return new SwitchViewHolder(view);
            case Element.TYPE_SWITCH3:
                view = LayoutInflater.from(context).inflate(R.layout.switch3_layout, parent, false);
                return new Switch3ViewHolder(view);
            case Element.TYPE_RGBLED:
                view = LayoutInflater.from(context).inflate(R.layout.led_layout, parent, false);
                return new RGBLEDViewHolder(view);
            case Element.TYPE_RGBLED10:
                view = LayoutInflater.from(context).inflate(R.layout.led10_layout, parent, false);
                return new RGBLED10ViewHolder(view);
            case Element.TYPE_DISPLAY:
                view = LayoutInflater.from(context).inflate(R.layout.display_layout, parent, false);
                return new DisplayViewHolder(view);
            case Element.TYPE_SLIDER:
                view = LayoutInflater.from(context).inflate(R.layout.slider_layout, parent, false);
                return new SliderViewHolder(view);
            case Element.TYPE_JOYSTICK:
                view = LayoutInflater.from(context).inflate(R.layout.joystick_layout, parent, false);
                return new JoystickViewHolder(view);
            case Element.TYPE_CONSOLE:
                view = LayoutInflater.from(context).inflate(R.layout.console_layout, parent, false);
                return new ConsoleViewHolder(view);
            default:
                throw new UnsupportedOperationException("Type " + viewType + " doesn't exist");
        }
    }

    /**
     * Replaces getItemViewType function to return type parameter of element in element list
     * @param position Position in element list
     * @return Type of element
     */
    @Override
    public int getItemViewType(int position){
        return elements.get(position).getType();
    }

    /**
     * Initializes elements depending on type
     * Adds functions on button click or interaction with element
     * @param holder view holder for element to initialize
     * @param position position of element in element list
     */
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        switch (getItemViewType(position)){
            case Element.TYPE_BUTTON:
                ((ButtonViewHolder) holder).setButtonDetails(elements.get(position));
                ((ButtonViewHolder) holder).myButton.setOnClickListener(view -> {
                    sendWiFi("b", elements.get(position).getHooks().get(0));
                });
                break;
            case Element.TYPE_BUTTON3:
                ((Button3ViewHolder) holder).setButton3Details(elements.get(position));
                ((Button3ViewHolder) holder).myButton1.setOnClickListener(view -> {
                    sendWiFi("b", elements.get(position).getHooks().get(0));
                });
                ((Button3ViewHolder) holder).myButton2.setOnClickListener(view -> {
                    sendWiFi("b", elements.get(position).getHooks().get(1));
                });
                ((Button3ViewHolder) holder).myButton3.setOnClickListener(view -> {
                    sendWiFi("b", elements.get(position).getHooks().get(2));
                });
                break;
            case Element.TYPE_LED:
                ((LEDViewHolder) holder).setLEDDetails(elements.get(position));
                break;
            case Element.TYPE_LED10:
                ((LED10ViewHolder) holder).setLED10Details(elements.get(position));
                break;
            case Element.TYPE_SWITCH:
                ((SwitchViewHolder) holder).setButtonDetails(elements.get(position));
                ((SwitchViewHolder) holder).mySwitch.setOnClickListener(view -> {
                    sendWiFi("s", elements.get(position).getHooks().get(0) + "~" + (((SwitchViewHolder) holder).mySwitch.isChecked() ? 1:0));
                });
                break;
            case Element.TYPE_SWITCH3:
                ((Switch3ViewHolder) holder).setSwitch3Details(elements.get(position));
                ((Switch3ViewHolder) holder).mySwitch1.setOnClickListener(view -> {
                    sendWiFi("s", elements.get(position).getHooks().get(0) + "~" + (((Switch3ViewHolder) holder).mySwitch1.isChecked() ? 1:0));
                });
                ((Switch3ViewHolder) holder).mySwitch2.setOnClickListener(view -> {
                    sendWiFi("s", elements.get(position).getHooks().get(1) + "~" + (((Switch3ViewHolder) holder).mySwitch2.isChecked() ? 1:0));
                });
                ((Switch3ViewHolder) holder).mySwitch3.setOnClickListener(view -> {
                    sendWiFi("s", elements.get(position).getHooks().get(2) + "~" + (((Switch3ViewHolder) holder).mySwitch3.isChecked() ? 1:0));
                });
                break;
            case Element.TYPE_RGBLED:
                ((RGBLEDViewHolder) holder).setRGBLEDDetails(elements.get(position));
                break;
            case Element.TYPE_RGBLED10:
                ((RGBLED10ViewHolder) holder).setRGBLED10Details(elements.get(position));
                break;
            case Element.TYPE_SLIDER:
                ((SliderViewHolder) holder).setSliderDetails(elements.get(position));
                //Add broadcast to swipe handler to disable swipe to delete/edit when slider is used
                ((SliderViewHolder) holder).mySlider.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        Intent intent = new Intent("swipe");
                        boolean action = true;
                        if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) intent.putExtra("enable", false);
                        else if(motionEvent.getAction() == MotionEvent.ACTION_UP) intent.putExtra("enable", true);
                        else action = false;
                        if (action) LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                        return false;
                    }
                });
                ((SliderViewHolder) holder).mySlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) { }
                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) { }
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        sendWiFi("i", elements.get(position).getHooks().get(0) + "~" + (((SliderViewHolder) holder).mySlider.getProgress()));
                    }
                });
                break;
            case Element.TYPE_DISPLAY:
                ((DisplayViewHolder) holder).setDisplayDetails(elements.get(position));
                break;
            case Element.TYPE_JOYSTICK:
                ((JoystickViewHolder) holder).setJoystickDetails(elements.get(position));
                ((JoystickViewHolder) holder).myButtonF.setOnClickListener(view -> {
                    sendWiFi("b", elements.get(position).getHooks().get(0));
                });
                ((JoystickViewHolder) holder).myButtonL.setOnClickListener(view -> {
                    sendWiFi("b", elements.get(position).getHooks().get(1));
                });
                ((JoystickViewHolder) holder).myButtonS.setOnClickListener(view -> {
                    sendWiFi("b", elements.get(position).getHooks().get(2));
                });
                ((JoystickViewHolder) holder).myButtonR.setOnClickListener(view -> {
                    sendWiFi("b", elements.get(position).getHooks().get(3));
                });
                ((JoystickViewHolder) holder).myButtonB.setOnClickListener(view -> {
                    sendWiFi("b", elements.get(position).getHooks().get(4));
                });
                break;
            case Element.TYPE_CONSOLE:
                ((ConsoleViewHolder) holder).setConsoleDetails(elements.get(position));
                ((ConsoleViewHolder) holder).btnSend.setOnClickListener(view -> {
                    lastConsolePosition = position;
                    sendWiFi("c", elements.get(position).getHooks().get(0) + "~" + (((ConsoleViewHolder) holder).txtOut.getText().toString()));
                });
                ((ConsoleViewHolder) holder).txtOut.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        Element thisElement = new Element();
                        for (Element e: elements){
                            if (e.getType() == Element.TYPE_CONSOLE && e.getHooks().get(0) == ((ConsoleViewHolder) holder).hook) {
                                thisElement = e;
                                break;
                            }
                        }
                        ArrayList<String> v = thisElement.getValues();
                        if (v == null) v = new ArrayList<>();
                        if (v.size() == 0) v.add("");
                        if (v.size() > 1) v.set(1, charSequence.toString());
                        else v.add(charSequence.toString());
                        thisElement.setValues(v);
                    }

                    @Override
                    public void afterTextChanged(Editable editable) { }
                });
                break;
        }
    }

    /**
     * Send data to wifi module
     * Checks if Internet connection exists
     * @param type
     * @param data
     */
    private void sendWiFi(String type, String data){
        if (wifi.checkConnection()) {
            Toast.makeText(this.context, "Send to " + ip + ": " + data, Toast.LENGTH_SHORT).show();
            wifi.sendData(ip, type, data);
        }
        else
            Toast.makeText(this.context, "No Internet Connection", Toast.LENGTH_SHORT).show();
    }

    /**
     * Last position in elements of console where user pressed send button
     * @return
     */
    public int getLastConsolePosition(){
        return lastConsolePosition;
    }

    /**
     * Adds element to element list and notifies adapter to update list in recycler view
     * @param element Element to add
     */
    public void addElement(Element element){
        int position = elements.size();
        elements.add(element);
        notifyItemInserted(position);
        notifyItemRangeChanged(position, elements.size());
    }

    /**
     * Adds element at position in element list and notifies adapter to update list in recycler view
     * @param element Element to add
     * @param position Position in element list
     */
    public void insertElement(Element element, int position){
        if(position == elements.size()) elements.add(element);
        else elements.add(position, element);
        notifyItemInserted(position);
        notifyItemRangeChanged(position, elements.size());
    }

    /**
     * Removes element at position in element list and notifies adapter to update list in recycler view
     * @param position Position in element list
     */
    public void removeElement(int position) {
        elements.remove(position);
        notifyItemRemoved(position);
    }

    /**
     * Returns element list of recycler view
     * @return Element list of recycler view
     */
    public ArrayList<Element> getElements() {
        return elements;
    }

    /**
     * Returns number of elements in element list
     * @return Number of elements in element list
     */
    @Override
    public int getItemCount() {
        return elements.size();
    }

    /**
     *
     * @param fromPosition
     * @param toPosition
     * @return
     */
    public boolean swapElement(int fromPosition, int toPosition) {
        Collections.swap(elements, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    /**
     * View Holder for button element with UI elements
     */
    class ButtonViewHolder extends RecyclerView.ViewHolder {

        private TextView txtName;
        private Button myButton;

        ButtonViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtName);
            myButton = itemView.findViewById(R.id.myButton);
        }

        void setButtonDetails(Element element) {
            txtName.setText(element.getNames().get(0));
            myButton.setText(element.getNames().get(1));
        }
    }

    /**
     * View Holder for 3 button element with UI elements
     */
    class Button3ViewHolder extends RecyclerView.ViewHolder {

        private Button myButton1;
        private Button myButton2;
        private Button myButton3;

        Button3ViewHolder(@NonNull View itemView) {
            super(itemView);
            myButton1 = itemView.findViewById(R.id.myButton1);
            myButton2 = itemView.findViewById(R.id.myButton2);
            myButton3 = itemView.findViewById(R.id.myButton3);
        }

        void setButton3Details(Element element) {
            myButton1.setText(element.getNames().get(0));
            myButton2.setText(element.getNames().get(1));
            myButton3.setText(element.getNames().get(2));
        }
    }

    /**
     * View Holder for led element with UI elements
     */
    class LEDViewHolder extends RecyclerView.ViewHolder {

        private TextView txtName;
        private ImageView ledView;

        LEDViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtName);
            ledView = itemView.findViewById(R.id.ledView);
        }

        void setLEDDetails(Element element) {
            txtName.setText(element.getNames().get(0));
            if (element.getValues() != null && element.getValues().size() > 0 && element.getValues().get(0).trim().equals("1")){
                ledView.setColorFilter(Color.parseColor(element.getNames().get(1)));
            }
            else{
                ledView.setColorFilter(R.color.black_200);
            }
        }
    }

    /**
     * View Holder for 10 led element with UI elements
     */
    class LED10ViewHolder extends RecyclerView.ViewHolder {

        private ImageView[] ledViews = new ImageView[10];

        LED10ViewHolder(@NonNull View itemView) {
            super(itemView);
            ledViews[0] = itemView.findViewById(R.id.ledView1);
            ledViews[1] = itemView.findViewById(R.id.ledView2);
            ledViews[2] = itemView.findViewById(R.id.ledView3);
            ledViews[3] = itemView.findViewById(R.id.ledView4);
            ledViews[4] = itemView.findViewById(R.id.ledView5);
            ledViews[5] = itemView.findViewById(R.id.ledView6);
            ledViews[6] = itemView.findViewById(R.id.ledView7);
            ledViews[7] = itemView.findViewById(R.id.ledView8);
            ledViews[8] = itemView.findViewById(R.id.ledView9);
            ledViews[9] = itemView.findViewById(R.id.ledView10);
        }

        void setLED10Details(Element element) {
            int s = element.getValues().size();
            for (int i = 0; i < 10; i ++){
                if(element.getValues() != null && s > i && element.getValues().get(i).trim().equals("1"))
                    ledViews[i].setColorFilter(Color.parseColor(element.getNames().get(i)));
                else
                    ledViews[i].setColorFilter(R.color.black_200);
            }
        }
    }

    /**
     * View Holder for switch element with UI elements
     */
    class SwitchViewHolder extends RecyclerView.ViewHolder {

        private TextView txtName;
        private SwitchMaterial mySwitch;

        SwitchViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtName);
            mySwitch = itemView.findViewById(R.id.mySwitch);
        }

        void setButtonDetails(Element element) {
            txtName.setText(element.getNames().get(0));
        }
    }

    /**
     * View Holder for 3 switch element with UI elements
     */
    class Switch3ViewHolder extends RecyclerView.ViewHolder {

        private SwitchMaterial mySwitch1;
        private SwitchMaterial mySwitch2;
        private SwitchMaterial mySwitch3;

        Switch3ViewHolder(@NonNull View itemView) {
            super(itemView);
            mySwitch1 = itemView.findViewById(R.id.mySwitch1);
            mySwitch2 = itemView.findViewById(R.id.mySwitch2);
            mySwitch3 = itemView.findViewById(R.id.mySwitch3);
        }

        void setSwitch3Details(Element element) {
            mySwitch1.setText(element.getNames().get(0));
            mySwitch2.setText(element.getNames().get(1));
            mySwitch3.setText(element.getNames().get(2));
        }
    }

    /**
     * View Holder for rgb led element with UI elements
     */
    class RGBLEDViewHolder extends RecyclerView.ViewHolder {

        private TextView txtName;
        private ImageView ledView;

        RGBLEDViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtName);
            ledView = itemView.findViewById(R.id.ledView);
        }

        void setRGBLEDDetails(Element element) {
            txtName.setText(element.getNames().get(0));
            if (element.getValues() != null && element.getValues().size() > 0){
                String c = element.getValues().get(0);
                if (!c.startsWith("#")) c = "#" + c;
                try {
                    ledView.setColorFilter(Color.parseColor(c));
                } catch (IllegalArgumentException iae) {
                    ledView.setColorFilter(R.color.black_200);
                }
            }
            else{
                ledView.setColorFilter(R.color.black_200);
            }
        }
    }

    /**
     * View Holder for 10 rgb led element with UI elements
     */
    class RGBLED10ViewHolder extends RecyclerView.ViewHolder {

        private ImageView[] ledViews = new ImageView[10];

        RGBLED10ViewHolder(@NonNull View itemView) {
            super(itemView);
            ledViews[0] = itemView.findViewById(R.id.ledView1);
            ledViews[1] = itemView.findViewById(R.id.ledView2);
            ledViews[2] = itemView.findViewById(R.id.ledView3);
            ledViews[3] = itemView.findViewById(R.id.ledView4);
            ledViews[4] = itemView.findViewById(R.id.ledView5);
            ledViews[5] = itemView.findViewById(R.id.ledView6);
            ledViews[6] = itemView.findViewById(R.id.ledView7);
            ledViews[7] = itemView.findViewById(R.id.ledView8);
            ledViews[8] = itemView.findViewById(R.id.ledView9);
            ledViews[9] = itemView.findViewById(R.id.ledView10);
        }

        void setRGBLED10Details(Element element) {
            int s = element.getValues().size();
            for (int i = 0; i < 10; i ++){
                if(element.getValues() != null && s > i) {
                    String c = element.getValues().get(i);
                    if (!c.startsWith("#")) c = "#" + c;
                    try {
                        ledViews[i].setColorFilter(Color.parseColor(c));
                    } catch (IllegalArgumentException iae) {
                        ledViews[i].setColorFilter(R.color.black_200);
                    }
                }
                else
                    ledViews[i].setColorFilter(R.color.black_200);
            }
        }
    }

    /**
     * View Holder for display element with UI elements
     */
    class DisplayViewHolder extends RecyclerView.ViewHolder {

        private TextView txtName;
        private TextView txtValue;

        DisplayViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtName);
            txtValue = itemView.findViewById(R.id.txtValue);
        }

        void setDisplayDetails(Element element) {
            txtName.setText(element.getNames().get(0));
            if (element.getValues() != null && element.getValues().size() > 0) txtValue.setText(element.getValues().get(0));
        }
    }

    /**
     * View Holder for slider element with UI elements
     */
    class SliderViewHolder extends RecyclerView.ViewHolder {

        private TextView txtName;
        private SeekBar mySlider;

        SliderViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtName);
            mySlider = itemView.findViewById(R.id.seekBar);
        }

        void setSliderDetails(Element element) {
            txtName.setText(element.getNames().get(0));
        }
    }

    /**
     * View Holder for joystick element with UI elements
     */
    class JoystickViewHolder extends RecyclerView.ViewHolder {

        private Button myButtonF;
        private Button myButtonL;
        private Button myButtonS;
        private Button myButtonR;
        private Button myButtonB;

        JoystickViewHolder(@NonNull View itemView) {
            super(itemView);
            myButtonF = itemView.findViewById(R.id.myButtonF);
            myButtonL = itemView.findViewById(R.id.myButtonL);
            myButtonS = itemView.findViewById(R.id.myButtonS);
            myButtonR = itemView.findViewById(R.id.myButtonR);
            myButtonB = itemView.findViewById(R.id.myButtonB);
        }

        void setJoystickDetails(Element element) {
            myButtonF.setText(element.getNames().get(0));
            myButtonL.setText(element.getNames().get(1));
            myButtonS.setText(element.getNames().get(2));
            myButtonR.setText(element.getNames().get(3));
            myButtonB.setText(element.getNames().get(4));
        }
    }

    /**
     * View Holder for console element with UI elements
     */
    class ConsoleViewHolder extends RecyclerView.ViewHolder {

        private TextView txtIn;
        private EditText txtOut;
        private Button btnSend;
        private String hook;

        ConsoleViewHolder(@NonNull View itemView) {
            super(itemView);
            txtIn = itemView.findViewById(R.id.txtIn);
            txtOut = itemView.findViewById(R.id.txtOut);
            btnSend = itemView.findViewById(R.id.btnSend);
            hook = "";
        }

        void setConsoleDetails(Element element) {
            txtOut.setHint(element.getNames().get(0));
            btnSend.setText(element.getNames().get(1));
            if (element.getValues() != null && element.getValues().size() > 0) txtIn.setText(element.getValues().get(0));
            else txtIn.setText("");
            if (element.getValues() != null && element.getValues().size() > 1) txtOut.setText(element.getValues().get(1));
            else txtOut.setText("");
            txtOut.setSelection(txtOut.getText().length());
            hook = element.getHooks().get(0);
        }
    }
}
