package com.gloomyer.blerq.callback;

import android.bluetooth.le.ScanResult;

import androidx.annotation.NonNull;

import com.gloomyer.blerq.BleRqClient;
import com.gloomyer.blerq.code.BleRqError;

/**
 * Time: 1/13/21
 * Author: Gloomy
 * Description: 扫描回调 用于确认要连接哪个设备
 */
public interface BleRqScanCallback {
    /**
     * 判断当前设备是否是要连接的那个
     *
     * @param callbackType callbackType
     * @param device       设备
     * @return 返回true表示就是这个设备 false表示不是
     */
    boolean isNeedConnDevice(int callbackType, @NonNull ScanResult device);

    /**
     * 当连接发生了错误
     *
     * @param error 错误
     */
    void onError(@NonNull BleRqError error);

    /**
     * 当成功连接设备 此方法调用之后表示可以开始发送数据了
     *
     * @param client ble rq客户端
     */
    void onSuccess(@NonNull BleRqClient client);
}
