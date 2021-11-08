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

import android.os.Bundle;

public class AddActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
    }
}