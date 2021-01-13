package com.gloomyer.blerq.guess;

import android.bluetooth.BluetoothGattService;

import androidx.annotation.NonNull;

import java.util.List;

/**
 * Time: 1/13/21
 * Author: Gloomy
 * Description: 服务猜测实现
 */
public class ServicesGuess {

    private enum INS {
        INS;
        ServicesGuess ins;

        INS() {
            //noinspection InstantiationOfUtilityClass
            ins = new ServicesGuess();
        }
    }

    public static ServicesGuess instance() {
        return INS.INS.ins;
    }

    private GuessRule<String, List<BluetoothGattService>, BluetoothGattService> customRule;

    private ServicesGuess() {

    }

    /**
     * 自定义猜测结果
     *
     * @param customRule customRule
     */
    public void setCustomRule(GuessRule<String, List<BluetoothGattService>, BluetoothGattService> customRule) {
        this.customRule = customRule;
    }

    /**
     * 猜测服务
     *
     * @param address  蓝牙设备mac地址
     * @param services 要猜测的服务列表
     * @return 猜测的结果
     */
    @NonNull
    public BluetoothGattService guess(String address, List<BluetoothGattService> services) {
        if (customRule != null) return customRule.guess(address, services);
        else return services.get(0);
    }
}
