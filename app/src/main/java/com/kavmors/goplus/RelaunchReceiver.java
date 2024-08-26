package com.kavmors.goplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RelaunchReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        MonitorService.start(context);
    }
}
