package com.gloomyer.blerq.guess;

import androidx.annotation.NonNull;

/**
 * Time: 1/13/21
 * Author: Gloomy
 * Description: 猜测逻辑接口
 */
public interface GuessRule<T1, T2, R> {
    /**
     * 猜测逻辑
     *
     * @param data1 数据1
     * @param data2 数据2
     * @return 猜测的结果
     */
    @NonNull
    R guess(T1 data1, T2 data2);
}
