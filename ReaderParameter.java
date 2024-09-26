package com.rfid.trans;

/* loaded from: classes.dex */
public class ReaderParameter {
    public byte ComAddr = -1;
    public int IvtType = 1;
    public int Memory = 2;
    public String Password = "00000000";
    public int WordPtr = 0;
    public int Length = 6;
    public int Session = 0;
    public int QValue = 4;
    public int ScanTime = 20;
    public int Antenna = 128;
    public int Interval = 0;
    public byte MaskMem = 1;
    public byte[] MaskAdr = new byte[2];
    public byte MaskLen = 0;
    public byte[] MaskData = new byte[96];
}
