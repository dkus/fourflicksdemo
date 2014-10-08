package com.github.dkus.fourflicks.util;


import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

    public static String formatLocation(double latitude, double longitude) {

        return String.valueOf(latitude)+","+String.valueOf(longitude);

    }

    public static <T> T toggle(T current, T first, T second) {

        if (current==null && first==null) return second;

        if (current==null) return first;

        return current.equals(first) ? second : first;

    }

    public static boolean areEquals(Object first, Object second) {

        return first==null ? second==null : first.equals(second);

    }

    public static File createImgFile() {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = null;
        try {
            image = File.createTempFile(imageFileName,".jpg", storageDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;

    }

}
