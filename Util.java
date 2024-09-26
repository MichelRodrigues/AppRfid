package com.UHF.scanlable;

import android.content.Context;
import android.widget.EditText;
import android.widget.Toast;

/* loaded from: classes.dex */
public class Util {
    public static boolean showWarning(Context context, int resRes) {
        Toast.makeText(context, resRes, 1).show();
        return false;
    }

    public static boolean isEtEmpty(EditText editText) {
        String obj = editText.getText().toString();
        return obj == null || obj.equals(android.device.sdk.BuildConfig.FLAVOR);
    }

    public static boolean isLenLegal(EditText editText) {
        String obj;
        return (isEtEmpty(editText) || (obj = editText.getText().toString()) == null || obj.length() % 2 != 0) ? false : true;
    }

    public static boolean isEtsLegal(EditText[] ets) {
        for (EditText editText : ets) {
            if (isLenLegal(editText)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isHexNumber(String str) {
        int i = 0;
        boolean z = false;
        while (i < str.length()) {
            char charAt = str.charAt(i);
            if (charAt != '0' && charAt != '1' && charAt != '2' && charAt != '3' && charAt != '4' && charAt != '5' && charAt != '6' && charAt != '7' && charAt != '8' && charAt != '9' && charAt != 'A' && charAt != 'B' && charAt != 'C' && charAt != 'D' && charAt != 'E' && charAt != 'F' && charAt != 'a' && charAt != 'b' && charAt != 'c' && charAt != 'c' && charAt != 'd' && charAt != 'e' && charAt != 'f') {
                return false;
            }
            i++;
            z = true;
        }
        return z;
    }

    public static String bytesToHexString(byte[] src, int offset, int length) {
        StringBuilder sb = new StringBuilder(android.device.sdk.BuildConfig.FLAVOR);
        if (src != null) {
            try {
                if (src.length > 0) {
                    while (offset < length) {
                        String hexString = Integer.toHexString(src[offset] & 255);
                        if (hexString.length() == 1) {
                            sb.append(0);
                        }
                        sb.append(hexString);
                        offset++;
                    }
                    return sb.toString().toUpperCase();
                }
            } catch (Exception unused) {
            }
        }
        return null;
    }

    public static byte[] hexStringToBytes(String hexString) {
        if (hexString != null) {
            try {
                if (!hexString.equals(android.device.sdk.BuildConfig.FLAVOR)) {
                    String upperCase = hexString.toUpperCase();
                    int length = upperCase.length() / 2;
                    char[] charArray = upperCase.toCharArray();
                    byte[] bArr = new byte[length];
                    for (int i = 0; i < length; i++) {
                        int i2 = i * 2;
                        bArr[i] = (byte) (charToByte(charArray[i2 + 1]) | (charToByte(charArray[i2]) << 4));
                    }
                    return bArr;
                }
            } catch (Exception unused) {
            }
        }
        return null;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }
}
