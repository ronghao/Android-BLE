package com.example.admin.mybledemo.utils;

/**
 * 16进制转换
 *
 * @author haohao(ronghao3508 gmail.com) on 2019/6/4 19:47
 * @version v1.0
 */
public class HexUtil {

    public static String toHexString(byte[] bytes) {
        if (bytes == null) {
            throw new IllegalArgumentException("Argument b ( byte array ) is null! ");
        } else {
            String hs = "";
            String stmp;
            for (byte aByte : bytes) {
                stmp = Integer.toHexString(aByte & 255);
                if (stmp.length() == 1) {
                    hs = hs + "0" + stmp;
                } else {
                    hs = hs + stmp;
                }
            }

            return hs.toUpperCase();
        }
    }

    public static byte[] fromHexString(String hexString) {
        if (hexString == null || "".equals(hexString)) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] data = new byte[length];
        int i = 0;
        while (i < length) {
            int pos = i * 2;
            data[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
            i++;
        }
        return data;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }
}
