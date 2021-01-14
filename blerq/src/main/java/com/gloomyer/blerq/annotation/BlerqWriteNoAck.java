package com.gloomyer.blerq.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;

/**
 * Time: 1/14/21
 * Author: Gloomy
 * Description:
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({METHOD})
public @interface BlerqWriteNoAck {
    /**
     * 要发送的数据
     *
     * @return 要发送的数据
     */
    byte[] sendData() default {};

    /**
     * 自定义返回结果
     *
     * @return 自定义返回结果
     */
    byte[] replyData() default {0x00};
}
