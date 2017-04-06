package com.example.module2_app;

import android.os.Environment;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class Util {
    //----------------------------------------------------------------------------------------------
    /**
     *
     * @param b input byte
     * @return unsigned byte value represented as an integer
     */
    public static int uByte(byte b) {
        return ((int) b & 0xFF);
    }

    //----------------------------------------------------------------------------------------------
    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    //----------------------------------------------------------------------------------------------
    public static String saveImage(byte[] imageByteArray, int offset, int size) {

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
            return photoJPEG.getPath();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    //----------------------------------------------------------------------------------------------
    public static MatOfPoint largerCountour(List<MatOfPoint> contours) {
        MatOfPoint chosenCountour = null;
        for (MatOfPoint contour : contours) {
            if (chosenCountour == null)
                chosenCountour = contour;
            else if (Imgproc.contourArea(contour) > Imgproc.contourArea(chosenCountour))
                chosenCountour = contour;
        }
        return chosenCountour;
    }

    //----------------------------------------------------------------------------------------------
    public static Point contourCenter(MatOfPoint contour) {
        Moments m = Imgproc.moments(contour, true);
        Point center = new Point(m.m10/m.m00, m.m01/m.m00);
        return center;
    }

    //----------------------------------------------------------------------------------------------
    public static Scalar converScalarHsv2Rgba(Scalar hsvColor) {
        Mat pointMatRgba = new Mat();
        Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);

        return new Scalar(pointMatRgba.get(0, 0));
    }

}
