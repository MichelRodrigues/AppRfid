package com.rfid.trans;

import android.content.Context;
import android.device.sdk.BuildConfig;
import android.media.SoundPool;
import android.os.SystemClock;
import java.util.ArrayList;
import java.util.List;

/* loaded from: classes.dex */
public class ReaderHelp implements CReader {
    public static volatile boolean isSound = false;
    private TagCallback callback;
    private BaseReader reader = new BaseReader();
    private ReaderParameter param = new ReaderParameter();
    private volatile boolean mWorking = true;
    private volatile Thread mThread = null;
    private volatile boolean soundworking = true;
    private volatile Thread sThread = null;
    private byte[] pOUcharIDList = new byte[25600];
    private volatile int NoCardCOunt = 0;
    private Integer soundid = null;
    private SoundPool soundPool = null;
    private boolean isOpen = false;
    private String devName = BuildConfig.FLAVOR;
    private int logswitch = 0;
    private int RF_Ctrl = 5;
    private int Cur_Ctrl = 5;
    public int ModuleType = 0;
    private int ReTryCount = 0;
    private boolean PermitControl = false;
    long beginTime = 0;
    private int Cfg_Power = 0;
    private List<MaskClass> MaskList = new ArrayList();
    byte CurSession = 0;
    int CurPower = 0;
    boolean firstTime = true;
    private byte Target = 0;
    private byte QValue = 4;
    private int Session = 1;
    private int CardCount = 0;
    private int ReadSpeed = 0;
    private volatile int maskIndex = 0;
    private List<MaskClass> ledMaskList = new ArrayList();
    private int readledType = 0;

    public ReaderHelp() {
        this.param.ComAddr = (byte) -1;
        this.param.IvtType = 0;
        this.param.Memory = 2;
        this.param.Password = "00000000";
        this.param.ScanTime = 50;
        this.param.Session = 1;
        this.param.QValue = 6;
        this.param.WordPtr = 0;
        this.param.Length = 6;
        this.param.Antenna = 128;
        this.param.Interval = 0;
        this.param.MaskLen = (byte) 0;
    }

    public void AddMaskList(MaskClass maskClass) {
        this.MaskList.add(maskClass);
    }

    public void ClearMaskList() {
        this.MaskList.clear();
        this.param.MaskLen = (byte) 0;
    }

    @Override // com.rfid.trans.CReader
    public boolean isConnect() {
        return this.isOpen;
    }

    @Override // com.rfid.trans.CReader
    public void PowerControll(Context context, boolean z) {
        OtgUtils.set53GPIOEnabled(z);
    }

    public void SetLogSwitch(int i) {
        this.reader.SetLogSwitch(i);
    }

    @Override // com.rfid.trans.CReader
    public int Connect(String str, int i, int i2) {
        int Connect = this.reader.Connect(str, i, i2);
        if (Connect == 0) {
            SystemClock.sleep(20L);
            int GetReaderInformation = GetReaderInformation(new byte[2], new byte[1], new byte[1], new byte[1], new byte[1]);
            if (GetReaderInformation != 0) {
                this.reader.DisConnect();
                return GetReaderInformation;
            }
            byte[] bArr = {0};
            if (this.ModuleType == 2) {
                Connect = this.reader.OperateControl(this.param.ComAddr, bArr);
                if (Connect == 0) {
                    this.RF_Ctrl = bArr[0];
                    this.Cur_Ctrl = bArr[0];
                }
            } else {
                Connect = GetReaderInformation;
            }
            this.logswitch = i2;
            this.devName = str;
            this.isOpen = true;
            isSound = false;
            this.soundworking = true;
            if (this.sThread == null) {
                this.sThread = new Thread(new Runnable() { // from class: com.rfid.trans.ReaderHelp.1
                    @Override // java.lang.Runnable
                    public void run() {
                        while (ReaderHelp.this.soundworking) {
                            if (ReaderHelp.isSound && ReaderHelp.this.mWorking) {
                                ReaderHelp.this.playSound();
                                SystemClock.sleep(50L);
                            }
                        }
                        ReaderHelp.this.sThread = null;
                    }
                });
                this.sThread.start();
            }
        }
        return Connect;
    }

    @Override // com.rfid.trans.CReader
    public int DisConnect() {
        try {
            isSound = false;
            this.soundworking = false;
            this.mWorking = false;
            this.isOpen = false;
            Thread.sleep(100L);
        } catch (Exception unused) {
        }
        return this.reader.DisConnect();
    }

    @Override // com.rfid.trans.CReader
    public int GetReaderInformation(byte[] bArr, byte[] bArr2, byte[] bArr3, byte[] bArr4, byte[] bArr5) {
        byte[] bArr6 = new byte[1];
        byte[] bArr7 = {-1};
        this.ModuleType = 0;
        BaseReader baseReader = this.reader;
        int GetReaderInformation = baseReader.GetReaderInformation(bArr7, bArr, bArr6, new byte[1], bArr3, bArr4, bArr5, bArr2, new byte[1], new byte[1], new byte[1], new byte[1], new byte[1]);
        if (GetReaderInformation == 0) {
            this.Cfg_Power = bArr2[0];
            this.param.ComAddr = bArr7[0];
            if ((bArr6[0] & 255) == 112 || (bArr6[0] & 255) == 113 || (bArr6[0] & 255) == 49) {
                this.ModuleType = 2;
            } else if ((bArr6[0] & 255) == 15 || (bArr6[0] & 255) == 16 || (bArr6[0] & 255) == 80 || (bArr6[0] & 255) == 81 || (bArr6[0] & 255) == 82) {
                this.ModuleType = 1;
            }
        }
        return GetReaderInformation;
    }

    public int GetReaderType() {
        byte[] bArr = new byte[1];
        byte[] bArr2 = new byte[1];
        byte[] bArr3 = new byte[1];
        byte[] bArr4 = new byte[1];
        byte[] bArr5 = new byte[1];
        byte[] bArr6 = {-1};
        byte[] bArr7 = new byte[1];
        this.ModuleType = 0;
        if (this.reader.GetReaderInformation(bArr6, new byte[2], bArr2, bArr3, new byte[1], new byte[1], new byte[1], bArr, bArr7, bArr4, bArr5, new byte[1], new byte[1]) != 0) {
            return -1;
        }
        this.Cfg_Power = bArr[0];
        this.param.ComAddr = bArr6[0];
        if ((bArr2[0] & 255) == 112 || (bArr2[0] & 255) == 113 || (bArr2[0] & 255) == 49) {
            this.ModuleType = 2;
        } else if ((bArr2[0] & 255) == 15 || (bArr2[0] & 255) == 16 || (bArr2[0] & 255) == 80 || (bArr2[0] & 255) == 81 || (bArr2[0] & 255) == 82) {
            this.ModuleType = 1;
        }
        return bArr2[0] & 255;
    }

    @Override // com.rfid.trans.CReader
    public int SetRfPower(byte b) {
        int SetRfPower = this.reader.SetRfPower(this.param.ComAddr, b);
        if (SetRfPower == 0) {
            this.Cfg_Power = b;
        }
        return SetRfPower;
    }

    @Override // com.rfid.trans.CReader
    public int SetRegion(byte b, byte b2, byte b3) {
        return this.reader.SetRegion(this.param.ComAddr, b, b2, b3);
    }

    public int SetRegion(int i, int i2, int i3, int i4) {
        return this.reader.SetRegion(this.param.ComAddr, i, i2, i3, i4);
    }

    @Override // com.rfid.trans.CReader
    public int SetGPIO(byte b) {
        return this.reader.SetGPIO(this.param.ComAddr, b);
    }

    @Override // com.rfid.trans.CReader
    public int GetGPIOStatus(byte[] bArr) {
        if (bArr.length < 1) {
            return 255;
        }
        return this.reader.GetGPIOStatus(this.param.ComAddr, bArr);
    }

    @Override // com.rfid.trans.CReader
    public String GetDeviceID() {
        byte[] bArr = new byte[4];
        if (this.reader.GetDeviceID(this.param.ComAddr, bArr) == 0) {
            return this.reader.bytesToHexString(bArr, 0, 4);
        }
        return null;
    }

    @Override // com.rfid.trans.CReader
    public int SetWritePower(byte b) {
        return this.reader.SetWritePower(this.param.ComAddr, b);
    }

    @Override // com.rfid.trans.CReader
    public int GetWritePower(byte[] bArr) {
        if (bArr.length < 1) {
            return 255;
        }
        return this.reader.GetWritePower(this.param.ComAddr, bArr);
    }

    @Override // com.rfid.trans.CReader
    public int SetRetryTimes(byte b) {
        return this.reader.RetryTimes(this.param.ComAddr, new byte[]{(byte) (b | 128)});
    }

    @Override // com.rfid.trans.CReader
    public int GetRetryTimes(byte[] bArr) {
        if (bArr.length < 1) {
            return 255;
        }
        bArr[0] = 0;
        return this.reader.RetryTimes(this.param.ComAddr, bArr);
    }

    @Override // com.rfid.trans.CReader
    public void SetSoundID(int i, SoundPool soundPool) {
        this.soundid = Integer.valueOf(i);
        this.soundPool = soundPool;
    }

    @Override // com.rfid.trans.CReader
    public String GetRFIDTempreture() {
        byte[] bArr = new byte[2];
        if (this.reader.MeasureTemperature(this.param.ComAddr, bArr) != 0) {
            return null;
        }
        return (bArr[0] == 0 ? "-" : BuildConfig.FLAVOR) + ((int) bArr[1]);
    }

    @Override // com.rfid.trans.CReader
    public int SetProfile(byte b) {
        byte[] bArr = {(byte) (b | 128)};
        int SetProfile = this.reader.SetProfile(this.param.ComAddr, bArr);
        if (SetProfile == 0) {
            this.RF_Ctrl = bArr[0];
            this.Cur_Ctrl = bArr[0];
        }
        return SetProfile;
    }

    @Override // com.rfid.trans.CReader
    public int GetProfile(byte[] bArr) {
        if (bArr.length < 1) {
            return 255;
        }
        bArr[0] = 0;
        this.reader.SetProfile(this.param.ComAddr, bArr);
        return 0;
    }

    @Override // com.rfid.trans.CReader
    public void SetMessageBack(RFIDLogCallBack rFIDLogCallBack) {
        this.reader.SetMsgCallBack(rFIDLogCallBack);
    }

    public void playSound() {
        SoundPool soundPool;
        Integer num = this.soundid;
        if (num == null || (soundPool = this.soundPool) == null) {
            return;
        }
        try {
            soundPool.play(num.intValue(), 1.0f, 1.0f, 1, 0, 1.0f);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override // com.rfid.trans.CReader
    public void SetCallBack(TagCallback tagCallback) {
        this.callback = tagCallback;
        this.reader.SetCallBack(tagCallback);
    }

    @Override // com.rfid.trans.CReader
    public void SetInventoryPatameter(ReaderParameter readerParameter) {
        this.param = readerParameter;
    }

    @Override // com.rfid.trans.CReader
    public ReaderParameter GetInventoryPatameter() {
        return this.param;
    }

    @Override // com.rfid.trans.CReader
    public int StartRead() {
        if (this.mThread != null) {
            return -1;
        }
        this.CurSession = (byte) this.param.Session;
        this.PermitControl = false;
        if (this.ModuleType == 2) {
            if (this.param.Session == 254 || this.param.Session == 253 || this.param.Session == 252 || this.param.Session == 251) {
                if (this.Session == 254) {
                    this.Session = 253;
                    this.CurSession = (byte) 2;
                } else if (this.param.Session == 251) {
                    this.Session = 1;
                    this.CurSession = (byte) 1;
                } else {
                    this.Session = 254;
                    this.CurSession = (byte) 3;
                }
                this.PermitControl = true;
            } else {
                int i = this.param.Session;
                this.Session = i;
                this.CurSession = (byte) i;
                if (i == 1) {
                    this.PermitControl = true;
                }
            }
        } else {
            int i2 = this.param.Session;
            this.Session = i2;
            this.CurSession = (byte) i2;
        }
        if (this.CurSession > 0) {
            this.reader.SelectCMDByTime(this.param.ComAddr, Byte.MIN_VALUE, this.CurSession, (byte) 0, (byte) 0, (byte) 0);
            this.reader.SelectCMDByTime(this.param.ComAddr, Byte.MIN_VALUE, this.CurSession, (byte) 0, (byte) 0, (byte) 0);
            this.reader.SelectCMDByTime(this.param.ComAddr, Byte.MIN_VALUE, this.CurSession, (byte) 0, (byte) 0, (byte) 0);
        }
        if (this.ModuleType == 2) {
            if (this.param.Session == 252 || this.param.Session == 254) {
                this.reader.SetRegionTable(this.param.ComAddr, (byte) 1);
            } else {
                this.reader.SetRegionTable(this.param.ComAddr, (byte) 0);
            }
            byte[] bArr = new byte[1];
            if (this.PermitControl) {
                if (this.param.Session == 254) {
                    bArr[0] = -59;
                } else if (this.param.Session == 253) {
                    bArr[0] = -63;
                } else if (this.param.Session == 251 || this.param.Session == 252) {
                    bArr[0] = -13;
                }
                this.firstTime = true;
            } else {
                bArr[0] = (byte) (this.RF_Ctrl | 192);
            }
            if (this.reader.OperateControl(this.param.ComAddr, bArr) == 0) {
                this.Cur_Ctrl = bArr[0];
            }
        }
        this.maskIndex = 0;
        this.mWorking = true;
        this.mThread = new Thread(new Runnable() { // from class: com.rfid.trans.ReaderHelp.2
            @Override // java.lang.Runnable
            public void run() {
                ReaderHelp.this.Target = (byte) 0;
                ReaderHelp readerHelp = ReaderHelp.this;
                readerHelp.QValue = (byte) readerHelp.param.QValue;
                while (ReaderHelp.this.mWorking) {
                    ReaderHelp.this.ReadRfid();
                    if (ReaderHelp.this.PermitControl && ReaderHelp.this.ModuleType == 2) {
                        if (ReaderHelp.this.param.Session != 253 || ReaderHelp.this.Cur_Ctrl != 1) {
                            if (ReaderHelp.this.param.Session != 252 || ReaderHelp.this.Cur_Ctrl != 51) {
                                if (ReaderHelp.this.NoCardCOunt <= 0 || ReaderHelp.this.Cur_Ctrl != 5) {
                                    if (ReaderHelp.this.param.Session == 251 && ReaderHelp.this.Cur_Ctrl == 13) {
                                        byte[] bArr2 = new byte[1];
                                        if (ReaderHelp.this.CardCount >= 50) {
                                            bArr2[0] = -13;
                                        } else if (ReaderHelp.this.CardCount > 10 && ReaderHelp.this.CardCount < 50) {
                                            bArr2[0] = -59;
                                        }
                                        if (ReaderHelp.this.reader.OperateControl(ReaderHelp.this.param.ComAddr, bArr2) == 0) {
                                            ReaderHelp.this.Cur_Ctrl = bArr2[0];
                                        }
                                    }
                                } else {
                                    byte[] bArr3 = {-51};
                                    if (ReaderHelp.this.reader.OperateControl(ReaderHelp.this.param.ComAddr, bArr3) == 0) {
                                        ReaderHelp.this.Cur_Ctrl = bArr3[0];
                                    }
                                }
                            } else if (ReaderHelp.this.CardCount < 150 || ReaderHelp.this.ReadSpeed < 150) {
                                if (!ReaderHelp.this.firstTime) {
                                    byte[] bArr4 = {-59};
                                    if (ReaderHelp.this.reader.OperateControl(ReaderHelp.this.param.ComAddr, bArr4) == 0) {
                                        ReaderHelp.this.Cur_Ctrl = bArr4[0];
                                    }
                                } else {
                                    ReaderHelp.this.firstTime = false;
                                }
                            }
                        } else if (ReaderHelp.this.CardCount < 150 || ReaderHelp.this.ReadSpeed < 150) {
                            if (!ReaderHelp.this.firstTime) {
                                byte[] bArr5 = {-59};
                                if (ReaderHelp.this.reader.OperateControl(ReaderHelp.this.param.ComAddr, bArr5) == 0) {
                                    ReaderHelp.this.Cur_Ctrl = bArr5[0];
                                }
                            } else {
                                ReaderHelp.this.firstTime = false;
                            }
                        }
                    }
                    if (ReaderHelp.this.ModuleType != 2) {
                        SystemClock.sleep(ReaderHelp.this.param.Interval * 10);
                    }
                }
                ReaderHelp.isSound = false;
                if (ReaderHelp.this.CurSession > 1) {
                    ReaderHelp.this.SelectBySession((byte) 2);
                    SystemClock.sleep(5L);
                    ReaderHelp.this.SelectBySession((byte) 3);
                } else if (ReaderHelp.this.CurSession == 1 && ReaderHelp.this.PermitControl) {
                    ReaderHelp.this.SelectBySession((byte) 1);
                }
                if (ReaderHelp.this.ModuleType == 2 && ReaderHelp.this.PermitControl) {
                    byte[] bArr6 = {(byte) (ReaderHelp.this.RF_Ctrl | 192)};
                    if (ReaderHelp.this.reader.OperateControl(ReaderHelp.this.param.ComAddr, bArr6) == 0) {
                        ReaderHelp.this.Cur_Ctrl = bArr6[0];
                    }
                }
                ReaderHelp.this.mThread = null;
                if (ReaderHelp.this.callback != null) {
                    ReaderHelp.this.callback.StopReadCallBack();
                }
            }
        });
        this.mThread.start();
        return 0;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void SelectBySession(byte b) {
        for (int i = 0; i < 8; i++) {
            this.reader.SelectCMDByTime(this.param.ComAddr, Byte.MIN_VALUE, b, (byte) 0, (byte) 0, (byte) 0);
            SystemClock.sleep(5L);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void ReadRfid() {
        int[] iArr;
        int i;
        int i2;
        int i3;
        int i4 = this.Session;
        if (i4 == 0 || i4 == 1) {
            this.Target = (byte) 0;
        }
        int[] iArr2 = {0};
        if (this.param.IvtType == 0) {
            iArr2[0] = 0;
            byte b = this.Session == 255 ? (byte) 0 : (byte) this.param.ScanTime;
            long currentTimeMillis = System.currentTimeMillis();
            this.CardCount = 0;
            this.ReadSpeed = 0;
            if (this.MaskList.size() > 0) {
                MaskClass maskClass = this.MaskList.get(this.maskIndex);
                this.maskIndex++;
                this.maskIndex %= this.MaskList.size();
                this.param.MaskMem = maskClass.MaskMem;
                System.arraycopy(maskClass.MaskAdr, 0, this.param.MaskAdr, 0, 2);
                this.param.MaskLen = maskClass.MaskLen;
                System.arraycopy(maskClass.MaskData, 0, this.param.MaskData, 0, ((maskClass.MaskLen & 255) + 7) / 8);
            }
            this.reader.Inventory_G2(this.param.ComAddr, this.QValue, (byte) this.Session, (byte) this.param.WordPtr, (byte) 0, this.Target, Byte.MIN_VALUE, b, this.param.MaskMem, this.param.MaskAdr, this.param.MaskLen, this.param.MaskData, null, iArr2, false);
            long currentTimeMillis2 = System.currentTimeMillis() - currentTimeMillis;
            this.CardCount = iArr2[0];
            if (currentTimeMillis2 > 0) {
                this.ReadSpeed = (int) ((r2 * 1000) / currentTimeMillis2);
            }
            iArr = iArr2;
            i2 = 255;
            i3 = 1;
            i = 2;
        } else {
            if (this.param.IvtType == 1) {
                this.QValue = (byte) this.param.QValue;
                byte[] bArr = {(byte) (this.param.WordPtr >> 8), (byte) (this.param.WordPtr & 255)};
                if (this.param.Length == 0) {
                    this.param.Length = 6;
                }
                byte b2 = (byte) this.param.Length;
                byte[] hexStringToBytes = this.reader.hexStringToBytes(this.param.Password);
                if (this.param.Session == 255) {
                    this.Session = 0;
                }
                if (this.MaskList.size() > 0) {
                    MaskClass maskClass2 = this.MaskList.get(this.maskIndex);
                    this.maskIndex++;
                    this.maskIndex %= this.MaskList.size();
                    this.param.MaskMem = maskClass2.MaskMem;
                    System.arraycopy(maskClass2.MaskAdr, 0, this.param.MaskAdr, 0, 2);
                    this.param.MaskLen = maskClass2.MaskLen;
                    System.arraycopy(maskClass2.MaskData, 0, this.param.MaskData, 0, ((maskClass2.MaskLen & 255) + 7) / 8);
                }
                if (this.ModuleType == 1) {
                    ArrayList arrayList = new ArrayList();
                    iArr2[0] = 0;
                    this.reader.Inventory_NoCallback(this.param.ComAddr, this.QValue, (byte) this.Session, (byte) 0, (byte) 0, this.Target, Byte.MIN_VALUE, this.Session == 255 ? (byte) 0 : (byte) this.param.ScanTime, this.param.MaskMem, this.param.MaskAdr, this.param.MaskLen, this.param.MaskData, arrayList, iArr2, false);
                    isSound = false;
                    if (iArr2[0] > 0) {
                        int i5 = 0;
                        while (i5 < arrayList.size()) {
                            ReadTag readTag = (ReadTag) arrayList.get(i5);
                            ArrayList arrayList2 = arrayList;
                            byte b3 = b2;
                            int[] iArr3 = iArr2;
                            String ReadData_G2 = ReadData_G2(readTag.epcId, (byte) this.param.Memory, (byte) this.param.WordPtr, b3, this.param.Password);
                            if (ReadData_G2 != null && ReadData_G2.length() > 0) {
                                readTag.memId = ReadData_G2;
                                TagCallback tagCallback = this.callback;
                                if (tagCallback != null) {
                                    tagCallback.tagCallback(readTag);
                                }
                                playSound();
                            }
                            i5++;
                            arrayList = arrayList2;
                            b2 = b3;
                            iArr2 = iArr3;
                        }
                    }
                    iArr = iArr2;
                    i = 2;
                } else {
                    iArr = iArr2;
                    i = 2;
                    this.reader.Inventory_Mix(this.param.ComAddr, this.QValue, (byte) this.Session, this.param.MaskMem, this.param.MaskAdr, this.param.MaskLen, this.param.MaskData, (byte) this.param.Memory, bArr, b2, hexStringToBytes, this.Target, Byte.MIN_VALUE, (byte) this.param.ScanTime, null, iArr);
                }
            } else {
                iArr = iArr2;
                i = 2;
                if (this.param.IvtType == 2) {
                    this.QValue = (byte) this.param.QValue;
                    if (this.param.Length == 0) {
                        this.param.Length = 6;
                    }
                    byte b4 = (byte) this.param.Length;
                    if (this.param.Session == 255) {
                        this.Session = 0;
                    }
                    if (this.MaskList.size() > 0) {
                        MaskClass maskClass3 = this.MaskList.get(this.maskIndex);
                        this.maskIndex++;
                        this.maskIndex %= this.MaskList.size();
                        this.param.MaskMem = maskClass3.MaskMem;
                        System.arraycopy(maskClass3.MaskAdr, 0, this.param.MaskAdr, 0, 2);
                        this.param.MaskLen = maskClass3.MaskLen;
                        System.arraycopy(maskClass3.MaskData, 0, this.param.MaskData, 0, ((maskClass3.MaskLen & 255) + 7) / 8);
                    }
                    if (this.ModuleType == 1) {
                        ArrayList arrayList3 = new ArrayList();
                        iArr[0] = 0;
                        this.reader.Inventory_NoCallback(this.param.ComAddr, this.QValue, (byte) this.Session, (byte) 0, (byte) 0, this.Target, Byte.MIN_VALUE, this.Session == 255 ? (byte) 0 : (byte) this.param.ScanTime, this.param.MaskMem, this.param.MaskAdr, this.param.MaskLen, this.param.MaskData, arrayList3, iArr, false);
                        isSound = false;
                        if (iArr[0] > 0) {
                            int i6 = 0;
                            while (i6 < arrayList3.size()) {
                                ReadTag readTag2 = (ReadTag) arrayList3.get(i6);
                                ArrayList arrayList4 = arrayList3;
                                byte b5 = b4;
                                String ReadData_G22 = ReadData_G2(readTag2.epcId, (byte) this.param.Memory, (byte) this.param.WordPtr, b4, this.param.Password);
                                if (ReadData_G22 != null && ReadData_G22.length() > 0) {
                                    readTag2.memId = ReadData_G22;
                                    TagCallback tagCallback2 = this.callback;
                                    if (tagCallback2 != null) {
                                        tagCallback2.tagCallback(readTag2);
                                    }
                                    playSound();
                                }
                                i6++;
                                arrayList3 = arrayList4;
                                b4 = b5;
                            }
                        }
                    } else {
                        iArr[0] = 0;
                        this.reader.Inventory_G2(this.param.ComAddr, (byte) (this.QValue | 32), (byte) this.Session, (byte) this.param.WordPtr, (byte) 0, this.Target, Byte.MIN_VALUE, (byte) 10, this.param.MaskMem, this.param.MaskAdr, this.param.MaskLen, this.param.MaskData, null, iArr, false);
                        if (this.Session == 0 && iArr[0] < 5) {
                            this.QValue = (byte) 2;
                        } else {
                            this.QValue = (byte) this.param.QValue;
                        }
                    }
                } else if (this.param.IvtType == 3) {
                    this.reader.Inventory_GB(this.param.ComAddr, Byte.MIN_VALUE, (byte) 10, null, iArr);
                } else if (this.param.IvtType == 4) {
                    this.reader.Inventory_GJB(this.param.ComAddr, (byte) 0, Byte.MIN_VALUE, (byte) 10, null, iArr);
                } else if (this.param.IvtType == 5) {
                    byte[] bArr2 = new byte[256];
                    iArr[0] = 0;
                    this.reader.InventoryMutiple_6B(this.param.ComAddr, (byte) 1, (byte) 0, (byte) -1, new byte[]{0, 0, 0, 0, 0, 0, 0, 0}, bArr2, iArr);
                    if (iArr[0] > 0) {
                        for (int i7 = 0; i7 < iArr[0]; i7++) {
                            int i8 = i7 * 10;
                            String bytesToHexString = this.reader.bytesToHexString(bArr2, i8 + 1, 8);
                            byte b6 = bArr2[i8 + 9];
                            ReadTag readTag3 = new ReadTag();
                            readTag3.epcId = bytesToHexString;
                            readTag3.memId = BuildConfig.FLAVOR;
                            readTag3.rssi = b6 & 255;
                            readTag3.phase = 0;
                            readTag3.antId = 1;
                        }
                        i2 = 255;
                        i3 = 1;
                        isSound = true;
                    }
                }
            }
            i2 = 255;
            i3 = 1;
        }
        if (iArr[0] == 0) {
            if (this.param.Session > i3 && this.param.Session < i2 && this.param.IvtType < i) {
                this.NoCardCOunt += i3;
                if (this.NoCardCOunt > i3) {
                    isSound = false;
                    this.Target = (byte) (1 - this.Target);
                    this.NoCardCOunt = 0;
                    if (this.PermitControl) {
                        byte[] bArr3 = new byte[i3];
                        if (this.param.Session == 254) {
                            bArr3[0] = -59;
                        } else if (this.param.Session == 253) {
                            bArr3[0] = -63;
                        } else if (this.param.Session == 252) {
                            bArr3[0] = -13;
                        }
                        if (this.reader.OperateControl(this.param.ComAddr, bArr3) == 0) {
                            this.Cur_Ctrl = bArr3[0];
                        }
                        this.firstTime = true;
                        return;
                    }
                    return;
                }
                return;
            }
            this.NoCardCOunt++;
            if (this.NoCardCOunt > i) {
                isSound = false;
                return;
            }
            return;
        }
        this.NoCardCOunt = 0;
    }

    @Override // com.rfid.trans.CReader
    public void StopRead() {
        if (this.ModuleType == 2) {
            this.reader.StopInventory(this.param.ComAddr);
        }
        this.mWorking = false;
        isSound = false;
    }

    /* JADX WARN: Removed duplicated region for block: B:10:0x029c  */
    /* JADX WARN: Removed duplicated region for block: B:13:? A[RETURN, SYNTHETIC] */
    @Override // com.rfid.trans.CReader
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public void ScanRfid() {
        /*
            Method dump skipped, instructions count: 680
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.rfid.trans.ReaderHelp.ScanRfid():void");
    }

    @Override // com.rfid.trans.CReader
    public int InventoryOnce(byte b, byte b2, byte b3, byte b4, byte b5, byte b6, byte b7, List<ReadTag> list) {
        List<ReadTag> list2;
        if (list == null) {
            list2 = new ArrayList();
        } else {
            list.clear();
            list2 = list;
        }
        int[] iArr = {0};
        int Inventory_G2 = this.reader.Inventory_G2(this.param.ComAddr, b2, b, b3, b4, b6, b5, b7, this.param.MaskMem, this.param.MaskAdr, this.param.MaskLen, this.param.MaskData, list2, iArr, false);
        isSound = false;
        if (iArr[0] > 0) {
            playSound();
        }
        return Inventory_G2;
    }

    @Override // com.rfid.trans.CReader
    public int SetAddress(byte b) {
        int SetAddress = this.reader.SetAddress(this.param.ComAddr, b);
        if (SetAddress == 0) {
            this.param.ComAddr = b;
        }
        return SetAddress;
    }

    @Override // com.rfid.trans.CReader
    public int SetBaudRate(int i) {
        byte b = 5;
        if (i == 9600) {
            b = 0;
        } else if (i == 19200) {
            b = 1;
        } else if (i == 38400) {
            b = 3;
        } else if (i != 57600 && i == 115200) {
            b = 6;
        }
        int SetBaudRate = this.reader.SetBaudRate(this.param.ComAddr, b);
        if (SetBaudRate == 0) {
            this.reader.DisConnect();
            this.reader.Connect(this.devName, i, this.logswitch);
        }
        return SetBaudRate;
    }

    @Override // com.rfid.trans.CReader
    public int ReadData_G2(byte b, byte[] bArr, byte b2, int i, byte b3, byte[] bArr2, byte[] bArr3, byte[] bArr4) {
        int i2 = b & 255;
        if (i2 > 15 && i2 < 255) {
            return 255;
        }
        if (((i2 == 255 || bArr != null) && i2 < 16 && bArr.length < i2 * 2) || bArr3 == null || bArr3.length < (b3 & 255) * 2 || bArr2 == null || bArr2.length < 4 || bArr4 == null || bArr4.length < 1) {
            return 255;
        }
        int i3 = 48;
        int i4 = 0;
        while (i4 < 10) {
            int i5 = i4;
            i3 = this.reader.ReadData_G2(this.param.ComAddr, b, bArr, b2, (byte) i, b3, bArr2, this.param.MaskMem, this.param.MaskAdr, this.param.MaskLen, this.param.MaskData, bArr3, bArr4);
            if (i3 == 0) {
                break;
            }
            i4 = i5 + 1;
        }
        return i3;
    }

    public String ReadDataByTID(String str, byte b, byte b2, byte b3, byte[] bArr) {
        byte[] bArr2;
        int i;
        int i2;
        if (str.length() == 0 || str.length() % 4 != 0) {
            return null;
        }
        byte b4 = -1;
        byte[] bArr3 = new byte[12];
        byte[] hexStringToBytes = this.reader.hexStringToBytes(str);
        byte[] bArr4 = {0, 0};
        byte length = (byte) (str.length() * 4);
        byte[] bArr5 = new byte[str.length()];
        System.arraycopy(hexStringToBytes, 0, bArr5, 0, hexStringToBytes.length);
        int i3 = (b3 & 255) * 2;
        byte[] bArr6 = new byte[i3];
        byte[] bArr7 = new byte[1];
        int i4 = 0;
        int i5 = 0;
        while (true) {
            if (i5 >= 10) {
                bArr2 = bArr6;
                i = i3;
                i2 = 0;
                break;
            }
            int i6 = i5;
            byte[] bArr8 = bArr7;
            bArr2 = bArr6;
            i = i3;
            i2 = 0;
            byte[] bArr9 = bArr5;
            byte b5 = length;
            byte[] bArr10 = bArr4;
            byte[] bArr11 = bArr3;
            i4 = this.reader.ReadData_G2(this.param.ComAddr, b4, bArr3, b, b2, b3, bArr, (byte) 2, bArr4, length, bArr9, bArr2, bArr8);
            if (i4 == 0) {
                break;
            }
            i5 = i6 + 1;
            bArr6 = bArr2;
            i3 = i;
            bArr7 = bArr8;
            bArr5 = bArr9;
            length = b5;
            bArr4 = bArr10;
            bArr3 = bArr11;
            b4 = -1;
        }
        if (i4 == 0) {
            return this.reader.bytesToHexString(bArr2, i2, i);
        }
        return null;
    }

    @Override // com.rfid.trans.CReader
    public int WriteData_G2(byte b, byte b2, byte[] bArr, byte b3, int i, byte[] bArr2, byte[] bArr3, byte[] bArr4) {
        int i2 = b2 & 255;
        if (i2 > 15 && i2 < 255) {
            return 255;
        }
        if (((i2 == 255 || bArr != null) && i2 < 16 && bArr.length < i2 * 2) || bArr2 == null || bArr2.length != (b & 255) * 2 || bArr3 == null || bArr3.length < 4 || bArr4 == null || bArr4.length < 1) {
            return 255;
        }
        int i3 = 48;
        int i4 = 0;
        while (i4 < 10) {
            int i5 = i4;
            i3 = this.reader.WriteData_G2(this.param.ComAddr, b, b2, bArr, b3, (byte) i, bArr2, bArr3, this.param.MaskMem, this.param.MaskAdr, this.param.MaskLen, this.param.MaskData, bArr4);
            if (i3 == 0) {
                break;
            }
            i4 = i5 + 1;
        }
        return i3;
    }

    public int WriteDataByTID(String str, byte b, byte b2, byte[] bArr, String str2) {
        if (str.length() == 0 || str.length() % 4 != 0 || str2.length() == 0 || str2.length() % 4 != 0) {
            return 255;
        }
        byte length = (byte) (str2.length() / 4);
        byte[] bArr2 = new byte[12];
        byte[] hexStringToBytes = this.reader.hexStringToBytes(str2);
        byte[] hexStringToBytes2 = this.reader.hexStringToBytes(str);
        byte[] bArr3 = {0, 0};
        byte length2 = (byte) (str.length() * 4);
        byte[] bArr4 = new byte[str.length()];
        System.arraycopy(hexStringToBytes2, 0, bArr4, 0, hexStringToBytes2.length);
        byte[] bArr5 = new byte[1];
        int i = 48;
        int i2 = 0;
        while (i2 < 10) {
            int i3 = i2;
            byte[] bArr6 = bArr5;
            byte[] bArr7 = bArr4;
            byte b3 = length2;
            byte[] bArr8 = bArr3;
            byte[] bArr9 = bArr2;
            i = this.reader.WriteData_G2(this.param.ComAddr, length, (byte) -1, bArr2, b, b2, hexStringToBytes, bArr, (byte) 2, bArr3, b3, bArr7, bArr6);
            if (i == 0) {
                break;
            }
            i2 = i3 + 1;
            bArr5 = bArr6;
            bArr4 = bArr7;
            length2 = b3;
            bArr3 = bArr8;
            bArr2 = bArr9;
        }
        return i;
    }

    @Override // com.rfid.trans.CReader
    public int WriteEPC_G2(byte b, byte[] bArr, byte[] bArr2, byte[] bArr3) {
        int i = b & 255;
        if (i > 31 || bArr == null || bArr.length != i * 2 || bArr2 == null || bArr2.length < 4 || bArr3 == null || bArr3.length < 1) {
            return 255;
        }
        int i2 = 48;
        for (int i3 = 0; i3 < 10 && (i2 = this.reader.WriteEPC_G2(this.param.ComAddr, b, bArr2, bArr, bArr3)) != 0; i3++) {
        }
        return i2;
    }

    @Override // com.rfid.trans.CReader
    public int Lock_G2(byte b, byte[] bArr, byte b2, byte b3, byte[] bArr2, byte[] bArr3) {
        int i = b & 255;
        if (i > 15) {
            return 255;
        }
        if ((bArr != null && bArr.length < i * 2) || bArr2 == null || bArr2.length < 4 || bArr3 == null || bArr3.length < 1) {
            return 255;
        }
        int i2 = 48;
        for (int i3 = 0; i3 < 10 && (i2 = this.reader.Lock_G2(this.param.ComAddr, b, bArr, b2, b3, bArr2, bArr3)) != 0; i3++) {
        }
        return i2;
    }

    public int LockbyTID(byte b, byte[] bArr, byte b2, byte b3, byte[] bArr2, byte[] bArr3) {
        int i;
        if (bArr == null || bArr.length < (i = b * 2) || bArr2 == null || bArr2.length < 4 || bArr3 == null || bArr3.length < 1) {
            return 255;
        }
        byte[] bArr4 = {0, 0};
        byte b4 = (byte) ((b & 255) * 16);
        byte[] bArr5 = new byte[100];
        System.arraycopy(bArr, 0, bArr5, 0, i);
        int i2 = 48;
        int i3 = 0;
        while (i3 < 10) {
            int i4 = i3;
            byte[] bArr6 = bArr5;
            byte b5 = b4;
            byte[] bArr7 = bArr4;
            i2 = this.reader.Lock_G2(this.param.ComAddr, (byte) 255, null, b2, b3, bArr2, (byte) 2, bArr4, b4, bArr6, bArr3);
            if (i2 == 0) {
                break;
            }
            i3 = i4 + 1;
            bArr5 = bArr6;
            b4 = b5;
            bArr4 = bArr7;
        }
        return i2;
    }

    @Override // com.rfid.trans.CReader
    public int Kill_G2(byte b, byte[] bArr, byte[] bArr2, byte[] bArr3) {
        int i = b & 255;
        if (i > 15) {
            return 255;
        }
        if ((bArr != null && bArr.length < i * 2) || bArr2 == null || bArr2.length < 4 || bArr3 == null || bArr3.length < 1) {
            return 255;
        }
        int i2 = 48;
        for (int i3 = 0; i3 < 10 && (i2 = this.reader.Kill_G2(this.param.ComAddr, b, bArr, bArr2, bArr3)) != 0; i3++) {
        }
        return i2;
    }

    public int KillbyTID(byte b, byte[] bArr, byte[] bArr2, byte[] bArr3) {
        int i;
        if (bArr == null || bArr.length < (i = b * 2) || bArr2 == null || bArr2.length < 4 || bArr3 == null || bArr3.length < 1) {
            return 255;
        }
        byte[] bArr4 = {0, 0};
        byte b2 = (byte) ((b & 255) * 16);
        byte[] bArr5 = new byte[100];
        System.arraycopy(bArr, 0, bArr5, 0, i);
        int i2 = 48;
        int i3 = 0;
        while (i3 < 10) {
            int i4 = i3;
            byte[] bArr6 = bArr5;
            byte b3 = b2;
            i2 = this.reader.Kill_G2(this.param.ComAddr, (byte) 255, null, bArr2, (byte) 2, bArr4, b2, bArr5, bArr3);
            if (i2 == 0) {
                break;
            }
            i3 = i4 + 1;
            bArr5 = bArr6;
            b2 = b3;
        }
        return i2;
    }

    @Override // com.rfid.trans.CReader
    public int BlockWrite_G2(byte b, byte b2, byte[] bArr, byte b3, byte b4, byte[] bArr2, byte[] bArr3, byte[] bArr4) {
        byte b5;
        int i = b2 & 255;
        if (i > 15) {
            return 255;
        }
        if (bArr == null) {
            b5 = 0;
        } else {
            if (bArr.length < i * 2) {
                return 255;
            }
            b5 = b2;
        }
        if (bArr2 == null || bArr2.length != (b & 255) * 2 || bArr3 == null || bArr3.length < 4 || bArr4 == null || bArr4.length < 1) {
            return 255;
        }
        int i2 = 48;
        int i3 = 0;
        while (i3 < 10) {
            int i4 = i3;
            i2 = this.reader.BlockWrite_G2(this.param.ComAddr, b, b5, bArr, b3, b4, bArr2, bArr3, this.param.MaskMem, this.param.MaskAdr, this.param.MaskLen, this.param.MaskData, bArr4);
            if (i2 == 0) {
                break;
            }
            i3 = i4 + 1;
        }
        return i2;
    }

    @Override // com.rfid.trans.CReader
    public String ReadData_G2(String str, byte b, int i, byte b2, String str2) {
        byte[] bArr;
        byte b3;
        byte[] bArr2;
        int i2;
        if (str2 == null || str2.length() != 8) {
            return null;
        }
        byte[] hexStringToBytes = this.reader.hexStringToBytes(str2);
        if (str == null || str.length() <= 0) {
            bArr = null;
            b3 = 0;
        } else {
            if (str.length() % 4 != 0) {
                return null;
            }
            bArr = this.reader.hexStringToBytes(str);
            b3 = (byte) (bArr.length / 2);
        }
        int i3 = b2 * 2;
        byte[] bArr3 = new byte[i3];
        byte[] bArr4 = new byte[1];
        int i4 = 48;
        int i5 = 0;
        while (true) {
            if (i5 >= 10) {
                bArr2 = bArr3;
                i2 = i3;
                break;
            }
            int i6 = i5;
            byte[] bArr5 = bArr4;
            bArr2 = bArr3;
            i2 = i3;
            i4 = this.reader.ReadData_G2(this.param.ComAddr, b3, bArr, b, (byte) i, b2, hexStringToBytes, this.param.MaskMem, this.param.MaskAdr, this.param.MaskLen, this.param.MaskData, bArr2, bArr5);
            if (i4 == 0) {
                break;
            }
            i5 = i6 + 1;
            bArr3 = bArr2;
            bArr4 = bArr5;
            i3 = i2;
        }
        if (i4 != 0) {
            return null;
        }
        return this.reader.bytesToHexString(bArr2, 0, i2);
    }

    @Override // com.rfid.trans.CReader
    public int WriteData_G2(String str, String str2, byte b, int i, String str3) {
        byte[] bArr;
        byte b2;
        if (str3 != null && str3.length() == 8) {
            byte[] hexStringToBytes = this.reader.hexStringToBytes(str3);
            int i2 = 0;
            if (str2 == null || str2.length() <= 0) {
                bArr = null;
                b2 = 0;
            } else {
                if (str2.length() % 4 != 0) {
                    return 255;
                }
                bArr = this.reader.hexStringToBytes(str2);
                b2 = (byte) (bArr.length / 2);
            }
            if (str == null || str.length() <= 0 || str.length() % 4 != 0) {
                return 255;
            }
            byte[] hexStringToBytes2 = this.reader.hexStringToBytes(str);
            byte[] bArr2 = new byte[1];
            byte length = (byte) (hexStringToBytes2.length / 2);
            int i3 = 48;
            while (i2 < 10) {
                byte b3 = length;
                byte b4 = b2;
                byte[] bArr3 = bArr;
                byte b5 = length;
                i3 = this.reader.WriteData_G2(this.param.ComAddr, b3, b4, bArr3, b, (byte) i, hexStringToBytes2, hexStringToBytes, this.param.MaskMem, this.param.MaskAdr, this.param.MaskLen, this.param.MaskData, bArr2);
                if (i3 == 0) {
                    break;
                }
                i2++;
                length = b5;
            }
            return i3;
        }
        return 255;
    }

    @Override // com.rfid.trans.CReader
    public int WriteEPC_G2(String str, String str2) {
        if (str2 != null && str2.length() == 8) {
            byte[] hexStringToBytes = this.reader.hexStringToBytes(str2);
            if (str == null || str.length() <= 0 || str.length() % 4 != 0) {
                return 255;
            }
            byte[] hexStringToBytes2 = this.reader.hexStringToBytes(str);
            byte length = (byte) (hexStringToBytes2.length / 2);
            byte[] bArr = new byte[1];
            int i = 48;
            for (int i2 = 0; i2 < 10 && (i = this.reader.WriteEPC_G2(this.param.ComAddr, length, hexStringToBytes, hexStringToBytes2, bArr)) != 0; i2++) {
            }
            return i;
        }
        return 255;
    }

    @Override // com.rfid.trans.CReader
    public int SetDRM(byte b) {
        return this.reader.ConfigDRM(this.param.ComAddr, new byte[]{(byte) (b | 128)});
    }

    @Override // com.rfid.trans.CReader
    public int GetDRM(byte[] bArr) {
        if (bArr.length < 1) {
            return 255;
        }
        bArr[0] = 0;
        return this.reader.ConfigDRM(this.param.ComAddr, bArr);
    }

    @Override // com.rfid.trans.CReader
    public int BlockErase_G2(byte b, byte[] bArr, byte b2, byte b3, byte b4, byte[] bArr2, byte[] bArr3) {
        int i = b & 255;
        if (i > 15) {
            return 255;
        }
        if ((bArr != null && bArr.length < i * 2) || bArr2.length < 4 || bArr3.length < 1) {
            return 255;
        }
        int i2 = 48;
        for (int i3 = 0; i3 < 10 && (i2 = this.reader.BlockErase_G2(this.param.ComAddr, b, bArr, b2, b3, b4, bArr2, bArr3)) != 0; i3++) {
        }
        return i2;
    }

    @Override // com.rfid.trans.CReader
    public int FST_TranImage(byte b, byte[] bArr, byte[] bArr2) {
        int i = b & 255;
        if (bArr == null || bArr.length < 2 || bArr2 == null || bArr2.length < i) {
            return 255;
        }
        return this.reader.FST_TranImage(this.param.ComAddr, b, bArr, bArr2);
    }

    @Override // com.rfid.trans.CReader
    public int FST_ShowImage(byte b, byte[] bArr) {
        int i = b & 255;
        if (i > 15) {
            return 255;
        }
        if (bArr != null && bArr.length < i * 2) {
            return 255;
        }
        return this.reader.FST_ShowImage(this.param.ComAddr, b, bArr);
    }

    @Override // com.rfid.trans.CReader
    public int LedOn_kx2005x(String str, String str2, byte b) {
        byte[] bArr;
        byte b2;
        if (str2 == null || str2.length() != 8) {
            return 255;
        }
        byte[] hexStringToBytes = this.reader.hexStringToBytes(str2);
        if (str == null || str.length() <= 0) {
            bArr = null;
            b2 = 0;
        } else {
            if (str.length() % 4 != 0) {
                return 255;
            }
            byte[] hexStringToBytes2 = this.reader.hexStringToBytes(str);
            bArr = hexStringToBytes2;
            b2 = (byte) (hexStringToBytes2.length / 2);
        }
        return this.reader.LedOn_kx2005x(this.param.ComAddr, b2, bArr, b, hexStringToBytes, this.param.MaskMem, this.param.MaskAdr, this.param.MaskLen, this.param.MaskData);
    }

    @Override // com.rfid.trans.CReader
    public int Fd_InitRegfile(String str, String str2) {
        byte[] bArr;
        byte b;
        byte[] bArr2 = new byte[1];
        if (str2 == null || str2.length() != 8) {
            return 255;
        }
        byte[] hexStringToBytes = this.reader.hexStringToBytes(str2);
        if (str == null || str.length() <= 0) {
            bArr = null;
            b = 0;
        } else {
            if (str.length() % 4 != 0) {
                return 255;
            }
            byte[] hexStringToBytes2 = this.reader.hexStringToBytes(str);
            bArr = hexStringToBytes2;
            b = (byte) (hexStringToBytes2.length / 2);
        }
        return this.reader.Fd_InitRegfile(this.param.ComAddr, b, bArr, hexStringToBytes, this.param.MaskMem, this.param.MaskAdr, this.param.MaskLen, this.param.MaskData, bArr2);
    }

    @Override // com.rfid.trans.CReader
    public int Fd_ReadReg(String str, int i, String str2, byte[] bArr) {
        byte[] bArr2;
        byte b;
        byte[] bArr3 = new byte[1];
        if (str2 == null || str2.length() != 8 || bArr == null || bArr.length < 2) {
            return 255;
        }
        byte[] hexStringToBytes = this.reader.hexStringToBytes(str2);
        if (str == null || str.length() <= 0) {
            bArr2 = null;
            b = 0;
        } else {
            if (str.length() % 4 != 0) {
                return 255;
            }
            byte[] hexStringToBytes2 = this.reader.hexStringToBytes(str);
            b = (byte) (hexStringToBytes2.length / 2);
            bArr2 = hexStringToBytes2;
        }
        return this.reader.Fd_ReadReg(this.param.ComAddr, b, bArr2, new byte[]{(byte) (i >> 8), (byte) (i & 255)}, hexStringToBytes, this.param.MaskMem, this.param.MaskAdr, this.param.MaskLen, this.param.MaskData, bArr, bArr3);
    }

    @Override // com.rfid.trans.CReader
    public int Fd_WriteReg(String str, int i, byte[] bArr, String str2) {
        byte[] bArr2;
        byte b;
        byte[] bArr3 = new byte[1];
        if (str2 == null || str2.length() != 8 || bArr == null || bArr.length != 2) {
            return 255;
        }
        byte[] hexStringToBytes = this.reader.hexStringToBytes(str2);
        if (str == null || str.length() <= 0) {
            bArr2 = null;
            b = 0;
        } else {
            if (str.length() % 4 != 0) {
                return 255;
            }
            byte[] hexStringToBytes2 = this.reader.hexStringToBytes(str);
            b = (byte) (hexStringToBytes2.length / 2);
            bArr2 = hexStringToBytes2;
        }
        return this.reader.Fd_WriteReg(this.param.ComAddr, b, bArr2, new byte[]{(byte) (i >> 8), (byte) (i & 255)}, bArr, hexStringToBytes, this.param.MaskMem, this.param.MaskAdr, this.param.MaskLen, this.param.MaskData, bArr3);
    }

    @Override // com.rfid.trans.CReader
    public int Fd_ReadMemory(String str, int i, byte b, String str2, byte b2, String str3, byte[] bArr) {
        byte[] bArr2;
        byte b3;
        byte[] bArr3 = new byte[1];
        if (str2 == null || str2.length() != 8 || str3 == null || str3.length() != 8 || bArr == null || bArr.length < (b & 255)) {
            return 255;
        }
        byte[] hexStringToBytes = this.reader.hexStringToBytes(str2);
        byte[] hexStringToBytes2 = this.reader.hexStringToBytes(str3);
        if (str == null || str.length() <= 0) {
            bArr2 = null;
            b3 = 0;
        } else {
            if (str.length() % 4 != 0) {
                return 255;
            }
            byte[] hexStringToBytes3 = this.reader.hexStringToBytes(str);
            b3 = (byte) (hexStringToBytes3.length / 2);
            bArr2 = hexStringToBytes3;
        }
        return this.reader.Fd_ReadMemory(this.param.ComAddr, b3, bArr2, new byte[]{(byte) (i >> 8), (byte) (i & 255)}, b, hexStringToBytes, b2, hexStringToBytes2, this.param.MaskMem, this.param.MaskAdr, this.param.MaskLen, this.param.MaskData, bArr, bArr3);
    }

    @Override // com.rfid.trans.CReader
    public int Fd_WriteMemory(String str, int i, byte[] bArr, String str2, byte b, String str3) {
        byte[] bArr2;
        byte b2;
        byte[] bArr3 = new byte[1];
        if (str2 == null || str2.length() != 8 || str3 == null || str3.length() != 8 || bArr == null || bArr.length % 4 != 0) {
            return 255;
        }
        byte[] hexStringToBytes = this.reader.hexStringToBytes(str2);
        byte[] hexStringToBytes2 = this.reader.hexStringToBytes(str3);
        if (str == null || str.length() <= 0) {
            bArr2 = null;
            b2 = 0;
        } else {
            if (str.length() % 4 != 0) {
                return 255;
            }
            byte[] hexStringToBytes3 = this.reader.hexStringToBytes(str);
            b2 = (byte) (hexStringToBytes3.length / 2);
            bArr2 = hexStringToBytes3;
        }
        return this.reader.Fd_WriteMemory(this.param.ComAddr, b2, bArr2, new byte[]{(byte) (i >> 8), (byte) (i & 255)}, bArr.length, bArr, hexStringToBytes, b, hexStringToBytes2, this.param.MaskMem, this.param.MaskAdr, this.param.MaskLen, this.param.MaskData, bArr3);
    }

    @Override // com.rfid.trans.CReader
    public int Fd_GetTemperature(String str, byte b, byte b2, byte b3, byte b4, byte b5, String str2, byte[] bArr) {
        byte[] bArr2;
        byte b6;
        byte[] bArr3 = new byte[1];
        if (str2 == null || str2.length() != 8 || bArr == null || bArr.length < 2) {
            return 255;
        }
        byte[] hexStringToBytes = this.reader.hexStringToBytes(str2);
        if (str == null || str.length() <= 0) {
            bArr2 = null;
            b6 = 0;
        } else {
            if (str.length() % 4 != 0) {
                return 255;
            }
            byte[] hexStringToBytes2 = this.reader.hexStringToBytes(str);
            bArr2 = hexStringToBytes2;
            b6 = (byte) (hexStringToBytes2.length / 2);
        }
        return this.reader.Fd_GetTemperature(this.param.ComAddr, b6, bArr2, b, b2, b3, b4, b5, hexStringToBytes, this.param.MaskMem, this.param.MaskAdr, this.param.MaskLen, this.param.MaskData, bArr, bArr3);
    }

    @Override // com.rfid.trans.CReader
    public int Fd_StartLogging(String str, int i, int i2, String str2) {
        byte[] bArr;
        byte b;
        byte[] bArr2 = new byte[1];
        if (str2 == null || str2.length() != 8) {
            return 255;
        }
        byte[] hexStringToBytes = this.reader.hexStringToBytes(str2);
        if (str == null || str.length() <= 0) {
            bArr = null;
            b = 0;
        } else {
            if (str.length() % 4 != 0) {
                return 255;
            }
            byte[] hexStringToBytes2 = this.reader.hexStringToBytes(str);
            b = (byte) (hexStringToBytes2.length / 2);
            bArr = hexStringToBytes2;
        }
        return this.reader.Fd_StartLogging(this.param.ComAddr, b, bArr, new byte[]{(byte) (i >> 8), (byte) (i & 255)}, new byte[]{(byte) (i2 >> 8), (byte) (i2 & 255)}, hexStringToBytes, this.param.MaskMem, this.param.MaskAdr, this.param.MaskLen, this.param.MaskData, bArr2);
    }

    @Override // com.rfid.trans.CReader
    public int Fd_StopLogging(String str, String str2, String str3) {
        byte[] bArr;
        byte b;
        byte[] bArr2 = new byte[1];
        if (str2 == null || str2.length() != 8 || str3 == null || str3.length() != 8) {
            return 255;
        }
        byte[] hexStringToBytes = this.reader.hexStringToBytes(str2);
        byte[] hexStringToBytes2 = this.reader.hexStringToBytes(str3);
        if (str == null || str.length() <= 0) {
            bArr = null;
            b = 0;
        } else {
            if (str.length() % 4 != 0) {
                return 255;
            }
            byte[] hexStringToBytes3 = this.reader.hexStringToBytes(str);
            bArr = hexStringToBytes3;
            b = (byte) (hexStringToBytes3.length / 2);
        }
        return this.reader.Fd_StopLogging(this.param.ComAddr, b, bArr, hexStringToBytes, hexStringToBytes2, this.param.MaskMem, this.param.MaskAdr, this.param.MaskLen, this.param.MaskData, bArr2);
    }

    @Override // com.rfid.trans.CReader
    public int Fd_ExtReadMemory(String str, int i, int i2, String str2, byte b, String str3, byte[] bArr, int[] iArr) {
        byte[] bArr2;
        byte b2;
        byte[] bArr3 = new byte[1];
        if (str2 == null || str2.length() != 8 || str3 == null || str3.length() != 8 || bArr == null || bArr.length < i2) {
            return 255;
        }
        byte[] hexStringToBytes = this.reader.hexStringToBytes(str2);
        byte[] hexStringToBytes2 = this.reader.hexStringToBytes(str3);
        if (str == null || str.length() <= 0) {
            bArr2 = null;
            b2 = 0;
        } else {
            if (str.length() % 4 != 0) {
                return 255;
            }
            byte[] hexStringToBytes3 = this.reader.hexStringToBytes(str);
            b2 = (byte) (hexStringToBytes3.length / 2);
            bArr2 = hexStringToBytes3;
        }
        return this.reader.Fd_ExtReadMemory(this.param.ComAddr, b2, bArr2, new byte[]{(byte) (i >> 8), (byte) (i & 255)}, new byte[]{(byte) (i2 >> 8), (byte) (i2 & 255)}, hexStringToBytes, b, hexStringToBytes2, this.param.MaskMem, this.param.MaskAdr, this.param.MaskLen, this.param.MaskData, bArr, iArr, bArr3);
    }

    @Override // com.rfid.trans.CReader
    public int Fd_OP_Mode_Chk(String str, byte b, String str2, byte[] bArr) {
        byte[] bArr2;
        byte b2;
        byte[] bArr3 = new byte[1];
        if (str2 == null || str2.length() != 8 || bArr == null || bArr.length < 2) {
            return 255;
        }
        byte[] hexStringToBytes = this.reader.hexStringToBytes(str2);
        if (str == null || str.length() <= 0) {
            bArr2 = null;
            b2 = 0;
        } else {
            if (str.length() % 4 != 0) {
                return 255;
            }
            byte[] hexStringToBytes2 = this.reader.hexStringToBytes(str);
            bArr2 = hexStringToBytes2;
            b2 = (byte) (hexStringToBytes2.length / 2);
        }
        return this.reader.Fd_OP_Mode_Chk(this.param.ComAddr, b2, bArr2, b, hexStringToBytes, this.param.MaskMem, this.param.MaskAdr, this.param.MaskLen, this.param.MaskData, bArr, bArr3);
    }

    public String ReadData_GB(String str, byte b, int i, byte b2, String str2) {
        byte[] bArr;
        byte b3;
        if (str2 != null && str2.length() == 8) {
            byte[] hexStringToBytes = this.reader.hexStringToBytes(str2);
            if (str == null || str.length() <= 0) {
                bArr = null;
                b3 = 0;
            } else {
                byte[] hexStringToBytes2 = this.reader.hexStringToBytes(str);
                b3 = (byte) (str.length() / 4);
                bArr = hexStringToBytes2;
            }
            int i2 = b2 * 2;
            byte[] bArr2 = new byte[i2];
            if (this.reader.ReadData_GB(this.param.ComAddr, b3, bArr, b, new byte[]{(byte) (i >> 8), (byte) (i & 255)}, b2, hexStringToBytes, bArr2, new byte[1]) == 0) {
                return this.reader.bytesToHexString(bArr2, 0, i2);
            }
        }
        return null;
    }

    public int WriteData_GB(String str, byte b, int i, String str2, String str3) {
        byte[] bArr;
        byte b2;
        if (str2 == null || str2.length() != 8) {
            return 255;
        }
        byte[] hexStringToBytes = this.reader.hexStringToBytes(str2);
        if (str == null || str.length() <= 0) {
            bArr = null;
            b2 = 0;
        } else {
            byte[] hexStringToBytes2 = this.reader.hexStringToBytes(str);
            b2 = (byte) (str.length() / 4);
            bArr = hexStringToBytes2;
        }
        if (str3 == null || str3.length() == 0 || str3.length() % 4 != 0) {
            return 255;
        }
        return this.reader.WriteData_GB(this.param.ComAddr, (byte) (str3.length() / 4), b2, bArr, b, new byte[]{(byte) (i >> 8), (byte) (i & 255)}, this.reader.hexStringToBytes(str3), hexStringToBytes, new byte[1]);
    }

    public int Lock_GB(String str, byte b, byte b2, byte b3, String str2) {
        byte[] bArr;
        byte b4;
        if (str2 == null || str2.length() != 8) {
            return 255;
        }
        byte[] hexStringToBytes = this.reader.hexStringToBytes(str2);
        if (str == null || str.length() <= 0) {
            bArr = null;
            b4 = 0;
        } else {
            byte[] hexStringToBytes2 = this.reader.hexStringToBytes(str);
            b4 = (byte) (str.length() / 4);
            bArr = hexStringToBytes2;
        }
        return this.reader.Lock_GB(this.param.ComAddr, b4, bArr, b, b2, b3, hexStringToBytes, new byte[1]);
    }

    public int Kill_GB(String str, String str2) {
        byte[] bArr;
        byte b;
        if (str2 == null || str2.length() != 8) {
            return 255;
        }
        byte[] hexStringToBytes = this.reader.hexStringToBytes(str2);
        if (str == null || str.length() <= 0) {
            bArr = null;
            b = 0;
        } else {
            byte[] hexStringToBytes2 = this.reader.hexStringToBytes(str);
            b = (byte) (str.length() / 4);
            bArr = hexStringToBytes2;
        }
        return this.reader.Kill_GB(this.param.ComAddr, b, bArr, hexStringToBytes, new byte[1]);
    }

    public int EraseData_GB(String str, byte b, int i, int i2, String str2) {
        byte[] bArr;
        byte b2;
        if (str2 == null || str2.length() != 8) {
            return 255;
        }
        byte[] hexStringToBytes = this.reader.hexStringToBytes(str2);
        if (str == null || str.length() <= 0) {
            bArr = null;
            b2 = 0;
        } else {
            byte[] hexStringToBytes2 = this.reader.hexStringToBytes(str);
            b2 = (byte) (str.length() / 4);
            bArr = hexStringToBytes2;
        }
        return this.reader.EraseData_GB(this.param.ComAddr, b2, bArr, b, new byte[]{(byte) (i >> 8), (byte) (i & 255)}, new byte[]{(byte) (i2 >> 8), (byte) (i2 & 255)}, hexStringToBytes, new byte[1]);
    }

    public String ReadData_GJB(String str, byte b, int i, byte b2, String str2) {
        byte[] bArr;
        byte b3;
        if (str2 != null && str2.length() == 8) {
            byte[] hexStringToBytes = this.reader.hexStringToBytes(str2);
            if (str == null || str.length() <= 0) {
                bArr = null;
                b3 = 0;
            } else {
                byte[] hexStringToBytes2 = this.reader.hexStringToBytes(str);
                b3 = (byte) (str.length() / 4);
                bArr = hexStringToBytes2;
            }
            int i2 = b2 * 2;
            byte[] bArr2 = new byte[i2];
            if (this.reader.ReadData_GJB(this.param.ComAddr, b3, bArr, b, new byte[]{(byte) (i >> 8), (byte) (i & 255)}, b2, hexStringToBytes, bArr2, new byte[1]) == 0) {
                return this.reader.bytesToHexString(bArr2, 0, i2);
            }
        }
        return null;
    }

    public int WriteData_GJB(String str, byte b, int i, String str2, String str3) {
        byte[] bArr;
        byte b2;
        if (str2 == null || str2.length() != 8) {
            return 255;
        }
        byte[] hexStringToBytes = this.reader.hexStringToBytes(str2);
        if (str == null || str.length() <= 0) {
            bArr = null;
            b2 = 0;
        } else {
            byte[] hexStringToBytes2 = this.reader.hexStringToBytes(str);
            b2 = (byte) (str.length() / 4);
            bArr = hexStringToBytes2;
        }
        if (str3 == null || str3.length() == 0 || str3.length() % 4 != 0) {
            return 255;
        }
        return this.reader.WriteData_GJB(this.param.ComAddr, (byte) (str3.length() / 4), b2, bArr, b, new byte[]{(byte) (i >> 8), (byte) (i & 255)}, this.reader.hexStringToBytes(str3), hexStringToBytes, new byte[1]);
    }

    public int Lock_GJB(String str, byte b, byte b2, byte b3, String str2) {
        byte[] bArr;
        byte b4;
        if (str2 == null || str2.length() != 8) {
            return 255;
        }
        byte[] hexStringToBytes = this.reader.hexStringToBytes(str2);
        if (str == null || str.length() <= 0) {
            bArr = null;
            b4 = 0;
        } else {
            byte[] hexStringToBytes2 = this.reader.hexStringToBytes(str);
            b4 = (byte) (str.length() / 4);
            bArr = hexStringToBytes2;
        }
        return this.reader.Lock_GJB(this.param.ComAddr, b4, bArr, b, b2, b3, hexStringToBytes, new byte[1]);
    }

    public int Kill_GJB(String str, String str2) {
        byte[] bArr;
        byte b;
        if (str2 == null || str2.length() != 8) {
            return 255;
        }
        byte[] hexStringToBytes = this.reader.hexStringToBytes(str2);
        if (str == null || str.length() <= 0) {
            bArr = null;
            b = 0;
        } else {
            byte[] hexStringToBytes2 = this.reader.hexStringToBytes(str);
            b = (byte) (str.length() / 4);
            bArr = hexStringToBytes2;
        }
        return this.reader.Kill_GJB(this.param.ComAddr, b, bArr, hexStringToBytes, new byte[1]);
    }

    public int EraseData_GJB(String str, byte b, int i, int i2, String str2) {
        byte[] bArr;
        byte b2;
        if (str2 == null || str2.length() != 8) {
            return 255;
        }
        byte[] hexStringToBytes = this.reader.hexStringToBytes(str2);
        if (str == null || str.length() <= 0) {
            bArr = null;
            b2 = 0;
        } else {
            byte[] hexStringToBytes2 = this.reader.hexStringToBytes(str);
            b2 = (byte) (str.length() / 4);
            bArr = hexStringToBytes2;
        }
        return this.reader.EraseData_GJB(this.param.ComAddr, b2, bArr, b, new byte[]{(byte) (i >> 8), (byte) (i & 255)}, new byte[]{(byte) (i2 >> 8), (byte) (i2 & 255)}, hexStringToBytes, new byte[1]);
    }

    public int Inventory_GJB(byte b, List<ReadTag> list) {
        return this.reader.Inventory_GJB(this.param.ComAddr, b, Byte.MIN_VALUE, (byte) 10, list, new int[1]);
    }

    public int Inventory_GB(List<ReadTag> list) {
        return this.reader.Inventory_GB(this.param.ComAddr, Byte.MIN_VALUE, (byte) 10, list, new int[1]);
    }

    public int InventorySingle_6B(byte[] bArr) {
        return this.reader.InventorySingle_6B(this.param.ComAddr, bArr);
    }

    public int InventoryMutiple_6B(byte b, byte b2, byte b3, byte[] bArr, byte[] bArr2, int[] iArr) {
        return this.reader.InventoryMutiple_6B(this.param.ComAddr, b, b2, b3, bArr, bArr2, iArr);
    }

    public int ReadData_6B(byte b, byte[] bArr, byte b2, byte[] bArr2) {
        return this.reader.ReadData_6B(this.param.ComAddr, b, bArr, b2, bArr2);
    }

    public int WriteData_6B(byte b, byte[] bArr, byte b2, byte[] bArr2) {
        return this.reader.WriteData_6B(this.param.ComAddr, b, bArr, b2, bArr2);
    }

    public int Lock_6B(byte b, byte[] bArr) {
        return this.reader.Lock_6B(this.param.ComAddr, b, bArr);
    }

    public int CheckLock_6B(byte b, byte[] bArr, byte[] bArr2) {
        return this.reader.CheckLock_6B(this.param.ComAddr, b, bArr, bArr2);
    }

    public int RfOutput(byte b) {
        return this.reader.RfOutput(this.param.ComAddr, b);
    }

    public int SetCfgParameter(byte b, byte b2, byte[] bArr, int i) {
        return this.reader.SetCfgParameter(this.param.ComAddr, b, b2, bArr, i);
    }

    public int GetCfgParameter(byte b, byte[] bArr, int[] iArr) {
        return this.reader.GetCfgParameter(this.param.ComAddr, b, bArr, iArr);
    }

    public int MeasureReturnLoss(byte[] bArr, byte b, byte[] bArr2) {
        return this.reader.MeasureReturnLoss(this.param.ComAddr, bArr, b, bArr2);
    }

    public int SetCustomRegion(byte b, int i, int i2, int i3, int i4) {
        return this.reader.SetCustomRegion(this.param.ComAddr, b, i, i2, i3, i4);
    }

    public int GetCustomRegion(int[] iArr, int[] iArr2, int[] iArr3, int[] iArr4) {
        return this.reader.GetCustomRegion(this.param.ComAddr, iArr, iArr2, iArr3, iArr4);
    }

    public int GetModuleDescribe(byte[] bArr) {
        return this.reader.GetModuleDescribe(this.param.ComAddr, bArr);
    }

    public int SetProtocol(byte[] bArr) {
        return this.reader.SetProtocol(this.param.ComAddr, bArr);
    }

    public int StartInventoryLed(int i, List<MaskClass> list) {
        if (this.mThread != null) {
            return -1;
        }
        this.readledType = i;
        this.ledMaskList = new ArrayList();
        if (list == null || list.size() == 0) {
            MaskClass maskClass = new MaskClass();
            maskClass.MaskMem = (byte) 2;
            maskClass.MaskLen = (byte) 24;
            maskClass.MaskAdr[0] = 0;
            maskClass.MaskAdr[1] = 0;
            if (i == 0) {
                maskClass.MaskData[0] = -30;
                maskClass.MaskData[1] = -127;
                maskClass.MaskData[2] = -48;
            } else {
                maskClass.MaskData[0] = -30;
                maskClass.MaskData[1] = 1;
                maskClass.MaskData[2] = -30;
            }
            this.ledMaskList.add(maskClass);
        } else {
            for (int i2 = 0; i2 < list.size(); i2++) {
                this.ledMaskList.add(list.get(i2));
            }
        }
        this.mWorking = true;
        this.mThread = new Thread(new Runnable() { // from class: com.rfid.trans.ReaderHelp.3
            @Override // java.lang.Runnable
            public void run() {
                while (ReaderHelp.this.mWorking) {
                    ReaderHelp.this.scanLed();
                }
                ReaderHelp.isSound = false;
                ReaderHelp.this.mThread = null;
                if (ReaderHelp.this.callback != null) {
                    ReaderHelp.this.callback.StopReadCallBack();
                }
            }
        });
        this.mThread.start();
        return 0;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void scanLed() {
        byte b;
        byte b2;
        byte b3;
        int[] iArr = {0};
        byte[] bArr = new byte[2];
        byte[] bArr2 = new byte[2];
        byte[] bArr3 = new byte[100];
        byte[] hexStringToBytes = this.reader.hexStringToBytes(this.param.Password);
        if (this.readledType == 0) {
            bArr[0] = 0;
            bArr[1] = 4;
            b = 0;
        } else {
            bArr[0] = 0;
            bArr[1] = 112;
            b = 3;
        }
        if (this.ledMaskList.size() > 0) {
            this.maskIndex %= this.ledMaskList.size();
            MaskClass maskClass = this.ledMaskList.get(this.maskIndex);
            this.maskIndex++;
            byte b4 = maskClass.MaskMem;
            System.arraycopy(maskClass.MaskAdr, 0, bArr2, 0, 2);
            byte b5 = maskClass.MaskLen;
            System.arraycopy(maskClass.MaskData, 0, bArr3, 0, ((maskClass.MaskLen & 255) + 7) / 8);
            b3 = b5;
            b2 = b4;
        } else {
            b2 = 0;
            b3 = 0;
        }
        this.reader.Inventory_Led(this.param.ComAddr, (byte) 4, (byte) 0, b2, bArr2, b3, bArr3, b, bArr, (byte) 1, hexStringToBytes, (byte) 0, Byte.MIN_VALUE, (byte) 10, null, iArr);
        if (iArr[0] == 0) {
            this.NoCardCOunt++;
            if (this.NoCardCOunt > 1) {
                isSound = false;
                return;
            }
            return;
        }
        this.NoCardCOunt = 0;
    }

    public void StopInventoryLed() {
        if (this.ModuleType == 2) {
            this.reader.StopInventory(this.param.ComAddr);
        }
        this.mWorking = false;
        isSound = false;
    }

    public int SetCheckAnt(byte b) {
        return this.reader.SetCheckAnt(this.param.ComAddr, b);
    }

    public int GetCheckAnt(byte[] bArr) {
        return this.reader.GetReaderInformation(new byte[]{-1}, new byte[2], new byte[1], new byte[1], new byte[2], new byte[2], new byte[2], new byte[2], new byte[1], new byte[1], new byte[1], new byte[1], bArr);
    }

    public void InventorySingle_G2() {
        int[] iArr = {0};
        if (this.param.IvtType == 0) {
            iArr[0] = 0;
            this.CardCount = 0;
            this.ReadSpeed = 0;
            if (this.MaskList.size() > 0) {
                MaskClass maskClass = this.MaskList.get(this.maskIndex);
                this.maskIndex++;
                this.maskIndex %= this.MaskList.size();
                this.param.MaskMem = maskClass.MaskMem;
                System.arraycopy(maskClass.MaskAdr, 0, this.param.MaskAdr, 0, 2);
                this.param.MaskLen = maskClass.MaskLen;
                System.arraycopy(maskClass.MaskData, 0, this.param.MaskData, 0, ((maskClass.MaskLen & 255) + 7) / 8);
            }
            this.reader.Inventory_G2(this.param.ComAddr, (byte) 2, (byte) 0, (byte) this.param.WordPtr, (byte) 0, (byte) 0, Byte.MIN_VALUE, (byte) 3, this.param.MaskMem, this.param.MaskAdr, this.param.MaskLen, this.param.MaskData, null, iArr, true);
            isSound = false;
            return;
        }
        if (this.param.IvtType == 1) {
            this.QValue = (byte) this.param.QValue;
            int i = this.param.WordPtr;
            int i2 = this.param.WordPtr;
            if (this.param.Length == 0) {
                this.param.Length = 6;
            }
            byte b = (byte) this.param.Length;
            this.reader.hexStringToBytes(this.param.Password);
            this.Session = 0;
            if (this.MaskList.size() > 0) {
                MaskClass maskClass2 = this.MaskList.get(this.maskIndex);
                this.maskIndex++;
                this.maskIndex %= this.MaskList.size();
                this.param.MaskMem = maskClass2.MaskMem;
                System.arraycopy(maskClass2.MaskAdr, 0, this.param.MaskAdr, 0, 2);
                this.param.MaskLen = maskClass2.MaskLen;
                System.arraycopy(maskClass2.MaskData, 0, this.param.MaskData, 0, ((maskClass2.MaskLen & 255) + 7) / 8);
            }
            ArrayList arrayList = new ArrayList();
            iArr[0] = 0;
            this.reader.Inventory_NoCallback(this.param.ComAddr, (byte) 2, (byte) 0, (byte) 0, (byte) 0, this.Target, Byte.MIN_VALUE, (byte) 3, this.param.MaskMem, this.param.MaskAdr, this.param.MaskLen, this.param.MaskData, arrayList, iArr, true);
            isSound = false;
            if (iArr[0] > 0) {
                int i3 = 0;
                while (i3 < arrayList.size()) {
                    ReadTag readTag = (ReadTag) arrayList.get(i3);
                    ArrayList arrayList2 = arrayList;
                    byte b2 = b;
                    String ReadData_G2 = ReadData_G2(readTag.epcId, (byte) this.param.Memory, (byte) this.param.WordPtr, b, this.param.Password);
                    if (ReadData_G2 != null && ReadData_G2.length() > 0) {
                        readTag.memId = ReadData_G2;
                        TagCallback tagCallback = this.callback;
                        if (tagCallback != null) {
                            tagCallback.tagCallback(readTag);
                        }
                        playSound();
                        return;
                    }
                    i3++;
                    arrayList = arrayList2;
                    b = b2;
                }
            }
        }
    }

    public ReadTag FindEPC(String str) {
        new ReadTag();
        this.mWorking = false;
        isSound = false;
        ArrayList arrayList = new ArrayList();
        this.reader.Inventory_G2(this.param.ComAddr, (byte) 2, (byte) 0, (byte) this.param.WordPtr, (byte) 0, (byte) 0, Byte.MIN_VALUE, (byte) 3, (byte) 1, new byte[]{0, 32}, (byte) (str.length() * 4), this.reader.hexStringToBytes(str), arrayList, new int[]{0}, false);
        ReadTag readTag = arrayList.size() > 0 ? (ReadTag) arrayList.get(0) : null;
        isSound = false;
        return readTag;
    }
}
