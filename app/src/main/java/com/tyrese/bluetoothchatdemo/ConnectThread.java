package com.tyrese.bluetoothchatdemo;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by Tyrese on 2016/7/8.
 */
public class ConnectThread extends Thread {

    private static final UUID MY_UUID =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    private Handler mHandler;
    private BluetoothDevice mDevice;
    private BluetoothSocket mSocket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private BufferedInputStream bis;
    private boolean isCancel = false;

    public ConnectThread(BluetoothDevice device, Handler mHandler) {
        this.mHandler = mHandler;
        this.mDevice = device;
    }

    @Override
    public void run() {
        try {
            mSocket = mDevice.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            e.printStackTrace();
            StreamUtil.close(mSocket);
            LogWrapper.d("连接失败1");
            return;
        }

        try {
            mSocket.connect();
        } catch (IOException e) {
            e.printStackTrace();
            StreamUtil.close(mSocket);
            LogWrapper.d("连接失败2");
            return;
        }

        try {
            inputStream = mSocket.getInputStream();
            outputStream = mSocket.getOutputStream();
//            bis = new BufferedInputStream(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
            LogWrapper.d("获取流失败");
            return;
        }

        byte[] buffer = new byte[1024];
        int n = 0;
        while (!isCancel) {
            try {
//                while ((n = inputStream.read(buffer, 0, buffer.length)) != -1) {
//                fos.write(buffer, 0, n);
//                }
                inputStream.read(buffer);
                String result = new String(buffer);
                LogWrapper.d(result.trim());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void write(byte[] buffer) {
        try {
            outputStream.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void flush() {
        try {
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cancel() {
        isCancel = true;
        StreamUtil.close(inputStream);
        StreamUtil.close(outputStream);
        StreamUtil.close(mSocket);
    }
}
