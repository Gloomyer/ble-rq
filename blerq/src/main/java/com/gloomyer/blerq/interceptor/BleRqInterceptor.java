package com.gloomyer.blerq.interceptor;

/**
 * Time: 1/13/21
 * Author: Gloomy
 * Description: 拦截器的定义
 */
public interface BleRqInterceptor {
    /**
     * 数据发送拦截过程方法
     *
     * @param request 请求的数据
     * @return 请求返回的数据
     */
    byte[] onInterceptor(byte[] request);
}
