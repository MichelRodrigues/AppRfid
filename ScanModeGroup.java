package com.UHF.scanlable;

import android.app.ActivityGroup;
import android.os.Bundle;

/* loaded from: classes.dex */
public class ScanModeGroup extends ActivityGroup {
    public ActivityGroup group;

    @Override // android.app.ActivityGroup, android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.group = this;
    }

    @Override // android.app.Activity
    public void onBackPressed() {
        this.group.getLocalActivityManager().getCurrentActivity().onBackPressed();
    }

    @Override // android.app.Activity
    protected void onStart() {
        super.onStart();
    }
}
