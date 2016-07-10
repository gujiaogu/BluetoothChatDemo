package com.tyrese.bluetoothchatdemo;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by Tyrese on 2016/7/8.
 */
public class ConnectThread extends Thread {

    private static final UUID MY_UUID =
            UUID.fromString("2b695c0a-e703-4167-875e-d230791fa275");

    public static final int STATE_OFF = 11;
    public static final int STATE_CONNECTING = 12;
    public static final int STATE_CONNECTED = 13;
    private int state = STATE_OFF;

    private Handler mHandler;
    private BluetoothDevice mDevice;
    private BluetoothSocket mSocket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private boolean isCancel = false;

    public ConnectThread(BluetoothDevice device, Handler mHandler) {
        this.mHandler = mHandler;
        this.mDevice = device;
    }

    @Override
    public void run() {
        mHandler.obtainMessage(MainActivity.CONNECTING).sendToTarget();
        state = STATE_CONNECTING;
        try {
            mSocket = mDevice.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            e.printStackTrace();
            mHandler.obtainMessage(MainActivity.CONNECTING_FAIL).sendToTarget();
            state = STATE_OFF;
            cancel();
            return;
        }

        try {
            mSocket.connect();
        } catch (IOException e) {
            e.printStackTrace();
            mHandler.obtainMessage(MainActivity.CONNECTING_FAIL).sendToTarget();
            state = STATE_OFF;
            cancel();
            return;
        }

        try {
            inputStream = mSocket.getInputStream();
            outputStream = mSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
            mHandler.obtainMessage(MainActivity.CONNECTING_FAIL).sendToTarget();
            state = STATE_OFF;
            cancel();
            return;
        }

        byte[] buffer = new byte[1024];
        mHandler.obtainMessage(MainActivity.CONNECTING_SUCCESS).sendToTarget();
        state = STATE_CONNECTED;
        while (!isCancel) {
            try {
                inputStream.read(buffer);
                String result = new String(buffer);
                LogWrapper.d(result.trim());
            } catch (IOException e) {
                mHandler.obtainMessage(MainActivity.CONNECTION_LOST).sendToTarget();
                state = STATE_OFF;
                e.printStackTrace();
                cancel();
            }
        }
    }

    public void write(byte[] buffer) {
        try {
            if (outputStream != null) {
                outputStream.write(buffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cancel() {
        isCancel = true;
        StreamUtil.close(inputStream);
        StreamUtil.close(outputStream);
        StreamUtil.close(mSocket);
        inputStream = null;
        outputStream = null;
        mSocket = null;
        mDevice = null;
    }

    public int getConnectState() {
        return state;
    }

    public void setConnectState(int state) {
        this.state = state;
    }
}
