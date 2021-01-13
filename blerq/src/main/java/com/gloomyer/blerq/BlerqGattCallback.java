package com.gloomyer.blerq;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

import com.gloomyer.blerq.log.BleRqLogger;

import java.util.List;

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
        List<BluetoothGattService> services = gatt.getServices();
        logger.info("onServicesDiscovered gatt services size : {0}", services.size());
        for (BluetoothGattService gattService : services) {
            logger.info("found onServicesDiscovered gattService: {0}", gattService.getUuid());
        }

    }
}
