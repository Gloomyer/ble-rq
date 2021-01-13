package com.gloomyer.blerq;

import android.bluetooth.BluetoothGattCallback;

import com.gloomyer.blerq.log.BleRqLogger;

/**
 * Time: 1/13/21
 * Author: Gloomy
 * Description: gatt callback
 */
class BlerqGattCallback extends BluetoothGattCallback {
    final BleRqLogger logger;

    public BlerqGattCallback(BleRqLogger logger) {
        this.logger = logger;
    }
}
