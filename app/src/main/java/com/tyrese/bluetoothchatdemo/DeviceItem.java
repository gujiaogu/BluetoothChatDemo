package com.tyrese.bluetoothchatdemo;

import android.bluetooth.BluetoothDevice;

/**
 * Created by Tyrese on 2016/7/8.
 */
public class DeviceItem {

    private BluetoothDevice device;

    public DeviceItem(BluetoothDevice device) {
        this.device = device;
    }

    public String getName() {
        if (device != null) {
            return device.getName();
        }
        return "";
    }
    public String getAddress() {
        if (device != null) {
            return device.getAddress();
        }
        return "";
    }

    public BluetoothDevice getDevice() {
        return device;
    }
}
