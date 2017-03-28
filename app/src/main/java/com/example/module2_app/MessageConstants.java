package com.example.module2_app;

// Bluetooth connection
// Defines several constants used when transmitting messages between the
// service and the UI.
public interface MessageConstants {
    // Handler Communication
    public static final int MESSAGE_READ = 0;
    public static final int MESSAGE_WRITE = 1;
    public static final int MESSAGE_TOAST = 2;
    public static final int MESSAGE_CONNECT_SUCCESS = 3;
    public static final int MESSAGE_CONNECT_FAIL = 4;

    // Message IDs
    public static final int ID_COMMAND_MOVE_TIME = 0x01;
    public static final int ID_COMMAND_MOVE_ANGLE = 0x02;
    public static final int ID_COMMAND_CHANGE_SPEED = 0x03;
    public static final int ID_COMMAND_MOVE_TIME_SPEED = 0x05;
    public static final int ID_COMMAND_FIRE = 0x04;
    public static final int ID_COMMAND_ANDROID_HANDSHAKE = 0xFF;
    public static final int ID_RESPONSE = 0xAA;
    public static final int ID_REQUEST = 0xF1;
    public static final int ID_MESG_IMAGE = 0xF2;


    // Field Sizes
    public static final int SIZE_FIELD_ID = 1;
    public static final int SIZE_FIELD_LENGTH = 2;
    public static final int SIZE_FIELD_HEADER = SIZE_FIELD_ID + SIZE_FIELD_LENGTH;
    public static final int SIZE_FIELD_COMMAND_MOVE_DIR = 1;
    public static final int SIZE_FIELD_COMMAND_MOVE_TIME = 4;
    public static final int SIZE_FIELD_COMMAND_MOVE_SPEED = 1;

    // Response Codes
    public static final int RESPONSE_NO_ERROR = 0x00;
    public static final int RESPONSE_INVALID_PARAM = 0x01;
    public static final int RESPONSE_INVALID_COMMAND = 0x02;
    public static final int RESPONSE_INVALID_REQUEST = 0x03;
    public static final int RESPONSE_NIOS_HANDSHAKE = 0x04;

    // Message Sizes
    public static final int SIZE_COMMAND_MOVE_TIME = 5;
    public static final int SIZE_COMMAND_MOVE_TIME_SPEED = 6;
    public static final int SIZE_REQUEST = 1;

    // Move Directions
    public static final int MOVE_UP = 2;
    public static final int MOVE_DOWN = 3;
    public static final int MOVE_LEFT = 1;
    public static final int MOVE_RIGHT = 0;


}