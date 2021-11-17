/*
    Name: Element
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
    This class stores the attributes of elements in the recycler view
*/

package de.protop_solutions.vhdplus.vhdplus_remote.ElementRecyclerView;

import java.io.Serializable;
import java.util.ArrayList;

public class Element implements Serializable {

    public static final int TYPE_BUTTON = 1;
    public static final int TYPE_BUTTON3 = 2;
    public static final int TYPE_LED = 3;
    public static final int TYPE_LED10 = 4;
    public static final int TYPE_SWITCH = 5;
    public static final int TYPE_SWITCH3 = 6;
    public static final int TYPE_RGBLED = 7;
    public static final int TYPE_RGBLED10 = 8;
    public static final int TYPE_DISPLAY = 9;
    public static final int TYPE_SLIDER = 10;
    public static final int TYPE_JOYSTICK = 11;
    public static final int TYPE_CONSOLE = 12;

    private int type = 0;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    private ArrayList<String> hooks = new ArrayList<>();
    private ArrayList<String> names = new ArrayList<>();
    private ArrayList<String> values = new ArrayList<>();

    public ArrayList<String> getHooks() {
        return hooks;
    }

    public void setHooks(ArrayList<String> hooks) {
        this.hooks = hooks;
    }

    public ArrayList<String> getNames() {
        return names;
    }

    public void setNames(ArrayList<String> names) {
        this.names = names;
    }

    public ArrayList<String> getValues() {
        return values;
    }

    public void setValues(ArrayList<String> values) {
        this.values = values;
    }

    /**
     * Returns type of element
     * E.g.: 3 buttons -> button, 10 RGB LEDs -> RGB LED
     * @param type
     * @param arrayIndex
     * @return
     */
    public static int getBaseType(int type, boolean arrayIndex){
        if (arrayIndex) type++;
        switch (type){
            case Element.TYPE_BUTTON:
            case Element.TYPE_BUTTON3:
            case Element.TYPE_JOYSTICK:
                type = Element.TYPE_BUTTON;
                break;
            case Element.TYPE_LED:
            case Element.TYPE_LED10:
                type = Element.TYPE_LED;
                break;
            case Element.TYPE_SWITCH:
            case Element.TYPE_SWITCH3:
                type = Element.TYPE_SWITCH;
                break;
            case Element.TYPE_RGBLED:
            case Element.TYPE_RGBLED10:
                type = Element.TYPE_RGBLED;
                break;
        }
        if (arrayIndex) type--;
        return type;
    }
}
