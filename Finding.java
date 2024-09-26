package com.UHF.scanlable;

import android.app.Activity;
import android.device.DeviceManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.rfid.trans.ReadTag;

/* loaded from: classes.dex */
public class Finding extends Activity implements View.OnClickListener {
    Button btFinding;
    TextView epcid;
    Handler handler;
    private CircleProgress mCircleProgress;
    private volatile boolean mWorking = true;
    private volatile Thread mThread = null;
    int rssi = 0;
    public boolean keyPress = false;

    @Override // android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finding);
        this.mCircleProgress = (CircleProgress) findViewById(R.id.circle_progress);
        this.epcid = (TextView) findViewById(R.id.epc_id);
        Button button = (Button) findViewById(R.id.btfind);
        this.btFinding = button;
        button.setOnClickListener(this);
        this.handler = new Handler() { // from class: com.UHF.scanlable.Finding.1
            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                try {
                    if (msg.what != 0) {
                        return;
                    }
                    Finding.this.mCircleProgress.setValue(Integer.valueOf(msg.obj + android.device.sdk.BuildConfig.FLAVOR).intValue());
                } catch (Exception e) {
                    e.toString();
                }
            }
        };
    }

    private void setOpenScan523(boolean isopen) {
        try {
            DeviceManager deviceManager = new DeviceManager();
            if (isopen) {
                deviceManager.setSettingProperty("persist-persist.sys.rfid.key", "0-");
                deviceManager.setSettingProperty("persist-persist.sys.scan.key", "520-521-522-523-");
            } else {
                deviceManager.setSettingProperty("persist-persist.sys.rfid.key", "0-");
                deviceManager.setSettingProperty("persist-persist.sys.scan.key", "520-521-522-");
            }
        } catch (Exception unused) {
        }
    }

    @Override // android.app.Activity
    protected void onResume() {
        this.epcid.setText(ScanMode.epc);
        this.mCircleProgress.setValue(0.0f);
        setOpenScan523(false);
        super.onResume();
    }

    @Override // android.app.Activity
    protected void onPause() {
        super.onPause();
        setOpenScan523(true);
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View v) {
        if (v == this.btFinding) {
            readTag();
        }
    }

    private void readTag() {
        if (this.btFinding.getText().toString().equals(getString(R.string.finding))) {
            if (this.mThread == null) {
                this.mWorking = true;
                this.btFinding.setText(R.string.btstop);
                this.mThread = new Thread(new Runnable() { // from class: com.UHF.scanlable.Finding.2
                    @Override // java.lang.Runnable
                    public void run() {
                        while (Finding.this.mWorking) {
                            ReadTag FindEPC = Reader.rrlib.FindEPC(ScanMode.epc);
                            if (FindEPC != null) {
                                Finding.this.rssi = FindEPC.rssi;
                                Reader.rrlib.playSound();
                                Finding.this.handler.obtainMessage();
                                Message obtainMessage = Finding.this.handler.obtainMessage();
                                obtainMessage.what = 0;
                                obtainMessage.obj = Finding.this.rssi + android.device.sdk.BuildConfig.FLAVOR;
                                Finding.this.handler.sendMessage(obtainMessage);
                            } else {
                                if (Finding.this.rssi > 0) {
                                    Finding finding = Finding.this;
                                    finding.rssi -= 2;
                                }
                                if (Finding.this.rssi < 0) {
                                    Finding.this.rssi = 0;
                                }
                                Finding.this.handler.obtainMessage();
                                Message obtainMessage2 = Finding.this.handler.obtainMessage();
                                obtainMessage2.what = 0;
                                obtainMessage2.obj = Finding.this.rssi + android.device.sdk.BuildConfig.FLAVOR;
                                Finding.this.handler.sendMessage(obtainMessage2);
                            }
                        }
                    }
                });
                this.mThread.start();
                return;
            }
            return;
        }
        if (this.mThread != null) {
            this.mWorking = false;
            try {
                this.mThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.mThread = null;
            this.btFinding.setText(R.string.finding);
        }
    }

    @Override // android.app.Activity, android.view.KeyEvent.Callback
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 523 && !this.keyPress) {
            this.keyPress = true;
            readTag();
        } else if (keyCode == 4) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override // android.app.Activity, android.view.KeyEvent.Callback
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == 523) {
            this.keyPress = false;
        }
        return super.onKeyUp(keyCode, event);
    }
}
