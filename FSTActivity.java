package com.UHF.scanlable;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/* loaded from: classes.dex */
public class FSTActivity extends Activity implements View.OnClickListener {
    Button ShowButton;
    Button TranButton;
    Handler handler;
    Button selectButton;
    private ArrayAdapter<String> spada_epc;
    Spinner spepc;
    TextView tvResult;
    byte[] TranData = null;
    Thread mThread = null;

    @Override // android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fst);
        initView();
        checkAndRequestPermission();
        this.handler = new Handler() { // from class: com.UHF.scanlable.FSTActivity.1
            AnonymousClass1() {
            }

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                try {
                    int i = msg.what;
                    if (i == 1) {
                        FSTActivity.this.tvResult.setText(msg.obj + android.device.sdk.BuildConfig.FLAVOR);
                    } else if (i == 2) {
                        FSTActivity.this.TranButton.setEnabled(true);
                        FSTActivity.this.ShowButton.setEnabled(true);
                    }
                } catch (Exception e) {
                    e.toString();
                }
            }
        };
    }

    /* renamed from: com.UHF.scanlable.FSTActivity$1 */
    /* loaded from: classes.dex */
    class AnonymousClass1 extends Handler {
        AnonymousClass1() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            try {
                int i = msg.what;
                if (i == 1) {
                    FSTActivity.this.tvResult.setText(msg.obj + android.device.sdk.BuildConfig.FLAVOR);
                } else if (i == 2) {
                    FSTActivity.this.TranButton.setEnabled(true);
                    FSTActivity.this.ShowButton.setEnabled(true);
                }
            } catch (Exception e) {
                e.toString();
            }
        }
    }

    private void initView() {
        this.spepc = (Spinner) findViewById(R.id.fst_spinner);
        this.tvResult = (TextView) findViewById(R.id.fst_result);
        this.selectButton = (Button) findViewById(R.id.button_fst_select);
        this.TranButton = (Button) findViewById(R.id.button_fst_tran);
        this.ShowButton = (Button) findViewById(R.id.button_fst_show);
        this.selectButton.setOnClickListener(this);
        this.TranButton.setOnClickListener(this);
        this.ShowButton.setOnClickListener(this);
    }

    @Override // android.app.Activity
    protected void onResume() {
        int size = ScanMode.mlist.size();
        String[] strArr = new String[size];
        for (int i = 0; i < ScanMode.mlist.size(); i++) {
            strArr[i] = ScanMode.mlist.get(i);
        }
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, strArr);
        this.spada_epc = arrayAdapter;
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.spepc.setAdapter((SpinnerAdapter) this.spada_epc);
        if (size > 0) {
            this.spepc.setSelection(0, false);
        }
        super.onResume();
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        if (view == this.selectButton) {
            Intent intent = new Intent("android.intent.action.GET_CONTENT");
            intent.putExtra("android.intent.extra.ALLOW_MULTIPLE", true);
            intent.setType("text/plain");
            intent.addCategory("android.intent.category.OPENABLE");
            startActivityForResult(Intent.createChooser(intent, "选择文件"), 101);
            return;
        }
        Button button = this.TranButton;
        if (view == button) {
            byte[] bArr = this.TranData;
            if (bArr == null || bArr.length != 4736) {
                Reader.writelog("没有选择文件", this.tvResult);
                return;
            }
            if (this.mThread == null) {
                button.setEnabled(false);
                this.ShowButton.setEnabled(false);
                Thread thread = new Thread(new Runnable() { // from class: com.UHF.scanlable.FSTActivity.2
                    AnonymousClass2() {
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        if (FSTActivity.this.TranImage()) {
                            FSTActivity.this.SendMessage("文件传输成功", 1);
                        } else {
                            FSTActivity.this.SendMessage("文件传输失败", 1);
                        }
                        FSTActivity.this.handler.removeMessages(2);
                        FSTActivity.this.handler.sendEmptyMessage(2);
                        FSTActivity.this.mThread = null;
                    }
                });
                this.mThread = thread;
                thread.start();
                return;
            }
            return;
        }
        if (view == this.ShowButton && this.mThread == null) {
            button.setEnabled(false);
            this.ShowButton.setEnabled(false);
            String obj = this.spepc.getSelectedItem() != null ? this.spepc.getSelectedItem().toString() : android.device.sdk.BuildConfig.FLAVOR;
            Thread thread2 = new Thread(new Runnable() { // from class: com.UHF.scanlable.FSTActivity.3
                final /* synthetic */ byte[] val$finalEPC;

                AnonymousClass3(final byte[] val$finalEPC) {
                    hexStringToBytes = val$finalEPC;
                }

                @Override // java.lang.Runnable
                public void run() {
                    FSTActivity.this.SendMessage("等待命令执行", 1);
                    byte[] bArr2 = hexStringToBytes;
                    if (Reader.rrlib.FST_ShowImage(bArr2 != null ? (byte) (bArr2.length / 2) : (byte) 0, hexStringToBytes) == 0) {
                        FSTActivity.this.SendMessage("命令执行成功", 1);
                    } else {
                        FSTActivity.this.SendMessage("命令执行失败", 1);
                    }
                    FSTActivity.this.handler.removeMessages(2);
                    FSTActivity.this.handler.sendEmptyMessage(2);
                    FSTActivity.this.mThread = null;
                }
            });
            this.mThread = thread2;
            thread2.start();
        }
    }

    /* renamed from: com.UHF.scanlable.FSTActivity$2 */
    /* loaded from: classes.dex */
    class AnonymousClass2 implements Runnable {
        AnonymousClass2() {
        }

        @Override // java.lang.Runnable
        public void run() {
            if (FSTActivity.this.TranImage()) {
                FSTActivity.this.SendMessage("文件传输成功", 1);
            } else {
                FSTActivity.this.SendMessage("文件传输失败", 1);
            }
            FSTActivity.this.handler.removeMessages(2);
            FSTActivity.this.handler.sendEmptyMessage(2);
            FSTActivity.this.mThread = null;
        }
    }

    /* renamed from: com.UHF.scanlable.FSTActivity$3 */
    /* loaded from: classes.dex */
    class AnonymousClass3 implements Runnable {
        final /* synthetic */ byte[] val$finalEPC;

        AnonymousClass3(final byte[] val$finalEPC) {
            hexStringToBytes = val$finalEPC;
        }

        @Override // java.lang.Runnable
        public void run() {
            FSTActivity.this.SendMessage("等待命令执行", 1);
            byte[] bArr2 = hexStringToBytes;
            if (Reader.rrlib.FST_ShowImage(bArr2 != null ? (byte) (bArr2.length / 2) : (byte) 0, hexStringToBytes) == 0) {
                FSTActivity.this.SendMessage("命令执行成功", 1);
            } else {
                FSTActivity.this.SendMessage("命令执行失败", 1);
            }
            FSTActivity.this.handler.removeMessages(2);
            FSTActivity.this.handler.sendEmptyMessage(2);
            FSTActivity.this.mThread = null;
        }
    }

    public boolean TranImage() {
        byte[] bArr = new byte[2];
        byte[] bArr2 = null;
        int i = 0;
        while (true) {
            boolean z = true;
            if (i < 23) {
                byte[] bArr3 = new byte[200];
                int i2 = i * 200;
                System.arraycopy(this.TranData, i2, bArr3, 0, 200);
                bArr[0] = (byte) (i2 >> 8);
                bArr[1] = (byte) (i2 & 255);
                int i3 = 0;
                while (true) {
                    if (i3 >= 3) {
                        z = false;
                        break;
                    }
                    if (Reader.rrlib.FST_TranImage((byte) -56, bArr, bArr3) == 0) {
                        SendMessage("文件已传输: " + (((i + 1) * 100) / 24) + "％", 1);
                        break;
                    }
                    i3++;
                }
                if (!z) {
                    return false;
                }
                i++;
                bArr2 = bArr3;
            } else {
                bArr[0] = 17;
                bArr[1] = -8;
                System.arraycopy(this.TranData, 4600, bArr2, 0, 136);
                for (int i4 = 0; i4 < 3; i4++) {
                    if (Reader.rrlib.FST_TranImage((byte) -120, bArr, bArr2) == 0) {
                        SendMessage("文件已传输: 100％", 1);
                        return true;
                    }
                }
                return false;
            }
        }
    }

    public void checkAndRequestPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            ArrayList arrayList = new ArrayList();
            if (checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE") != 0) {
                arrayList.add("android.permission.READ_EXTERNAL_STORAGE");
            }
            if (arrayList.size() == 0) {
                return;
            }
            String[] strArr = new String[arrayList.size()];
            arrayList.toArray(strArr);
            ActivityCompat.requestPermissions(this, strArr, 1);
        }
    }

    @Override // android.app.Activity
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        String[] split;
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != 101 || data == null || (split = data.getDataString().split(":")) == null || split.length < 2) {
            return;
        }
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(new File(split[1])));
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String str = android.device.sdk.BuildConfig.FLAVOR;
            while (true) {
                String readLine = bufferedReader.readLine();
                if (readLine == null) {
                    break;
                } else if (!android.device.sdk.BuildConfig.FLAVOR.equals(readLine)) {
                    str = readLine.split("\\+")[0];
                }
            }
            inputStreamReader.close();
            bufferedReader.close();
            if (str.length() > 0) {
                this.TranData = Util.hexStringToBytes(str);
                SendMessage("图片已选择", 1);
            }
        } catch (FileNotFoundException e) {
            SendMessage("图片选择失败", 1);
            e.printStackTrace();
        } catch (IOException e2) {
            SendMessage("图片选择失败", 1);
            e2.printStackTrace();
        }
    }

    public void SendMessage(String msgdata, int mtype) {
        String str = new SimpleDateFormat("HH:mm:ss").format((Date) new java.sql.Date(System.currentTimeMillis())) + " " + msgdata;
        Message obtainMessage = this.handler.obtainMessage();
        obtainMessage.what = mtype;
        obtainMessage.obj = str;
        this.handler.sendMessage(obtainMessage);
    }

    @Override // android.app.Activity
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 102 && grantResults.length > 0) {
            ArrayList arrayList = new ArrayList();
            ArrayList arrayList2 = new ArrayList();
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != 0) {
                    arrayList.add(permissions[i]);
                } else {
                    arrayList2.add(permissions[i]);
                }
            }
            arrayList.isEmpty();
        }
    }
}
