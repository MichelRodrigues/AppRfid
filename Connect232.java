package com.UHF.scanlable;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import com.rfid.trans.ReaderParameter;
import java.util.HashMap;

/* loaded from: classes.dex */
public class Connect232 extends AppCompatActivity {
    private static final boolean DEBUG = true;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final String TAG = "COONECTRS232";
    private static AudioManager am = null;
    public static int baud = 115200;
    private static String devport = "/dev/ttyHSL0";
    private static SoundPool soundPool;
    private static float volumnRatio;
    private RadioButton mBaud115200View;
    private RadioButton mBaud57600View;
    private TextView mConectButton;
    private int mPosPort = -1;
    private VirtualKeyListenerBroadcastReceiver mVirtualKeyListenerBroadcastReceiver;
    private static String[] PERMISSIONS_STORAGE = {"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"};
    static HashMap<Integer, Integer> soundMap = new HashMap<>();
    public static boolean mSwitchFlag = false;

    @Override // android.support.v7.app.AppCompatActivity, android.support.v4.app.FragmentActivity, android.support.v4.app.SupportActivity, android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(128, 128);
        setContentView(R.layout.activity_connect232);
        this.mVirtualKeyListenerBroadcastReceiver = new VirtualKeyListenerBroadcastReceiver();
        registerReceiver(this.mVirtualKeyListenerBroadcastReceiver, new IntentFilter("android.intent.action.CLOSE_SYSTEM_DIALOGS"));
        initSound();
        verifyStoragePermissions(this);
        this.mConectButton = (TextView) findViewById(R.id.textview_connect);
        this.mBaud57600View = (RadioButton) findViewById(R.id.baud_57600);
        this.mBaud115200View = (RadioButton) findViewById(R.id.baud_115200);
        baud = 115200;
        this.mBaud57600View.setOnClickListener(new View.OnClickListener() { // from class: com.UHF.scanlable.Connect232.1
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                Connect232.baud = 57600;
            }
        });
        this.mBaud115200View.setOnClickListener(new View.OnClickListener() { // from class: com.UHF.scanlable.Connect232.2
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                Connect232.baud = 115200;
            }
        });
        this.mConectButton.setOnClickListener(new View.OnClickListener() { // from class: com.UHF.scanlable.Connect232.3
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                try {
                    if (Reader.rrlib.Connect(Connect232.devport, 57600, 1) != 0) {
                        if (Reader.rrlib.Connect(Connect232.devport, 115200, 1) == 0) {
                            Connect232.baud = 115200;
                            Connect232.this.mBaud115200View.setChecked(true);
                            Connect232.this.initRfid();
                            Connect232.this.startActivity(new Intent().setClass(Connect232.this, MainActivity.class));
                        } else {
                            Toast.makeText(Connect232.this.getApplicationContext(), Connect232.this.getString(R.string.openport_failed), 0).show();
                        }
                    } else {
                        Connect232.baud = 57600;
                        Connect232.this.mBaud57600View.setChecked(true);
                        Connect232.this.initRfid();
                        Connect232.this.startActivity(new Intent().setClass(Connect232.this, MainActivity.class));
                    }
                } catch (Exception unused) {
                    Toast.makeText(Connect232.this.getApplicationContext(), Connect232.this.getString(R.string.openport_failed), 0).show();
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void initRfid() {
        int GetReaderType = Reader.rrlib.GetReaderType();
        ReaderParameter GetInventoryPatameter = Reader.rrlib.GetInventoryPatameter();
        if (GetReaderType == 33 || GetReaderType == 40 || GetReaderType == 35 || GetReaderType == 55 || GetReaderType == 54) {
            GetInventoryPatameter.Session = 1;
        } else if (GetReaderType == 112 || GetReaderType == 113 || GetReaderType == 49) {
            GetInventoryPatameter.Session = 254;
        } else if (GetReaderType == 97 || GetReaderType == 99 || GetReaderType == 101 || GetReaderType == 102) {
            GetInventoryPatameter.Session = 1;
        } else {
            GetInventoryPatameter.Session = 0;
        }
        Reader.rrlib.SetInventoryPatameter(GetInventoryPatameter);
    }

    public static void verifyStoragePermissions(Activity activity) {
        if (ActivityCompat.checkSelfPermission(activity, "android.permission.WRITE_EXTERNAL_STORAGE") != 0) {
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, 1);
        }
    }

    private void initSound() {
        soundPool = new SoundPool(10, 3, 5);
        soundMap.put(1, Integer.valueOf(soundPool.load(this, R.raw.barcodebeep, 1)));
        am = (AudioManager) getSystemService("audio");
        Reader.rrlib.SetSoundID(soundMap.get(1).intValue(), soundPool);
    }

    @Override // android.support.v4.app.FragmentActivity, android.app.Activity
    protected void onResume() {
        OtgUtils.set53GPIOEnabled(true);
        super.onResume();
    }

    @Override // android.support.v7.app.AppCompatActivity, android.app.Activity, android.view.KeyEvent.Callback
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 4) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override // android.support.v7.app.AppCompatActivity, android.support.v4.app.FragmentActivity, android.app.Activity
    protected void onDestroy() {
        OtgUtils.set53GPIOEnabled(false);
        unregisterReceiver(this.mVirtualKeyListenerBroadcastReceiver);
        super.onDestroy();
    }

    /* loaded from: classes.dex */
    private class VirtualKeyListenerBroadcastReceiver extends BroadcastReceiver {
        private final String SYSTEM_HOME_KEY;
        private final String SYSTEM_REASON;
        private final String SYSTEM_RECENT_APPS;

        private VirtualKeyListenerBroadcastReceiver() {
            this.SYSTEM_REASON = "reason";
            this.SYSTEM_HOME_KEY = "homekey";
            this.SYSTEM_RECENT_APPS = "recentapps";
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String stringExtra;
            if (!intent.getAction().equals("android.intent.action.CLOSE_SYSTEM_DIALOGS") || (stringExtra = intent.getStringExtra("reason")) == null) {
                return;
            }
            Connect232.mSwitchFlag = true;
            if (stringExtra.equals("homekey")) {
                System.out.println("Press HOME key");
                OtgUtils.set53GPIOEnabled(false);
            } else if (stringExtra.equals("recentapps")) {
                System.out.println("Press RECENT_APPS key");
                OtgUtils.set53GPIOEnabled(true);
            }
        }
    }
}
