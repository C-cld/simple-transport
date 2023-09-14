package com.sail.util;

public class FrameUtil {
    /**
     * byte[]转16进制字符串
     * @param data
     * @return
     */
    public static String byteArr2HexString(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            sb.append(String.format("%02x", ((int) b & 0xff))).append(" ");
        }
        return sb.toString().toUpperCase();
    }
}
