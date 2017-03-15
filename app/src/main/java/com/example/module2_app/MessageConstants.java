package com.example.module2_app;

// Bluetooth connection
// Defines several constants used when transmitting messages between the
// service and the UI.
public interface MessageConstants {

    public static final int MESSAGE_READ = 0;
    public static final int MESSAGE_WRITE = 1;
    public static final int MESSAGE_TOAST = 2;


    // Message IDs
    public static final int MOVE_COMMAND_TIME_ID = 0x01;

    // Field Sizes
    public static final int MESG_FIELD_ID_SIZE = 1;
    public static final int MESG_FIELD_LENGTH_SIZE = 2;
    public static final int MESG_FIELD_HEADER_SIZE = MESG_FIELD_ID_SIZE + MESG_FIELD_LENGTH_SIZE;
    public static final int MESG_MOVE_FIELD_DIR_SIZE = 1;


    // Message Sizes
    public static final int MESG_MOVE_TIME_SIZE = 5;

    // Move Directions
    public static final int MOVE_UP = 2;
    public static final int MOVE_DOWN = 3;
    public static final int MOVE_LEFT = 1;
    public static final int MOVE_RIGHT = 0;
}