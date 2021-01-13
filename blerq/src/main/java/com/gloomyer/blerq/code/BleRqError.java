package com.gloomyer.blerq.code;

import com.gloomyer.blerq.R;

/**
 * Time: 1/13/21
 * Author: Gloomy
 * Description: 全局错误码管理
 */
public enum BleRqError {

    DEVICE_NOT_SUPPORT(0xF0, R.string.blerq_device_not_support),
    DEVICE_NOT_FOUND(0xF1, R.string.blerq_device_not_found),
    DEVICE_CONNECTION_FAILED(0xF2, R.string.blerq_device_connection_failed),
    DEVICE_NO_PERMISSION(0xF3, R.string.blerq_device_no_permission),
    DEVICE_BLUETOOTH_NOT_OPEN(0xF4, R.string.blerq_device_not_open);


    public final int code;
    public final int msg;

    BleRqError(int code, int msg) {
        this.code = code;
        this.msg = msg;
    }


}
