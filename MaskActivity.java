package com.UHF.scanlable;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.rfid.trans.MaskClass;

/* loaded from: classes.dex */
public class MaskActivity extends Activity implements View.OnClickListener {
    private Button btAdd;
    private Button btClear;
    private Spinner spMem;
    private ArrayAdapter<String> spada_Mem;
    private EditText tvAddr;
    private EditText tvData;
    private EditText tvLen;
    TextView txt_mask;
    private String[] strMem = new String[3];
    String MaskData = android.device.sdk.BuildConfig.FLAVOR;

    @Override // android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mask);
        this.tvAddr = (EditText) findViewById(R.id.et_addr);
        this.tvLen = (EditText) findViewById(R.id.et_len);
        this.tvData = (EditText) findViewById(R.id.et_data);
        this.txt_mask = (TextView) findViewById(R.id.txt_mask);
        String[] strArr = this.strMem;
        strArr[0] = "EPC";
        strArr[1] = "TID";
        strArr[2] = "USER";
        this.spMem = (Spinner) findViewById(R.id.mem_spinner);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, this.strMem);
        this.spada_Mem = arrayAdapter;
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.spMem.setAdapter((SpinnerAdapter) this.spada_Mem);
        this.spMem.setSelection(0, false);
        this.btAdd = (Button) findViewById(R.id.button_add);
        this.btClear = (Button) findViewById(R.id.button_clear);
        this.btAdd.setOnClickListener(this);
        this.btClear.setOnClickListener(this);
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View v) {
        if (v == this.btAdd) {
            if (this.tvAddr.getText() == null || this.tvLen.getText() == null || this.tvData.getText() == null) {
                return;
            }
            try {
                int intValue = Integer.valueOf(this.tvAddr.getText().toString()).intValue();
                int selectedItemPosition = this.spMem.getSelectedItemPosition() + 1;
                int intValue2 = Integer.valueOf(this.tvLen.getText().toString()).intValue();
                String obj = this.tvData.getText().toString();
                if (obj.length() % 2 != 0) {
                    obj = obj + "0";
                }
                if (obj.length() / 2 < (intValue2 + 7) / 8) {
                    Toast.makeText(getApplicationContext(), getString(R.string.strfailed), 0).show();
                    return;
                }
                MaskClass maskClass = new MaskClass();
                maskClass.MaskData = Util.hexStringToBytes(obj);
                maskClass.MaskAdr[0] = (byte) (intValue >> 8);
                maskClass.MaskAdr[1] = (byte) intValue;
                maskClass.MaskLen = (byte) intValue2;
                maskClass.MaskMem = (byte) selectedItemPosition;
                Reader.rrlib.AddMaskList(maskClass);
                String str = this.MaskData + (selectedItemPosition + "," + intValue + "," + intValue2 + "," + obj) + "\r\n";
                this.MaskData = str;
                this.txt_mask.setText(str);
                Toast.makeText(getApplicationContext(), getString(R.string.strsuccess), 0).show();
                return;
            } catch (Exception unused) {
                Toast.makeText(getApplicationContext(), getString(R.string.strfailed), 0).show();
                return;
            }
        }
        if (v == this.btClear) {
            this.MaskData = android.device.sdk.BuildConfig.FLAVOR;
            this.txt_mask.setText(android.device.sdk.BuildConfig.FLAVOR);
            Reader.rrlib.ClearMaskList();
            Toast.makeText(getApplicationContext(), "已清空掩码列表", 0).show();
        }
    }
}
