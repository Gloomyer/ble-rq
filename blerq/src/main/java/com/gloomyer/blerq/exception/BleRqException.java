package com.gloomyer.blerq.exception;

import androidx.annotation.StringRes;

import com.gloomyer.blerq.utils.ContextUtils;

import java.lang.reflect.Field;

/**
 * Time: 1/13/21
 * Author: Gloomy
 * Description:
 */
public class BleRqException extends RuntimeException {

    public BleRqException(@StringRes int msgRes) {
        String msg = ContextUtils.getAppContext().getResources().getString(msgRes);
        try {
            Field field = getClass().getDeclaredField("detailMessage");
            field.setAccessible(true);
            field.set(this, msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public BleRqException(String msg) {
        super(msg);
    }
}
