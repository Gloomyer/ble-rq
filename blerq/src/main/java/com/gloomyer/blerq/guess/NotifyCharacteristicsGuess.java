package com.gloomyer.blerq.guess;

import android.bluetooth.BluetoothGattCharacteristic;

/**
 * Time: 1/13/21
 * Author: Gloomy
 * Description: 通知 Characteristics 猜测器
 */
public class NotifyCharacteristicsGuess {

    private enum INS {
        INS;
        NotifyCharacteristicsGuess ins;

        INS() {
            ins = new NotifyCharacteristicsGuess();
        }
    }

    public static NotifyCharacteristicsGuess instance() {
        return INS.INS.ins;
    }

    private GuessRule<String, BluetoothGattCharacteristic, Boolean> customRule;

    private NotifyCharacteristicsGuess() {

    }

    /**
     * 自定义猜测结果
     *
     * @param customRule customRule
     */
    public void setCustomRule(GuessRule<String, BluetoothGattCharacteristic, Boolean> customRule) {
        this.customRule = customRule;
    }

    /**
     * 猜测是否可以通知
     *
     * @param address        蓝牙设备mac地址
     * @param characteristic 要猜测的characteristic
     * @return 猜测的结果
     */
    public boolean guess(String address, BluetoothGattCharacteristic characteristic) {
        if (customRule != null) return customRule.guess(address, characteristic);
        else return (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0
                || (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0;
    }
}
