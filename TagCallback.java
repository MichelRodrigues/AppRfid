package com.rfid.trans;

/* loaded from: classes.dex */
public interface TagCallback {
    void StopReadCallBack();

    void tagCallback(ReadTag readTag);
}
