package com.github.dkus.fourflicks.util;

import android.util.Log;

import com.github.dkus.fourflicks.BuildConfig;


public class Logger {

    static final String TAG = "FourFlicksDemo";
    static final String MSG_DELIMITER = " >> ";

    public static void log(String msg) {

        log(msg, Logger.class, null);

    }

    public static void log(String msg, Class<?> clazz) {

        log(msg, clazz, null);

    }

    public static void log(String msg, Class<?> clazz, Throwable t) {

        if (BuildConfig.DEBUG) {
            Log.d(TAG, clazz == null ?
                    Logger.class.getSimpleName() :
                    clazz.getSimpleName() + MSG_DELIMITER + msg, t);
        }

    }


}
