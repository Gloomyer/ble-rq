package com.gloomyer.blerq;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.os.Handler;
import android.os.Looper;

import com.gloomyer.blerq.callback.BleRqScanCallback;
import com.gloomyer.blerq.code.BleRqError;
import com.gloomyer.blerq.guess.NotifyCharacteristicsGuess;
import com.gloomyer.blerq.guess.ServicesGuess;
import com.gloomyer.blerq.log.BleRqLogger;

import java.util.List;
import java.util.UUID;

/**
 * Time: 1/13/21
 * Author: Gloomy
 * Description: gatt callback
 */
class BlerqGattCallback extends BluetoothGattCallback {

    private final BleRqClient bleRqClient;
    private final BleRqLogger logger;
    private final long connTimeout;
    private final Handler mHandler;

    private final UUID serviceUuid;
    private final UUID writeChannelUuid;
    private final UUID readChannelUuid;
    private final UUID notifyChannelUuid;

    private BleRqScanCallback scanCallback;
    private BluetoothGatt bluetoothGatt;

    public BlerqGattCallback(BleRqClient bleRqClient, BleRqLogger logger, long connTimeout, BleRqScanCallback scanCallback,
                             UUID serviceUuid, UUID writeChannelUuid, UUID readChannelUuid, UUID notifyChannelUuid) {
        this.bleRqClient = bleRqClient;
        this.logger = logger;
        this.connTimeout = connTimeout;
        this.scanCallback = scanCallback;

        this.serviceUuid = serviceUuid;
        this.writeChannelUuid = writeChannelUuid;
        this.readChannelUuid = readChannelUuid;
        this.notifyChannelUuid = notifyChannelUuid;

        //当前构造方法被调用说明开始尝试连接了 设定一个超时时间 标示连接失败.
        mHandler = new Handler(Looper.getMainLooper());
        mHandler.postDelayed(autoCancelRunnable, connTimeout);
    }

    private Runnable autoCancelRunnable = new Runnable() {
        @Override
        public void run() {
            logger.info(BleRqError.DEVICE_CONNECT_TIMEOUT.toString());
            if (scanCallback != null) scanCallback.onError(BleRqError.DEVICE_CONNECT_TIMEOUT);
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
            if (autoCancelRunnable != null) {
                //只有第一次成功 才启动定时检测连接是否成功
                mHandler.removeCallbacks(autoCancelRunnable);
                mHandler.postDelayed(autoCancelRunnable, connTimeout);
                autoCancelRunnable = null;
            }

            gatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH);
            gatt.discoverServices();
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        logger.info("onServicesDiscovered status: {0}", status);

        if (autoCancelRunnable != null) {
            mHandler.removeCallbacks(autoCancelRunnable);
        }

        if (serviceUuid == null) {
            List<BluetoothGattService> services = gatt.getServices();
            logger.info("onServicesDiscovered gatt services size : {0}", services.size());
            if (services.isEmpty()) {
                if (scanCallback != null)
                    scanCallback.onError(BleRqError.DEVICE_NOT_FOUND_SERVICES);
            } else {
                BluetoothGattService service = ServicesGuess.instance().guess(gatt.getDevice().getAddress(), services);
                queryCharacteristic(gatt, service);
            }
            for (BluetoothGattService gattService : services) {
                logger.info("found onServicesDiscovered gattService: {0}", gattService.getUuid());
            }
        } else {
            BluetoothGattService service = gatt.getService(serviceUuid);
            if (service == null) {
                if (scanCallback != null)
                    scanCallback.onError(BleRqError.DEVICE_NOT_FOUND_SERVICES);
            } else {
                queryCharacteristic(gatt, service);
            }
        }

        //到了这一步就可以认为连接完成了
        //但是常见情况 这里虽然完成但是立马发送数据会失败,所以做一个延时
        //这种特殊情况 只要不停的重复连接一个设备非常容易触发
        //这里非主线程直接暂停就好了
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (scanCallback != null) scanCallback.onSuccess(bleRqClient);
    }

    /**
     * 根据 service 查询 channel 并且设置channel
     *
     * @param gatt    gatt
     * @param service service
     */
    private void queryCharacteristic(BluetoothGatt gatt, BluetoothGattService service) {
        List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
        for (BluetoothGattCharacteristic characteristic : characteristics) {
            if (readChannelUuid != null && readChannelUuid.equals(characteristic.getUuid())) {
                configCharacteristicNotification(gatt, characteristic);
            } else {
                if (NotifyCharacteristicsGuess.instance().guess(gatt.getDevice().getAddress(), characteristic)) {
                    configCharacteristicNotification(gatt, characteristic);
                }
            }
        }
    }

    private void configCharacteristicNotification(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        boolean resp = gatt.setCharacteristicNotification(characteristic, true);
        logger.info("set setCharacteristicNotification, uuid: {0}, result: {1}", characteristic.getUuid(), resp);

        //兼容小米手机系列
        List<BluetoothGattDescriptor> descriptors = characteristic.getDescriptors();
        for (BluetoothGattDescriptor descriptor : descriptors) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            gatt.writeDescriptor(descriptor);
        }
    }

}
