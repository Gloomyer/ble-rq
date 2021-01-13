package com.gloomyer.blerq.utils;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import java.lang.reflect.Method;

/**
 * Time: 1/13/21
 * Author: Gloomy
 * Description:
 */
public class ContextUtils {

    private static Application appContext;

    @SuppressLint("PrivateApi")
    public static Context getAppContext() {
        if (appContext == null) {
            synchronized (ContextUtils.class) {
                if (appContext == null) {
                    try {
                        Class<?> atClass = Class.forName("android.app.ActivityThread");
                        @SuppressLint("DiscouragedPrivateApi")
                        Method currentApplicationMethod = atClass.getDeclaredMethod("currentApplication");
                        currentApplicationMethod.setAccessible(true);
                        appContext = (Application) currentApplicationMethod.invoke(null);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (appContext == null) {
                        try {
                            Class<?> atClass = Class.forName("android.app.AppGlobals");
                            @SuppressLint("DiscouragedPrivateApi")
                            Method currentApplicationMethod = atClass.getDeclaredMethod("getInitialApplication");
                            currentApplicationMethod.setAccessible(true);
                            appContext = (Application) currentApplicationMethod.invoke(null);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }

                }
            }
        }


        return appContext;
    }
}
