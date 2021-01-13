package com.gloomyer.blerq.callback;

import android.bluetooth.le.ScanResult;

/**
 * Time: 1/13/21
 * Author: Gloomy
 * Description: 扫描回调 用于确认要连接哪个设备
 */
public interface BleRqScanCallback {
    boolean isNeedConnDevice(ScanResult device);
}
