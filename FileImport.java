package com.UHF.scanlable;

import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/* loaded from: classes.dex */
public class FileImport {
    static String xlsFilePath = Environment.getExternalStorageDirectory() + "/XLSFile/";

    public static boolean daochu(String tmpname, ArrayList<HashMap<String, String>> lists2) {
        try {
            String str = tmpname.isEmpty() ? xlsFilePath + "Tag_" + GetTimesyyyymmddhhmmss() + ".xls" : xlsFilePath + tmpname;
            new File(xlsFilePath).mkdirs();
            ArrayList arrayList = new ArrayList();
            ArrayList arrayList2 = new ArrayList();
            arrayList2.add("EPC");
            arrayList.add(arrayList2);
            FileXls.writeXLS(str, arrayList);
            ArrayList arrayList3 = new ArrayList();
            String str2 = android.device.sdk.BuildConfig.FLAVOR;
            for (int i = 0; i < lists2.size(); i++) {
                ArrayList arrayList4 = new ArrayList();
                for (Map.Entry<String, String> entry : lists2.get(i).entrySet()) {
                    if (entry.getKey().equals("tagUii")) {
                        str2 = entry.getValue().toString();
                    }
                }
                arrayList4.add(str2);
                arrayList3.add(arrayList4);
            }
            return FileXls.writeXLS(str, arrayList3);
        } catch (Exception e) {
            Log.i("导出异常", e.getMessage());
            return false;
        }
    }

    public static String GetTimesyyyymmdd() {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis()));
    }

    public static String GetTimesddMMyy() {
        return new SimpleDateFormat("dd/MM/yy").format(new Date(System.currentTimeMillis()));
    }

    public static String GetTimesyyyymmddhhmmss() {
        return new SimpleDateFormat("yyyy-MM-dd HHmmss").format(new Date(System.currentTimeMillis()));
    }
}
