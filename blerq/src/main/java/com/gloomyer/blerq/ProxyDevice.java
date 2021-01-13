package com.gloomyer.blerq;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Context;

import com.gloomyer.blerq.callback.BleRqScanCallback;
import com.gloomyer.blerq.log.BleRqLogger;

import java.util.UUID;

/**
 * Time: 1/13/21
 * Author: Gloomy
 * Description: 设备代理对象
 */
class ProxyDevice {
    private final BluetoothDevice device;
    private final BleRqLogger logger;

    private BluetoothGatt bluetoothGatt;
    private BlerqGattCallback blerqGattCallback;

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

    public void connect(Context context, long connTimeout, BleRqScanCallback scanCallback,
                        UUID serviceUuid, UUID writeChannelUuid, UUID readChannelUuid, UUID notifyChannelUuid) {
        blerqGattCallback = new BlerqGattCallback(logger, connTimeout, scanCallback,
                serviceUuid, writeChannelUuid, readChannelUuid, notifyChannelUuid);
        bluetoothGatt = device.connectGatt(context, true, blerqGattCallback);
    }

    public void close() {
        if (blerqGattCallback != null) blerqGattCallback.close();
        blerqGattCallback = null;
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
