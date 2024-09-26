package com.rfid.trans;

import android.device.sdk.BuildConfig;
import android.os.SystemClock;
import android.support.graphics.drawable.PathInterpolatorCompat;
import android.support.v4.internal.view.SupportMenu;
import android.util.Log;
import java.util.List;

/* loaded from: classes.dex */
public class BaseReader {
    private TagCallback callback;
    private RFIDLogCallBack msgCallback;
    private MessageTran msg = new MessageTran();
    private long maxScanTime = 2000;
    private int[] recvLength = new int[1];
    private byte[] recvBuff = new byte[20000];
    private int logswitch = 0;
    private int lastPacket = 0;
    private volatile String strEPC = BuildConfig.FLAVOR;
    int packIndex = -1;

    public void PowerControll(int i) {
    }

    private void getCRC(byte[] bArr, int i) {
        int i2 = SupportMenu.USER_MASK;
        int i3 = 0;
        while (i3 < i) {
            try {
                i2 ^= bArr[i3] & 255;
                for (int i4 = 0; i4 < 8; i4++) {
                    i2 = (i2 & 1) != 0 ? (i2 >> 1) ^ 33800 : i2 >> 1;
                }
                i3++;
            } catch (Exception unused) {
                return;
            }
        }
        bArr[i3] = (byte) (i2 & 255);
        bArr[i3 + 1] = (byte) ((i2 >> 8) & 255);
    }

    private boolean CheckCRC(byte[] bArr, int i) {
        try {
            byte[] bArr2 = new byte[256];
            System.arraycopy(bArr, 0, bArr2, 0, i);
            getCRC(bArr2, i);
            if (bArr2[i + 1] == 0) {
                if (bArr2[i] == 0) {
                    return true;
                }
            }
        } catch (Exception unused) {
        }
        return false;
    }

    public String bytesToHexString(byte[] bArr, int i, int i2) {
        StringBuilder sb = new StringBuilder(BuildConfig.FLAVOR);
        if (bArr != null) {
            try {
                if (bArr.length > 0) {
                    while (i < i2) {
                        String hexString = Integer.toHexString(bArr[i] & 255);
                        if (hexString.length() == 1) {
                            sb.append(0);
                        }
                        sb.append(hexString);
                        i++;
                    }
                    return sb.toString().toUpperCase();
                }
            } catch (Exception unused) {
            }
        }
        return null;
    }

    public byte[] hexStringToBytes(String str) {
        if (str != null) {
            try {
                if (!str.equals(BuildConfig.FLAVOR)) {
                    String upperCase = str.toUpperCase();
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

    private byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public void SetCallBack(TagCallback tagCallback) {
        this.callback = tagCallback;
    }

    public void SetMsgCallBack(RFIDLogCallBack rFIDLogCallBack) {
        this.msgCallback = rFIDLogCallBack;
    }

    public int Connect(String str, int i, int i2) {
        this.logswitch = i2;
        return this.msg.open(str, i);
    }

    public void SetLogSwitch(int i) {
        this.logswitch = i;
    }

    public int DisConnect() {
        return this.msg.close();
    }

    private int SendCMD(byte[] bArr) {
        if (this.logswitch == 1) {
            Log.d("Send", bytesToHexString(bArr, 0, (bArr[0] & 255) + 1));
            RFIDLogCallBack rFIDLogCallBack = this.msgCallback;
            if (rFIDLogCallBack != null) {
                rFIDLogCallBack.SendMessageCallback(bArr);
            }
        }
        return this.msg.Write(bArr);
    }

    private int GetCMDData(byte[] bArr, int[] iArr, int i, int i2) {
        int i3;
        byte[] bArr2 = new byte[2000];
        long currentTimeMillis = System.currentTimeMillis();
        while (true) {
            int i4 = 0;
            while (System.currentTimeMillis() - currentTimeMillis < i2) {
                try {
                    byte[] Read = this.msg.Read();
                    if (Read != null) {
                        if (this.logswitch == 1) {
                            Log.d("Recv", bytesToHexString(Read, 0, Read.length));
                            RFIDLogCallBack rFIDLogCallBack = this.msgCallback;
                            if (rFIDLogCallBack != null) {
                                rFIDLogCallBack.RecvMessageCallback(Read);
                            }
                        }
                        int length = Read.length;
                        if (length != 0) {
                            int i5 = length + i4;
                            byte[] bArr3 = new byte[i5];
                            System.arraycopy(bArr2, 0, bArr3, 0, i4);
                            System.arraycopy(Read, 0, bArr3, i4, length);
                            int i6 = 0;
                            while (true) {
                                i3 = i5 - i6;
                                if (i3 <= 4) {
                                    break;
                                }
                                if ((bArr3[i6] & 255) >= 4 && (bArr3[i6 + 2] & 255) == i) {
                                    int i7 = bArr3[i6] & 255;
                                    if (i5 < i6 + i7 + 1) {
                                        break;
                                    }
                                    int i8 = i7 + 1;
                                    byte[] bArr4 = new byte[i8];
                                    System.arraycopy(bArr3, i6, bArr4, 0, i8);
                                    if (CheckCRC(bArr4, i8)) {
                                        System.arraycopy(bArr4, 0, bArr, 0, i8);
                                        iArr[0] = i8;
                                        return 0;
                                    }
                                }
                                i6++;
                            }
                            if (i5 > i6) {
                                System.arraycopy(bArr3, i6, bArr2, 0, i3);
                                i4 = i3;
                            }
                        }
                    }
                } catch (Exception e) {
                    e.toString();
                    return 48;
                }
            }
            return 48;
        }
    }

    public int GetReaderInformation(byte[] bArr, byte[] bArr2, byte[] bArr3, byte[] bArr4, byte[] bArr5, byte[] bArr6, byte[] bArr7, byte[] bArr8, byte[] bArr9, byte[] bArr10, byte[] bArr11, byte[] bArr12, byte[] bArr13) {
        byte[] bArr14 = {4, bArr[0], 33};
        getCRC(bArr14, bArr14[0] - 1);
        SendCMD(bArr14);
        if (GetCMDData(this.recvBuff, this.recvLength, 33, 500) != 0) {
            return 48;
        }
        byte[] bArr15 = this.recvBuff;
        bArr[0] = bArr15[1];
        bArr2[0] = bArr15[4];
        bArr2[1] = bArr15[5];
        bArr3[0] = bArr15[6];
        bArr4[0] = bArr15[7];
        bArr6[0] = (byte) (bArr15[8] & 63);
        bArr7[0] = (byte) (bArr15[9] & 63);
        bArr5[0] = (byte) (((bArr15[9] & 192) >> 6) | ((bArr15[8] & 192) >> 4));
        bArr8[0] = bArr15[10];
        bArr9[0] = bArr15[11];
        this.maxScanTime = (bArr9[0] & 255) * 100;
        bArr10[0] = bArr15[12];
        bArr11[0] = bArr15[13];
        bArr12[0] = bArr15[14];
        bArr13[0] = bArr15[15];
        return 0;
    }

    public int SetInventoryScanTime(byte b, byte b2) {
        byte[] bArr = {5, b, 37, b2};
        getCRC(bArr, bArr[0] - 1);
        SendCMD(bArr);
        if (GetCMDData(this.recvBuff, this.recvLength, 37, 500) == 0) {
            return this.recvBuff[3] & 255;
        }
        return 48;
    }

    private int GetInventoryData(byte b, int i, int i2, List<ReadTag> list, int[] iArr, boolean z, boolean z2) {
        int i3;
        long j;
        int i4;
        int i5;
        long j2;
        int i6 = 0;
        iArr[0] = 0;
        byte[] bArr = new byte[2000];
        long elapsedRealtime = SystemClock.elapsedRealtime();
        int i7 = 0;
        boolean z3 = false;
        int i8 = b;
        do {
            try {
                byte[] Read = this.msg.Read();
                if (Read != null) {
                    boolean z4 = true;
                    if (this.logswitch == 1) {
                        Log.d("Recv", bytesToHexString(Read, i6, Read.length));
                        RFIDLogCallBack rFIDLogCallBack = this.msgCallback;
                        if (rFIDLogCallBack != null) {
                            rFIDLogCallBack.RecvMessageCallback(Read);
                        }
                    }
                    long elapsedRealtime2 = SystemClock.elapsedRealtime();
                    int length = Read.length;
                    if (length == 0) {
                        elapsedRealtime = elapsedRealtime2;
                    } else {
                        int i9 = length + i7;
                        byte[] bArr2 = new byte[i9];
                        System.arraycopy(bArr, i6, bArr2, i6, i7);
                        System.arraycopy(Read, i6, bArr2, i7, length);
                        int i10 = 0;
                        while (true) {
                            i7 = i9 - i10;
                            if (i7 <= 5) {
                                break;
                            }
                            if ((i8 & 255) == 255) {
                                i8 = 0;
                            }
                            if ((bArr2[i10] & 255) >= 5) {
                                int i11 = i10 + 1;
                                if (bArr2[i11] == i8 && (bArr2[i10 + 2] & 255) == i) {
                                    int i12 = bArr2[i10] & 255;
                                    if (i9 < i10 + i12 + 1) {
                                        break;
                                    }
                                    int i13 = i12 + 1;
                                    byte[] bArr3 = new byte[i13];
                                    System.arraycopy(bArr2, i10, bArr3, 0, i13);
                                    if (CheckCRC(bArr3, i13)) {
                                        int i14 = i10 + (bArr3[0] & 255) + (z4 ? 1 : 0);
                                        int i15 = bArr3[3] & 255;
                                        if (i15 != z4 && i15 != 2 && i15 != 3 && i15 != 4) {
                                            return i15;
                                        }
                                        int i16 = bArr3[5] & 255;
                                        if (i16 > 0) {
                                            iArr[0] = iArr[0] + 1;
                                            boolean z5 = z3;
                                            int i17 = 0;
                                            int i18 = 6;
                                            while (i17 < i16) {
                                                if (z) {
                                                    ReaderHelp.isSound = z4;
                                                }
                                                int i19 = bArr3[i18] & 255 & 127;
                                                int i20 = i8;
                                                int i21 = (bArr3[i18] & 255) >> 7;
                                                ReadTag readTag = new ReadTag();
                                                int i22 = i14;
                                                readTag.antId = 1;
                                                if (i19 > 0) {
                                                    i5 = i16;
                                                    byte[] bArr4 = new byte[i19];
                                                    j2 = elapsedRealtime2;
                                                    System.arraycopy(bArr3, i18 + 1, bArr4, 0, i19);
                                                    if (i21 == 0) {
                                                        readTag.epcId = bytesToHexString(bArr4, 0, i19);
                                                        readTag.memId = null;
                                                    } else {
                                                        String bytesToHexString = bytesToHexString(bArr4, 0, i19);
                                                        if (bytesToHexString.length() == 24) {
                                                            readTag.epcId = BuildConfig.FLAVOR;
                                                            readTag.memId = bytesToHexString;
                                                        } else {
                                                            readTag.epcId = bytesToHexString.substring(0, bytesToHexString.length() - 24);
                                                            readTag.memId = bytesToHexString.substring(bytesToHexString.length() - 24, bytesToHexString.length());
                                                        }
                                                    }
                                                } else {
                                                    i5 = i16;
                                                    j2 = elapsedRealtime2;
                                                    readTag.epcId = BuildConfig.FLAVOR;
                                                    readTag.memId = null;
                                                }
                                                readTag.rssi = bArr3[i18 + 1 + i19] & 255;
                                                readTag.phase = 0;
                                                TagCallback tagCallback = this.callback;
                                                if (tagCallback != null && z && !z5) {
                                                    tagCallback.tagCallback(readTag);
                                                }
                                                if (list != null) {
                                                    list.add(readTag);
                                                }
                                                i18 = i18 + 2 + i19;
                                                if (z2 && !z5) {
                                                    StopInventory((byte) -1);
                                                    z5 = true;
                                                }
                                                i17++;
                                                i8 = i20;
                                                i14 = i22;
                                                i16 = i5;
                                                elapsedRealtime2 = j2;
                                                z4 = true;
                                            }
                                            i3 = i8;
                                            i4 = i14;
                                            j = elapsedRealtime2;
                                            z3 = z5;
                                        } else {
                                            i3 = i8;
                                            i4 = i14;
                                            j = elapsedRealtime2;
                                        }
                                        if (i15 != 1 && i15 != 2) {
                                            i10 = i4;
                                        }
                                        return 0;
                                    }
                                    i3 = i8;
                                    j = elapsedRealtime2;
                                    i10 = i11;
                                    i8 = i3;
                                    elapsedRealtime2 = j;
                                    z4 = true;
                                }
                            }
                            i3 = i8;
                            j = elapsedRealtime2;
                            i10++;
                            i8 = i3;
                            elapsedRealtime2 = j;
                            z4 = true;
                        }
                        long j3 = elapsedRealtime2;
                        if (i9 > i10) {
                            i6 = 0;
                            System.arraycopy(bArr2, i10, bArr, 0, i7);
                        } else {
                            i6 = 0;
                            i7 = 0;
                        }
                        elapsedRealtime = j3;
                    }
                } else {
                    SystemClock.sleep(1L);
                }
            } catch (Exception e) {
                e.toString();
                return 48;
            }
        } while (SystemClock.elapsedRealtime() - elapsedRealtime < 5000);
        return 48;
    }

    public void StopInventory(byte b) {
        byte[] bArr = {4, b, -109};
        getCRC(bArr, bArr[0] - 1);
        SendCMD(bArr);
    }

    private int GetInventorySingleData(byte b, int i, int i2, List<ReadTag> list, int[] iArr) {
        byte[] bArr;
        byte[] bArr2;
        long j;
        byte[] bArr3;
        int i3;
        int i4 = 0;
        iArr[0] = 0;
        byte[] bArr4 = new byte[2000];
        long elapsedRealtime = SystemClock.elapsedRealtime();
        int i5 = 0;
        byte b2 = b;
        while (true) {
            try {
                byte[] Read = this.msg.Read();
                if (Read != null) {
                    int i6 = 1;
                    if (this.logswitch == 1) {
                        Log.d("Recv", bytesToHexString(Read, i4, Read.length));
                        RFIDLogCallBack rFIDLogCallBack = this.msgCallback;
                        if (rFIDLogCallBack != null) {
                            rFIDLogCallBack.RecvMessageCallback(Read);
                        }
                    }
                    long elapsedRealtime2 = SystemClock.elapsedRealtime();
                    int length = Read.length;
                    if (length == 0) {
                        bArr = bArr4;
                        elapsedRealtime = elapsedRealtime2;
                    } else {
                        int i7 = length + i5;
                        byte[] bArr5 = new byte[i7];
                        System.arraycopy(bArr4, i4, bArr5, i4, i5);
                        System.arraycopy(Read, i4, bArr5, i5, length);
                        int i8 = 0;
                        while (true) {
                            i5 = i7 - i8;
                            if (i5 <= 5) {
                                break;
                            }
                            if ((b2 & 255) == 255) {
                                b2 = 0;
                            }
                            if ((bArr5[i8] & 255) >= 5) {
                                int i9 = i8 + 1;
                                if (bArr5[i9] == b2 && (bArr5[i8 + 2] & 255) == i) {
                                    int i10 = bArr5[i8] & 255;
                                    if (i7 < i8 + i10 + 1) {
                                        break;
                                    }
                                    int i11 = i10 + 1;
                                    byte[] bArr6 = new byte[i11];
                                    System.arraycopy(bArr5, i8, bArr6, i4, i11);
                                    if (CheckCRC(bArr6, i11)) {
                                        int i12 = i8 + (bArr6[i4] & 255) + i6;
                                        int i13 = bArr6[3] & 255;
                                        if (i13 != i6 && i13 != 2 && i13 != 3 && i13 != 4) {
                                            return i13;
                                        }
                                        int i14 = bArr6[5] & 255;
                                        if (i14 > 0) {
                                            iArr[i4] = iArr[i4] + i6;
                                            int i15 = 6;
                                            while (i4 < i14) {
                                                int i16 = i12;
                                                int i17 = bArr6[i15] & 255 & 127;
                                                int i18 = (bArr6[i15] & 255) >> 7;
                                                ReadTag readTag = new ReadTag();
                                                long j2 = elapsedRealtime2;
                                                readTag.antId = 1;
                                                if (i17 > 0) {
                                                    byte[] bArr7 = new byte[i17];
                                                    i3 = i14;
                                                    bArr3 = bArr4;
                                                    System.arraycopy(bArr6, i15 + 1, bArr7, 0, i17);
                                                    if (i18 == 0) {
                                                        readTag.epcId = bytesToHexString(bArr7, 0, i17);
                                                        readTag.memId = null;
                                                    } else {
                                                        String bytesToHexString = bytesToHexString(bArr7, 0, i17);
                                                        if (bytesToHexString.length() == 24) {
                                                            readTag.epcId = BuildConfig.FLAVOR;
                                                            readTag.memId = bytesToHexString;
                                                        } else {
                                                            readTag.epcId = bytesToHexString.substring(0, bytesToHexString.length() - 24);
                                                            readTag.memId = bytesToHexString.substring(bytesToHexString.length() - 24, bytesToHexString.length());
                                                        }
                                                    }
                                                    StopInventory(b2);
                                                } else {
                                                    bArr3 = bArr4;
                                                    i3 = i14;
                                                    readTag.epcId = BuildConfig.FLAVOR;
                                                    readTag.memId = null;
                                                }
                                                readTag.rssi = bArr6[i15 + 1 + i17] & 255;
                                                readTag.phase = 0;
                                                if (list != null) {
                                                    list.add(readTag);
                                                }
                                                i15 = i15 + 2 + i17;
                                                i4++;
                                                i12 = i16;
                                                elapsedRealtime2 = j2;
                                                i14 = i3;
                                                bArr4 = bArr3;
                                            }
                                        }
                                        bArr2 = bArr4;
                                        int i19 = i12;
                                        j = elapsedRealtime2;
                                        if (i13 != 1 && i13 != 2) {
                                            i8 = i19;
                                        }
                                        return 0;
                                    }
                                    bArr2 = bArr4;
                                    j = elapsedRealtime2;
                                    i8 = i9;
                                    elapsedRealtime2 = j;
                                    bArr4 = bArr2;
                                    i4 = 0;
                                    i6 = 1;
                                }
                            }
                            bArr2 = bArr4;
                            j = elapsedRealtime2;
                            i8++;
                            elapsedRealtime2 = j;
                            bArr4 = bArr2;
                            i4 = 0;
                            i6 = 1;
                        }
                        byte[] bArr8 = bArr4;
                        long j3 = elapsedRealtime2;
                        if (i7 > i8) {
                            bArr = bArr8;
                            System.arraycopy(bArr5, i8, bArr, 0, i5);
                        } else {
                            bArr = bArr8;
                            i5 = 0;
                        }
                        elapsedRealtime = j3;
                    }
                } else {
                    bArr = bArr4;
                    SystemClock.sleep(5L);
                }
                if (SystemClock.elapsedRealtime() - elapsedRealtime >= 5000) {
                    return 48;
                }
                bArr4 = bArr;
                i4 = 0;
            } catch (Exception e) {
                e.toString();
                return 48;
            }
        }
    }

    public int Inventory_G2(byte b, byte b2, byte b3, byte b4, byte b5, byte b6, byte b7, byte b8, byte b9, byte[] bArr, byte b10, byte[] bArr2, List<ReadTag> list, int[] iArr, boolean z) {
        byte[] bArr3;
        byte[] bArr4;
        if (b10 == 0) {
            bArr4 = b5 == 0 ? new byte[]{9, b, 1, b2, b3, b6, b7, b8} : new byte[]{11, b, 1, b2, b3, b4, b5, b6, b7, b8};
        } else {
            int i = ((b10 & 255) + 7) / 8;
            if (b5 == 0) {
                bArr3 = new byte[i + 14];
                bArr3[0] = (byte) (i + 13);
                bArr3[1] = b;
                bArr3[2] = 1;
                bArr3[3] = b2;
                bArr3[4] = b3;
                bArr3[5] = b9;
                bArr3[6] = bArr[0];
                bArr3[7] = bArr[1];
                bArr3[8] = b10;
                System.arraycopy(bArr2, 0, bArr3, 9, i);
                bArr3[i + 9] = b6;
                bArr3[i + 10] = b7;
                bArr3[i + 11] = b8;
            } else {
                bArr3 = new byte[i + 16];
                bArr3[0] = (byte) (i + 15);
                bArr3[1] = b;
                bArr3[2] = 1;
                bArr3[3] = b2;
                bArr3[4] = b3;
                bArr3[5] = b9;
                bArr3[6] = bArr[0];
                bArr3[7] = bArr[1];
                bArr3[8] = b10;
                System.arraycopy(bArr2, 0, bArr3, 9, i);
                bArr3[i + 9] = b4;
                bArr3[i + 10] = b5;
                bArr3[i + 11] = b6;
                bArr3[i + 12] = b7;
                bArr3[i + 13] = b8;
            }
            bArr4 = bArr3;
        }
        getCRC(bArr4, bArr4[0] - 1);
        SendCMD(bArr4);
        return GetInventoryData(b, 1, (b8 & 255) * 100, list, iArr, true, z);
    }

    public int Inventory_NoCallback(byte b, byte b2, byte b3, byte b4, byte b5, byte b6, byte b7, byte b8, byte b9, byte[] bArr, byte b10, byte[] bArr2, List<ReadTag> list, int[] iArr, boolean z) {
        byte[] bArr3;
        byte[] bArr4;
        if (b10 == 0) {
            bArr4 = b5 == 0 ? new byte[]{9, b, 1, b2, b3, b6, b7, b8} : new byte[]{11, b, 1, b2, b3, b4, b5, b6, b7, b8};
        } else {
            int i = ((b10 & 255) + 7) / 8;
            if (b5 == 0) {
                bArr3 = new byte[i + 14];
                bArr3[0] = (byte) (i + 13);
                bArr3[1] = b;
                bArr3[2] = 1;
                bArr3[3] = b2;
                bArr3[4] = b3;
                bArr3[5] = b9;
                bArr3[6] = bArr[0];
                bArr3[7] = bArr[1];
                bArr3[8] = b10;
                System.arraycopy(bArr2, 0, bArr3, 9, i);
                bArr3[i + 9] = b6;
                bArr3[i + 10] = b7;
                bArr3[i + 11] = b8;
            } else {
                bArr3 = new byte[i + 16];
                bArr3[0] = (byte) (i + 15);
                bArr3[1] = b;
                bArr3[2] = 1;
                bArr3[3] = b2;
                bArr3[4] = b3;
                bArr3[5] = b9;
                bArr3[6] = bArr[0];
                bArr3[7] = bArr[1];
                bArr3[8] = b10;
                System.arraycopy(bArr2, 0, bArr3, 9, i);
                bArr3[i + 9] = b4;
                bArr3[i + 10] = b5;
                bArr3[i + 11] = b6;
                bArr3[i + 12] = b7;
                bArr3[i + 13] = b8;
            }
            bArr4 = bArr3;
        }
        getCRC(bArr4, bArr4[0] - 1);
        SendCMD(bArr4);
        return GetInventoryData(b, 1, (b8 & 255) * 100, list, iArr, false, z);
    }

    public int InventorySingle_G2(byte b, byte b2, byte b3, byte b4, byte b5, List<ReadTag> list, int[] iArr) {
        byte[] bArr = b5 == 0 ? new byte[]{9, b, 1, b2, b3, 0, Byte.MIN_VALUE, 3} : new byte[]{11, b, 1, b2, b3, b4, b5, 0, Byte.MIN_VALUE, 3};
        getCRC(bArr, bArr[0] - 1);
        SendCMD(bArr);
        return GetInventorySingleData(b, 1, 500, list, iArr);
    }

    public int Inventory_GJB(byte b, byte b2, byte b3, byte b4, List<ReadTag> list, int[] iArr) {
        byte[] bArr = {7, b, 86, b2, b3, b4};
        getCRC(bArr, bArr[0] - 1);
        SendCMD(bArr);
        return GetInventoryData(b, 86, (b4 & 255) * 100, list, iArr, true, false);
    }

    public int Inventory_GB(byte b, byte b2, byte b3, List<ReadTag> list, int[] iArr) {
        byte[] bArr = {6, b, 86, b2, b3};
        getCRC(bArr, bArr[0] - 1);
        SendCMD(bArr);
        return GetInventoryData(b, 86, (b3 & 255) * 100, list, iArr, true, false);
    }

    /* JADX WARN: Code restructure failed: missing block: B:78:0x011b, code lost:            if (r12 <= r7) goto L68;     */
    /* JADX WARN: Code restructure failed: missing block: B:79:0x011d, code lost:            r2 = 0;        java.lang.System.arraycopy(r13, r7, r4, 0, r8);        r7 = r8;     */
    /* JADX WARN: Code restructure failed: missing block: B:80:0x0123, code lost:            r2 = 0;        r7 = 0;     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private int GetInventoryMixData(byte r21, int r22, int r23, java.util.List<com.rfid.trans.ReadTag> r24, int[] r25) {
        /*
            Method dump skipped, instructions count: 327
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.rfid.trans.BaseReader.GetInventoryMixData(byte, int, int, java.util.List, int[]):int");
    }

    public int Inventory_Mix(byte b, byte b2, byte b3, byte b4, byte[] bArr, byte b5, byte[] bArr2, byte b6, byte[] bArr3, byte b7, byte[] bArr4, byte b8, byte b9, byte b10, List<ReadTag> list, int[] iArr) {
        byte[] bArr5;
        if (b5 == 0) {
            bArr5 = new byte[]{17, b, 25, b2, b3, b6, bArr3[0], bArr3[1], b7, bArr4[0], bArr4[1], bArr4[2], bArr4[3], b8, b9, b10};
        } else {
            int i = ((b5 & 255) + 7) / 8;
            byte[] bArr6 = new byte[i + 22];
            bArr6[0] = (byte) (i + 21);
            bArr6[1] = b;
            bArr6[2] = 25;
            bArr6[3] = b2;
            bArr6[4] = b3;
            bArr6[5] = b4;
            bArr6[6] = bArr[0];
            bArr6[7] = bArr[1];
            bArr6[8] = b5;
            if (i > 0) {
                System.arraycopy(bArr2, 0, bArr6, 9, i);
            }
            bArr6[i + 9] = b6;
            bArr6[i + 10] = bArr3[0];
            bArr6[i + 11] = bArr3[1];
            bArr6[i + 12] = b7;
            bArr6[i + 13] = bArr4[0];
            bArr6[i + 14] = bArr4[1];
            bArr6[i + 15] = bArr4[2];
            bArr6[i + 16] = bArr4[3];
            bArr6[i + 17] = b8;
            bArr6[i + 18] = b9;
            bArr6[i + 19] = b10;
            bArr5 = bArr6;
        }
        getCRC(bArr5, bArr5[0] - 1);
        SendCMD(bArr5);
        this.strEPC = BuildConfig.FLAVOR;
        return GetInventoryMixData(b, 25, (b10 & 255) * 100, list, iArr);
    }

    /* JADX WARN: Code restructure failed: missing block: B:69:0x00f5, code lost:            if (r10 <= r6) goto L61;     */
    /* JADX WARN: Code restructure failed: missing block: B:70:0x00f7, code lost:            r2 = 0;        java.lang.System.arraycopy(r11, r6, r3, 0, r7);        r6 = r7;     */
    /* JADX WARN: Code restructure failed: missing block: B:71:0x00fd, code lost:            r2 = 0;        r6 = 0;     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private int GetInventoryMixData_led(byte r18, int r19, int r20, java.util.List<com.rfid.trans.ReadTag> r21, int[] r22) {
        /*
            Method dump skipped, instructions count: 282
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.rfid.trans.BaseReader.GetInventoryMixData_led(byte, int, int, java.util.List, int[]):int");
    }

    public int Inventory_Led(byte b, byte b2, byte b3, byte b4, byte[] bArr, byte b5, byte[] bArr2, byte b6, byte[] bArr3, byte b7, byte[] bArr4, byte b8, byte b9, byte b10, List<ReadTag> list, int[] iArr) {
        byte[] bArr5;
        if (b5 == 0) {
            bArr5 = new byte[]{17, b, 25, b2, b3, b6, bArr3[0], bArr3[1], b7, bArr4[0], bArr4[1], bArr4[2], bArr4[3], b8, b9, b10};
        } else {
            int i = ((b5 & 255) + 7) / 8;
            byte[] bArr6 = new byte[i + 22];
            bArr6[0] = (byte) (i + 21);
            bArr6[1] = b;
            bArr6[2] = 25;
            bArr6[3] = b2;
            bArr6[4] = b3;
            bArr6[5] = b4;
            bArr6[6] = bArr[0];
            bArr6[7] = bArr[1];
            bArr6[8] = b5;
            if (i > 0) {
                System.arraycopy(bArr2, 0, bArr6, 9, i);
            }
            bArr6[i + 9] = b6;
            bArr6[i + 10] = bArr3[0];
            bArr6[i + 11] = bArr3[1];
            bArr6[i + 12] = b7;
            bArr6[i + 13] = bArr4[0];
            bArr6[i + 14] = bArr4[1];
            bArr6[i + 15] = bArr4[2];
            bArr6[i + 16] = bArr4[3];
            bArr6[i + 17] = b8;
            bArr6[i + 18] = b9;
            bArr6[i + 19] = b10;
            bArr5 = bArr6;
        }
        getCRC(bArr5, bArr5[0] - 1);
        SendCMD(bArr5);
        return GetInventoryMixData_led(b, 25, (b10 & 255) * 100, list, iArr);
    }

    public int SetRfPower(byte b, byte b2) {
        byte[] bArr = {5, b, 47, b2};
        getCRC(bArr, bArr[0] - 1);
        SendCMD(bArr);
        if (GetCMDData(this.recvBuff, this.recvLength, 47, 500) == 0) {
            return this.recvBuff[3] & 255;
        }
        return 48;
    }

    public int SetAddress(byte b, byte b2) {
        byte[] bArr = {5, b, 36, b2};
        getCRC(bArr, bArr[0] - 1);
        SendCMD(bArr);
        if (GetCMDData(this.recvBuff, this.recvLength, 36, 500) == 0) {
            return this.recvBuff[3] & 255;
        }
        return 48;
    }

    public int SetRegion(byte b, int i, int i2, int i3) {
        byte[] bArr = {6, b, 34, (byte) (((i & 12) << 4) | (i2 & 63)), (byte) (((i & 3) << 6) | (i3 & 63))};
        getCRC(bArr, bArr[0] - 1);
        SendCMD(bArr);
        if (GetCMDData(this.recvBuff, this.recvLength, 34, 500) == 0) {
            return this.recvBuff[3] & 255;
        }
        return 48;
    }

    public int SetRegion(byte b, int i, int i2, int i3, int i4) {
        byte[] bArr = {8, b, 34, (byte) i, (byte) i2, (byte) i3, (byte) i4};
        getCRC(bArr, bArr[0] - 1);
        SendCMD(bArr);
        if (GetCMDData(this.recvBuff, this.recvLength, 34, 500) == 0) {
            return this.recvBuff[3] & 255;
        }
        return 48;
    }

    public int SetAntennaMultiplexing(byte b, byte b2, byte b3, byte b4) {
        byte[] bArr = {7, b, 63, b2, b3, b4};
        getCRC(bArr, bArr[0] - 1);
        SendCMD(bArr);
        if (GetCMDData(this.recvBuff, this.recvLength, 63, 500) == 0) {
            return this.recvBuff[3] & 255;
        }
        return 48;
    }

    public int SetBaudRate(byte b, byte b2) {
        byte[] bArr = {5, b, 40, b2};
        getCRC(bArr, bArr[0] - 1);
        SendCMD(bArr);
        if (GetCMDData(this.recvBuff, this.recvLength, 40, 1000) == 0) {
            return this.recvBuff[3] & 255;
        }
        return 48;
    }

    public int ConfigDRM(byte b, byte[] bArr) {
        byte[] bArr2 = {5, b, -112, bArr[0]};
        getCRC(bArr2, bArr2[0] - 1);
        SendCMD(bArr2);
        if (GetCMDData(this.recvBuff, this.recvLength, 144, 400) != 0) {
            return 48;
        }
        byte[] bArr3 = this.recvBuff;
        if (bArr3[3] == 0) {
            bArr[0] = bArr3[4];
        }
        return bArr3[3] & 255;
    }

    public int SetGPIO(byte b, byte b2) {
        byte[] bArr = {5, b, 70, b2};
        getCRC(bArr, bArr[0] - 1);
        SendCMD(bArr);
        if (GetCMDData(this.recvBuff, this.recvLength, 70, 1000) == 0) {
            return this.recvBuff[3] & 255;
        }
        return 48;
    }

    public int GetDeviceID(byte b, byte[] bArr) {
        byte[] bArr2 = {4, b, 76};
        getCRC(bArr2, bArr2[0] - 1);
        SendCMD(bArr2);
        if (GetCMDData(this.recvBuff, this.recvLength, 76, 1000) != 0) {
            return 48;
        }
        byte[] bArr3 = this.recvBuff;
        if (bArr3[3] == 0) {
            System.arraycopy(bArr3, 4, bArr, 0, 4);
        }
        return this.recvBuff[3] & 255;
    }

    public int GetGPIOStatus(byte b, byte[] bArr) {
        byte[] bArr2 = {4, b, 71};
        getCRC(bArr2, bArr2[0] - 1);
        SendCMD(bArr2);
        if (GetCMDData(this.recvBuff, this.recvLength, 71, 1000) != 0) {
            return 48;
        }
        byte[] bArr3 = this.recvBuff;
        if (bArr3[3] == 0) {
            bArr[0] = bArr3[4];
        }
        return bArr3[3] & 255;
    }

    public int SetWritePower(byte b, byte b2) {
        byte[] bArr = {5, b, 121, b2};
        getCRC(bArr, bArr[0] - 1);
        SendCMD(bArr);
        if (GetCMDData(this.recvBuff, this.recvLength, 121, 1000) == 0) {
            return this.recvBuff[3] & 255;
        }
        return 48;
    }

    public int GetWritePower(byte b, byte[] bArr) {
        byte[] bArr2 = {4, b, 122};
        getCRC(bArr2, bArr2[0] - 1);
        SendCMD(bArr2);
        if (GetCMDData(this.recvBuff, this.recvLength, 122, 1000) != 0) {
            return 48;
        }
        byte[] bArr3 = this.recvBuff;
        if (bArr3[3] == 0) {
            bArr[0] = bArr3[4];
        }
        return bArr3[3] & 255;
    }

    public int RetryTimes(byte b, byte[] bArr) {
        byte[] bArr2 = {5, b, 123, bArr[0]};
        getCRC(bArr2, bArr2[0] - 1);
        SendCMD(bArr2);
        if (GetCMDData(this.recvBuff, this.recvLength, 123, 1000) != 0) {
            return 48;
        }
        byte[] bArr3 = this.recvBuff;
        if (bArr3[3] == 0) {
            bArr[0] = bArr3[4];
        }
        return bArr3[3] & 255;
    }

    public int SetBeepNotification(byte b, byte b2) {
        byte[] bArr = {5, b, 64, b2};
        getCRC(bArr, bArr[0] - 1);
        SendCMD(bArr);
        if (GetCMDData(this.recvBuff, this.recvLength, 64, 400) == 0) {
            return this.recvBuff[3] & 255;
        }
        return 48;
    }

    public int ReadData_G2(byte b, byte b2, byte[] bArr, byte b3, byte b4, byte b5, byte[] bArr2, byte b6, byte[] bArr3, byte b7, byte[] bArr4, byte[] bArr5, byte[] bArr6) {
        int i;
        byte b8;
        int i2 = b2 & 255;
        if (i2 < 16) {
            int i3 = b2 * 2;
            byte[] bArr7 = new byte[i3 + 13];
            bArr7[0] = (byte) (i3 + 12);
            bArr7[1] = b;
            bArr7[2] = 2;
            bArr7[3] = b2;
            if (i2 > 0) {
                System.arraycopy(bArr, 0, bArr7, 4, i3);
            }
            bArr7[i3 + 4] = b3;
            bArr7[i3 + 5] = b4;
            bArr7[i3 + 6] = b5;
            System.arraycopy(bArr2, 0, bArr7, i3 + 7, 4);
            getCRC(bArr7, bArr7[0] - 1);
            SendCMD(bArr7);
            if (GetCMDData(this.recvBuff, this.recvLength, 2, PathInterpolatorCompat.MAX_NUM_POINTS) != 0) {
                return 48;
            }
            byte[] bArr8 = this.recvBuff;
            if (bArr8[3] == 0) {
                bArr6[0] = 0;
                System.arraycopy(bArr8, 4, bArr5, 0, b5 * 2);
            } else if ((bArr8[3] & 255) == 252) {
                bArr6[0] = bArr8[4];
            }
            b8 = this.recvBuff[3];
        } else {
            if (i2 != 255 || b7 == 0) {
                return 255;
            }
            int i4 = b7 & 255;
            if (i4 % 8 == 0) {
                i = i4 / 8;
            } else {
                i = (i4 / 8) + 1;
            }
            byte[] bArr9 = new byte[i + 17];
            bArr9[0] = (byte) (i + 16);
            bArr9[1] = b;
            bArr9[2] = 2;
            bArr9[3] = b2;
            bArr9[4] = b3;
            bArr9[5] = b4;
            bArr9[6] = b5;
            System.arraycopy(bArr2, 0, bArr9, 7, 4);
            bArr9[11] = b6;
            bArr9[12] = bArr3[0];
            bArr9[13] = bArr3[1];
            bArr9[14] = b7;
            System.arraycopy(bArr4, 0, bArr9, 15, i);
            getCRC(bArr9, bArr9[0] - 1);
            SendCMD(bArr9);
            if (GetCMDData(this.recvBuff, this.recvLength, 2, PathInterpolatorCompat.MAX_NUM_POINTS) != 0) {
                return 48;
            }
            byte[] bArr10 = this.recvBuff;
            if (bArr10[3] == 0) {
                bArr6[0] = 0;
                System.arraycopy(bArr10, 4, bArr5, 0, b5 * 2);
            } else if ((bArr10[3] & 255) == 252) {
                bArr6[0] = bArr10[4];
            }
            b8 = this.recvBuff[3];
        }
        return b8 & 255;
    }

    public int ExtReadData_G2(byte b, byte b2, byte[] bArr, byte b3, byte[] bArr2, byte b4, byte[] bArr3, byte b5, byte[] bArr4, byte b6, byte[] bArr5, byte[] bArr6, byte[] bArr7) {
        int i;
        byte b7;
        byte b8;
        int i2 = b2 & 255;
        if (i2 < 16) {
            int i3 = b2 * 2;
            byte[] bArr8 = new byte[i3 + 14];
            bArr8[0] = (byte) (i3 + 13);
            bArr8[1] = b;
            bArr8[2] = 21;
            bArr8[3] = b2;
            if (i2 > 0) {
                System.arraycopy(bArr, 0, bArr8, 4, i3);
            }
            bArr8[i3 + 4] = b3;
            bArr8[i3 + 5] = bArr2[0];
            bArr8[i3 + 6] = bArr2[1];
            bArr8[i3 + 7] = b4;
            System.arraycopy(bArr3, 0, bArr8, i3 + 8, 4);
            getCRC(bArr8, bArr8[0] - 1);
            SendCMD(bArr8);
            if (GetCMDData(this.recvBuff, this.recvLength, 21, PathInterpolatorCompat.MAX_NUM_POINTS) != 0) {
                return 48;
            }
            byte[] bArr9 = this.recvBuff;
            if (bArr9[3] == 0) {
                bArr7[0] = 0;
                System.arraycopy(bArr9, 4, bArr6, 0, b4 * 2);
                b8 = 255;
            } else {
                b8 = 255;
                if ((bArr9[3] & 255) == 252) {
                    bArr7[0] = bArr9[4];
                }
            }
            return this.recvBuff[3] & b8;
        }
        if (i2 != 255 || b6 == 0) {
            return 255;
        }
        int i4 = b6 & 255;
        if (i4 % 8 == 0) {
            i = i4 / 8;
        } else {
            i = (i4 / 8) + 1;
        }
        byte[] bArr10 = new byte[i + 18];
        bArr10[0] = (byte) (i + 17);
        bArr10[1] = b;
        bArr10[2] = 21;
        bArr10[3] = b2;
        bArr10[4] = b3;
        bArr10[5] = bArr2[0];
        bArr10[6] = bArr2[1];
        bArr10[7] = b4;
        System.arraycopy(bArr3, 0, bArr10, 8, 4);
        bArr10[12] = b5;
        bArr10[13] = bArr4[0];
        bArr10[14] = bArr4[1];
        bArr10[15] = b6;
        System.arraycopy(bArr5, 0, bArr10, 16, i);
        getCRC(bArr10, bArr10[0] - 1);
        SendCMD(bArr10);
        if (GetCMDData(this.recvBuff, this.recvLength, 21, PathInterpolatorCompat.MAX_NUM_POINTS) != 0) {
            return 48;
        }
        byte[] bArr11 = this.recvBuff;
        if (bArr11[3] == 0) {
            bArr7[0] = 0;
            System.arraycopy(bArr11, 4, bArr6, 0, b4 * 2);
            b7 = 255;
        } else {
            b7 = 255;
            if ((bArr11[3] & 255) == 252) {
                bArr7[0] = bArr11[4];
            }
        }
        return this.recvBuff[3] & b7;
    }

    public int WriteData_G2(byte b, byte b2, byte b3, byte[] bArr, byte b4, byte b5, byte[] bArr2, byte[] bArr3, byte b6, byte[] bArr4, byte b7, byte[] bArr5, byte[] bArr6) {
        int i;
        byte b8;
        int i2 = b3 & 255;
        if (i2 < 16) {
            int i3 = (b3 + b2) * 2;
            byte[] bArr7 = new byte[i3 + 13];
            bArr7[0] = (byte) (i3 + 12);
            bArr7[1] = b;
            bArr7[2] = 3;
            bArr7[3] = b2;
            bArr7[4] = b3;
            if (i2 > 0) {
                System.arraycopy(bArr, 0, bArr7, 5, b3 * 2);
            }
            int i4 = b3 * 2;
            bArr7[i4 + 5] = b4;
            bArr7[i4 + 6] = b5;
            int i5 = b2 * 2;
            System.arraycopy(bArr2, 0, bArr7, i4 + 7, i5);
            System.arraycopy(bArr3, 0, bArr7, i4 + i5 + 7, 4);
            getCRC(bArr7, bArr7[0] - 1);
            SendCMD(bArr7);
            if (GetCMDData(this.recvBuff, this.recvLength, 3, PathInterpolatorCompat.MAX_NUM_POINTS) != 0) {
                return 48;
            }
            byte[] bArr8 = this.recvBuff;
            if (bArr8[3] == 0) {
                bArr6[0] = 0;
            } else if ((bArr8[3] & 255) == 252) {
                bArr6[0] = bArr8[4];
            }
            return bArr8[3] & 255;
        }
        if (i2 != 255 || b7 == 0) {
            return 255;
        }
        int i6 = b7 & 255;
        if (i6 % 8 == 0) {
            i = i6 / 8;
        } else {
            i = (i6 / 8) + 1;
        }
        int i7 = b2 * 2;
        byte[] bArr9 = new byte[i7 + 17 + i];
        bArr9[0] = (byte) (i7 + 16 + i);
        bArr9[1] = b;
        bArr9[2] = 3;
        bArr9[3] = b2;
        bArr9[4] = b3;
        bArr9[5] = b4;
        bArr9[6] = b5;
        System.arraycopy(bArr2, 0, bArr9, 7, i7);
        System.arraycopy(bArr3, 0, bArr9, i7 + 7, 4);
        bArr9[i7 + 11] = b6;
        bArr9[i7 + 12] = bArr4[0];
        bArr9[i7 + 13] = bArr4[1];
        bArr9[i7 + 14] = b7;
        System.arraycopy(bArr5, 0, bArr9, i7 + 15, i);
        getCRC(bArr9, bArr9[0] - 1);
        SendCMD(bArr9);
        if (GetCMDData(this.recvBuff, this.recvLength, 3, PathInterpolatorCompat.MAX_NUM_POINTS) != 0) {
            return 48;
        }
        byte[] bArr10 = this.recvBuff;
        if (bArr10[3] == 0) {
            bArr6[0] = 0;
            b8 = 255;
        } else {
            b8 = 255;
            if ((bArr10[3] & 255) == 252) {
                bArr6[0] = bArr10[4];
            }
        }
        return bArr10[3] & b8;
    }

    public int ExtWriteData_G2(byte b, byte b2, byte b3, byte[] bArr, byte b4, byte[] bArr2, byte[] bArr3, byte[] bArr4, byte b5, byte[] bArr5, byte b6, byte[] bArr6, byte[] bArr7) {
        int i;
        byte b7;
        int i2 = b3 & 255;
        if (i2 < 16) {
            int i3 = (b3 + b2) * 2;
            byte[] bArr8 = new byte[i3 + 14];
            bArr8[0] = (byte) (i3 + 13);
            bArr8[1] = b;
            bArr8[2] = 22;
            bArr8[3] = b2;
            bArr8[4] = b3;
            if (b3 > 0) {
                System.arraycopy(bArr, 0, bArr8, 5, b3 * 2);
            }
            int i4 = b3 * 2;
            bArr8[i4 + 5] = b4;
            bArr8[i4 + 6] = bArr2[0];
            bArr8[i4 + 7] = bArr2[1];
            int i5 = b2 * 2;
            System.arraycopy(bArr3, 0, bArr8, i4 + 8, i5);
            System.arraycopy(bArr4, 0, bArr8, i4 + i5 + 8, 4);
            getCRC(bArr8, bArr8[0] - 1);
            SendCMD(bArr8);
            if (GetCMDData(this.recvBuff, this.recvLength, 22, PathInterpolatorCompat.MAX_NUM_POINTS) != 0) {
                return 48;
            }
            byte[] bArr9 = this.recvBuff;
            if (bArr9[3] == 0) {
                bArr7[0] = 0;
            } else if ((bArr9[3] & 255) == 252) {
                bArr7[0] = bArr9[4];
            }
            return bArr9[3] & 255;
        }
        if (i2 != 255 || b6 == 0) {
            return 255;
        }
        int i6 = b6 & 255;
        if (i6 % 8 == 0) {
            i = i6 / 8;
        } else {
            i = (i6 / 8) + 1;
        }
        int i7 = b2 * 2;
        byte[] bArr10 = new byte[i7 + 18 + i];
        bArr10[0] = (byte) (i7 + 17 + i);
        bArr10[1] = b;
        bArr10[2] = 22;
        bArr10[3] = b2;
        bArr10[4] = b3;
        bArr10[5] = b4;
        bArr10[6] = bArr2[0];
        bArr10[7] = bArr2[1];
        System.arraycopy(bArr3, 0, bArr10, 8, i7);
        System.arraycopy(bArr4, 0, bArr10, i7 + 8, 4);
        bArr10[i7 + 12] = b5;
        bArr10[i7 + 13] = bArr5[0];
        bArr10[i7 + 14] = bArr5[1];
        bArr10[i7 + 15] = b6;
        System.arraycopy(bArr6, 0, bArr10, i7 + 16, i);
        getCRC(bArr10, bArr10[0] - 1);
        SendCMD(bArr10);
        if (GetCMDData(this.recvBuff, this.recvLength, 22, PathInterpolatorCompat.MAX_NUM_POINTS) != 0) {
            return 48;
        }
        byte[] bArr11 = this.recvBuff;
        if (bArr11[3] == 0) {
            bArr7[0] = 0;
            b7 = 255;
        } else {
            b7 = 255;
            if ((bArr11[3] & 255) == 252) {
                bArr7[0] = bArr11[4];
            }
        }
        return bArr11[3] & b7;
    }

    public int BlockWrite_G2(byte b, byte b2, byte b3, byte[] bArr, byte b4, byte b5, byte[] bArr2, byte[] bArr3, byte b6, byte[] bArr4, byte b7, byte[] bArr5, byte[] bArr6) {
        int i;
        byte b8;
        byte b9 = b3;
        int i2 = b9 & 255;
        if (i2 < 16) {
            int i3 = (b9 + b2) * 2;
            byte[] bArr7 = new byte[i3 + 13];
            bArr7[0] = (byte) (i3 + 12);
            bArr7[1] = b;
            bArr7[2] = 16;
            bArr7[3] = b2;
            bArr7[4] = b9;
            if (b9 > 0) {
                System.arraycopy(bArr, 0, bArr7, 5, b9 * 2);
            }
            int i4 = b9 * 2;
            bArr7[i4 + 5] = b4;
            bArr7[i4 + 6] = b5;
            int i5 = b2 * 2;
            System.arraycopy(bArr2, 0, bArr7, i4 + 7, i5);
            System.arraycopy(bArr3, 0, bArr7, i4 + i5 + 7, 4);
            getCRC(bArr7, bArr7[0] - 1);
            SendCMD(bArr7);
            if (GetCMDData(this.recvBuff, this.recvLength, 16, PathInterpolatorCompat.MAX_NUM_POINTS) != 0) {
                return 48;
            }
            byte[] bArr8 = this.recvBuff;
            if (bArr8[3] == 0) {
                bArr6[0] = 0;
            } else if ((bArr8[3] & 255) == 252) {
                bArr6[0] = bArr8[4];
            }
            return bArr8[3] & 255;
        }
        if (i2 != 255 || b7 == 0) {
            return 255;
        }
        int i6 = b7 & 255;
        if (i6 % 8 == 0) {
            i = i6 / 8;
        } else {
            i = (i6 / 8) + 1;
        }
        int i7 = b2 * 2;
        byte[] bArr9 = new byte[i7 + 17 + i];
        bArr9[0] = (byte) (i7 + 16 + i);
        bArr9[1] = b;
        bArr9[2] = 16;
        bArr9[3] = b2;
        bArr9[4] = b9;
        if (i2 == 255) {
            b9 = 0;
        }
        int i8 = b9 * 2;
        System.arraycopy(bArr, 0, bArr9, 5, i8);
        bArr9[i8 + 5] = b4;
        bArr9[i8 + 6] = b5;
        System.arraycopy(bArr2, 0, bArr9, i8 + 7, i7);
        int i9 = i8 + i7;
        System.arraycopy(bArr3, 0, bArr9, i9 + 7, 4);
        bArr9[i9 + 11] = b6;
        bArr9[i9 + 12] = bArr4[0];
        bArr9[i9 + 13] = bArr4[1];
        bArr9[i9 + 14] = b7;
        System.arraycopy(bArr5, 0, bArr9, i9 + 15, i);
        getCRC(bArr9, bArr9[0] - 1);
        SendCMD(bArr9);
        if (GetCMDData(this.recvBuff, this.recvLength, 16, PathInterpolatorCompat.MAX_NUM_POINTS) != 0) {
            return 48;
        }
        byte[] bArr10 = this.recvBuff;
        if (bArr10[3] == 0) {
            bArr6[0] = 0;
            b8 = 255;
        } else {
            b8 = 255;
            if ((bArr10[3] & 255) == 252) {
                bArr6[0] = bArr10[4];
            }
        }
        return bArr10[3] & b8;
    }

    public int WriteEPC_G2(byte b, byte b2, byte[] bArr, byte[] bArr2, byte[] bArr3) {
        int i = b2 & 255;
        if (i > 31 || i < 0) {
            return 255;
        }
        int i2 = b2 * 2;
        byte[] bArr4 = new byte[i2 + 10];
        bArr4[0] = (byte) (i2 + 9);
        bArr4[1] = b;
        bArr4[2] = 4;
        bArr4[3] = b2;
        System.arraycopy(bArr, 0, bArr4, 4, 4);
        System.arraycopy(bArr2, 0, bArr4, 8, i2);
        getCRC(bArr4, bArr4[0] - 1);
        SendCMD(bArr4);
        if (GetCMDData(this.recvBuff, this.recvLength, 4, 2000) != 0) {
            return 48;
        }
        byte[] bArr5 = this.recvBuff;
        if (bArr5[3] == 0) {
            bArr3[0] = 0;
        } else if ((bArr5[3] & 255) == 252) {
            bArr3[0] = bArr5[4];
        }
        return bArr5[3] & 255;
    }

    public int Lock_G2(byte b, byte b2, byte[] bArr, byte b3, byte b4, byte[] bArr2, byte[] bArr3) {
        int i = b2 * 2;
        byte[] bArr4 = new byte[i + 12];
        bArr4[0] = (byte) (i + 11);
        bArr4[1] = b;
        bArr4[2] = 6;
        bArr4[3] = b2;
        if (b2 > 0) {
            System.arraycopy(bArr, 0, bArr4, 4, i);
        }
        bArr4[i + 4] = b3;
        bArr4[i + 5] = b4;
        System.arraycopy(bArr2, 0, bArr4, i + 6, 4);
        getCRC(bArr4, bArr4[0] - 1);
        SendCMD(bArr4);
        if (GetCMDData(this.recvBuff, this.recvLength, 6, 1000) != 0) {
            return 48;
        }
        byte[] bArr5 = this.recvBuff;
        if (bArr5[3] == 0) {
            bArr3[0] = 0;
        } else if ((bArr5[3] & 255) == 252) {
            bArr3[0] = bArr5[4];
        }
        return bArr5[3] & 255;
    }

    public int Kill_G2(byte b, byte b2, byte[] bArr, byte[] bArr2, byte[] bArr3) {
        int i = b2 * 2;
        byte[] bArr4 = new byte[i + 10];
        bArr4[0] = (byte) (i + 9);
        bArr4[1] = b;
        bArr4[2] = 5;
        bArr4[3] = b2;
        if (b2 > 0) {
            System.arraycopy(bArr, 0, bArr4, 4, i);
        }
        System.arraycopy(bArr2, 0, bArr4, i + 4, 4);
        getCRC(bArr4, bArr4[0] - 1);
        SendCMD(bArr4);
        if (GetCMDData(this.recvBuff, this.recvLength, 5, 1000) != 0) {
            return 48;
        }
        byte[] bArr5 = this.recvBuff;
        if (bArr5[3] == 0) {
            bArr3[0] = 0;
        } else if ((bArr5[3] & 255) == 252) {
            bArr3[0] = bArr5[4];
        }
        return bArr5[3] & 255;
    }

    public int Kill_G2(byte b, byte b2, byte[] bArr, byte[] bArr2, byte b3, byte[] bArr3, byte b4, byte[] bArr4, byte[] bArr5) {
        byte[] bArr6;
        if (b2 >= 0 && b2 < 16) {
            int i = b2 * 2;
            bArr6 = new byte[i + 10];
            bArr6[0] = (byte) (i + 9);
            bArr6[1] = b;
            bArr6[2] = 5;
            bArr6[3] = b2;
            if (b2 > 0) {
                System.arraycopy(bArr, 0, bArr6, 4, i);
            }
            System.arraycopy(bArr2, 0, bArr6, i + 4, 4);
        } else {
            if ((b2 & 255) != 255) {
                return 255;
            }
            int i2 = ((b4 & 255) + 7) / 8;
            byte[] bArr7 = new byte[i2 + 14];
            bArr7[0] = (byte) (i2 + 13);
            bArr7[1] = b;
            bArr7[2] = 5;
            bArr7[3] = b2;
            System.arraycopy(bArr2, 0, bArr7, 4, 4);
            bArr7[8] = b3;
            bArr7[9] = bArr3[0];
            bArr7[10] = bArr3[1];
            bArr7[11] = b4;
            if (i2 > 0) {
                System.arraycopy(bArr4, 0, bArr7, 12, i2);
            }
            bArr6 = bArr7;
        }
        getCRC(bArr6, bArr6[0] - 1);
        SendCMD(bArr6);
        if (GetCMDData(this.recvBuff, this.recvLength, 5, 1000) != 0) {
            return 48;
        }
        byte[] bArr8 = this.recvBuff;
        if (bArr8[3] == 0) {
            bArr5[0] = 0;
        } else if ((bArr8[3] & 255) == 252) {
            bArr5[0] = bArr8[4];
        }
        return bArr8[3] & 255;
    }

    public int Lock_G2(byte b, byte b2, byte[] bArr, byte b3, byte b4, byte[] bArr2, byte b5, byte[] bArr3, byte b6, byte[] bArr4, byte[] bArr5) {
        byte[] bArr6;
        if (b2 >= 0 && b2 < 16) {
            int i = b2 * 2;
            bArr6 = new byte[i + 12];
            bArr6[0] = (byte) (i + 11);
            bArr6[1] = b;
            bArr6[2] = 6;
            bArr6[3] = b2;
            if (b2 > 0) {
                System.arraycopy(bArr, 0, bArr6, 4, i);
            }
            bArr6[i + 4] = b3;
            bArr6[i + 5] = b4;
            System.arraycopy(bArr2, 0, bArr6, i + 6, 4);
        } else {
            if ((b2 & 255) != 255) {
                return 255;
            }
            int i2 = ((b6 & 255) + 7) / 8;
            byte[] bArr7 = new byte[i2 + 16];
            bArr7[0] = (byte) (i2 + 15);
            bArr7[1] = b;
            bArr7[2] = 6;
            bArr7[3] = b2;
            bArr7[4] = b3;
            bArr7[5] = b4;
            System.arraycopy(bArr2, 0, bArr7, 6, 4);
            bArr7[10] = b5;
            bArr7[11] = bArr3[0];
            bArr7[12] = bArr3[1];
            bArr7[13] = b6;
            if (i2 > 0) {
                System.arraycopy(bArr4, 0, bArr7, 14, i2);
            }
            bArr6 = bArr7;
        }
        getCRC(bArr6, bArr6[0] - 1);
        SendCMD(bArr6);
        if (GetCMDData(this.recvBuff, this.recvLength, 6, 1000) != 0) {
            return 48;
        }
        byte[] bArr8 = this.recvBuff;
        if (bArr8[3] == 0) {
            bArr5[0] = 0;
        } else if ((bArr8[3] & 255) == 252) {
            bArr5[0] = bArr8[4];
        }
        return bArr8[3] & 255;
    }

    public int BlockErase_G2(byte b, byte b2, byte[] bArr, byte b3, byte b4, byte b5, byte[] bArr2, byte[] bArr3) {
        int i = b2 * 2;
        byte[] bArr4 = new byte[i + 13];
        bArr4[0] = (byte) (i + 12);
        bArr4[1] = b;
        bArr4[2] = 7;
        bArr4[3] = b2;
        if (b2 > 0) {
            System.arraycopy(bArr, 0, bArr4, 4, i);
        }
        bArr4[i + 4] = b3;
        bArr4[i + 5] = b4;
        bArr4[i + 6] = b5;
        System.arraycopy(bArr2, 0, bArr4, i + 7, 4);
        getCRC(bArr4, bArr4[0] - 1);
        SendCMD(bArr4);
        if (GetCMDData(this.recvBuff, this.recvLength, 7, 1000) != 0) {
            return 48;
        }
        byte[] bArr5 = this.recvBuff;
        if (bArr5[3] == 0) {
            bArr3[0] = 0;
        } else if ((bArr5[3] & 255) == 252) {
            bArr3[0] = bArr5[4];
        }
        return bArr5[3] & 255;
    }

    public int MeasureReturnLoss(byte b, byte[] bArr, byte b2, byte[] bArr2) {
        byte[] bArr3 = new byte[10];
        bArr3[0] = 9;
        bArr3[1] = b;
        bArr3[2] = -111;
        System.arraycopy(bArr, 0, bArr3, 3, 4);
        bArr3[7] = b2;
        getCRC(bArr3, bArr3[0] - 1);
        SendCMD(bArr3);
        if (GetCMDData(this.recvBuff, this.recvLength, 145, 600) != 0) {
            return 48;
        }
        byte[] bArr4 = this.recvBuff;
        if (bArr4[3] == 0) {
            bArr2[0] = bArr4[4];
        }
        return bArr4[3] & 255;
    }

    public int SetCheckAnt(byte b, byte b2) {
        byte[] bArr = {5, b, 102, b2};
        getCRC(bArr, bArr[0] - 1);
        SendCMD(bArr);
        if (GetCMDData(this.recvBuff, this.recvLength, 102, 500) == 0) {
            return this.recvBuff[3] & 255;
        }
        return 48;
    }

    public int SetReadParameter(byte b, byte[] bArr) {
        byte[] bArr2 = {9, b, 117, bArr[0], bArr[1], bArr[2], bArr[3], bArr[4]};
        getCRC(bArr2, bArr2[0] - 1);
        SendCMD(bArr2);
        if (GetCMDData(this.recvBuff, this.recvLength, 117, 500) == 0) {
            return this.recvBuff[3] & 255;
        }
        return 48;
    }

    public int GetReadParameter(byte b, byte[] bArr) {
        byte[] bArr2 = {4, b, 119};
        getCRC(bArr2, bArr2[0] - 1);
        SendCMD(bArr2);
        if (GetCMDData(this.recvBuff, this.recvLength, 119, 300) != 0) {
            return 48;
        }
        System.arraycopy(this.recvBuff, 4, bArr, 0, 6);
        return this.recvBuff[3] & 255;
    }

    public int SetWorkMode(byte b, byte b2) {
        byte[] bArr = {5, b, 118, b2};
        getCRC(bArr, bArr[0] - 1);
        SendCMD(bArr);
        if (GetCMDData(this.recvBuff, this.recvLength, 119, 300) == 0) {
            return this.recvBuff[3] & 255;
        }
        return 48;
    }

    public int MeasureTemperature(byte b, byte[] bArr) {
        byte[] bArr2 = {4, b, -110};
        getCRC(bArr2, bArr2[0] - 1);
        SendCMD(bArr2);
        if (GetCMDData(this.recvBuff, this.recvLength, 146, 600) != 0) {
            return 48;
        }
        byte[] bArr3 = this.recvBuff;
        if (bArr3[3] == 0) {
            bArr[0] = bArr3[4];
            bArr[1] = bArr3[5];
        }
        return bArr3[3] & 255;
    }

    public int SetProfile(byte b, byte[] bArr) {
        byte[] bArr2 = {5, b, Byte.MAX_VALUE, bArr[0]};
        getCRC(bArr2, bArr2[0] - 1);
        SendCMD(bArr2);
        if (GetCMDData(this.recvBuff, this.recvLength, 127, 400) != 0) {
            return 48;
        }
        byte[] bArr3 = this.recvBuff;
        if (bArr3[3] == 0) {
            bArr[0] = bArr3[4];
        }
        return bArr3[3] & 255;
    }

    public int FST_TranImage(byte b, byte b2, byte[] bArr, byte[] bArr2) {
        int i = b2 & 255;
        byte[] bArr3 = new byte[i + 8];
        bArr3[0] = (byte) (i + 7);
        bArr3[1] = b;
        bArr3[2] = -48;
        bArr3[3] = b2;
        bArr3[4] = bArr[0];
        bArr3[5] = bArr[1];
        System.arraycopy(bArr2, 0, bArr3, 6, i);
        getCRC(bArr3, (bArr3[0] & 255) - 1);
        SendCMD(bArr3);
        if (GetCMDData(this.recvBuff, this.recvLength, 208, 1500) == 0) {
            return this.recvBuff[3] & 255;
        }
        return 48;
    }

    public int FST_ShowImage(byte b, byte b2, byte[] bArr) {
        int i = b2 * 2;
        byte[] bArr2 = new byte[i + 6];
        bArr2[0] = (byte) (i + 5);
        bArr2[1] = b;
        bArr2[2] = -47;
        bArr2[3] = b2;
        if (i > 0) {
            System.arraycopy(bArr, 0, bArr2, 4, i);
        }
        getCRC(bArr2, (bArr2[0] & 255) - 1);
        SendCMD(bArr2);
        if (GetCMDData(this.recvBuff, this.recvLength, 209, 12000) == 0) {
            return this.recvBuff[3] & 255;
        }
        return 48;
    }

    public int LedOn_kx2005x(byte b, byte b2, byte[] bArr, byte b3, byte[] bArr2, byte b4, byte[] bArr3, byte b5, byte[] bArr4) {
        int i;
        int i2 = b2 & 255;
        if (i2 < 16) {
            int i3 = b2 * 2;
            byte[] bArr5 = new byte[i3 + 11];
            bArr5[0] = (byte) (i3 + 10);
            bArr5[1] = b;
            bArr5[2] = -52;
            bArr5[3] = b2;
            if (b2 > 0 && b2 < 32) {
                System.arraycopy(bArr, 0, bArr5, 4, i3);
            }
            bArr5[i3 + 4] = b3;
            System.arraycopy(bArr2, 0, bArr5, i3 + 5, 4);
            getCRC(bArr5, bArr5[0] - 1);
            SendCMD(bArr5);
            if (GetCMDData(this.recvBuff, this.recvLength, 204, PathInterpolatorCompat.MAX_NUM_POINTS) == 0) {
                return this.recvBuff[3] & 255;
            }
            return 48;
        }
        if (i2 != 255 || b5 == 0) {
            return 255;
        }
        int i4 = b5 & 255;
        if (i4 % 8 == 0) {
            i = i4 / 8;
        } else {
            i = (i4 / 8) + 1;
        }
        byte[] bArr6 = new byte[i + 15];
        bArr6[0] = (byte) (i + 14);
        bArr6[1] = b;
        bArr6[2] = -52;
        bArr6[3] = b2;
        bArr6[4] = b3;
        System.arraycopy(bArr2, 0, bArr6, 5, 4);
        bArr6[9] = b4;
        bArr6[10] = bArr3[0];
        bArr6[11] = bArr3[1];
        bArr6[12] = b5;
        System.arraycopy(bArr4, 0, bArr6, 13, i);
        getCRC(bArr6, bArr6[0] - 1);
        SendCMD(bArr6);
        if (GetCMDData(this.recvBuff, this.recvLength, 204, PathInterpolatorCompat.MAX_NUM_POINTS) == 0) {
            return this.recvBuff[3] & 255;
        }
        return 48;
    }

    public int Fd_InitRegfile(byte b, byte b2, byte[] bArr, byte[] bArr2, byte b3, byte[] bArr3, byte b4, byte[] bArr4, byte[] bArr5) {
        int i;
        int i2 = b2 & 255;
        if (i2 >= 0 && i2 < 16) {
            int i3 = b2 * 2;
            byte[] bArr6 = new byte[i3 + 10];
            bArr6[0] = (byte) (i3 + 9);
            bArr6[1] = b;
            bArr6[2] = -64;
            bArr6[3] = b2;
            if (b2 > 0 && b2 < 16) {
                System.arraycopy(bArr, 0, bArr6, 4, i3);
            }
            System.arraycopy(bArr2, 0, bArr6, i3 + 4, 4);
            getCRC(bArr6, bArr6[0] - 1);
            SendCMD(bArr6);
            if (GetCMDData(this.recvBuff, this.recvLength, 192, 1500) != 0) {
                return 48;
            }
            bArr5[0] = 0;
            byte[] bArr7 = this.recvBuff;
            if ((bArr7[3] & 255) == 252) {
                bArr5[0] = bArr7[4];
            }
            return bArr7[3] & 255;
        }
        if (i2 != 255 || b4 == 0) {
            return 255;
        }
        int i4 = b4 & 255;
        if (i4 % 8 == 0) {
            i = i4 / 8;
        } else {
            i = (i4 / 8) + 1;
        }
        byte[] bArr8 = new byte[i + 14];
        bArr8[0] = (byte) (i + 13);
        bArr8[1] = b;
        bArr8[2] = -64;
        bArr8[3] = b2;
        System.arraycopy(bArr2, 0, bArr8, 4, 4);
        bArr8[8] = b3;
        bArr8[9] = bArr3[0];
        bArr8[10] = bArr3[1];
        bArr8[11] = b4;
        System.arraycopy(bArr4, 0, bArr8, 12, i);
        getCRC(bArr8, bArr8[0] - 1);
        SendCMD(bArr8);
        if (GetCMDData(this.recvBuff, this.recvLength, 192, 1500) != 0) {
            return 48;
        }
        bArr5[0] = 0;
        byte[] bArr9 = this.recvBuff;
        if ((bArr9[3] & 255) == 252) {
            bArr5[0] = bArr9[4];
        }
        return bArr9[3] & 255;
    }

    public int Fd_ReadReg(byte b, byte b2, byte[] bArr, byte[] bArr2, byte[] bArr3, byte b3, byte[] bArr4, byte b4, byte[] bArr5, byte[] bArr6, byte[] bArr7) {
        int i;
        byte b5;
        byte b6;
        int i2 = b2 & 255;
        if (i2 >= 0 && i2 < 16) {
            int i3 = b2 * 2;
            byte[] bArr8 = new byte[i3 + 12];
            bArr8[0] = (byte) (i3 + 11);
            bArr8[1] = b;
            bArr8[2] = -63;
            bArr8[3] = b2;
            if (b2 > 0 && b2 < 16) {
                System.arraycopy(bArr, 0, bArr8, 4, i3);
            }
            System.arraycopy(bArr2, 0, bArr8, i3 + 4, 2);
            System.arraycopy(bArr3, 0, bArr8, i3 + 6, 4);
            getCRC(bArr8, bArr8[0] - 1);
            SendCMD(bArr8);
            if (GetCMDData(this.recvBuff, this.recvLength, 193, 1500) != 0) {
                return 48;
            }
            bArr7[0] = 0;
            byte[] bArr9 = this.recvBuff;
            if (bArr9[3] == 0) {
                System.arraycopy(bArr9, 4, bArr6, 0, 2);
                b6 = 255;
            } else {
                b6 = 255;
                if ((bArr9[3] & 255) == 252) {
                    bArr7[0] = bArr9[4];
                }
            }
            return this.recvBuff[3] & b6;
        }
        if (i2 != 255 || b4 == 0) {
            return 255;
        }
        int i4 = b4 & 255;
        if (i4 % 8 == 0) {
            i = i4 / 8;
        } else {
            i = (i4 / 8) + 1;
        }
        byte[] bArr10 = new byte[i + 16];
        bArr10[0] = (byte) (i + 15);
        bArr10[1] = b;
        bArr10[2] = -63;
        bArr10[3] = b2;
        System.arraycopy(bArr2, 0, bArr10, 4, 2);
        System.arraycopy(bArr3, 0, bArr10, 6, 4);
        bArr10[10] = b3;
        bArr10[11] = bArr4[0];
        bArr10[12] = bArr4[1];
        bArr10[13] = b4;
        System.arraycopy(bArr5, 0, bArr10, 14, i);
        getCRC(bArr10, bArr10[0] - 1);
        SendCMD(bArr10);
        if (GetCMDData(this.recvBuff, this.recvLength, 193, 1500) != 0) {
            return 48;
        }
        bArr7[0] = 0;
        byte[] bArr11 = this.recvBuff;
        if (bArr11[3] == 0) {
            System.arraycopy(bArr11, 4, bArr6, 0, 2);
            b5 = 255;
        } else {
            b5 = 255;
            if ((bArr11[3] & 255) == 252) {
                bArr7[0] = bArr11[4];
            }
        }
        return this.recvBuff[3] & b5;
    }

    public int Fd_WriteReg(byte b, byte b2, byte[] bArr, byte[] bArr2, byte[] bArr3, byte[] bArr4, byte b3, byte[] bArr5, byte b4, byte[] bArr6, byte[] bArr7) {
        int i;
        int i2 = b2 & 255;
        if (i2 >= 0 && i2 < 16) {
            int i3 = b2 * 2;
            byte[] bArr8 = new byte[i3 + 14];
            bArr8[0] = (byte) (i3 + 13);
            bArr8[1] = b;
            bArr8[2] = -62;
            bArr8[3] = b2;
            if (b2 > 0 && b2 < 16) {
                System.arraycopy(bArr, 0, bArr8, 4, i3);
            }
            System.arraycopy(bArr2, 0, bArr8, i3 + 4, 2);
            System.arraycopy(bArr3, 0, bArr8, i3 + 6, 2);
            System.arraycopy(bArr4, 0, bArr8, i3 + 8, 4);
            getCRC(bArr8, bArr8[0] - 1);
            SendCMD(bArr8);
            if (GetCMDData(this.recvBuff, this.recvLength, 194, 1500) != 0) {
                return 48;
            }
            bArr7[0] = 0;
            byte[] bArr9 = this.recvBuff;
            if ((bArr9[3] & 255) == 252) {
                bArr7[0] = bArr9[4];
            }
            return bArr9[3] & 255;
        }
        if (i2 != 255 || b4 == 0) {
            return 255;
        }
        int i4 = b4 & 255;
        if (i4 % 8 == 0) {
            i = i4 / 8;
        } else {
            i = (i4 / 8) + 1;
        }
        byte[] bArr10 = new byte[i + 18];
        bArr10[0] = (byte) (i + 17);
        bArr10[1] = b;
        bArr10[2] = -62;
        bArr10[3] = b2;
        System.arraycopy(bArr2, 0, bArr10, 4, 2);
        System.arraycopy(bArr3, 0, bArr10, 6, 2);
        System.arraycopy(bArr4, 0, bArr10, 8, 4);
        bArr10[12] = b3;
        bArr10[13] = bArr5[0];
        bArr10[14] = bArr5[1];
        bArr10[15] = b4;
        System.arraycopy(bArr6, 0, bArr10, 16, i);
        getCRC(bArr10, bArr10[0] - 1);
        SendCMD(bArr10);
        if (GetCMDData(this.recvBuff, this.recvLength, 194, 1500) != 0) {
            return 48;
        }
        bArr7[0] = 0;
        byte[] bArr11 = this.recvBuff;
        if ((bArr11[3] & 255) == 252) {
            bArr7[0] = bArr11[4];
        }
        return bArr11[3] & 255;
    }

    public int Fd_ReadMemory(byte b, byte b2, byte[] bArr, byte[] bArr2, byte b3, byte[] bArr3, byte b4, byte[] bArr4, byte b5, byte[] bArr5, byte b6, byte[] bArr6, byte[] bArr7, byte[] bArr8) {
        int i;
        byte b7;
        byte b8;
        int i2 = b2 & 255;
        if (i2 >= 0 && i2 < 16) {
            int i3 = b2 * 2;
            byte[] bArr9 = new byte[i3 + 18];
            bArr9[0] = (byte) (i3 + 17);
            bArr9[1] = b;
            bArr9[2] = -61;
            bArr9[3] = b2;
            if (b2 > 0 && b2 < 16) {
                System.arraycopy(bArr, 0, bArr9, 4, i3);
            }
            System.arraycopy(bArr2, 0, bArr9, i3 + 4, 2);
            bArr9[i3 + 6] = b3;
            System.arraycopy(bArr3, 0, bArr9, i3 + 7, 4);
            bArr9[i3 + 11] = b4;
            System.arraycopy(bArr4, 0, bArr9, i3 + 12, 4);
            getCRC(bArr9, bArr9[0] - 1);
            SendCMD(bArr9);
            if (GetCMDData(this.recvBuff, this.recvLength, 195, 1500) != 0) {
                return 48;
            }
            byte[] bArr10 = this.recvBuff;
            if (bArr10[3] == 0) {
                bArr8[0] = 0;
                b8 = 255;
                System.arraycopy(bArr10, 4, bArr7, 0, b3 & 255);
            } else {
                b8 = 255;
                if ((bArr10[3] & 255) == 252) {
                    bArr8[0] = bArr10[4];
                }
            }
            return this.recvBuff[3] & b8;
        }
        if (i2 != 255 || b6 == 0) {
            return 255;
        }
        int i4 = b6 & 255;
        if (i4 % 8 == 0) {
            i = i4 / 8;
        } else {
            i = (i4 / 8) + 1;
        }
        byte[] bArr11 = new byte[i + 22];
        bArr11[0] = (byte) (i + 21);
        bArr11[1] = b;
        bArr11[2] = -61;
        bArr11[3] = b2;
        System.arraycopy(bArr2, 0, bArr11, 4, 2);
        bArr11[6] = b3;
        System.arraycopy(bArr3, 0, bArr11, 7, 4);
        bArr11[11] = b4;
        System.arraycopy(bArr4, 0, bArr11, 12, 4);
        bArr11[16] = b5;
        bArr11[17] = bArr5[0];
        bArr11[18] = bArr5[1];
        bArr11[19] = b6;
        System.arraycopy(bArr6, 0, bArr11, 20, i);
        getCRC(bArr11, bArr11[0] - 1);
        SendCMD(bArr11);
        if (GetCMDData(this.recvBuff, this.recvLength, 195, 1500) != 0) {
            return 48;
        }
        byte[] bArr12 = this.recvBuff;
        if (bArr12[3] == 0) {
            bArr8[0] = 0;
            b7 = 255;
            System.arraycopy(bArr12, 4, bArr7, 0, b3 & 255);
        } else {
            b7 = 255;
            if ((bArr12[3] & 255) == 252) {
                bArr8[0] = bArr12[4];
            }
        }
        return this.recvBuff[3] & b7;
    }

    public int Fd_WriteMemory(byte b, byte b2, byte[] bArr, byte[] bArr2, int i, byte[] bArr3, byte[] bArr4, byte b3, byte[] bArr5, byte b4, byte[] bArr6, byte b5, byte[] bArr7, byte[] bArr8) {
        int i2;
        int i3 = b2 & 255;
        if (i3 >= 0 && i3 < 16) {
            int i4 = b2 * 2;
            byte[] bArr9 = new byte[i4 + 18 + i];
            bArr9[0] = (byte) (i4 + 17 + i);
            bArr9[1] = b;
            bArr9[2] = -60;
            bArr9[3] = (byte) i;
            bArr9[4] = b2;
            if (b2 > 0 && b2 < 16) {
                System.arraycopy(bArr, 0, bArr9, 5, i4);
            }
            System.arraycopy(bArr2, 0, bArr9, i4 + 5, 2);
            int i5 = i4 + 7;
            System.arraycopy(bArr3, 0, bArr9, i5, i);
            System.arraycopy(bArr4, 0, bArr9, i5 + i, 4);
            bArr9[i4 + 11] = b3;
            System.arraycopy(bArr5, 0, bArr9, i4 + 12, 4);
            getCRC(bArr9, bArr9[0] - 1);
            SendCMD(bArr9);
            if (GetCMDData(this.recvBuff, this.recvLength, 196, 1500) != 0) {
                return 48;
            }
            bArr8[0] = 0;
            byte[] bArr10 = this.recvBuff;
            if ((bArr10[3] & 255) == 252) {
                bArr8[0] = bArr10[4];
            }
            return bArr10[3] & 255;
        }
        if (i3 != 255 || b5 == 0) {
            return 255;
        }
        int i6 = b5 & 255;
        if (i6 % 8 == 0) {
            i2 = i6 / 8;
        } else {
            i2 = (i6 / 8) + 1;
        }
        byte[] bArr11 = new byte[i2 + 22 + i];
        bArr11[0] = (byte) (i2 + 21 + i);
        bArr11[1] = b;
        bArr11[2] = -60;
        bArr11[3] = (byte) i;
        bArr11[4] = b2;
        System.arraycopy(bArr2, 0, bArr11, 5, 2);
        System.arraycopy(bArr3, 0, bArr11, 7, i);
        System.arraycopy(bArr4, 0, bArr11, i + 7, 4);
        bArr11[i + 11] = b3;
        System.arraycopy(bArr5, 0, bArr11, i + 12, 4);
        bArr11[i + 16] = b4;
        bArr11[i + 17] = bArr6[0];
        bArr11[i + 18] = bArr6[1];
        bArr11[i + 19] = b5;
        System.arraycopy(bArr7, 0, bArr11, i + 20, i2);
        getCRC(bArr11, bArr11[0] - 1);
        SendCMD(bArr11);
        if (GetCMDData(this.recvBuff, this.recvLength, 196, 1500) != 0) {
            return 48;
        }
        bArr8[0] = 0;
        byte[] bArr12 = this.recvBuff;
        if ((bArr12[3] & 255) == 252) {
            bArr8[0] = bArr12[4];
        }
        return bArr12[3] & 255;
    }

    public int Fd_GetTemperature(byte b, byte b2, byte[] bArr, byte b3, byte b4, byte b5, byte b6, byte b7, byte[] bArr2, byte b8, byte[] bArr3, byte b9, byte[] bArr4, byte[] bArr5, byte[] bArr6) {
        int i;
        byte b10;
        byte b11;
        int i2 = b2 & 255;
        if (i2 >= 0 && i2 < 16) {
            int i3 = b2 * 2;
            byte[] bArr7 = new byte[i3 + 15];
            bArr7[0] = (byte) (i3 + 14);
            bArr7[1] = b;
            bArr7[2] = -59;
            bArr7[3] = b2;
            if (b2 > 0 && b2 < 16) {
                System.arraycopy(bArr, 0, bArr7, 4, i3);
            }
            bArr7[i3 + 4] = b3;
            bArr7[i3 + 5] = b4;
            bArr7[i3 + 6] = b5;
            bArr7[i3 + 7] = b6;
            bArr7[i3 + 8] = b7;
            System.arraycopy(bArr2, 0, bArr7, i3 + 9, 4);
            getCRC(bArr7, bArr7[0] - 1);
            SendCMD(bArr7);
            if (GetCMDData(this.recvBuff, this.recvLength, 197, 1500) != 0) {
                return 48;
            }
            bArr6[0] = 0;
            byte[] bArr8 = this.recvBuff;
            if (bArr8[3] == 0) {
                System.arraycopy(bArr8, 4, bArr5, 0, 2);
                b11 = 255;
            } else {
                b11 = 255;
                if ((bArr8[3] & 255) == 252) {
                    bArr6[0] = bArr8[4];
                }
            }
            return this.recvBuff[3] & b11;
        }
        if (i2 != 255 || b9 == 0) {
            return 255;
        }
        int i4 = b9 & 255;
        if (i4 % 8 == 0) {
            i = i4 / 8;
        } else {
            i = (i4 / 8) + 1;
        }
        byte[] bArr9 = new byte[i + 19];
        bArr9[0] = (byte) (i + 18);
        bArr9[1] = b;
        bArr9[2] = -59;
        bArr9[3] = b2;
        bArr9[4] = b3;
        bArr9[5] = b4;
        bArr9[6] = b5;
        bArr9[7] = b6;
        bArr9[8] = b7;
        System.arraycopy(bArr2, 0, bArr9, 9, 4);
        bArr9[13] = b8;
        bArr9[14] = bArr3[0];
        bArr9[15] = bArr3[1];
        bArr9[16] = b9;
        System.arraycopy(bArr4, 0, bArr9, 17, i);
        getCRC(bArr9, bArr9[0] - 1);
        SendCMD(bArr9);
        if (GetCMDData(this.recvBuff, this.recvLength, 197, 1500) != 0) {
            return 48;
        }
        bArr6[0] = 0;
        byte[] bArr10 = this.recvBuff;
        if (bArr10[3] == 0) {
            System.arraycopy(bArr10, 4, bArr5, 0, 2);
            b10 = 255;
        } else {
            b10 = 255;
            if ((bArr10[3] & 255) == 252) {
                bArr6[0] = bArr10[4];
            }
        }
        return this.recvBuff[3] & b10;
    }

    public int Fd_StartLogging(byte b, byte b2, byte[] bArr, byte[] bArr2, byte[] bArr3, byte[] bArr4, byte b3, byte[] bArr5, byte b4, byte[] bArr6, byte[] bArr7) {
        int i;
        bArr7[0] = 0;
        int i2 = b2 & 255;
        if (i2 >= 0 && i2 < 16) {
            int i3 = b2 * 2;
            byte[] bArr8 = new byte[i3 + 14];
            bArr8[0] = (byte) (i3 + 13);
            bArr8[1] = b;
            bArr8[2] = -58;
            bArr8[3] = b2;
            if (b2 > 0 && b2 < 16) {
                System.arraycopy(bArr, 0, bArr8, 4, i3);
            }
            System.arraycopy(bArr2, 0, bArr8, i3 + 4, 2);
            System.arraycopy(bArr3, 0, bArr8, i3 + 6, 2);
            System.arraycopy(bArr4, 0, bArr8, i3 + 8, 4);
            getCRC(bArr8, bArr8[0] - 1);
            SendCMD(bArr8);
            if (GetCMDData(this.recvBuff, this.recvLength, 198, 1500) != 0) {
                return 48;
            }
            bArr7[0] = 0;
            byte[] bArr9 = this.recvBuff;
            if ((bArr9[3] & 255) == 252) {
                bArr7[0] = bArr9[4];
            }
            return bArr9[3] & 255;
        }
        if (i2 != 255 || b4 == 0) {
            return 255;
        }
        int i4 = b4 & 255;
        if (i4 % 8 == 0) {
            i = i4 / 8;
        } else {
            i = (i4 / 8) + 1;
        }
        byte[] bArr10 = new byte[i + 18];
        bArr10[0] = (byte) (i + 17);
        bArr10[1] = b;
        bArr10[2] = -58;
        bArr10[3] = b2;
        System.arraycopy(bArr2, 0, bArr10, 4, 2);
        System.arraycopy(bArr3, 0, bArr10, 6, 2);
        System.arraycopy(bArr4, 0, bArr10, 8, 4);
        bArr10[12] = b3;
        bArr10[13] = bArr5[0];
        bArr10[14] = bArr5[1];
        bArr10[15] = b4;
        System.arraycopy(bArr6, 0, bArr10, 16, i);
        getCRC(bArr10, bArr10[0] - 1);
        SendCMD(bArr10);
        if (GetCMDData(this.recvBuff, this.recvLength, 198, 1500) != 0) {
            return 48;
        }
        bArr7[0] = 0;
        byte[] bArr11 = this.recvBuff;
        if ((bArr11[3] & 255) == 252) {
            bArr7[0] = bArr11[4];
        }
        return bArr11[3] & 255;
    }

    public int Fd_StopLogging(byte b, byte b2, byte[] bArr, byte[] bArr2, byte[] bArr3, byte b3, byte[] bArr4, byte b4, byte[] bArr5, byte[] bArr6) {
        int i;
        bArr6[0] = 0;
        int i2 = b2 & 255;
        if (i2 >= 0 && i2 < 16) {
            int i3 = b2 * 2;
            byte[] bArr7 = new byte[i3 + 14];
            bArr7[0] = (byte) (i3 + 13);
            bArr7[1] = b;
            bArr7[2] = -57;
            bArr7[3] = b2;
            if (b2 > 0 && b2 < 16) {
                System.arraycopy(bArr, 0, bArr7, 4, i3);
            }
            System.arraycopy(bArr2, 0, bArr7, i3 + 4, 4);
            System.arraycopy(bArr3, 0, bArr7, i3 + 8, 4);
            getCRC(bArr7, bArr7[0] - 1);
            SendCMD(bArr7);
            if (GetCMDData(this.recvBuff, this.recvLength, 199, 1500) != 0) {
                return 48;
            }
            bArr6[0] = 0;
            byte[] bArr8 = this.recvBuff;
            if ((bArr8[3] & 255) == 252) {
                bArr6[0] = bArr8[4];
            }
            return bArr8[3] & 255;
        }
        if (i2 != 255 || b4 == 0) {
            return 255;
        }
        int i4 = b4 & 255;
        if (i4 % 8 == 0) {
            i = i4 / 8;
        } else {
            i = (i4 / 8) + 1;
        }
        byte[] bArr9 = new byte[i + 18];
        bArr9[0] = (byte) (i + 17);
        bArr9[1] = b;
        bArr9[2] = -57;
        bArr9[3] = b2;
        System.arraycopy(bArr2, 0, bArr9, 4, 4);
        System.arraycopy(bArr2, 0, bArr9, 8, 4);
        bArr9[12] = b3;
        bArr9[13] = bArr4[0];
        bArr9[14] = bArr4[1];
        bArr9[15] = b4;
        System.arraycopy(bArr5, 0, bArr9, 16, i);
        getCRC(bArr9, bArr9[0] - 1);
        SendCMD(bArr9);
        if (GetCMDData(this.recvBuff, this.recvLength, 199, 1500) != 0) {
            return 48;
        }
        bArr6[0] = 0;
        byte[] bArr10 = this.recvBuff;
        if ((bArr10[3] & 255) == 252) {
            bArr6[0] = bArr10[4];
        }
        return bArr10[3] & 255;
    }

    public int Fd_OP_Mode_Chk(byte b, byte b2, byte[] bArr, byte b3, byte[] bArr2, byte b4, byte[] bArr3, byte b5, byte[] bArr4, byte[] bArr5, byte[] bArr6) {
        int i;
        bArr6[0] = 0;
        int i2 = b2 & 255;
        if (i2 >= 0 && i2 < 16) {
            int i3 = b2 * 2;
            byte[] bArr7 = new byte[i3 + 11];
            bArr7[0] = (byte) (i3 + 10);
            bArr7[1] = b;
            bArr7[2] = -56;
            bArr7[3] = b2;
            if (b2 > 0 && b2 < 16) {
                System.arraycopy(bArr, 0, bArr7, 4, i3);
            }
            bArr7[i3 + 4] = b3;
            System.arraycopy(bArr2, 0, bArr7, i3 + 5, 4);
            getCRC(bArr7, bArr7[0] - 1);
            SendCMD(bArr7);
            if (GetCMDData(this.recvBuff, this.recvLength, 200, 1500) != 0) {
                return 48;
            }
            bArr6[0] = 0;
            byte[] bArr8 = this.recvBuff;
            if ((bArr8[3] & 255) == 0) {
                bArr5[0] = bArr8[4];
                bArr5[1] = bArr8[5];
            } else if ((bArr8[3] & 255) == 252) {
                bArr6[0] = bArr8[4];
            }
            return bArr8[3] & 255;
        }
        if (i2 != 255 || b5 == 0) {
            return 255;
        }
        int i4 = b5 & 255;
        if (i4 % 8 == 0) {
            i = i4 / 8;
        } else {
            i = (i4 / 8) + 1;
        }
        byte[] bArr9 = new byte[i + 15];
        bArr9[0] = (byte) (i + 14);
        bArr9[1] = b;
        bArr9[2] = -56;
        bArr9[3] = b2;
        bArr9[4] = b3;
        System.arraycopy(bArr2, 0, bArr9, 5, 4);
        bArr9[9] = b4;
        bArr9[10] = bArr3[0];
        bArr9[11] = bArr3[1];
        bArr9[12] = b5;
        System.arraycopy(bArr4, 0, bArr9, 13, i);
        getCRC(bArr9, bArr9[0] - 1);
        SendCMD(bArr9);
        if (GetCMDData(this.recvBuff, this.recvLength, 200, 1500) != 0) {
            return 48;
        }
        bArr6[0] = 0;
        byte[] bArr10 = this.recvBuff;
        if ((bArr10[3] & 255) == 0) {
            bArr5[0] = bArr10[4];
            bArr5[1] = bArr10[5];
        } else if ((bArr10[3] & 255) == 252) {
            bArr6[0] = bArr10[4];
        }
        return bArr10[3] & 255;
    }

    /* JADX WARN: Code restructure failed: missing block: B:33:0x00bc, code lost:            return r12;     */
    /* JADX WARN: Code restructure failed: missing block: B:44:0x00b8, code lost:            if (r20[0] <= 0) goto L44;     */
    /* JADX WARN: Code restructure failed: missing block: B:45:0x00ba, code lost:            return 0;     */
    /* JADX WARN: Code restructure failed: missing block: B:46:0x00bb, code lost:            return 1;     */
    /* JADX WARN: Code restructure failed: missing block: B:56:0x00d4, code lost:            if (r10 <= r6) goto L54;     */
    /* JADX WARN: Code restructure failed: missing block: B:57:0x00d6, code lost:            r0 = 0;        java.lang.System.arraycopy(r11, r6, r2, 0, r7);        r6 = r7;     */
    /* JADX WARN: Code restructure failed: missing block: B:64:0x00dc, code lost:            r0 = 0;        r6 = 0;     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    int GetMemDataFromPort(byte r18, byte[] r19, int[] r20, int r21) {
        /*
            Method dump skipped, instructions count: 246
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.rfid.trans.BaseReader.GetMemDataFromPort(byte, byte[], int[], int):int");
    }

    public int Fd_ExtReadMemory(byte b, byte b2, byte[] bArr, byte[] bArr2, byte[] bArr3, byte[] bArr4, byte b3, byte[] bArr5, byte b4, byte[] bArr6, byte b5, byte[] bArr7, byte[] bArr8, int[] iArr, byte[] bArr9) {
        int i;
        this.packIndex = -1;
        int i2 = b2 & 255;
        if (i2 >= 0 && i2 < 16) {
            int i3 = b2 * 2;
            byte[] bArr10 = new byte[i3 + 19];
            bArr10[0] = (byte) (i3 + 18);
            bArr10[1] = b;
            bArr10[2] = -53;
            bArr10[3] = b2;
            if (b2 > 0 && b2 < 16) {
                System.arraycopy(bArr, 0, bArr10, 4, i3);
            }
            System.arraycopy(bArr2, 0, bArr10, i3 + 4, 2);
            System.arraycopy(bArr3, 0, bArr10, i3 + 6, 2);
            System.arraycopy(bArr4, 0, bArr10, i3 + 8, 4);
            bArr10[i3 + 12] = b3;
            System.arraycopy(bArr5, 0, bArr10, i3 + 13, 4);
            getCRC(bArr10, bArr10[0] - 1);
            SendCMD(bArr10);
            return GetMemDataFromPort(b, bArr8, iArr, 203);
        }
        if (i2 != 255 || b5 == 0) {
            return 255;
        }
        int i4 = b5 & 255;
        if (i4 % 8 == 0) {
            i = i4 / 8;
        } else {
            i = (i4 / 8) + 1;
        }
        byte[] bArr11 = new byte[i + 23];
        bArr11[0] = (byte) (i + 22);
        bArr11[1] = b;
        bArr11[2] = -53;
        bArr11[3] = b2;
        System.arraycopy(bArr2, 0, bArr11, 4, 2);
        System.arraycopy(bArr3, 0, bArr11, 6, 2);
        System.arraycopy(bArr4, 0, bArr11, 8, 4);
        bArr11[12] = b3;
        System.arraycopy(bArr5, 0, bArr11, 13, 4);
        bArr11[17] = b4;
        bArr11[18] = bArr6[0];
        bArr11[19] = bArr6[1];
        bArr11[20] = b5;
        System.arraycopy(bArr7, 0, bArr11, 21, i);
        getCRC(bArr11, bArr11[0] - 1);
        SendCMD(bArr11);
        return GetMemDataFromPort(b, bArr8, iArr, 203);
    }

    public int ReadData_GJB(byte b, byte b2, byte[] bArr, byte b3, byte[] bArr2, byte b4, byte[] bArr3, byte[] bArr4, byte[] bArr5) {
        byte b5 = b2;
        int i = b5 * 2;
        byte[] bArr6 = new byte[i + 14];
        bArr6[0] = (byte) (i + 13);
        bArr6[1] = b;
        bArr6[2] = 88;
        bArr6[3] = b5;
        if ((b5 & 255) == 255) {
            b5 = 0;
        }
        if (b5 > 0) {
            System.arraycopy(bArr, 0, bArr6, 4, b5 * 2);
        }
        int i2 = b5 * 2;
        bArr6[i2 + 4] = b3;
        bArr6[i2 + 5] = bArr2[0];
        bArr6[i2 + 6] = bArr2[1];
        bArr6[i2 + 7] = b4;
        System.arraycopy(bArr3, 0, bArr6, i2 + 8, 4);
        getCRC(bArr6, bArr6[0] - 1);
        SendCMD(bArr6);
        if (GetCMDData(this.recvBuff, this.recvLength, 88, PathInterpolatorCompat.MAX_NUM_POINTS) != 0) {
            return 48;
        }
        byte[] bArr7 = this.recvBuff;
        if (bArr7[3] == 0) {
            bArr5[0] = 0;
            System.arraycopy(bArr7, 4, bArr4, 0, b4 * 2);
        } else if ((bArr7[3] & 255) == 252) {
            bArr5[0] = bArr7[4];
        }
        return this.recvBuff[3] & 255;
    }

    public int WriteData_GJB(byte b, byte b2, byte b3, byte[] bArr, byte b4, byte[] bArr2, byte[] bArr3, byte[] bArr4, byte[] bArr5) {
        byte b5 = b3;
        int i = (b5 + b2) * 2;
        byte[] bArr6 = new byte[i + 14];
        bArr6[0] = (byte) (i + 13);
        bArr6[1] = b;
        bArr6[2] = 89;
        bArr6[3] = b2;
        bArr6[4] = b5;
        if ((b5 & 255) == 255) {
            b5 = 0;
        }
        if (b5 > 0) {
            System.arraycopy(bArr, 0, bArr6, 5, b5 * 2);
        }
        int i2 = b5 * 2;
        bArr6[i2 + 5] = b4;
        bArr6[i2 + 6] = bArr2[0];
        bArr6[i2 + 7] = bArr2[1];
        int i3 = b2 * 2;
        System.arraycopy(bArr3, 0, bArr6, i2 + 8, i3);
        System.arraycopy(bArr4, 0, bArr6, i2 + i3 + 9, 4);
        getCRC(bArr6, bArr6[0] - 1);
        SendCMD(bArr6);
        if (GetCMDData(this.recvBuff, this.recvLength, 89, PathInterpolatorCompat.MAX_NUM_POINTS) != 0) {
            return 48;
        }
        byte[] bArr7 = this.recvBuff;
        if (bArr7[3] == 0) {
            bArr5[0] = 0;
        } else if ((bArr7[3] & 255) == 252) {
            bArr5[0] = bArr7[4];
        }
        return bArr7[3] & 255;
    }

    public int Lock_GJB(byte b, byte b2, byte[] bArr, byte b3, byte b4, byte b5, byte[] bArr2, byte[] bArr3) {
        byte b6 = b2;
        int i = b6 * 2;
        byte[] bArr4 = new byte[i + 13];
        bArr4[0] = (byte) (i + 12);
        bArr4[1] = b;
        bArr4[2] = 91;
        bArr4[3] = b6;
        if ((b6 & 255) == 255) {
            b6 = 0;
        }
        if (b6 > 0) {
            System.arraycopy(bArr, 0, bArr4, 4, b6 * 2);
        }
        int i2 = b6 * 2;
        bArr4[i2 + 4] = b3;
        bArr4[i2 + 5] = b4;
        bArr4[i2 + 6] = b5;
        System.arraycopy(bArr2, 0, bArr4, i2 + 7, 4);
        getCRC(bArr4, bArr4[0] - 1);
        SendCMD(bArr4);
        if (GetCMDData(this.recvBuff, this.recvLength, 91, 1000) != 0) {
            return 48;
        }
        byte[] bArr5 = this.recvBuff;
        if (bArr5[3] == 0) {
            bArr3[0] = 0;
        } else if ((bArr5[3] & 255) == 252) {
            bArr3[0] = bArr5[4];
        }
        return bArr5[3] & 255;
    }

    public int Kill_GJB(byte b, byte b2, byte[] bArr, byte[] bArr2, byte[] bArr3) {
        int i = b2 * 2;
        byte[] bArr4 = new byte[i + 10];
        bArr4[0] = (byte) (i + 9);
        bArr4[1] = b;
        bArr4[2] = 92;
        bArr4[3] = b2;
        if ((b2 & 255) == 255) {
            b2 = 0;
        }
        if (b2 > 0) {
            System.arraycopy(bArr, 0, bArr4, 4, b2 * 2);
        }
        System.arraycopy(bArr2, 0, bArr4, (b2 * 2) + 4, 4);
        getCRC(bArr4, bArr4[0] - 1);
        SendCMD(bArr4);
        if (GetCMDData(this.recvBuff, this.recvLength, 92, 1000) != 0) {
            return 48;
        }
        byte[] bArr5 = this.recvBuff;
        if (bArr5[3] == 0) {
            bArr3[0] = 0;
        } else if ((bArr5[3] & 255) == 252) {
            bArr3[0] = bArr5[4];
        }
        return bArr5[3] & 255;
    }

    public int EraseData_GJB(byte b, byte b2, byte[] bArr, byte b3, byte[] bArr2, byte[] bArr3, byte[] bArr4, byte[] bArr5) {
        byte b4 = b2;
        int i = b4 * 2;
        byte[] bArr6 = new byte[i + 15];
        bArr6[0] = (byte) (i + 14);
        bArr6[1] = b;
        bArr6[2] = 90;
        bArr6[3] = b4;
        if ((b4 & 255) == 255) {
            b4 = 0;
        }
        if (b4 > 0) {
            System.arraycopy(bArr, 0, bArr6, 4, b4 * 2);
        }
        int i2 = b4 * 2;
        bArr6[i2 + 4] = b3;
        bArr6[i2 + 5] = bArr2[0];
        bArr6[i2 + 6] = bArr2[1];
        bArr6[i2 + 7] = bArr3[0];
        bArr6[i2 + 8] = bArr3[1];
        System.arraycopy(bArr4, 0, bArr6, i2 + 9, 4);
        getCRC(bArr6, bArr6[0] - 1);
        SendCMD(bArr6);
        if (GetCMDData(this.recvBuff, this.recvLength, 90, PathInterpolatorCompat.MAX_NUM_POINTS) != 0) {
            return 48;
        }
        byte[] bArr7 = this.recvBuff;
        if (bArr7[3] == 0) {
            bArr5[0] = 0;
        } else if ((bArr7[3] & 255) == 252) {
            bArr5[0] = bArr7[4];
        }
        return bArr7[3] & 255;
    }

    public int ReadData_GB(byte b, byte b2, byte[] bArr, byte b3, byte[] bArr2, byte b4, byte[] bArr3, byte[] bArr4, byte[] bArr5) {
        byte b5 = b2;
        int i = b5 * 2;
        byte[] bArr6 = new byte[i + 14];
        bArr6[0] = (byte) (i + 13);
        bArr6[1] = b;
        bArr6[2] = 88;
        bArr6[3] = b5;
        if ((b5 & 255) == 255) {
            b5 = 0;
        }
        if (b5 > 0) {
            System.arraycopy(bArr, 0, bArr6, 4, b5 * 2);
        }
        int i2 = b5 * 2;
        bArr6[i2 + 4] = b3;
        bArr6[i2 + 5] = bArr2[0];
        bArr6[i2 + 6] = bArr2[1];
        bArr6[i2 + 7] = b4;
        System.arraycopy(bArr3, 0, bArr6, i2 + 8, 4);
        getCRC(bArr6, bArr6[0] - 1);
        SendCMD(bArr6);
        if (GetCMDData(this.recvBuff, this.recvLength, 88, 2000) != 0) {
            return 48;
        }
        byte[] bArr7 = this.recvBuff;
        if (bArr7[3] == 0) {
            bArr5[0] = 0;
            System.arraycopy(bArr7, 4, bArr4, 0, b4 * 2);
        } else if ((bArr7[3] & 255) == 252) {
            bArr5[0] = bArr7[4];
        }
        return this.recvBuff[3] & 255;
    }

    public int WriteData_GB(byte b, byte b2, byte b3, byte[] bArr, byte b4, byte[] bArr2, byte[] bArr3, byte[] bArr4, byte[] bArr5) {
        byte b5 = b3;
        int i = (b5 + b2) * 2;
        byte[] bArr6 = new byte[i + 14];
        bArr6[0] = (byte) (i + 13);
        bArr6[1] = b;
        bArr6[2] = 89;
        bArr6[3] = b2;
        bArr6[4] = b5;
        if ((b5 & 255) == 255) {
            b5 = 0;
        }
        if (b5 > 0) {
            System.arraycopy(bArr, 0, bArr6, 5, b5 * 2);
        }
        int i2 = b5 * 2;
        bArr6[i2 + 5] = b4;
        bArr6[i2 + 6] = bArr2[0];
        bArr6[i2 + 7] = bArr2[1];
        int i3 = b2 * 2;
        System.arraycopy(bArr3, 0, bArr6, i2 + 8, i3);
        System.arraycopy(bArr4, 0, bArr6, i2 + i3 + 9, 4);
        getCRC(bArr6, bArr6[0] - 1);
        SendCMD(bArr6);
        if (GetCMDData(this.recvBuff, this.recvLength, 89, 2000) != 0) {
            return 48;
        }
        byte[] bArr7 = this.recvBuff;
        if (bArr7[3] == 0) {
            bArr5[0] = 0;
        } else if ((bArr7[3] & 255) == 252) {
            bArr5[0] = bArr7[4];
        }
        return bArr7[3] & 255;
    }

    public int Lock_GB(byte b, byte b2, byte[] bArr, byte b3, byte b4, byte b5, byte[] bArr2, byte[] bArr3) {
        byte b6 = b2;
        int i = b6 * 2;
        byte[] bArr4 = new byte[i + 13];
        bArr4[0] = (byte) (i + 12);
        bArr4[1] = b;
        bArr4[2] = 91;
        bArr4[3] = b6;
        if ((b6 & 255) == 255) {
            b6 = 0;
        }
        if (b6 > 0) {
            System.arraycopy(bArr, 0, bArr4, 4, b6 * 2);
        }
        int i2 = b6 * 2;
        bArr4[i2 + 4] = b3;
        bArr4[i2 + 5] = b4;
        bArr4[i2 + 6] = b5;
        System.arraycopy(bArr2, 0, bArr4, i2 + 7, 4);
        getCRC(bArr4, bArr4[0] - 1);
        SendCMD(bArr4);
        if (GetCMDData(this.recvBuff, this.recvLength, 91, 1000) != 0) {
            return 48;
        }
        byte[] bArr5 = this.recvBuff;
        if (bArr5[3] == 0) {
            bArr3[0] = 0;
        } else if ((bArr5[3] & 255) == 252) {
            bArr3[0] = bArr5[4];
        }
        return bArr5[3] & 255;
    }

    public int Kill_GB(byte b, byte b2, byte[] bArr, byte[] bArr2, byte[] bArr3) {
        int i = b2 * 2;
        byte[] bArr4 = new byte[i + 10];
        bArr4[0] = (byte) (i + 9);
        bArr4[1] = b;
        bArr4[2] = 92;
        bArr4[3] = b2;
        if ((b2 & 255) == 255) {
            b2 = 0;
        }
        if (b2 > 0) {
            System.arraycopy(bArr, 0, bArr4, 4, b2 * 2);
        }
        System.arraycopy(bArr2, 0, bArr4, (b2 * 2) + 4, 4);
        getCRC(bArr4, bArr4[0] - 1);
        SendCMD(bArr4);
        if (GetCMDData(this.recvBuff, this.recvLength, 92, 1000) != 0) {
            return 48;
        }
        byte[] bArr5 = this.recvBuff;
        if (bArr5[3] == 0) {
            bArr3[0] = 0;
        } else if ((bArr5[3] & 255) == 252) {
            bArr3[0] = bArr5[4];
        }
        return bArr5[3] & 255;
    }

    public int EraseData_GB(byte b, byte b2, byte[] bArr, byte b3, byte[] bArr2, byte[] bArr3, byte[] bArr4, byte[] bArr5) {
        byte b4 = b2;
        int i = b4 * 2;
        byte[] bArr6 = new byte[i + 15];
        bArr6[0] = (byte) (i + 14);
        bArr6[1] = b;
        bArr6[2] = 90;
        bArr6[3] = b4;
        if ((b4 & 255) == 255) {
            b4 = 0;
        }
        if (b4 > 0) {
            System.arraycopy(bArr, 0, bArr6, 4, b4 * 2);
        }
        int i2 = b4 * 2;
        bArr6[i2 + 4] = b3;
        bArr6[i2 + 5] = bArr2[0];
        bArr6[i2 + 6] = bArr2[1];
        bArr6[i2 + 7] = bArr3[0];
        bArr6[i2 + 8] = bArr3[1];
        System.arraycopy(bArr4, 0, bArr6, i2 + 9, 4);
        getCRC(bArr6, bArr6[0] - 1);
        SendCMD(bArr6);
        if (GetCMDData(this.recvBuff, this.recvLength, 90, 2000) != 0) {
            return 48;
        }
        byte[] bArr7 = this.recvBuff;
        if (bArr7[3] == 0) {
            bArr5[0] = 0;
        } else if ((bArr7[3] & 255) == 252) {
            bArr5[0] = bArr7[4];
        }
        return bArr7[3] & 255;
    }

    public int InventorySingle_6B(byte b, byte[] bArr) {
        byte[] bArr2 = {4, b, 80};
        getCRC(bArr2, bArr2[0] - 1);
        SendCMD(bArr2);
        if (GetCMDData(this.recvBuff, this.recvLength, 80, 1000) != 0) {
            return 48;
        }
        byte[] bArr3 = this.recvBuff;
        if (bArr3[3] == 0) {
            System.arraycopy(bArr3, 5, bArr, 0, 10);
        }
        return this.recvBuff[3] & 255;
    }

    public int InventoryMutiple_6B(byte b, byte b2, byte b3, byte b4, byte[] bArr, byte[] bArr2, int[] iArr) {
        byte[] bArr3 = new byte[16];
        bArr3[0] = 15;
        bArr3[1] = b;
        bArr3[2] = 81;
        bArr3[3] = b2;
        bArr3[4] = b3;
        bArr3[5] = b4;
        System.arraycopy(bArr, 0, bArr3, 6, 8);
        getCRC(bArr3, bArr3[0] - 1);
        SendCMD(bArr3);
        if (GetCMDData(this.recvBuff, this.recvLength, 81, PathInterpolatorCompat.MAX_NUM_POINTS) != 0) {
            return 48;
        }
        byte[] bArr4 = this.recvBuff;
        if (bArr4[3] == 21 || bArr4[3] == 22 || bArr4[3] == 23 || bArr4[3] == 24) {
            iArr[0] = bArr4[5] & 255;
            System.arraycopy(bArr4, 6, bArr2, 0, iArr[0] * 10);
        }
        return this.recvBuff[3] & 255;
    }

    public int ReadData_6B(byte b, byte b2, byte[] bArr, byte b3, byte[] bArr2) {
        byte[] bArr3 = new byte[15];
        bArr3[0] = 14;
        bArr3[1] = b;
        bArr3[2] = 82;
        bArr3[3] = b2;
        System.arraycopy(bArr, 0, bArr3, 4, 8);
        bArr3[12] = b3;
        getCRC(bArr3, bArr3[0] - 1);
        SendCMD(bArr3);
        if (GetCMDData(this.recvBuff, this.recvLength, 82, 2000) != 0) {
            return 48;
        }
        byte[] bArr4 = this.recvBuff;
        if (bArr4[3] == 0) {
            System.arraycopy(bArr4, 4, bArr2, 0, b3 & 255);
        }
        return this.recvBuff[3] & 255;
    }

    public int WriteData_6B(byte b, byte b2, byte[] bArr, byte b3, byte[] bArr2) {
        int i = b3 & 255;
        byte[] bArr3 = new byte[i + 14];
        bArr3[0] = (byte) (i + 13);
        bArr3[1] = b;
        bArr3[2] = 83;
        bArr3[3] = b2;
        System.arraycopy(bArr, 0, bArr3, 4, 8);
        System.arraycopy(bArr2, 0, bArr3, 12, b3);
        getCRC(bArr3, bArr3[0] - 1);
        SendCMD(bArr3);
        if (GetCMDData(this.recvBuff, this.recvLength, 83, 2000) == 0) {
            return this.recvBuff[3] & 255;
        }
        return 48;
    }

    public int Lock_6B(byte b, byte b2, byte[] bArr) {
        byte[] bArr2 = new byte[14];
        bArr2[0] = 13;
        bArr2[1] = b;
        bArr2[2] = 85;
        bArr2[3] = b2;
        System.arraycopy(bArr, 0, bArr2, 4, 8);
        getCRC(bArr2, bArr2[0] - 1);
        SendCMD(bArr2);
        if (GetCMDData(this.recvBuff, this.recvLength, 85, 1000) == 0) {
            return this.recvBuff[3] & 255;
        }
        return 48;
    }

    public int CheckLock_6B(byte b, byte b2, byte[] bArr, byte[] bArr2) {
        byte[] bArr3 = new byte[14];
        bArr3[0] = 13;
        bArr3[1] = b;
        bArr3[2] = 84;
        bArr3[3] = b2;
        System.arraycopy(bArr, 0, bArr3, 4, 8);
        getCRC(bArr3, bArr3[0] - 1);
        SendCMD(bArr3);
        if (GetCMDData(this.recvBuff, this.recvLength, 84, 1000) != 0) {
            return 48;
        }
        byte[] bArr4 = this.recvBuff;
        if (bArr4[3] == 0) {
            bArr2[0] = bArr4[4];
        }
        return bArr4[3] & 255;
    }

    public int RfOutput(byte b, byte b2) {
        byte[] bArr = {5, b, 48, b2};
        getCRC(bArr, bArr[0] - 1);
        SendCMD(bArr);
        if (GetCMDData(this.recvBuff, this.recvLength, 48, 1000) == 0) {
            return this.recvBuff[3] & 255;
        }
        return 48;
    }

    public int SelectCMD(byte b, byte b2, byte b3, byte b4, byte b5) {
        byte[] bArr = {12, b, -102, b2, b3, b4, 1, 0, 0, 0, b5, 0, 0};
        getCRC(bArr, bArr[0] - 1);
        SendCMD(bArr);
        if (GetCMDData(this.recvBuff, this.recvLength, 154, 500) == 0) {
            return this.recvBuff[3] & 255;
        }
        return 48;
    }

    public int SelectCMDByTime(byte b, byte b2, byte b3, byte b4, byte b5, byte b6) {
        byte[] bArr = {13, b, -104, b2, b3, b4, 1, 0, 0, 0, b5, 0, 0, 0};
        getCRC(bArr, bArr[0] - 1);
        SendCMD(bArr);
        if (GetCMDData(this.recvBuff, this.recvLength, 152, 500) == 0) {
            return this.recvBuff[3] & 255;
        }
        return 48;
    }

    public int SelectCMDByTimeNoACk(byte b, byte b2, byte b3, byte b4, byte b5, byte b6) {
        byte[] bArr = {13, b, -104, b2, b3, b4, 1, 0, 0, 0, b5, 0, 0, 0};
        getCRC(bArr, bArr[0] - 1);
        SendCMD(bArr);
        return 0;
    }

    public int SetCfgParameter(byte b, byte b2, byte b3, byte[] bArr, int i) {
        byte[] bArr2 = new byte[i + 7];
        bArr2[0] = (byte) (i + 6);
        bArr2[1] = b;
        bArr2[2] = -22;
        bArr2[3] = b2;
        bArr2[4] = b3;
        if (i > 0) {
            System.arraycopy(bArr, 0, bArr2, 5, i);
        }
        getCRC(bArr2, bArr2[0] - 1);
        SendCMD(bArr2);
        if (GetCMDData(this.recvBuff, this.recvLength, 234, 1000) == 0) {
            return this.recvBuff[3] & 255;
        }
        return 48;
    }

    public int GetCfgParameter(byte b, byte b2, byte[] bArr, int[] iArr) {
        byte[] bArr2 = {5, b, -21, b2};
        getCRC(bArr2, bArr2[0] - 1);
        SendCMD(bArr2);
        if (GetCMDData(this.recvBuff, this.recvLength, 235, 1000) != 0) {
            return 48;
        }
        byte[] bArr3 = this.recvBuff;
        if (bArr3[3] == 0) {
            iArr[0] = this.recvLength[0] - 6;
            System.arraycopy(bArr3, 4, bArr, 0, iArr[0]);
        } else {
            iArr[0] = 0;
        }
        return this.recvBuff[3] & 255;
    }

    public int OperateControl(byte b, byte[] bArr) {
        byte[] bArr2 = {5, b, Byte.MAX_VALUE, bArr[0]};
        getCRC(bArr2, bArr2[0] - 1);
        SendCMD(bArr2);
        if (GetCMDData(this.recvBuff, this.recvLength, 127, 400) != 0) {
            return 48;
        }
        byte[] bArr3 = this.recvBuff;
        if (bArr3[3] == 0) {
            bArr[0] = bArr3[4];
        }
        return bArr3[3] & 255;
    }

    public int SetCustomRegion(byte b, byte b2, int i, int i2, int i3, int i4) {
        byte[] bArr = {11, b, 34, b2, (byte) i, (byte) i2, (byte) i3, (byte) (i4 >> 16), (byte) (i4 >> 8), (byte) i4};
        getCRC(bArr, bArr[0] - 1);
        SendCMD(bArr);
        if (GetCMDData(this.recvBuff, this.recvLength, 34, 500) == 0) {
            return this.recvBuff[3] & 255;
        }
        return 48;
    }

    public int GetCustomRegion(byte b, int[] iArr, int[] iArr2, int[] iArr3, int[] iArr4) {
        byte[] bArr = {4, b, -98};
        getCRC(bArr, bArr[0] - 1);
        SendCMD(bArr);
        if (GetCMDData(this.recvBuff, this.recvLength, 158, 500) != 0) {
            return 48;
        }
        byte[] bArr2 = this.recvBuff;
        if (bArr2[0] == 11) {
            iArr[0] = bArr2[4] & 255;
            iArr2[0] = bArr2[5] & 255;
            iArr3[0] = bArr2[6] & 255;
            iArr4[0] = ((bArr2[7] & 255) << 16) + ((bArr2[8] & 255) << 8) + (bArr2[9] & 255);
        } else if (bArr2[0] == 8) {
            iArr[0] = bArr2[4] & 255;
            iArr2[0] = bArr2[5] & 255;
            iArr3[0] = bArr2[6] & 255;
        }
        return bArr2[3] & 255;
    }

    public int GetModuleDescribe(byte b, byte[] bArr) {
        byte[] bArr2 = {5, b, -26, 2};
        getCRC(bArr2, bArr2[0] - 1);
        SendCMD(bArr2);
        if (GetCMDData(this.recvBuff, this.recvLength, 230, 500) != 0) {
            return 48;
        }
        byte[] bArr3 = this.recvBuff;
        if (bArr3[3] == 0 && bArr3[0] == 22) {
            System.arraycopy(bArr3, 5, bArr, 0, 16);
        }
        return this.recvBuff[3] & 255;
    }

    public int SetProtocol(byte b, byte[] bArr) {
        byte[] bArr2 = {5, b, -50, bArr[0]};
        getCRC(bArr2, bArr2[0] - 1);
        SendCMD(bArr2);
        if (GetCMDData(this.recvBuff, this.recvLength, 206, 400) != 0) {
            return 48;
        }
        byte[] bArr3 = this.recvBuff;
        if (bArr3[3] == 0) {
            bArr[0] = bArr3[4];
        }
        return bArr3[3] & 255;
    }

    public int SetRegionTable(byte b, byte b2) {
        byte[] bArr = {6, b, -26, 4, b2};
        getCRC(bArr, bArr[0] - 1);
        SendCMD(bArr);
        if (GetCMDData(this.recvBuff, this.recvLength, 230, 400) == 0) {
            return this.recvBuff[3] & 255;
        }
        return 48;
    }
}
