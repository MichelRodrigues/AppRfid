package com.UHF.scanlable;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import com.rfid.trans.ReaderParameter;

/* loaded from: classes.dex */
public class ScanView extends Activity implements View.OnClickListener {
    private static final String TAG = "SacnView";
    Button Getparam;
    Button Setparam;
    private Button bRead;
    private Button bSetting;
    Button btActive;
    Button btAnswer;
    Button btCloserf;
    Button btGetAntCheck;
    Button btGetFocus;
    Button btOpenrf;
    Button btReadLoss;
    Button btReadTemp;
    Button btSetAntCheck;
    Button btSetBaud;
    Button btSetFocus;
    Button btSetPro;
    private Button getRange;
    Spinner jgTime;
    private LinearLayout lineantcheck;
    private LinearLayout linefocus;
    private LinearLayout lineloss;
    private LinearLayout lineprofile;
    private LinearLayout linerange;
    private Button measure_loss;
    private Button paramRead;
    private Button paramSet;
    private Button setRange;
    private int soundid;
    Spinner spAntCheck;
    Spinner spBand;
    Spinner spDwell;
    private Spinner spMem;
    Spinner spProfilr;
    Spinner spRange;
    Spinner spTagfocus;
    private Spinner spType;
    private ArrayAdapter<String> spada_Band;
    private ArrayAdapter<String> spada_baudrate;
    private ArrayAdapter<String> spada_dwell;
    private ArrayAdapter<String> spada_jgTime;
    private ArrayAdapter<String> spada_lowPwr;
    private ArrayAdapter<String> spada_maxFrm;
    private ArrayAdapter<String> spada_minFrm;
    private ArrayAdapter<String> spada_profile;
    private ArrayAdapter<String> spada_qvalue;
    private ArrayAdapter<String> spada_range;
    private ArrayAdapter<String> spada_session;
    private ArrayAdapter<String> spada_tagfocus;
    private ArrayAdapter<String> spada_tidaddr;
    private ArrayAdapter<String> spada_tidlen;
    private ArrayAdapter<String> spada_time;
    Spinner spbaudRate;
    Spinner spmaxFrm;
    Spinner spminFrm;
    Spinner spqvalue;
    Spinner spsession;
    Spinner sptidaddr;
    Spinner sptidlen;
    Spinner sptime;
    private TextView tvLoss;
    private TextView tvResult;
    EditText tvRun;
    private TextView tvTemp;
    private TextView tvVersion;
    private Spinner tvpowerdBm;
    private int tty_speed = 57600;
    private byte addr = -1;
    private String[] strBand = new String[6];
    private String[] strmaxFrm = null;
    private String[] strminFrm = null;
    private String[] strtime = new String[256];
    private String[] strjtTime = new String[7];
    private String[] strBaudRate = new String[2];
    private String[] dwelltime = new String[254];
    private String[] strProfile = new String[12];
    private String[] strRange = new String[101];
    private int ReaderType = -1;
    private int ModuleType = -1;
    private int ReaderCode = 0;
    private int curband = 0;

    @Override // android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(128, 128);
        setContentView(R.layout.scan_view);
        initView();
        int i = this.ModuleType;
        if (i == 0) {
            ReaderParameter GetInventoryPatameter = Reader.rrlib.GetInventoryPatameter();
            GetInventoryPatameter.Session = 0;
            Reader.rrlib.SetInventoryPatameter(GetInventoryPatameter);
            ReadParam();
            return;
        }
        if (i == 1) {
            ReaderParameter GetInventoryPatameter2 = Reader.rrlib.GetInventoryPatameter();
            GetInventoryPatameter2.Session = 1;
            Reader.rrlib.SetInventoryPatameter(GetInventoryPatameter2);
            ReadParam();
            return;
        }
        if (i == 2) {
            ReaderParameter GetInventoryPatameter3 = Reader.rrlib.GetInventoryPatameter();
            GetInventoryPatameter3.Session = 254;
            Reader.rrlib.SetInventoryPatameter(GetInventoryPatameter3);
            ReadParam();
            return;
        }
        if (i == 3) {
            ReaderParameter GetInventoryPatameter4 = Reader.rrlib.GetInventoryPatameter();
            GetInventoryPatameter4.Session = 1;
            Reader.rrlib.SetInventoryPatameter(GetInventoryPatameter4);
            ReadParam();
        }
    }

    @Override // android.app.Activity
    protected void onResume() {
        super.onResume();
        int i = Connect232.baud;
        if (i == 57600) {
            this.spbaudRate.setSelection(0, true);
        } else if (i == 115200) {
            this.spbaudRate.setSelection(1, true);
        }
        int i2 = this.ModuleType;
        if (i2 == 0) {
            ReadParam();
            ReadInformation();
            getRangeControll();
            return;
        }
        if (i2 == 1) {
            ReadParam();
            ReadInformation();
            ReadProfile();
            ReadCheckAnt();
            return;
        }
        if (i2 == 2) {
            ReadParam();
            ReadInformation();
            ReadFocus();
            ReadProfile();
            ReadCheckAnt();
            return;
        }
        if (i2 == 3) {
            ReadParam();
            ReadInformation();
            ReadProfile();
        }
    }

    private void initView() {
        int i;
        this.tvTemp = (TextView) findViewById(R.id.txt_tempe);
        this.tvLoss = (TextView) findViewById(R.id.txt_loss);
        this.tvRun = (EditText) findViewById(R.id.tv_runtime);
        this.btReadTemp = (Button) findViewById(R.id.bt_Readtemp);
        this.btReadLoss = (Button) findViewById(R.id.bt_Readloss);
        this.tvVersion = (TextView) findViewById(R.id.version);
        this.tvResult = (TextView) findViewById(R.id.param_result);
        this.tvpowerdBm = (Spinner) findViewById(R.id.power_spinner);
        ArrayAdapter<CharSequence> createFromResource = ArrayAdapter.createFromResource(this, R.array.Power_select, android.R.layout.simple_spinner_item);
        createFromResource.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.tvpowerdBm.setAdapter((SpinnerAdapter) createFromResource);
        this.tvpowerdBm.setSelection(33, true);
        this.bSetting = (Button) findViewById(R.id.pro_setting);
        this.bRead = (Button) findViewById(R.id.pro_read);
        this.paramRead = (Button) findViewById(R.id.ivt_read);
        this.paramSet = (Button) findViewById(R.id.ivt_setting);
        this.btOpenrf = (Button) findViewById(R.id.ivt_open);
        this.btCloserf = (Button) findViewById(R.id.ivt_close);
        this.btAnswer = (Button) findViewById(R.id.bt_answer);
        this.btActive = (Button) findViewById(R.id.bt_active);
        this.btSetBaud = (Button) findViewById(R.id.bt_SetBdRate);
        this.btSetAntCheck = (Button) findViewById(R.id.bt_setantcheck);
        this.btGetAntCheck = (Button) findViewById(R.id.bt_getantcheck);
        this.btSetFocus = (Button) findViewById(R.id.bt_SetFocus);
        this.btGetFocus = (Button) findViewById(R.id.bt_GetFocus);
        this.btSetPro = (Button) findViewById(R.id.bt_SetProfile);
        this.getRange = (Button) findViewById(R.id.bt_GetRange);
        this.setRange = (Button) findViewById(R.id.bt_SetRange);
        this.btSetAntCheck.setOnClickListener(this);
        this.btGetAntCheck.setOnClickListener(this);
        this.bSetting.setOnClickListener(this);
        this.bRead.setOnClickListener(this);
        this.paramRead.setOnClickListener(this);
        this.paramSet.setOnClickListener(this);
        this.btOpenrf.setOnClickListener(this);
        this.btCloserf.setOnClickListener(this);
        this.btAnswer.setOnClickListener(this);
        this.btActive.setOnClickListener(this);
        this.btSetBaud.setOnClickListener(this);
        this.btReadLoss.setOnClickListener(this);
        this.btReadTemp.setOnClickListener(this);
        this.getRange.setOnClickListener(this);
        this.setRange.setOnClickListener(this);
        this.btSetFocus.setOnClickListener(this);
        this.btGetFocus.setOnClickListener(this);
        this.btSetPro.setOnClickListener(this);
        this.linefocus = (LinearLayout) findViewById(R.id.linetagfocus);
        this.lineprofile = (LinearLayout) findViewById(R.id.lineprogile);
        this.linerange = (LinearLayout) findViewById(R.id.linerange);
        this.lineloss = (LinearLayout) findViewById(R.id.lineloss);
        this.lineantcheck = (LinearLayout) findViewById(R.id.linecheckant);
        this.linefocus.setVisibility(8);
        this.linerange.setVisibility(8);
        this.lineprofile.setVisibility(8);
        this.lineloss.setVisibility(8);
        this.lineantcheck.setVisibility(8);
        int i2 = 0;
        while (true) {
            if (i2 >= 256) {
                break;
            }
            this.strtime[i2] = String.valueOf(i2) + "*100ms";
            i2++;
        }
        this.sptime = (Spinner) findViewById(R.id.time_spinner);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, this.strtime);
        this.spada_time = arrayAdapter;
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.sptime.setAdapter((SpinnerAdapter) this.spada_time);
        this.sptime.setSelection(50, false);
        String[] strArr = this.strBand;
        strArr[0] = "Chinese band2";
        strArr[1] = "US band";
        strArr[2] = "Korean band";
        strArr[3] = "EU band";
        strArr[4] = "Chinese band1";
        strArr[5] = "ALL band";
        String[] strArr2 = this.strBaudRate;
        strArr2[0] = "57600bps";
        strArr2[1] = "115200bps";
        this.spBand = (Spinner) findViewById(R.id.band_spinner);
        ArrayAdapter<String> arrayAdapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, this.strBand);
        this.spada_Band = arrayAdapter2;
        arrayAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.spBand.setAdapter((SpinnerAdapter) this.spada_Band);
        this.spBand.setSelection(1, false);
        SetFre(2);
        this.spBand.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() { // from class: com.UHF.scanlable.ScanView.1
            @Override // android.widget.AdapterView.OnItemSelectedListener
            public void onNothingSelected(AdapterView<?> arg0) {
            }

            @Override // android.widget.AdapterView.OnItemSelectedListener
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                arg0.setVisibility(0);
                if (arg2 == 0) {
                    ScanView.this.SetFre(1);
                }
                if (arg2 == 1) {
                    ScanView.this.SetFre(2);
                }
                if (arg2 == 2) {
                    ScanView.this.SetFre(3);
                }
                if (arg2 == 3) {
                    ScanView.this.SetFre(4);
                }
                if (arg2 == 4) {
                    ScanView.this.SetFre(8);
                }
                if (arg2 == 5) {
                    ScanView.this.SetFre(0);
                }
            }
        });
        for (int i3 = 0; i3 < 7; i3++) {
            this.strjtTime[i3] = String.valueOf(i3 * 10) + "ms";
        }
        this.jgTime = (Spinner) findViewById(R.id.jgTime_spinner);
        ArrayAdapter<String> arrayAdapter3 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, this.strjtTime);
        this.spada_jgTime = arrayAdapter3;
        arrayAdapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.jgTime.setAdapter((SpinnerAdapter) this.spada_jgTime);
        this.jgTime.setSelection(3, false);
        this.spqvalue = (Spinner) findViewById(R.id.qvalue_spinner);
        ArrayAdapter<CharSequence> createFromResource2 = ArrayAdapter.createFromResource(this, R.array.men_q, android.R.layout.simple_spinner_item);
        createFromResource2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.spqvalue.setAdapter((SpinnerAdapter) createFromResource2);
        this.spqvalue.setSelection(6, true);
        this.spsession = (Spinner) findViewById(R.id.session_spinner);
        ArrayAdapter<CharSequence> createFromResource3 = ArrayAdapter.createFromResource(this, R.array.men_s, android.R.layout.simple_spinner_item);
        createFromResource3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.spsession.setAdapter((SpinnerAdapter) createFromResource3);
        this.spsession.setSelection(5, true);
        this.sptidaddr = (Spinner) findViewById(R.id.tidptr_spinner);
        this.sptidlen = (Spinner) findViewById(R.id.tidlen_spinner);
        ArrayAdapter<CharSequence> createFromResource4 = ArrayAdapter.createFromResource(this, R.array.men_tid, android.R.layout.simple_spinner_item);
        createFromResource4.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.sptidaddr.setAdapter((SpinnerAdapter) createFromResource4);
        this.sptidaddr.setSelection(0, true);
        this.sptidlen.setAdapter((SpinnerAdapter) createFromResource4);
        this.sptidlen.setSelection(6, true);
        this.spbaudRate = (Spinner) findViewById(R.id.baud_spinner);
        ArrayAdapter<String> arrayAdapter4 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, this.strBaudRate);
        this.spada_baudrate = arrayAdapter4;
        arrayAdapter4.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.spbaudRate.setAdapter((SpinnerAdapter) this.spada_baudrate);
        this.spType = (Spinner) findViewById(R.id.IvtType_spinner);
        ArrayAdapter<CharSequence> createFromResource5 = ArrayAdapter.createFromResource(this, R.array.IvtType_select, android.R.layout.simple_spinner_item);
        createFromResource5.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.spType.setAdapter((SpinnerAdapter) createFromResource5);
        this.spType.setSelection(0, false);
        this.spMem = (Spinner) findViewById(R.id.mixmem_spinner);
        ArrayAdapter<CharSequence> createFromResource6 = ArrayAdapter.createFromResource(this, R.array.readmen_select, android.R.layout.simple_spinner_item);
        createFromResource6.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.spMem.setAdapter((SpinnerAdapter) createFromResource6);
        this.spMem.setSelection(1, false);
        int i4 = 2;
        for (i = 256; i4 < i; i = 256) {
            this.dwelltime[i4 - 2] = String.valueOf(i4 * 100) + "ms";
            i4++;
        }
        this.spDwell = (Spinner) findViewById(R.id.dwell_spinner);
        ArrayAdapter<String> arrayAdapter5 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, this.dwelltime);
        this.spada_dwell = arrayAdapter5;
        arrayAdapter5.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.spDwell.setAdapter((SpinnerAdapter) this.spada_dwell);
        this.spDwell.setSelection(48, false);
        this.spTagfocus = (Spinner) findViewById(R.id.focus_spinner);
        ArrayAdapter<CharSequence> createFromResource7 = ArrayAdapter.createFromResource(this, R.array.en_select, android.R.layout.simple_spinner_item);
        createFromResource7.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.spTagfocus.setAdapter((SpinnerAdapter) createFromResource7);
        this.spTagfocus.setSelection(0, false);
        this.spAntCheck = (Spinner) findViewById(R.id.checkant_spinner);
        ArrayAdapter<CharSequence> createFromResource8 = ArrayAdapter.createFromResource(this, R.array.en_select, android.R.layout.simple_spinner_item);
        createFromResource8.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.spAntCheck.setAdapter((SpinnerAdapter) createFromResource8);
        this.spAntCheck.setSelection(1, false);
        int GetReaderType = Reader.rrlib.GetReaderType();
        this.ReaderType = GetReaderType;
        if (GetReaderType == 33 || GetReaderType == 40 || GetReaderType == 35 || GetReaderType == 55 || GetReaderType == 54) {
            this.strProfile = r4;
            String[] strArr3 = {" 0:40K, FM0,25us", " 1:250K,M4, 25us", " 2:300K,M4, 25us", " 3:400K,FM0,6.25us"};
            this.ModuleType = 1;
            this.lineprofile.setVisibility(0);
        } else if (GetReaderType == 112 || GetReaderType == 113 || GetReaderType == 49) {
            this.strProfile = r4;
            String[] strArr4 = {"11:640K,FM0,7.5us", " 1:640K, M2,7.5us", "15:640K, M4,7.5us", "12:320K, M2, 15us", " 3:320K, M2, 20us", " 5:320K, M4, 20us", " 7:250K, M4, 20us", "13:160K, M8, 20us", "103:640K,FM0,6.25us", "120:640K, M2,6.25us", "202:426K,FM0, 15us", "345:640K, M4,7.5us"};
            this.ModuleType = 2;
            this.linefocus.setVisibility(0);
            this.lineprofile.setVisibility(0);
            this.lineloss.setVisibility(0);
            this.lineantcheck.setVisibility(0);
        } else if (GetReaderType == 97 || GetReaderType == 99 || GetReaderType == 101 || GetReaderType == 102) {
            this.ModuleType = 3;
            this.strProfile = r4;
            String[] strArr5 = {" 0:160K,FM0,12.5us", " 1:160K, M8,12.5us", " 2:250K,FM0,12.5us", " 3:320K, M4,6.25us", " 4:160K, M4,12.5us"};
            this.ModuleType = 1;
            this.lineprofile.setVisibility(0);
        } else {
            this.ModuleType = 0;
            this.linerange.setVisibility(0);
        }
        this.spProfilr = (Spinner) findViewById(R.id.prof_spinner);
        ArrayAdapter<String> arrayAdapter6 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, this.strProfile);
        this.spada_profile = arrayAdapter6;
        arrayAdapter6.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.spProfilr.setAdapter((SpinnerAdapter) this.spada_profile);
        if (this.ModuleType == 2) {
            this.spProfilr.setSelection(5, false);
        } else {
            this.spProfilr.setSelection(1, false);
        }
        for (int i5 = 0; i5 <= 100; i5++) {
            this.strRange[i5] = String.valueOf(i5);
        }
        this.spRange = (Spinner) findViewById(R.id.range_spinner);
        ArrayAdapter<String> arrayAdapter7 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, this.strRange);
        this.spada_range = arrayAdapter7;
        arrayAdapter7.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.spRange.setAdapter((SpinnerAdapter) this.spada_range);
        this.spRange.setSelection(16, false);
    }

    private void ReadParam() {
        ReaderParameter GetInventoryPatameter = Reader.rrlib.GetInventoryPatameter();
        this.sptidlen.setSelection(GetInventoryPatameter.Length, true);
        this.sptidaddr.setSelection(GetInventoryPatameter.WordPtr, true);
        this.spqvalue.setSelection(GetInventoryPatameter.QValue, true);
        this.sptime.setSelection(GetInventoryPatameter.ScanTime, true);
        this.spType.setSelection(GetInventoryPatameter.IvtType, true);
        this.spMem.setSelection(GetInventoryPatameter.Memory - 1, true);
        int i = GetInventoryPatameter.Session;
        if (i == 255) {
            i = 4;
        }
        if (i == 254) {
            i = 5;
        }
        if (i == 253) {
            i = 6;
        }
        if (i == 252) {
            i = 7;
        }
        if (i == 251) {
            i = 8;
        }
        this.spsession.setSelection(i, true);
        if (Reader.rrlib.ModuleType == 2) {
            byte[] bArr = new byte[30];
            int[] iArr = new int[1];
            if (Reader.rrlib.GetCfgParameter((byte) 7, bArr, iArr) == 0 && iArr[0] == 3) {
                this.jgTime.setSelection(bArr[0], true);
                this.spDwell.setSelection(bArr[1] - 2, true);
            }
        }
        this.tvRun.setText(ScanMode.runtime + android.device.sdk.BuildConfig.FLAVOR);
        Reader.writelog(getString(R.string.get_success), this.tvResult);
    }

    private void ReadInformation() {
        String str;
        String str2;
        byte[] bArr = new byte[2];
        byte[] bArr2 = new byte[1];
        byte[] bArr3 = new byte[1];
        byte[] bArr4 = new byte[1];
        byte[] bArr5 = new byte[1];
        if (Reader.rrlib.GetReaderInformation(bArr, bArr2, bArr3, bArr4, bArr5) == 0) {
            String valueOf = String.valueOf(bArr[0] & 255);
            if (valueOf.length() == 1) {
                valueOf = "0" + valueOf;
            }
            String valueOf2 = String.valueOf(bArr[1] & 255);
            if (valueOf2.length() == 1) {
                valueOf2 = "0" + valueOf2;
            }
            int GetReaderType = Reader.rrlib.GetReaderType();
            this.ReaderCode = GetReaderType;
            if (GetReaderType == 112 || GetReaderType == 113 || GetReaderType == 49) {
                byte[] bArr6 = new byte[16];
                Reader.rrlib.GetModuleDescribe(bArr6);
                if (bArr6[0] == 0) {
                    str = "S";
                } else if (bArr6[0] == 1) {
                    str = "Plus";
                } else {
                    str = bArr6[0] == 2 ? "Pro" : android.device.sdk.BuildConfig.FLAVOR;
                }
                str2 = valueOf + "." + valueOf2 + " (" + Integer.toHexString(this.ReaderCode) + "-" + str + ")";
            } else {
                str2 = valueOf + "." + valueOf2 + " (" + Integer.toHexString(this.ReaderCode) + ")";
            }
            this.tvVersion.setText(str2);
            this.tvpowerdBm.setSelection(bArr2[0], true);
            this.curband = bArr3[0];
            SetFre(bArr3[0]);
            byte b = bArr3[0];
            this.spBand.setSelection(b == 8 ? b - 4 : b == 0 ? 5 : b - 1, true);
            this.spminFrm.setSelection(bArr5[0], true);
            this.spmaxFrm.setSelection(bArr4[0], true);
            Reader.writelog(getString(R.string.get_success), this.tvResult);
            return;
        }
        Reader.writelog(getString(R.string.get_failed), this.tvResult);
    }

    private void ReadFocus() {
        byte[] bArr = new byte[250];
        int[] iArr = new int[1];
        if (Reader.rrlib.GetCfgParameter((byte) 8, bArr, iArr) == 0 && iArr[0] == 1) {
            this.spTagfocus.setSelection(bArr[0], true);
            Reader.writelog(getString(R.string.get_success), this.tvResult);
        } else {
            Reader.writelog(getString(R.string.get_failed), this.tvResult);
        }
    }

    private void ReadCheckAnt() {
        byte[] bArr = new byte[1];
        if (Reader.rrlib.GetCheckAnt(bArr) == 0) {
            this.spAntCheck.setSelection(bArr[0], true);
            Reader.writelog(getString(R.string.get_success), this.tvResult);
        } else {
            Reader.writelog(getString(R.string.get_failed), this.tvResult);
        }
    }

    private void getRangeControll() {
        byte[] bArr = new byte[250];
        int[] iArr = new int[1];
        if (Reader.rrlib.GetCfgParameter((byte) 16, bArr, iArr) == 0 && iArr[0] == 4) {
            this.spRange.setSelection(bArr[3] & 255, true);
            Reader.writelog(getString(R.string.get_success), this.tvResult);
        } else {
            Reader.writelog(getString(R.string.get_failed), this.tvResult);
        }
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARN: Removed duplicated region for block: B:18:0x002a  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private void ReadProfile() {
        /*
            r7 = this;
            r0 = 1
            byte[] r1 = new byte[r0]
            com.rfid.trans.ReaderHelp r2 = com.UHF.scanlable.Reader.rrlib
            int r2 = r2.GetProfile(r1)
            r3 = 3
            r4 = 0
            if (r2 != 0) goto L5f
            int r2 = r7.ModuleType
            r5 = 2
            if (r2 != r5) goto L49
            r1 = r1[r4]
            r1 = r1 & 255(0xff, float:3.57E-43)
            r2 = 7
            r6 = 5
            if (r1 == r0) goto L42
            if (r1 == r3) goto L40
            if (r1 == r6) goto L3e
            if (r1 == r2) goto L3c
            r6 = 15
            if (r1 == r6) goto L3a
            switch(r1) {
                case 11: goto L2a;
                case 12: goto L43;
                case 13: goto L38;
                default: goto L27;
            }
        L27:
            switch(r1) {
                case 50: goto L35;
                case 51: goto L32;
                case 52: goto L2f;
                case 53: goto L2c;
                default: goto L2a;
            }
        L2a:
            r3 = 0
            goto L43
        L2c:
            r3 = 11
            goto L43
        L2f:
            r3 = 10
            goto L43
        L32:
            r3 = 9
            goto L43
        L35:
            r3 = 8
            goto L43
        L38:
            r3 = 7
            goto L43
        L3a:
            r3 = 2
            goto L43
        L3c:
            r3 = 6
            goto L43
        L3e:
            r3 = 5
            goto L43
        L40:
            r3 = 4
            goto L43
        L42:
            r3 = 1
        L43:
            android.widget.Spinner r1 = r7.spProfilr
            r1.setSelection(r3, r0)
            goto L52
        L49:
            android.widget.Spinner r2 = r7.spProfilr
            r1 = r1[r4]
            r1 = r1 & 255(0xff, float:3.57E-43)
            r2.setSelection(r1, r0)
        L52:
            r0 = 2131558590(0x7f0d00be, float:1.87425E38)
            java.lang.String r0 = r7.getString(r0)
            android.widget.TextView r1 = r7.tvResult
            com.UHF.scanlable.Reader.writelog(r0, r1)
            goto L7b
        L5f:
            int r2 = r7.ModuleType
            if (r2 != r3) goto L6f
            android.widget.Spinner r2 = r7.spProfilr
            r1 = r1[r4]
            r1 = r1 & 255(0xff, float:3.57E-43)
            int r1 = r1 + (-16)
            r2.setSelection(r1, r0)
            goto L7b
        L6f:
            r0 = 2131558589(0x7f0d00bd, float:1.8742498E38)
            java.lang.String r0 = r7.getString(r0)
            android.widget.TextView r1 = r7.tvResult
            com.UHF.scanlable.Reader.writelog(r0, r1)
        L7b:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.UHF.scanlable.ScanView.ReadProfile():void");
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        try {
            if (view == this.paramRead) {
                ReadParam();
                return;
            }
            int i = 7;
            int i2 = 8;
            int i3 = 0;
            if (view == this.paramSet) {
                ScanMode.runtime = Integer.valueOf(this.tvRun.getText().toString()).intValue();
                ReaderParameter GetInventoryPatameter = Reader.rrlib.GetInventoryPatameter();
                GetInventoryPatameter.Length = this.sptidlen.getSelectedItemPosition();
                GetInventoryPatameter.WordPtr = this.sptidaddr.getSelectedItemPosition();
                GetInventoryPatameter.QValue = this.spqvalue.getSelectedItemPosition();
                GetInventoryPatameter.ScanTime = this.sptime.getSelectedItemPosition();
                GetInventoryPatameter.IvtType = this.spType.getSelectedItemPosition();
                GetInventoryPatameter.Memory = this.spMem.getSelectedItemPosition() + 1;
                int selectedItemPosition = this.spsession.getSelectedItemPosition();
                if (selectedItemPosition == 4) {
                    selectedItemPosition = 255;
                }
                if (selectedItemPosition == 5) {
                    selectedItemPosition = 254;
                }
                if (selectedItemPosition == 6) {
                    selectedItemPosition = 253;
                }
                if (selectedItemPosition == 7) {
                    selectedItemPosition = 252;
                }
                if (selectedItemPosition == 8) {
                    selectedItemPosition = 251;
                }
                GetInventoryPatameter.Session = selectedItemPosition;
                GetInventoryPatameter.Interval = 0;
                Reader.rrlib.SetInventoryPatameter(GetInventoryPatameter);
                if (Reader.rrlib.ModuleType == 2) {
                    int selectedItemPosition2 = this.jgTime.getSelectedItemPosition();
                    int selectedItemPosition3 = this.spDwell.getSelectedItemPosition();
                    GetInventoryPatameter.Interval = 0;
                    Reader.rrlib.SetInventoryPatameter(GetInventoryPatameter);
                    Reader.rrlib.SetCfgParameter((byte) 0, (byte) 7, new byte[]{(byte) selectedItemPosition2, (byte) (selectedItemPosition3 + 2), 2}, 3);
                }
                Reader.writelog(getString(R.string.set_success), this.tvResult);
                return;
            }
            if (view == this.bSetting) {
                int selectedItemPosition4 = this.tvpowerdBm.getSelectedItemPosition();
                int selectedItemPosition5 = this.spBand.getSelectedItemPosition();
                int i4 = selectedItemPosition5 == 0 ? 1 : 0;
                if (selectedItemPosition5 == 1) {
                    i4 = 2;
                }
                if (selectedItemPosition5 == 2) {
                    i4 = 3;
                }
                if (selectedItemPosition5 == 3) {
                    i4 = 4;
                }
                if (selectedItemPosition5 != 4) {
                    i2 = i4;
                }
                if (selectedItemPosition5 != 5) {
                    i3 = i2;
                }
                int selectedItemPosition6 = this.spminFrm.getSelectedItemPosition();
                int selectedItemPosition7 = this.spmaxFrm.getSelectedItemPosition();
                String string = Reader.rrlib.SetRfPower((byte) selectedItemPosition4) != 0 ? getString(R.string.power_error) : android.device.sdk.BuildConfig.FLAVOR;
                if (Reader.rrlib.SetRegion((byte) i3, (byte) selectedItemPosition7, (byte) selectedItemPosition6) != 0) {
                    if (string == android.device.sdk.BuildConfig.FLAVOR) {
                        string = getString(R.string.frequent_error);
                    } else {
                        string = string + ",\r\n" + getString(R.string.frequent_error);
                    }
                }
                if (string != android.device.sdk.BuildConfig.FLAVOR) {
                    Reader.writelog(string, this.tvResult);
                    return;
                } else {
                    Reader.writelog(getString(R.string.set_success), this.tvResult);
                    return;
                }
            }
            if (view == this.setRange) {
                if (Reader.rrlib.SetCfgParameter((byte) 0, (byte) 16, new byte[]{0, 0, 0, (byte) this.spRange.getSelectedItemPosition()}, 4) == 0) {
                    Reader.writelog(getString(R.string.set_success), this.tvResult);
                    return;
                } else {
                    Reader.writelog(getString(R.string.set_failed), this.tvResult);
                    return;
                }
            }
            if (view == this.getRange) {
                getRangeControll();
                return;
            }
            if (view == this.btGetAntCheck) {
                ReadCheckAnt();
                return;
            }
            if (view == this.btSetAntCheck) {
                if (Reader.rrlib.SetCheckAnt((byte) this.spAntCheck.getSelectedItemPosition()) == 0) {
                    Reader.writelog(getString(R.string.set_success), this.tvResult);
                    return;
                } else {
                    Reader.writelog(getString(R.string.set_failed), this.tvResult);
                    return;
                }
            }
            if (view == this.bRead) {
                ReadInformation();
                return;
            }
            if (view == this.btSetBaud) {
                int selectedItemPosition8 = this.spbaudRate.getSelectedItemPosition();
                int i5 = 57600;
                if (selectedItemPosition8 != 0 && selectedItemPosition8 == 1) {
                    i5 = 115200;
                }
                if (Reader.rrlib.SetBaudRate(i5) == 0) {
                    Reader.writelog(getString(R.string.set_success), this.tvResult);
                    return;
                } else {
                    Reader.writelog(getString(R.string.set_failed), this.tvResult);
                    return;
                }
            }
            if (view == this.btSetFocus) {
                if (Reader.rrlib.SetCfgParameter((byte) 0, (byte) 8, new byte[]{(byte) this.spTagfocus.getSelectedItemPosition()}, 1) == 0) {
                    Reader.writelog(getString(R.string.set_success), this.tvResult);
                    return;
                } else {
                    Reader.writelog(getString(R.string.set_failed), this.tvResult);
                    return;
                }
            }
            if (view == this.btGetFocus) {
                ReadFocus();
                return;
            }
            if (view == this.btReadLoss) {
                byte[] bArr = new byte[4];
                byte[] bArr2 = new byte[1];
                if (this.curband == 4) {
                    bArr[0] = 0;
                    bArr[1] = 13;
                    bArr[2] = 51;
                    bArr[3] = 76;
                } else {
                    bArr[0] = 0;
                    bArr[1] = 13;
                    bArr[2] = -9;
                    bArr[3] = 50;
                }
                if (Reader.rrlib.MeasureReturnLoss(bArr, (byte) 0, bArr2) == 0) {
                    this.tvLoss.setText(((int) bArr2[0]) + android.device.sdk.BuildConfig.FLAVOR);
                    Reader.writelog(getString(R.string.get_success), this.tvResult);
                    return;
                }
                Reader.writelog(getString(R.string.get_failed), this.tvResult);
                return;
            }
            if (view == this.btSetPro) {
                int selectedItemPosition9 = this.spProfilr.getSelectedItemPosition();
                if (this.ModuleType == 2) {
                    switch (selectedItemPosition9) {
                        case 0:
                            i = 11;
                            break;
                        case 1:
                            i = 1;
                            break;
                        case 2:
                            i = 15;
                            break;
                        case 3:
                            i = 12;
                            break;
                        case 4:
                            i = 3;
                            break;
                        case 5:
                        default:
                            i = 5;
                            break;
                        case 6:
                            break;
                        case 7:
                            i = 13;
                            break;
                        case 8:
                            i = 50;
                            break;
                        case 9:
                            i = 51;
                            break;
                        case 10:
                            i = 52;
                            break;
                        case 11:
                            i = 53;
                            break;
                    }
                    selectedItemPosition9 = i;
                }
                if (Reader.rrlib.SetProfile((byte) selectedItemPosition9) == 0) {
                    Reader.writelog(getString(R.string.set_success), this.tvResult);
                } else {
                    Reader.writelog(getString(R.string.set_failed), this.tvResult);
                }
            }
        } catch (Exception unused) {
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void SetFre(int m) {
        if (m == 1) {
            this.strmaxFrm = new String[20];
            this.strminFrm = new String[20];
            int i = 0;
            for (int i2 = 20; i < i2; i2 = 20) {
                String str = String.valueOf((float) ((i * 0.25d) + 920.125d)) + "MHz";
                this.strminFrm[i] = str;
                this.strmaxFrm[i] = str;
                i++;
            }
            this.spmaxFrm = (Spinner) findViewById(R.id.max_spinner);
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, this.strmaxFrm);
            this.spada_maxFrm = arrayAdapter;
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            this.spmaxFrm.setAdapter((SpinnerAdapter) this.spada_maxFrm);
            this.spmaxFrm.setSelection(19, false);
            this.spminFrm = (Spinner) findViewById(R.id.min_spinner);
            ArrayAdapter<String> arrayAdapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, this.strminFrm);
            this.spada_minFrm = arrayAdapter2;
            arrayAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            this.spminFrm.setAdapter((SpinnerAdapter) this.spada_minFrm);
            this.spminFrm.setSelection(0, false);
            return;
        }
        if (m == 2) {
            this.strmaxFrm = new String[50];
            this.strminFrm = new String[50];
            for (int i3 = 0; i3 < 50; i3++) {
                String str2 = String.valueOf((float) ((i3 * 0.5d) + 902.75d)) + "MHz";
                this.strminFrm[i3] = str2;
                this.strmaxFrm[i3] = str2;
            }
            this.spmaxFrm = (Spinner) findViewById(R.id.max_spinner);
            ArrayAdapter<String> arrayAdapter3 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, this.strmaxFrm);
            this.spada_maxFrm = arrayAdapter3;
            arrayAdapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            this.spmaxFrm.setAdapter((SpinnerAdapter) this.spada_maxFrm);
            this.spmaxFrm.setSelection(49, false);
            this.spminFrm = (Spinner) findViewById(R.id.min_spinner);
            ArrayAdapter<String> arrayAdapter4 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, this.strminFrm);
            this.spada_minFrm = arrayAdapter4;
            arrayAdapter4.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            this.spminFrm.setAdapter((SpinnerAdapter) this.spada_minFrm);
            this.spminFrm.setSelection(0, false);
            return;
        }
        if (m == 3) {
            this.strmaxFrm = new String[32];
            this.strminFrm = new String[32];
            for (int i4 = 0; i4 < 32; i4++) {
                String str3 = String.valueOf((float) ((i4 * 0.2d) + 917.1d)) + "MHz";
                this.strminFrm[i4] = str3;
                this.strmaxFrm[i4] = str3;
            }
            this.spmaxFrm = (Spinner) findViewById(R.id.max_spinner);
            ArrayAdapter<String> arrayAdapter5 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, this.strmaxFrm);
            this.spada_maxFrm = arrayAdapter5;
            arrayAdapter5.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            this.spmaxFrm.setAdapter((SpinnerAdapter) this.spada_maxFrm);
            this.spmaxFrm.setSelection(31, false);
            this.spminFrm = (Spinner) findViewById(R.id.min_spinner);
            ArrayAdapter<String> arrayAdapter6 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, this.strminFrm);
            this.spada_minFrm = arrayAdapter6;
            arrayAdapter6.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            this.spminFrm.setAdapter((SpinnerAdapter) this.spada_minFrm);
            this.spminFrm.setSelection(0, false);
            return;
        }
        if (m == 4) {
            this.strmaxFrm = new String[15];
            this.strminFrm = new String[15];
            for (int i5 = 0; i5 < 15; i5++) {
                String str4 = String.valueOf((float) ((i5 * 0.2d) + 865.1d)) + "MHz";
                this.strminFrm[i5] = str4;
                this.strmaxFrm[i5] = str4;
            }
            this.spmaxFrm = (Spinner) findViewById(R.id.max_spinner);
            ArrayAdapter<String> arrayAdapter7 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, this.strmaxFrm);
            this.spada_maxFrm = arrayAdapter7;
            arrayAdapter7.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            this.spmaxFrm.setAdapter((SpinnerAdapter) this.spada_maxFrm);
            this.spmaxFrm.setSelection(14, false);
            this.spminFrm = (Spinner) findViewById(R.id.min_spinner);
            ArrayAdapter<String> arrayAdapter8 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, this.strminFrm);
            this.spada_minFrm = arrayAdapter8;
            arrayAdapter8.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            this.spminFrm.setAdapter((SpinnerAdapter) this.spada_minFrm);
            this.spminFrm.setSelection(0, false);
            return;
        }
        if (m == 8) {
            this.strmaxFrm = new String[20];
            this.strminFrm = new String[20];
            int i6 = 0;
            for (int i7 = 20; i6 < i7; i7 = 20) {
                String str5 = String.valueOf((float) ((i6 * 0.25d) + 840.125d)) + "MHz";
                this.strminFrm[i6] = str5;
                this.strmaxFrm[i6] = str5;
                i6++;
            }
            this.spmaxFrm = (Spinner) findViewById(R.id.max_spinner);
            ArrayAdapter<String> arrayAdapter9 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, this.strmaxFrm);
            this.spada_maxFrm = arrayAdapter9;
            arrayAdapter9.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            this.spmaxFrm.setAdapter((SpinnerAdapter) this.spada_maxFrm);
            this.spmaxFrm.setSelection(19, false);
            this.spminFrm = (Spinner) findViewById(R.id.min_spinner);
            ArrayAdapter<String> arrayAdapter10 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, this.strminFrm);
            this.spada_minFrm = arrayAdapter10;
            arrayAdapter10.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            this.spminFrm.setAdapter((SpinnerAdapter) this.spada_minFrm);
            this.spminFrm.setSelection(0, false);
            return;
        }
        if (m == 0) {
            this.strmaxFrm = new String[61];
            this.strminFrm = new String[61];
            for (int i8 = 0; i8 < 61; i8++) {
                String str6 = String.valueOf((i8 * 2) + 840) + "MHz";
                this.strminFrm[i8] = str6;
                this.strmaxFrm[i8] = str6;
            }
            this.spmaxFrm = (Spinner) findViewById(R.id.max_spinner);
            ArrayAdapter<String> arrayAdapter11 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, this.strmaxFrm);
            this.spada_maxFrm = arrayAdapter11;
            arrayAdapter11.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            this.spmaxFrm.setAdapter((SpinnerAdapter) this.spada_maxFrm);
            this.spmaxFrm.setSelection(60, false);
            this.spminFrm = (Spinner) findViewById(R.id.min_spinner);
            ArrayAdapter<String> arrayAdapter12 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, this.strminFrm);
            this.spada_minFrm = arrayAdapter12;
            arrayAdapter12.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            this.spminFrm.setAdapter((SpinnerAdapter) this.spada_minFrm);
            this.spminFrm.setSelection(0, false);
        }
    }
}
