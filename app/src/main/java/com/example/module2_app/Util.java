package com.example.module2_app;

import android.os.Environment;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Util {

    /**
     *
     * @param b input byte
     * @return unsigned byte value represented as an integer
     */
    public static int uByte(byte b) {
        return ((int) b & 0xFF);
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public static void saveImage(byte[] imageByteArray, int offset, int size) {

        // Get the image directory
        try {
            File picDir = Environment.getExternalStoragePublicDirectory("Pictures/");
            File dtrPicDir = new File(picDir, "DTR Photos");
            dtrPicDir.mkdir();
            // Check next available number to store file as
            // Create the new file
            String timeStamp = new SimpleDateFormat("yyyy_mm_dd_HHmmss").format(Calendar.getInstance().getTime());
            File photoJPEG = new File(dtrPicDir, "dtr_" + timeStamp +".jpg");
            photoJPEG.createNewFile();
            // Write to the file
            FileOutputStream fos = new FileOutputStream (photoJPEG);
            fos.write(imageByteArray, offset, size);
            fos.close();
            State.lastPhotoNumber++;
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
