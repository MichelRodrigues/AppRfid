package com.UHF.scanlable;

import android.widget.TextView;
import com.rfid.trans.ReaderHelp;
import java.text.SimpleDateFormat;
import java.util.Date;

/* loaded from: classes.dex */
public class Reader {
    public static ReaderHelp rrlib = new ReaderHelp();

    public static void writelog(String log, TextView tvResult) {
        tvResult.setText(new SimpleDateFormat("HH:mm:ss").format((Date) new java.sql.Date(System.currentTimeMillis())) + " " + log);
    }
}
