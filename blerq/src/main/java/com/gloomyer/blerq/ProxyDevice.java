package com.gloomyer.blerq;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Context;

import com.gloomyer.blerq.log.BleRqLogger;

/**
 * Time: 1/13/21
 * Author: Gloomy
 * Description: 设备代理对象
 */
class ProxyDevice {
    private final BluetoothDevice device;
    private final BleRqLogger logger;

    private Context context;
    private BluetoothGatt bluetoothGatt;

    ProxyDevice(BluetoothDevice device, BleRqLogger logger) {
        this.device = device;
        this.logger = logger;
    }

    public String getAddress() {
        return device.getAddress();
    }

    public String getName() {
        return device.getName();
    }

    public void connect(Context context) {
        this.context = context;
        BlerqGattCallback blerqGattCallback = new BlerqGattCallback(logger);
        bluetoothGatt = device.connectGatt(context, true, blerqGattCallback);
        logger.setDeviceAddress(device.getAddress());
    }

    public void close() {
        context = null;
        if (bluetoothGatt != null) {
            try {
                bluetoothGatt.close();
            } catch (Exception e) {
                logger.info(e);
            }
        }
        bluetoothGatt = null;
    }
}
