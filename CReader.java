package com.rfid.trans;

import android.content.Context;
import android.media.SoundPool;
import java.util.List;

/* loaded from: classes.dex */
public interface CReader {
    int BlockErase_G2(byte b, byte[] bArr, byte b2, byte b3, byte b4, byte[] bArr2, byte[] bArr3);

    int BlockWrite_G2(byte b, byte b2, byte[] bArr, byte b3, byte b4, byte[] bArr2, byte[] bArr3, byte[] bArr4);

    int Connect(String str, int i, int i2);

    int DisConnect();

    int FST_ShowImage(byte b, byte[] bArr);

    int FST_TranImage(byte b, byte[] bArr, byte[] bArr2);

    int Fd_ExtReadMemory(String str, int i, int i2, String str2, byte b, String str3, byte[] bArr, int[] iArr);

    int Fd_GetTemperature(String str, byte b, byte b2, byte b3, byte b4, byte b5, String str2, byte[] bArr);

    int Fd_InitRegfile(String str, String str2);

    int Fd_OP_Mode_Chk(String str, byte b, String str2, byte[] bArr);

    int Fd_ReadMemory(String str, int i, byte b, String str2, byte b2, String str3, byte[] bArr);

    int Fd_ReadReg(String str, int i, String str2, byte[] bArr);

    int Fd_StartLogging(String str, int i, int i2, String str2);

    int Fd_StopLogging(String str, String str2, String str3);

    int Fd_WriteMemory(String str, int i, byte[] bArr, String str2, byte b, String str3);

    int Fd_WriteReg(String str, int i, byte[] bArr, String str2);

    int GetDRM(byte[] bArr);

    String GetDeviceID();

    int GetGPIOStatus(byte[] bArr);

    ReaderParameter GetInventoryPatameter();

    int GetProfile(byte[] bArr);

    String GetRFIDTempreture();

    int GetReaderInformation(byte[] bArr, byte[] bArr2, byte[] bArr3, byte[] bArr4, byte[] bArr5);

    int GetRetryTimes(byte[] bArr);

    int GetWritePower(byte[] bArr);

    int InventoryOnce(byte b, byte b2, byte b3, byte b4, byte b5, byte b6, byte b7, List<ReadTag> list);

    int Kill_G2(byte b, byte[] bArr, byte[] bArr2, byte[] bArr3);

    int LedOn_kx2005x(String str, String str2, byte b);

    int Lock_G2(byte b, byte[] bArr, byte b2, byte b3, byte[] bArr2, byte[] bArr3);

    void PowerControll(Context context, boolean z);

    int ReadData_G2(byte b, byte[] bArr, byte b2, int i, byte b3, byte[] bArr2, byte[] bArr3, byte[] bArr4);

    String ReadData_G2(String str, byte b, int i, byte b2, String str2);

    void ScanRfid();

    int SetAddress(byte b);

    int SetBaudRate(int i);

    void SetCallBack(TagCallback tagCallback);

    int SetDRM(byte b);

    int SetGPIO(byte b);

    void SetInventoryPatameter(ReaderParameter readerParameter);

    void SetMessageBack(RFIDLogCallBack rFIDLogCallBack);

    int SetProfile(byte b);

    int SetRegion(byte b, byte b2, byte b3);

    int SetRetryTimes(byte b);

    int SetRfPower(byte b);

    void SetSoundID(int i, SoundPool soundPool);

    int SetWritePower(byte b);

    int StartRead();

    void StopRead();

    int WriteData_G2(byte b, byte b2, byte[] bArr, byte b3, int i, byte[] bArr2, byte[] bArr3, byte[] bArr4);

    int WriteData_G2(String str, String str2, byte b, int i, String str3);

    int WriteEPC_G2(byte b, byte[] bArr, byte[] bArr2, byte[] bArr3);

    int WriteEPC_G2(String str, String str2);

    boolean isConnect();
}
