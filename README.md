# VHDPlus Remote App
Written in Java with Android Studio</br>
Designed for VHDPlus WiFi Extension

- This app can send and receive data via http requests
- You can select the IP (or URL) and port
- Request format for sending data:
```
http://[ip address (+ port)]/send?b=hook1       -> Button with hook "hook1" pressed
http://[ip address (+ port)]/send?s=hook2~1     -> "hook2" Switch enabled
http://[ip address (+ port)]/send?i=hook3~50    -> "hook3" Slider set to 50
http://[ip address (+ port)]/send?c=hook4~text  -> "hook4" console send "text"
```
- Response format:
```
Button, Switch and Slider: OK
-> Could be everything that does not include the letter 'C' or 'R'
Console: C~text
-> Text of last console where the send button was pressed will be changed to "text"
```

- Request format for requesting data: 
```
http://[ip address (+ port)]/read?hooks=~l_hook1~r_hook2~d_hook3~c_hook4
-> LED with hook "hook1", RGB LED with "hook2", display with "hook3", console with "hook4"
```
- Response format:
```
R~1~#FF0000~text~text2
-> LED is on, RGB LED is red, display has text "text" and console displays text "text2"
```

# Overview
![Overview Image](Overview.png)

## Java

### Connect Activity
In this code the last IP and/or Port settings are loaded and the new settings are saved

### Legal Notice
Displays legal notice

### Main Activity
In this code the main recycler view is managed, the wifi connection is controlled,
the listener for the "Add Element" button is implemented and this file contains
the communication between the AddElement Activity

### Drag and Drop Callback
This class handles element swap when moved up or down

### Swipe Callback
This class draws a colored square and an icon next to the element that is swiped to one side

### Element List Adapter
Recycler View adapter for element list
Handles different types of elements in the recycler view

### WiFi Connection
Implements functions to check internet connection, send data to wifi module
and request data from wifi module

### WiFi Request
This code allows to do a request to the WiFi module.
The OnTaskCompleted callback return the response and if an error occurred

### WiFi Timer
This code implements a timer that updates the elements with values from the wifi module

### Add Activity
In this code the "New Element" selector and the "Element Settings" List is managed.
Also the communication to the MainActivity is implemented

### Setting List Adapter
Recycler View adapter for element setting list

## Res

### Layout
Starting with activity_ -> Activity layouts </br>
Ending with _layout -> Layout for elements in MainActivity recycler view
spinner_item -> Layout for spinner in AddActivity
string_setting_layout -> Layout for elements in AddActivity recycler view

### Colors
Used colors for app

### Dimens
Layout dimensions for elements

### Element Choise
Choise of elements in AddActivity

### Element Settings
Settings for elements in AddActivity
Two items = one entry (first lable, second value)

### Strings
Strings in App
