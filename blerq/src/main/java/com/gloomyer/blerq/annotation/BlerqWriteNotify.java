package com.gloomyer.blerq.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;

/**
 * Time: 1/13/21
 * Author: Gloomy
 * Description: [写<->通知] 获取数据模型
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({METHOD})
public @interface BlerqWriteNotify {

    /**
     * 要发送的数据
     *
     * @return 要发送的数据
     */
    byte[] sendData() default {};

}
