package com.tyrese.bluetoothchatdemo;

import java.io.Closeable;

/**
 * Created by Tyrese on 2016/7/8.
 */
public class StreamUtil {

    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
