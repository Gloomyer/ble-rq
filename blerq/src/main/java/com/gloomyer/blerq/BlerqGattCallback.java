package com.gloomyer.blerq;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattService;
import android.os.Handler;
import android.os.Looper;

import com.gloomyer.blerq.callback.BleRqScanCallback;
import com.gloomyer.blerq.code.BleRqError;
import com.gloomyer.blerq.log.BleRqLogger;

import java.util.List;

/**
 * Time: 1/13/21
 * Author: Gloomy
 * Description: gatt callback
 */
class BlerqGattCallback extends BluetoothGattCallback {

    private final BleRqLogger logger;
    private final long connTimeout;
    private final Handler mHandler;

    private BleRqScanCallback scanCallback;
    private BluetoothGatt bluetoothGatt;

    public BlerqGattCallback(BleRqLogger logger, long connTimeout, BleRqScanCallback scanCallback) {
        this.logger = logger;
        this.connTimeout = connTimeout;
        this.scanCallback = scanCallback;
        //当前构造方法被调用说明开始尝试连接了 设定一个超时时间 标示连接失败.
        mHandler = new Handler(Looper.getMainLooper());
        mHandler.postDelayed(AutoCancelRunnable, connTimeout);
    }

    private final Runnable AutoCancelRunnable = new Runnable() {
        @Override
        public void run() {
            logger.info(BleRqError.DEVICE_CONNECT_TIMEOUT.toString());
            scanCallback.onError(BleRqError.DEVICE_CONNECT_TIMEOUT);
        }
    };


    public void close() {
        scanCallback = null;
        bluetoothGatt = null;
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        logger.info("onConnectionStateChange status: {0}, newState: {1}", status, newState);
        if (BluetoothGatt.STATE_CONNECTED == newState) {
            this.bluetoothGatt = gatt;
            mHandler.removeCallbacks(AutoCancelRunnable);
            mHandler.postDelayed(AutoCancelRunnable, connTimeout);

            gatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH);
            gatt.discoverServices();
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        super.onServicesDiscovered(gatt, status);
        mHandler.removeCallbacks(AutoCancelRunnable);

        logger.info("onServicesDiscovered status: {0}", status);
        List<BluetoothGattService> services = gatt.getServices();
        logger.info("onServicesDiscovered gatt services size : {0}", services.size());
        for (BluetoothGattService gattService : services) {
            logger.info("found onServicesDiscovered gattService: {0}", gattService.getUuid());
        }

    }

}
