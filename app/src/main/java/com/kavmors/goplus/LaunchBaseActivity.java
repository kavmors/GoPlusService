package com.kavmors.goplus;

import android.app.Activity;
import android.os.Bundle;

public class LaunchBaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MonitorService.start(this);
    }
}
