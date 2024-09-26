package com.rfid.trans;

/* loaded from: classes.dex */
public interface RFIDLogCallBack {
    void RecvMessageCallback(byte[] bArr);

    void SendMessageCallback(byte[] bArr);
}
