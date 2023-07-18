# PlotMaster - Android CNC Plotting Machine Controller
## Images

![Image 1](https://user-images.githubusercontent.com/99191971/254010142-c8870568-9ab9-443e-be4c-f0ce2d998a7c.png)

![Image 2](https://user-images.githubusercontent.com/99191971/254010135-b6b4fb79-d93d-42ba-894b-f39c709d8fd1.jpg) ![Image 3](https://user-images.githubusercontent.com/99191971/254010168-0baedecf-3b12-4d9f-bc96-7a0e17fbcf8d.png) ![Image 4](https://user-images.githubusercontent.com/99191971/254010179-d558e434-eafe-4527-bd10-e3eb67ffcc68.jpg)

## Introduction
PlotMaster is an Android application designed to control and monitor a plotting CNC (Computer Numerical Control) machine. The application provides a user-friendly interface with three main fragments for different functionalities: jogging controls, console interaction, sending G-code files to the machine, and real time graphical representation of the plotting process. The app communicates with the CNC machine using Bluetooth and the GRBL library. The CNC machine itself is built using an Arduino Uno microcontroller.

## Features
- **Jogging Controls**: This fragment allows users to manually control the CNC machine's movements. Users can jog the machine in various directions (X, Y, and Z axes) and adjust the jogging distance.
- **Console Interaction**: The console fragment provides a terminal-like interface where users can send commands directly to the CNC machine and receive responses. This feature enables advanced control and monitoring of the CNC machine's behavior.
- **G-code File Sending**: Users can send G-code files to the CNC machine for automatic execution. The application supports browsing and selecting G-code files from the device's storage and transferring them to the CNC machine.
- Real time machine status reporting.
- Real time graphical visualization of the plotting process
- Application can work in background mode, by utilizing the less resources, there by consuming less power.
  
## Technology Stack
- **Android**: The application is built for the Android platform, utilizing the Android SDK and framework.
- **Java and Kotlin**: The codebase is written in a combination of Java and Kotlin programming languages, allowing for flexibility and compatibility.
- **Bluetooth**: PlotMaster utilizes Bluetooth technology to establish a wireless connection between the Android device and the CNC machine.
- **GRBL Library**: The application integrates the GRBL library, which provides the necessary functionality to communicate with the CNC machine and control its behavior.

## Architecture
PlotMaster follows the Model-View-Controller (MVC) architectural pattern to ensure a clear separation of concerns and maintainable code. The breakdown of components is as follows:

- **Model**: Represents the data and business logic of the application. This includes handling Bluetooth communication, parsing and executing G-code commands, and managing the machine's state.
- **View**: Handles the user interface components and their interactions. It consists of the three main fragments (Jogging Controls, Console, and G-code Sender) responsible for displaying relevant information and capturing user input.
- **Controller**: Acts as an intermediary between the Model and the View, coordinating the flow of data and events. It handles user input, updates the Model accordingly, and ensures the View reflects the current state.

## Prerequisites
To use PlotMaster, you will need the following:

- An Android device running Android 4.4 (KitKat) or later.
- A CNC machine built with an Arduino Uno microcontroller.
- The custom GRBL library uploaded to the Arduino Uno from the following link: https://github.com/lavolpecheprogramma/grbl-1-1h-servo
- Bluetooth capability on both the Android device and the CNC machine.
- Sufficient permissions on the Android device to establish Bluetooth connections.
- If you are connecting Bluetooth module first time to your machine, then make sure you have changed the baud rate of the BT module to 115200. (Default baud rate of the GRBL 1.1v firmware is 115200 as 8-bits, no parity, and 1-stop bit).
- HC-05 Bluetooth module setup http://www.buildlog.net/blog/2017/10/using-the-hc-05-bluetooth-module/
- HC-06 Bluetooth module setup https://github.com/zeevy/grblcontroller/wiki/Bluetooth-Setup-HC-06

## Contact
If you have any questions, suggestions, or feedback, please feel free to contact me at [riadmahfoudh09@gmail.com]

Thank you for using PlotMaster!
