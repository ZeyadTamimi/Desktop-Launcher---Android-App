package com.example.module2_app;

public class Util {

    /**
     *
     * @param b input byte
     * @return unsigned byte value represented as an integer
     */
    public static int uByte(byte b) {
        return ((int) b & 0xFF);
    }
}
