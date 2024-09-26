package com.UHF.scanlable;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TabHost;

/* loaded from: classes.dex */
public class MainActivity extends TabActivity {
    public static TabHost myTabHost;

    @Override // android.app.Activity
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override // android.app.ActivityGroup, android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(1);
        setContentView(R.layout.activity_main);
        myTabHost = getTabHost();
        Intent intent = new Intent(this, (Class<?>) ScanMode.class);
        Intent intent2 = new Intent(this, (Class<?>) ReadWriteActivity.class);
        Intent intent3 = new Intent(this, (Class<?>) ScanView.class);
        Intent intent4 = new Intent(this, (Class<?>) MaskActivity.class);
        Intent intent5 = new Intent(this, (Class<?>) Finding.class);
        TabHost.TabSpec content = myTabHost.newTabSpec(getString(R.string.tab_scan)).setIndicator(getString(R.string.tab_scan)).setContent(intent);
        TabHost.TabSpec content2 = myTabHost.newTabSpec(getString(R.string.tab_rw)).setIndicator(getString(R.string.tab_rw)).setContent(intent2);
        TabHost.TabSpec content3 = myTabHost.newTabSpec(getString(R.string.tab_param)).setIndicator(getString(R.string.tab_param)).setContent(intent3);
        TabHost.TabSpec content4 = myTabHost.newTabSpec(getString(R.string.tab_mask)).setIndicator(getString(R.string.tab_mask)).setContent(intent4);
        TabHost.TabSpec content5 = myTabHost.newTabSpec(getString(R.string.finding)).setIndicator(getString(R.string.finding)).setContent(intent5);
        myTabHost.addTab(content);
        myTabHost.addTab(content5);
        myTabHost.addTab(content2);
        myTabHost.addTab(content4);
        myTabHost.addTab(content3);
        myTabHost.setCurrentTab(0);
    }

    @Override // android.app.ActivityGroup, android.app.Activity
    protected void onResume() {
        super.onResume();
    }

    @Override // android.app.ActivityGroup, android.app.Activity
    protected void onPause() {
        super.onPause();
    }

    @Override // android.app.ActivityGroup, android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
        Reader.rrlib.DisConnect();
    }
}
