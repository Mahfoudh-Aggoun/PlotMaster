package com.example.plotmaster.model

interface Constants {
    companion object {
        const val MIN_SUPPORTED_VERSION = 1.1
        const val GRBL_STATUS_UPDATE_INTERVAL: Long = 150
        const val USB_BAUD_RATE = "115200"

        // Message types sent from the BluetoothService Handler
        const val MESSAGE_STATE_CHANGE = 1
        const val MESSAGE_READ = 2
        const val MESSAGE_WRITE = 3
        const val MESSAGE_DEVICE_NAME = 4
        const val MESSAGE_TOAST = 5
        const val REQUEST_READ_PERMISSIONS = 6
        const val PROBE_TYPE_NORMAL = 7
        const val PROBE_TYPE_TOOL_OFFSET = 8
        const val CONNECT_DEVICE_SECURE = 9
        const val CONNECT_DEVICE_INSECURE = 10
        const val FILE_PICKER_REQUEST_CODE = 11
        const val CONSOLE_LOGGER_MAX_SIZE = 256
        const val DEFAULT_JOGGING_FEED_RATE = 2000.0
        const val DEFAULT_PLANNER_BUFFER = 15
        const val DEFAULT_SERIAL_RX_BUFFER = 128
        const val PROBING_FEED_RATE = 50
        const val PROBING_PLATE_THICKNESS = 20
        const val PROBING_DISTANCE = 15
        const val MACHINE_STATUS_IDLE = "Idle"
        const val MACHINE_STATUS_JOG = "Jog"
        const val MACHINE_STATUS_RUN = "Run"
        const val MACHINE_STATUS_HOLD = "Hold"
        const val MACHINE_STATUS_ALARM = "Alarm"
        const val MACHINE_STATUS_CHECK = "Check"
        const val MACHINE_STATUS_SLEEP = "Sleep"
        const val MACHINE_STATUS_DOOR = "Door"
        const val MACHINE_STATUS_HOME = "Home"
        const val MACHINE_STATUS_NOT_CONNECTED = "Unknown"
        val SUPPORTED_FILE_TYPES = arrayOf(".tap", ".gcode", ".nc", ".ngc", ".fnc", ".txt")
        const val SUPPORTED_FILE_TYPES_STRING =
            "^.*\\.(tap|gcode|nc|ngc|cnc|txt|ncc|fnc|dnc|fan|gc|txt|ncg|ncp|fgc)$"
        const val JUST_STOP_STREAMING = "0"
        const val STOP_STREAMING_AND_RESET = "1"
        const val DEVICE_NAME = "device_name"
        const val TOAST = "toast"
        const val SERIAL_CONNECTION_TYPE_BLUETOOTH = "bluetooth"
        const val SERIAL_CONNECTION_TYPE_USB_OTG = "usbotg"
        const val BLUETOOTH_SERVICE_NOTIFICATION_ID = 100
        const val FILE_STREAMING_NOTIFICATION_ID = 101
        const val USB_OTG_SERVICE_NOTIFICATION_ID = 102
        const val TEXT_CATEGORY_UPDATE = "update"
        const val TEXT_CATEGORY_LINK = "link"
        const val CAM_FEED_RATE = "1000.0"
        const val CAM_TRAVERSAL = "5.0"
        const val CAM_STEP_OVER = "5.0"
        const val CAM_GCODE_HEAD = "G17\nG90\nG21\nM7\nM8\nG21\n"
        const val CAM_GCODE_END = "M9\nM30\n"
        const val CAM_ZDEEP = "0.0"
        const val CAM_ZSTEP = "0.0"
        const val CAM_TOOL_DIA = "10.0"
    }
}