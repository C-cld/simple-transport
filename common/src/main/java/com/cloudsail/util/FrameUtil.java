package com.cloudsail.util;

import java.util.Arrays;

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

    /**
     * 16进制字符串转byte[]
     * @param hexString
     * @return
     */
    public static byte[] hexString2ByteArr(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return new byte[0];
        }
        hexString = hexString.replace(" ", "");
        int length = hexString.length() >> 1;
        byte[] buff = new byte[length];
        for (int i = 0; i < length; i ++) {
            String sub = hexString.substring(i * 2, i * 2 + 2);
            byte hex = Integer.valueOf(sub, 16).byteValue();
            buff[i] = hex;
        }
        return buff;
    }

    /**
     * 读取报文中的整型
     * @param buff 整个报文
     * @param offset 起始位置
     * @param littleEndian 小端对齐（高字节在前，低字节在后）？
     * @return
     */
    public static int readInt16(byte[] buff, int offset, boolean littleEndian) {
        int retval = 0;

        if(littleEndian) {
            retval = (((buff[offset + 1] & 0xFF) << 8) | (buff[offset] & 0xFF));
        } else {
            retval = (((buff[offset] & 0xFF) << 8) | (buff[offset + 1] & 0xFF));
        }

        return retval;
    }

    public static int fillInt16(byte[] buff, int offset, int val, boolean littleEndian) {
        int retval = offset;

        if (littleEndian) {
            buff[retval++] = (byte) val;
            buff[retval++] = (byte) (val >> 8);
        } else {
            buff[retval++] = (byte) (val >> 8);
            buff[retval++] = (byte) val;
        }

        return retval;
    }

    /**
     * 读取报文中的整型
     * @param buff 整个报文
     * @param offset 起始位置
     * @param littleEndian 小端对齐（高字节在前，低字节在后）？
     * @return
     */
    public static int readInt32(byte[] buff, int offset, boolean littleEndian) {
        int retval = 0;
        if (littleEndian) {
            retval = (((buff[offset + 3] & 0xFF) << 24) | ((buff[offset + 2] & 0xFF) << 16) |
                    ((buff[offset + 1] & 0xFF) << 8) | (buff[offset] & 0xFF));
        } else {
            retval = (((buff[offset] & 0xFF) << 24) | ((buff[offset + 1] & 0xFF) << 16) |
                    ((buff[offset + 2] & 0xFF) << 8) | (buff[offset + 3] & 0xFF));
        }
        return retval;
    }

    public static int fillInt32(byte[] buff, int offset, int val, boolean littleEndian) {
        int retval = offset;

        if (littleEndian) {
            buff[retval++] = (byte) val;
            buff[retval++] = (byte) (val >> 8);
            buff[retval++] = (byte) (val >> 16);
            buff[retval++] = (byte) (val >> 24);
        } else {
            buff[retval++] = (byte) (val >> 24);
            buff[retval++] = (byte) (val >> 16);
            buff[retval++] = (byte) (val >> 8);
            buff[retval++] = (byte) val;
        }

        return retval;
    }

    /**
     * 判断两个二维数组是否相同
     * @param array1
     * @param array2
     * @return
     */
    public static boolean twoDArraysEquals(int[][] array1, int[][] array2){
        if (array1==array2)
            return true;
        if (array1==null || array2==null)
            return false;

        if (array1.length != array2.length)
            return false;

        if (array1[0].length != array2[0].length)
            return false;

        for (int i = 0; i < array1.length; i++) {  //二维数组中的每个数组使用equals方法比较
            if (Arrays.equals(array1[i],array2[i])==false)
                return false;  //有一个为false，返回false
        }
        return true;
    }
}
