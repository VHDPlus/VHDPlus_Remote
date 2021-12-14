/*
    Name: SettingListAdapter
    Rev: 1.0
    Creator: Leon Beier
    Date: 16.11.2021
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
    Recycler View adapter for element setting list
*/

package de.protop_solutions.vhdplus.vhdplus_remote.SettingRecyclerView;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.protop_solutions.vhdplus.vhdplus_remote.ElementRecyclerView.Element;
import de.protop_solutions.vhdplus.vhdplus_remote.R;

public class SettingListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    //Context of AddActivity
    private Context context;
    //Settings in recycler view
    private ArrayList<Setting> settings;
    //Element type for this settings
    private int type = 1;
    //List of used hooks to compare with hooks in list
    private ArrayList<String>[] usedHooks;
    //Recycler view needed to find view holders
    RecyclerView recyclerView;

    //Constructor
    public SettingListAdapter(Context context, ArrayList<Setting> settings, int type, ArrayList<String>[] usedHooks, RecyclerView recyclerView) {
        this.context = context;
        this.settings = settings;
        this.type = type;
        this.usedHooks = usedHooks;
        this.recyclerView = recyclerView;
    }

    /**
     * Returns view holder for element setting
     * Gets layout of setting list entry and creates view holder
     * @param parent
     * @param viewType
     * @return
     */
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.string_setting_layout, parent, false);
        return new NameViewHolder(view);
    }

    /**
     * Initializes setting entries
     * Adds functions for text change
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((NameViewHolder) holder).setNameDetails(settings.get(position));
        ((NameViewHolder) holder).txtValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence cs, int s, int b, int c) { }

            @Override
            public void afterTextChanged(Editable editable) {
                for (int pos = 0; pos < settings.size(); pos++) {
                    NameViewHolder nameViewHolder =
                            ((NameViewHolder) recyclerView.findViewHolderForAdapterPosition(pos));
                    if (nameViewHolder != null) {
                        String value = nameViewHolder.txtValue.getText().toString();

                        Setting set = settings.get(pos);
                        set.setValue(value);
                        settings.set(pos, set);

                        if (settings.get(pos).getName().contains("Hook") && hookAlreadyUsed(value, pos))
                            nameViewHolder.txtValue.setBackgroundTintList(
                                    context.getApplicationContext().getResources().getColorStateList(R.color.red));
                        else
                            nameViewHolder.txtValue.setBackgroundTintList(
                                    context.getApplicationContext().getResources().getColorStateList(R.color.white));
                    }
                }
            }
        });
    }

    /**
     * Checks hooks of elements and current element to set up
     * Returns true if hook exists for different ui element
     * @param hook hook to compare
     * @param position index of hook in settings list
     * @return
     */
    private boolean hookAlreadyUsed(String hook, int position){
        ArrayList<String> hooks = usedHooks[Element.getBaseType(type,false)-1];
        if (hooks != null && hooks.contains(hook)) return true;
        for (Setting s: settings) {
            if (s.getName().contains("Hook") && s.getValue().equals(hook) &&
                    settings.indexOf(s) != position) return true;
        }
        return false;
    }

    /**
     * Returns number of settings in setting list
     * @return Number of settings in setting list
     */
    @Override
    public int getItemCount() {
        return settings.size();
    }

    /**
     * Returns user defined names for labels and buttons or colors for LEDs depending on element type
     * @return
     */
    public ArrayList<String> getNames(){
        ArrayList<String> names = new ArrayList<>();
        switch (type){
            case Element.TYPE_BUTTON:
            case Element.TYPE_LED:
            case Element.TYPE_CONSOLE:
                names.add(settings.get(0).getValue());
                names.add(settings.get(1).getValue());
                break;
            case Element.TYPE_SWITCH:
            case Element.TYPE_SWITCH3:
            case Element.TYPE_BUTTON3:
            case Element.TYPE_LED10:
            case Element.TYPE_JOYSTICK:
                for (int i = 0; i < settings.size(); i += 2)
                    names.add(settings.get(i).getValue());
                break;
            case Element.TYPE_RGBLED:
            case Element.TYPE_DISPLAY:
            case Element.TYPE_SLIDER:
                names.add(settings.get(0).getValue());
                break;
        }
        return names;
    }

    /**
     * Returns user defined hooks for interaction with wifi module depending on element type
     * @return
     */
    public ArrayList<String> getHooks(){
        ArrayList<String> hooks = new ArrayList<>();
        switch (type){
            case Element.TYPE_BUTTON:
            case Element.TYPE_LED:
            case Element.TYPE_CONSOLE:
                hooks.add(settings.get(2).getValue());
                break;
            case Element.TYPE_SWITCH:
            case Element.TYPE_SWITCH3:
            case Element.TYPE_BUTTON3:
            case Element.TYPE_LED10:
            case Element.TYPE_JOYSTICK:
                for (int i = 0; i < settings.size(); i += 2)
                    hooks.add(settings.get(i+1).getValue());
                break;
            case Element.TYPE_RGBLED:
            case Element.TYPE_DISPLAY:
            case Element.TYPE_SLIDER:
                hooks.add(settings.get(1).getValue());
                break;
            case Element.TYPE_RGBLED10:
                for (int i = 0; i < settings.size(); i ++)
                    hooks.add(settings.get(i).getValue());
                break;
        }
        return hooks;
    }

    /**
     * View Holder for setting element with UI elements
     */
    class NameViewHolder extends RecyclerView.ViewHolder {

        private TextView txtName;
        private EditText txtValue;

        NameViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtSettingName);
            txtValue = itemView.findViewById(R.id.txtSettingValue);
        }

        void setNameDetails(Setting setting) {
            txtName.setText(setting.getName());
            txtValue.setText(setting.getValue());
        }
    }
}
