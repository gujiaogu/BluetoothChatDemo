package com.tyrese.bluetoothchatdemo;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by Tyrese on 2016/7/8.
 */
public class ConnectFileThread extends Thread {

    private static final UUID MY_UUID =
            UUID.fromString("7a051945-79c9-40cc-9947-0e9f3862db16");

    public static final int STATE_FILE_NOT_SEND = 1;
    public static final int STATE_FILE_SENDING = 2;
    public static final int STATE_FILE_SEND_FAIL = 3;
    public static final int STATE_FILE_SEND_SUCCESS = 4;

    private Handler mHandler;
    private BluetoothDevice mDevice;
    private BluetoothSocket mSocket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private String filePath;
    private int state = STATE_FILE_NOT_SEND;

    public ConnectFileThread(BluetoothDevice device, Handler mHandler, String filePath) {
        this.mHandler = mHandler;
        this.mDevice = device;
        this.filePath = filePath;
    }

    @Override
    public void run() {
        setSendState(STATE_FILE_SENDING);
        mHandler.obtainMessage(MainActivity.MESSAGE_START).sendToTarget();
        try {
            mSocket = mDevice.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            e.printStackTrace();
            LogWrapper.d("连接失败1");
            setSendState(STATE_FILE_SEND_FAIL);
            mHandler.obtainMessage(MainActivity.MESSAGE_FAIL).sendToTarget();
            cancel();
            return;
        }

        try {
            mSocket.connect();
        } catch (IOException e) {
            e.printStackTrace();
            LogWrapper.d("连接失败2");
            setSendState(STATE_FILE_SEND_FAIL);
            mHandler.obtainMessage(MainActivity.MESSAGE_FAIL).sendToTarget();
            cancel();
            return;
        }

        try {
            inputStream = mSocket.getInputStream();
            outputStream = mSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
            LogWrapper.d("获取流失败");
            setSendState(STATE_FILE_SEND_FAIL);
            mHandler.obtainMessage(MainActivity.MESSAGE_FAIL).sendToTarget();
            cancel();
            return;
        }

        FileInputStream fis;
        try {
            File file = new File(filePath);
            fis = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            while (fis.read(buffer, 0, buffer.length) != -1) {
                outputStream.write(buffer);
            }
            outputStream.flush();
            LogWrapper.d(filePath);
            TimeUnit.MILLISECONDS.sleep(300);
            setSendState(STATE_FILE_SEND_SUCCESS);
            mHandler.obtainMessage(MainActivity.MESSAGE_SUCCESS).sendToTarget();
        } catch (IOException | InterruptedException e) {
            setSendState(STATE_FILE_SEND_FAIL);
            mHandler.obtainMessage(MainActivity.MESSAGE_FAIL).sendToTarget();
            e.printStackTrace();
        } finally {
            cancel();
        }
    }

    public void cancel() {
        StreamUtil.close(mSocket);
        StreamUtil.close(inputStream);
        StreamUtil.close(outputStream);
    }

    public int getSendState() {
        return state;
    }

    public void setSendState(int state) {
        this.state = state;
    }
}
