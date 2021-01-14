package com.gloomyer.blerq;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Time: 1/14/21
 * Author: Gloomy
 * Description: 接口代理助手类
 */
class ApiProxyHelper {


    public static <T> T getApi(Class<T> clazz) {
        Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[] {clazz}, new InvocationHandler() {


            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (method.getDeclaringClass() == Object.class) {
                    return method.invoke(this, args);
                }


                return null;
            }
        });
        return null;
    }
}
