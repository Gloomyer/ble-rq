package com.gloomyer.blerq;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

import com.gloomyer.blerq.log.BleRqLogger;

/**
 * Time: 1/13/21
 * Author: Gloomy
 * Description: gatt callback
 */
class BlerqGattCallback extends BluetoothGattCallback {

    private final BleRqLogger logger;
    private BluetoothGatt bluetoothGatt;

    public BlerqGattCallback(BleRqLogger logger) {
        this.logger = logger;
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        logger.info("onConnectionStateChange status: {0}, newState: {1}", status, newState);
        if (BluetoothGatt.STATE_CONNECTED == newState) {
            this.bluetoothGatt = gatt;
            gatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH);
            gatt.discoverServices();
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        super.onServicesDiscovered(gatt, status);
        logger.info("onServicesDiscovered status: {0}", status);

    }
}
