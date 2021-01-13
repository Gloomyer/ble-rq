package com.gloomyer.blerq.utils;

/**
 * Time: 1/13/21
 * Author: Gloomy
 * Description: 十六进制工具类
 */
public class HexUtils {

    private final static char[] mChars = "0123456789ABCDEF".toCharArray();

    /**
     * 将字节 转换为可视化的十六进制字符串
     *
     * @param b 需要转换的字节数组
     * @return 返回转换完之后的数据
     */
    public static String bytes2HexString(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (byte value : b) {
            sb.append(mChars[(value & 0xFF) >> 4]);
            sb.append(mChars[value & 0x0F]);
            sb.append(' ');
        }
        return sb.toString().trim();
    }
}
