package com.UHF.scanlable;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

/* loaded from: classes.dex */
public class ReadWriteActivity extends Activity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private static final int CHECK_R_6B = 1;
    private static final int CHECK_R_6C = 3;
    private static final int CHECK_W_6B = 0;
    private static final int CHECK_W_6C = 2;
    EditText b_addr;
    EditText b_id;
    EditText b_num;
    Button btKill;
    Button btLed;
    Button btLock;
    Button btWriteEPC;
    EditText c_kwd;
    EditText c_len;
    Spinner c_mem;
    EditText c_ptr;
    EditText c_pwd;
    EditText c_wordPtr;
    EditText content;
    EditText edENum0;
    TextView epcText;
    Spinner lock_mem;
    Spinner lock_type;
    private int mode;
    Button rButton;
    EditText readContent;
    int selectedEd = 3;
    int selectedWhenPause = 0;
    TextView tvResult;
    CheckBox u9lock;
    Button wButton;

    @Override // android.widget.AdapterView.OnItemSelectedListener
    public void onNothingSelected(AdapterView<?> arg0) {
    }

    @Override // android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(128, 128);
        setContentView(R.layout.rw_6c);
        initView();
    }

    @Override // android.app.Activity
    protected void onResume() {
        this.epcText.setText(ScanMode.epc);
        super.onResume();
    }

    @Override // android.app.Activity
    protected void onPause() {
        super.onPause();
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initView() {
        this.epcText = (TextView) findViewById(R.id.epc_text);
        this.tvResult = (TextView) findViewById(R.id.rw_result);
        this.c_mem = (Spinner) findViewById(R.id.mem_spinner);
        ArrayAdapter<CharSequence> createFromResource = ArrayAdapter.createFromResource(this, R.array.men_select, android.R.layout.simple_spinner_item);
        createFromResource.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.c_mem.setAdapter((SpinnerAdapter) createFromResource);
        this.c_mem.setSelection(3, true);
        this.c_mem.setOnItemSelectedListener(this);
        this.lock_mem = (Spinner) findViewById(R.id.lockmem_spinner);
        ArrayAdapter<CharSequence> createFromResource2 = ArrayAdapter.createFromResource(this, R.array.arrayLockMem, android.R.layout.simple_spinner_item);
        createFromResource2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.lock_mem.setAdapter((SpinnerAdapter) createFromResource2);
        this.lock_mem.setSelection(4, true);
        this.lock_type = (Spinner) findViewById(R.id.locktype_spinner);
        ArrayAdapter<CharSequence> createFromResource3 = ArrayAdapter.createFromResource(this, R.array.arrayLock, android.R.layout.simple_spinner_item);
        createFromResource3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.lock_type.setAdapter((SpinnerAdapter) createFromResource3);
        this.lock_type.setSelection(2, true);
        this.u9lock = (CheckBox) findViewById(R.id.checku9);
        EditText editText = (EditText) findViewById(R.id.et_wordptr);
        this.c_wordPtr = editText;
        editText.setText("0");
        EditText editText2 = (EditText) findViewById(R.id.et_length);
        this.c_len = editText2;
        editText2.setText("6");
        EditText editText3 = (EditText) findViewById(R.id.et_pwd);
        this.c_pwd = editText3;
        editText3.setText("00000000");
        this.c_kwd = (EditText) findViewById(R.id.et_kwd);
        this.content = (EditText) findViewById(R.id.et_content_6c);
        this.readContent = (EditText) findViewById(R.id.et_read_6c);
        this.rButton = (Button) findViewById(R.id.button_read_6c);
        this.wButton = (Button) findViewById(R.id.button_write_6c);
        this.btWriteEPC = (Button) findViewById(R.id.button_write_epc);
        this.btKill = (Button) findViewById(R.id.button_kill);
        this.btLock = (Button) findViewById(R.id.button_lock);
        this.btLed = (Button) findViewById(R.id.button_led);
        this.rButton.setOnClickListener(this);
        this.wButton.setOnClickListener(this);
        this.btWriteEPC.setOnClickListener(this);
        this.btKill.setOnClickListener(this);
        this.btLock.setOnClickListener(this);
        this.btLed.setOnClickListener(this);
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        byte b;
        byte b2;
        Button button = this.wButton;
        String str = android.device.sdk.BuildConfig.FLAVOR;
        if (view == button) {
            if (checkContent(2)) {
                try {
                    if (this.epcText.getText() != null) {
                        str = this.epcText.getText().toString();
                    }
                    String str2 = str;
                    if (this.content.getText() == null) {
                        return;
                    }
                    if (Reader.rrlib.WriteData_G2(this.content.getText().toString(), str2, (byte) this.c_mem.getSelectedItemPosition(), (byte) Integer.valueOf(this.c_wordPtr.getText().toString()).intValue(), this.c_pwd.getText().toString()) != 0) {
                        Reader.writelog(getString(R.string.write_failed), this.tvResult);
                        return;
                    } else {
                        Reader.writelog(getString(R.string.write_success), this.tvResult);
                        return;
                    }
                } catch (Exception unused) {
                    Reader.writelog(getString(R.string.write_failed), this.tvResult);
                    return;
                }
            }
            return;
        }
        if (view == this.rButton) {
            if (checkContent(3)) {
                try {
                    String ReadData_G2 = Reader.rrlib.ReadData_G2(this.epcText.getText() != null ? this.epcText.getText().toString() : android.device.sdk.BuildConfig.FLAVOR, (byte) this.c_mem.getSelectedItemPosition(), Integer.valueOf(this.c_wordPtr.getText().toString()).intValue(), (byte) Integer.valueOf(this.c_len.getText().toString()).intValue(), this.c_pwd.getText().toString());
                    if (ReadData_G2 == null) {
                        this.readContent.setText(android.device.sdk.BuildConfig.FLAVOR);
                        Reader.writelog(getString(R.string.get_failed), this.tvResult);
                        return;
                    } else {
                        this.readContent.setText(ReadData_G2);
                        Reader.writelog(getString(R.string.get_success), this.tvResult);
                        return;
                    }
                } catch (Exception unused2) {
                    Reader.writelog(getString(R.string.get_failed), this.tvResult);
                    return;
                }
            }
            return;
        }
        if (view == this.btWriteEPC) {
            if (checkContent(2)) {
                try {
                    if (Reader.rrlib.WriteEPC_G2(this.content.getText().toString(), this.c_pwd.getText().toString()) != 0) {
                        Reader.writelog(getString(R.string.write_failed), this.tvResult);
                    } else {
                        Reader.writelog(getString(R.string.write_success), this.tvResult);
                    }
                    return;
                } catch (Exception unused3) {
                    Reader.writelog(getString(R.string.write_failed), this.tvResult);
                    return;
                }
            }
            return;
        }
        if (view == this.btKill) {
            try {
                String obj = this.c_kwd.getText().toString();
                if (obj != null && obj.length() == 8) {
                    byte[] hexStringToBytes = Util.hexStringToBytes(obj);
                    if (this.epcText.getText() != null) {
                        str = this.epcText.getText().toString();
                    }
                    if (Reader.rrlib.Kill_G2((byte) (str.length() / 4), null, hexStringToBytes, new byte[1]) != 0) {
                        Reader.writelog(getString(R.string.kill_failed), this.tvResult);
                        return;
                    } else {
                        Reader.writelog(getString(R.string.kill_success), this.tvResult);
                        return;
                    }
                }
                return;
            } catch (Exception unused4) {
                Reader.writelog(getString(R.string.kill_failed), this.tvResult);
                return;
            }
        }
        if (view == this.btLock) {
            try {
                byte[] hexStringToBytes2 = Util.hexStringToBytes(this.c_pwd.getText().toString());
                if (this.epcText.getText() != null) {
                    str = this.epcText.getText().toString();
                }
                byte length = (byte) (str.length() / 4);
                byte[] bArr = new byte[1];
                byte selectedItemPosition = (byte) this.lock_mem.getSelectedItemPosition();
                byte selectedItemPosition2 = (byte) this.lock_type.getSelectedItemPosition();
                if (this.u9lock.isChecked()) {
                    b = -1;
                    b2 = -1;
                } else {
                    b = selectedItemPosition;
                    b2 = selectedItemPosition2;
                }
                if (Reader.rrlib.Lock_G2(length, null, b, b2, hexStringToBytes2, bArr) != 0) {
                    Reader.writelog(getString(R.string.lock_failed), this.tvResult);
                    return;
                } else {
                    Reader.writelog(getString(R.string.lock_success), this.tvResult);
                    return;
                }
            } catch (Exception unused5) {
                Reader.writelog(getString(R.string.lock_failed), this.tvResult);
                return;
            }
        }
        if (view == this.btLed) {
            int i = 48;
            try {
                String obj2 = this.c_pwd.getText().toString();
                if (this.epcText.getText() != null) {
                    str = this.epcText.getText().toString();
                }
                for (int i2 = 0; i2 < 5 && (i = Reader.rrlib.LedOn_kx2005x(str, obj2, (byte) 7)) != 0; i2++) {
                }
                if (i != 0) {
                    Reader.writelog(getString(R.string.Optfailed), this.tvResult);
                } else {
                    Reader.writelog(getString(R.string.Optsuccess), this.tvResult);
                }
            } catch (Exception unused6) {
                Reader.writelog(getString(R.string.Optfailed), this.tvResult);
            }
        }
    }

    @Override // android.widget.AdapterView.OnItemSelectedListener
    public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
        this.selectedEd = position;
    }

    private boolean checkContent(int check) {
        if (check != 2) {
            if (check != 3) {
                return true;
            }
        } else {
            if (Util.isEtEmpty(this.content)) {
                return Util.showWarning(this, R.string.content_empty_warning);
            }
            if (this.content.getText().toString().length() % 4 != 0) {
                return Util.showWarning(this, R.string.length_content_warning);
            }
            if (!Util.isLenLegal(this.content)) {
                return Util.showWarning(this, R.string.str_lenght_odd_warning);
            }
            if (!Util.isLenLegal(this.c_pwd)) {
                return Util.showWarning(this, R.string.str_lenght_odd_warning);
            }
        }
        if (Util.isEtEmpty(this.c_wordPtr)) {
            return Util.showWarning(this, R.string.wordptr_empty_warning);
        }
        if (Util.isEtEmpty(this.c_len)) {
            return Util.showWarning(this, R.string.length_empty_warning);
        }
        if (Util.isEtEmpty(this.c_pwd)) {
            return Util.showWarning(this, R.string.pwd_empty_warning);
        }
        if (Util.isLenLegal(this.c_pwd)) {
            return true;
        }
        return Util.showWarning(this, R.string.str_lenght_odd_warning);
    }

    public String bytesToHexString(byte[] src, int offset, int length) {
        StringBuilder sb = new StringBuilder(android.device.sdk.BuildConfig.FLAVOR);
        if (src == null || src.length <= 0) {
            return null;
        }
        while (offset < length) {
            String hexString = Integer.toHexString(src[offset] & 255);
            if (hexString.length() == 1) {
                sb.append(0);
            }
            sb.append(hexString);
            offset++;
        }
        return sb.toString();
    }

    public byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals(android.device.sdk.BuildConfig.FLAVOR)) {
            return null;
        }
        String upperCase = hexString.toUpperCase();
        int length = upperCase.length() / 2;
        char[] charArray = upperCase.toCharArray();
        byte[] bArr = new byte[length];
        for (int i = 0; i < length; i++) {
            int i2 = i * 2;
            bArr[i] = (byte) (charToByte(charArray[i2 + 1]) | (charToByte(charArray[i2]) << 4));
        }
        return bArr;
    }

    private byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }
}
