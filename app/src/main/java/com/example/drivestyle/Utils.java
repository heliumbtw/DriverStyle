package com.example.drivestyle;

import android.annotation.SuppressLint;
import android.os.Environment;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

class Utils {
    private static final File folder = new File(Environment.getExternalStorageDirectory() +
            File.separator + "DriveStyle");

    static final DecimalFormat df = new DecimalFormat("0.000");
    static final DecimalFormat df_int = new DecimalFormat("0");

    static String getDate() {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());
        return formatter.format(curDate);
    }

    static void checkFolder() {
        boolean isDirectoryCreated=folder.exists();
        if (!isDirectoryCreated) {
            folder.mkdir();
        }
    }

//    static double calculateAcceleration(float[] linear_acceleration,
//                                      float[] mOrientation_d) {
//        double acceleration;
//        if (mOrientation_d[1] <= 7 && mOrientation_d[1] >= -7)
//            acceleration = 0.5 * linear_acceleration[0] + 0.85 * linear_acceleration[1];
//            return acceleration;
//    }
}
