package com.tyrese.bluetoothchatdemo;

import android.util.Log;

/**
 * Created by Tyrese on 2016/7/8.
 */
public class LogWrapper {
    private static boolean DEBUG = true;

    private static final String TAG = "<<<<===>>>>";

    public static void d(String info) {
        if (DEBUG) {
            Log.d(TAG, info);
        }
    }
}
