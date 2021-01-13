package com.gloomyer.blerq.exception;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.gloomyer.blerq.utils.ContextUtils;

import java.lang.reflect.Field;

/**
 * Time: 1/13/21
 * Author: Gloomy
 * Description:
 */
public class BleRqException extends RuntimeException {

    private String message;

    public BleRqException(@StringRes int msgRes) {
        message = ContextUtils.getAppContext().getResources().getString(msgRes);
    }

    public BleRqException(String msg) {
        super(msg);
        this.message = message;
    }

    @Nullable
    @Override
    public String getMessage() {
        return message;
    }
}
