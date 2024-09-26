package com.UHF.scanlable;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.device.DeviceManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.rfid.trans.MaskClass;
import com.rfid.trans.ReadTag;
import com.rfid.trans.TagCallback;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/* loaded from: classes.dex */
public class ScanMode extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener {
    private static final int MSG_UPDATE_LISTVIEW = 0;
    private static final int MSG_UPDATE_SPEED = 2;
    private static final int MSG_UPDATE_STOP = 3;
    private static final int MSG_UPDATE_TIME = 1;
    public static String epc;
    Button BtClear;
    Button BtInventory;
    Button Btfilter;
    Button Btimport;
    public long CardNumber;
    ListView LvTags;
    RadioButton RbInventoryLoop;
    RadioButton RbInventorySingle;
    RadioGroup RgInventory;
    SimpleAdapter adapter;
    public long beginTime;
    CheckBox chkled;
    Handler handler;
    private LinearLayout llContinuous;
    LinearLayout lyoutled;
    private HashMap<String, String> map;
    PopupWindow popFilter;
    private Spinner spfactory;
    private ArrayList<HashMap<String, String>> tagList;
    private Timer timer;
    TextView tv_alltag;
    TextView tv_count;
    TextView tv_speed;
    TextView tv_time;
    public static List<String> mlist = new ArrayList();
    public static List<String> ledlist = new ArrayList();
    public static int runtime = 0;
    private int inventoryFlag = 1;
    String[] items = null;
    boolean[] chk = null;
    public boolean isStopThread = false;
    MsgCallback callback = new MsgCallback();
    public long lastTime = 0;
    public int lastCount = 0;
    public boolean keyPress = false;
    View.OnCreateContextMenuListener lvjzwOnCreateContextMenuListener = new View.OnCreateContextMenuListener() { // from class: com.UHF.scanlable.ScanMode.4
        @Override // android.view.View.OnCreateContextMenuListener
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle(ScanMode.this.getString(R.string.strtagoperate));
            menu.add(0, 1, 0, ScanMode.this.getString(R.string.strreadandwrite));
            menu.add(0, 2, 1, ScanMode.this.getString(R.string.strfindtag));
        }
    };

    @Override // android.widget.AdapterView.OnItemClickListener
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
    }

    /* loaded from: classes.dex */
    public class FilterLed {
        String epc;
        boolean isChedk;

        public FilterLed() {
        }
    }

    @Override // android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(128, 128);
        try {
            setContentView(R.layout.query);
            CheckBox checkBox = (CheckBox) findViewById(R.id.chkLed);
            this.chkled = checkBox;
            checkBox.setOnClickListener(this);
            LinearLayout linearLayout = (LinearLayout) findViewById(R.id.layoutled);
            this.lyoutled = linearLayout;
            linearLayout.setVisibility(8);
            this.spfactory = (Spinner) findViewById(R.id.spfactory);
            ArrayAdapter<CharSequence> createFromResource = ArrayAdapter.createFromResource(this, R.array.arrayfactory, android.R.layout.simple_spinner_item);
            createFromResource.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            this.spfactory.setAdapter((SpinnerAdapter) createFromResource);
            this.spfactory.setSelection(0, false);
            this.tagList = new ArrayList<>();
            this.BtClear = (Button) findViewById(R.id.BtClear);
            this.Btfilter = (Button) findViewById(R.id.Btfilter);
            this.Btimport = (Button) findViewById(R.id.BtImport);
            this.tv_count = (TextView) findViewById(R.id.tv_count);
            this.tv_time = (TextView) findViewById(R.id.tv_times);
            this.tv_alltag = (TextView) findViewById(R.id.tv_alltag);
            this.tv_speed = (TextView) findViewById(R.id.tv_tagspeed);
            this.RgInventory = (RadioGroup) findViewById(R.id.RgInventory);
            this.RbInventorySingle = (RadioButton) findViewById(R.id.RbInventorySingle);
            this.RbInventoryLoop = (RadioButton) findViewById(R.id.RbInventoryLoop);
            this.BtInventory = (Button) findViewById(R.id.BtInventory);
            ListView listView = (ListView) findViewById(R.id.LvTags);
            this.LvTags = listView;
            listView.setOnCreateContextMenuListener(this.lvjzwOnCreateContextMenuListener);
            this.llContinuous = (LinearLayout) findViewById(R.id.llContinuous);
            this.adapter = new SimpleAdapter(this, this.tagList, R.layout.listtag_items, new String[]{"tagUii", "tagLen", "tagCount", "tagRssi"}, new int[]{R.id.TvTagUii, R.id.TvTagLen, R.id.TvTagCount, R.id.TvTagRssi});
            this.Btfilter.setOnClickListener(this);
            this.BtClear.setOnClickListener(this);
            this.Btimport.setOnClickListener(this);
            this.RgInventory.setOnCheckedChangeListener(new RgInventoryCheckedListener());
            this.BtInventory.setOnClickListener(this);
            Reader.rrlib.SetCallBack(this.callback);
            this.LvTags.setAdapter((ListAdapter) this.adapter);
            clearData();
            Log.i("MY", "UHFReadTagFragment.EtCountOfTags=" + ((Object) this.tv_count.getText()));
            this.handler = new Handler() { // from class: com.UHF.scanlable.ScanMode.1
                @Override // android.os.Handler
                public void handleMessage(Message msg) {
                    try {
                        int i = msg.what;
                        if (i == 0) {
                            String[] split = (msg.obj + android.device.sdk.BuildConfig.FLAVOR).split(",");
                            if (split.length == 2) {
                                ScanMode.this.addEPCToList(split[0], split[1]);
                                return;
                            }
                            ScanMode.this.addEPCToList(split[0] + "," + split[1], split[2]);
                            return;
                        }
                        if (i != 1) {
                            if (i == 2) {
                                ScanMode.this.tv_speed.setText(msg.obj + android.device.sdk.BuildConfig.FLAVOR);
                                return;
                            }
                            if (i != 3) {
                                return;
                            }
                            if (ScanMode.this.timer != null) {
                                ScanMode.this.timer.cancel();
                                ScanMode.this.timer = null;
                                ScanMode.this.BtInventory.setText(ScanMode.this.getString(R.string.btStoping));
                            }
                            ScanMode.this.setViewEnabled(true);
                            ScanMode.this.BtInventory.setText(ScanMode.this.getString(R.string.btInventory));
                            if (ScanMode.ledlist.size() == 0) {
                                ScanMode scanMode = ScanMode.this;
                                scanMode.items = new String[scanMode.tagList.size()];
                                ScanMode scanMode2 = ScanMode.this;
                                scanMode2.chk = new boolean[scanMode2.tagList.size()];
                                for (int i2 = 0; i2 < ScanMode.this.tagList.size(); i2++) {
                                    ScanMode.this.items[i2] = (String) ((HashMap) ScanMode.this.tagList.get(i2)).get("tagUii");
                                    ScanMode.this.chk[i2] = false;
                                }
                                return;
                            }
                            return;
                        }
                        long intValue = Integer.valueOf(msg.obj + android.device.sdk.BuildConfig.FLAVOR).intValue();
                        long j = intValue / 3600000;
                        long j2 = j * 60 * 60;
                        long j3 = ((intValue / 1000) - j2) / 60;
                        long j4 = ((intValue / 1000) - j2) - (60 * j3);
                        String valueOf = String.valueOf(j);
                        if (valueOf.length() < 2) {
                            valueOf = "0" + valueOf;
                        }
                        String valueOf2 = String.valueOf(j3);
                        if (valueOf2.length() < 2) {
                            valueOf2 = "0" + valueOf2;
                        }
                        String valueOf3 = String.valueOf(j4);
                        if (valueOf3.length() < 2) {
                            valueOf3 = "0" + valueOf3;
                        }
                        ScanMode.this.tv_time.setText(valueOf + ":" + valueOf2 + ":" + valueOf3);
                    } catch (Exception e) {
                        e.toString();
                    }
                }
            };
        } catch (Exception unused) {
        }
    }

    /* loaded from: classes.dex */
    public class RgInventoryCheckedListener implements RadioGroup.OnCheckedChangeListener {
        public RgInventoryCheckedListener() {
        }

        @Override // android.widget.RadioGroup.OnCheckedChangeListener
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId == ScanMode.this.RbInventorySingle.getId()) {
                ScanMode.this.inventoryFlag = 0;
            } else if (checkedId == ScanMode.this.RbInventoryLoop.getId()) {
                ScanMode.this.inventoryFlag = 1;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setViewEnabled(boolean enabled) {
        this.RbInventorySingle.setEnabled(enabled);
        this.RbInventoryLoop.setEnabled(enabled);
        this.BtClear.setEnabled(enabled);
        this.chkled.setEnabled(enabled);
        if (enabled) {
            this.Btfilter.setEnabled(enabled);
            this.BtInventory.setEnabled(enabled);
        }
    }

    public int checkIsExist(String strEPC) {
        if (strEPC == null || strEPC.length() == 0) {
            return -1;
        }
        for (int i = 0; i < this.tagList.size(); i++) {
            new HashMap();
            if (strEPC.equals(this.tagList.get(i).get("tagUii"))) {
                return i;
            }
        }
        return -1;
    }

    private void clearData() {
        this.tv_count.setText("0");
        this.tv_time.setText("00:00:00");
        this.tv_alltag.setText("0");
        this.tv_speed.setText("0");
        this.tagList.clear();
        mlist.clear();
        this.CardNumber = 0L;
        this.items = null;
        this.chk = null;
        ledlist.clear();
        Log.i("MY", "tagList.size " + this.tagList.size());
        this.adapter.notifyDataSetChanged();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void addEPCToList(String rfid, String rssi) {
        String str;
        if (TextUtils.isEmpty(rfid)) {
            return;
        }
        String[] split = rfid.split(",");
        if (split.length == 1) {
            str = split[0];
        } else {
            str = "EPC:" + split[0] + "\r\nMem:" + split[1];
        }
        int checkIsExist = checkIsExist(str);
        HashMap<String, String> hashMap = new HashMap<>();
        this.map = hashMap;
        hashMap.put("tagUii", str);
        this.map.put("tagCount", String.valueOf(1));
        this.map.put("tagRssi", rssi);
        this.CardNumber++;
        if (checkIsExist == -1) {
            this.tagList.add(this.map);
            this.LvTags.setAdapter((ListAdapter) this.adapter);
            this.tv_count.setText(android.device.sdk.BuildConfig.FLAVOR + this.adapter.getCount());
            mlist.add(split[0]);
        } else {
            this.map.put("tagCount", String.valueOf(Integer.parseInt(this.tagList.get(checkIsExist).get("tagCount"), 10) + 1));
            this.tagList.set(checkIsExist, this.map);
        }
        this.tv_alltag.setText(String.valueOf(this.CardNumber));
        this.adapter.notifyDataSetChanged();
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
        super.onResume();
        setOpenScan523(false);
        this.isStopThread = false;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View arg0) {
        try {
            if (arg0 == this.BtInventory) {
                readTag();
                return;
            }
            if (arg0 == this.BtClear) {
                clearData();
                return;
            }
            CheckBox checkBox = this.chkled;
            if (arg0 == checkBox) {
                if (checkBox.isChecked()) {
                    this.lyoutled.setVisibility(0);
                    return;
                } else {
                    this.lyoutled.setVisibility(8);
                    return;
                }
            }
            if (arg0 == this.Btfilter) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.strselecttag));
                builder.setPositiveButton(getString(R.string.strcancle), (DialogInterface.OnClickListener) null);
                builder.setPositiveButton(getString(R.string.strok), (DialogInterface.OnClickListener) null);
                builder.setMultiChoiceItems(this.items, this.chk, new DialogInterface.OnMultiChoiceClickListener() { // from class: com.UHF.scanlable.ScanMode.2
                    @Override // android.content.DialogInterface.OnMultiChoiceClickListener
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        FilterLed filterLed = new FilterLed();
                        filterLed.epc = ScanMode.this.items[which];
                        filterLed.isChedk = isChecked;
                        if (isChecked) {
                            if (ScanMode.ledlist.indexOf(filterLed.epc) == -1) {
                                ScanMode.ledlist.add(filterLed.epc);
                            }
                        } else if (ScanMode.ledlist.indexOf(filterLed.epc) != -1) {
                            ScanMode.ledlist.remove(filterLed.epc);
                        }
                        ScanMode.this.chk[which] = isChecked;
                    }
                }).create();
                builder.show();
                return;
            }
            if (arg0 == this.Btimport) {
                if (this.tagList.size() == 0) {
                    Toast.makeText(getApplicationContext(), getString(R.string.msgNodata), 0).show();
                } else if (FileImport.daochu(android.device.sdk.BuildConfig.FLAVOR, this.tagList)) {
                    Toast.makeText(getApplicationContext(), getString(R.string.msgImportsuc), 0).show();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.msgImportfailed), 0).show();
                }
            }
        } catch (Exception unused) {
            stopInventory();
        }
    }

    private void readTag() {
        int StartRead;
        epc = android.device.sdk.BuildConfig.FLAVOR;
        if (this.BtInventory.getText().equals(getString(R.string.btInventory))) {
            int i = this.inventoryFlag;
            if (i == 0) {
                new ArrayList();
                Reader.rrlib.ScanRfid();
                return;
            }
            if (i != 1) {
                return;
            }
            if (this.chkled.isChecked()) {
                int selectedItemPosition = this.spfactory.getSelectedItemPosition();
                ArrayList arrayList = null;
                if (ledlist.size() > 0) {
                    arrayList = new ArrayList();
                    for (int i2 = 0; i2 < ledlist.size(); i2++) {
                        MaskClass maskClass = new MaskClass();
                        maskClass.MaskAdr[0] = 0;
                        maskClass.MaskAdr[1] = 32;
                        maskClass.MaskMem = (byte) 1;
                        maskClass.MaskLen = (byte) (ledlist.get(i2).length() * 4);
                        maskClass.MaskData = Util.hexStringToBytes(ledlist.get(i2));
                        arrayList.add(maskClass);
                    }
                }
                StartRead = Reader.rrlib.StartInventoryLed(selectedItemPosition, arrayList);
            } else {
                StartRead = Reader.rrlib.StartRead();
            }
            if (StartRead == 0) {
                this.Btfilter.setEnabled(false);
                this.lastTime = System.currentTimeMillis();
                this.lastCount = 0;
                this.BtInventory.setText(getString(R.string.title_stop_Inventory));
                setViewEnabled(false);
                if (this.timer == null) {
                    this.beginTime = System.currentTimeMillis();
                    Timer timer = new Timer();
                    this.timer = timer;
                    timer.schedule(new TimerTask() { // from class: com.UHF.scanlable.ScanMode.3
                        @Override // java.util.TimerTask, java.lang.Runnable
                        public void run() {
                            long currentTimeMillis = System.currentTimeMillis() - ScanMode.this.beginTime;
                            Message obtainMessage = ScanMode.this.handler.obtainMessage();
                            obtainMessage.what = 1;
                            obtainMessage.obj = String.valueOf(currentTimeMillis);
                            ScanMode.this.handler.sendMessage(obtainMessage);
                            if (ScanMode.runtime == 0 || currentTimeMillis <= ScanMode.runtime * 1000) {
                                return;
                            }
                            ScanMode.this.stopInventory();
                        }
                    }, 0L, 200L);
                    return;
                }
                return;
            }
            return;
        }
        stopInventory();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void stopInventory() {
        if (this.chkled.isChecked()) {
            Reader.rrlib.StopInventoryLed();
        } else {
            Reader.rrlib.StopRead();
        }
    }

    /* loaded from: classes.dex */
    public class MsgCallback implements TagCallback {
        public MsgCallback() {
        }

        @Override // com.rfid.trans.TagCallback
        public void tagCallback(ReadTag arg0) {
            String upperCase = arg0.epcId != null ? arg0.epcId.toUpperCase() : android.device.sdk.BuildConfig.FLAVOR;
            String upperCase2 = arg0.memId != null ? arg0.memId.toUpperCase() : android.device.sdk.BuildConfig.FLAVOR;
            String valueOf = String.valueOf(arg0.rssi);
            Message obtainMessage = ScanMode.this.handler.obtainMessage();
            obtainMessage.what = 0;
            if (upperCase2.length() == 0) {
                obtainMessage.obj = upperCase + "," + valueOf;
            } else {
                obtainMessage.obj = upperCase + "," + upperCase2 + "," + valueOf;
            }
            ScanMode.this.handler.sendMessage(obtainMessage);
            ScanMode.this.lastCount++;
            if (System.currentTimeMillis() - ScanMode.this.lastTime >= 1000) {
                Message obtainMessage2 = ScanMode.this.handler.obtainMessage();
                obtainMessage2.what = 2;
                obtainMessage2.obj = ((ScanMode.this.lastCount * 1000) / (System.currentTimeMillis() - ScanMode.this.lastTime)) + android.device.sdk.BuildConfig.FLAVOR;
                ScanMode.this.handler.sendMessage(obtainMessage2);
                ScanMode.this.lastTime = System.currentTimeMillis();
                ScanMode.this.lastCount = 0;
            }
        }

        @Override // com.rfid.trans.TagCallback
        public void StopReadCallBack() {
            Message obtainMessage = ScanMode.this.handler.obtainMessage();
            obtainMessage.what = 3;
            obtainMessage.obj = android.device.sdk.BuildConfig.FLAVOR;
            ScanMode.this.handler.sendMessage(obtainMessage);
        }
    }

    @Override // android.app.Activity
    public boolean onContextItemSelected(MenuItem item) {
        epc = this.tagList.get(((AdapterView.AdapterContextMenuInfo) item.getMenuInfo()).position).get("tagUii");
        if (item.getItemId() == 1) {
            MainActivity.myTabHost.setCurrentTab(2);
            return false;
        }
        MainActivity.myTabHost.setCurrentTab(1);
        return false;
    }

    @Override // android.app.Activity
    protected void onPause() {
        super.onPause();
        setOpenScan523(true);
        stopInventory();
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
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
