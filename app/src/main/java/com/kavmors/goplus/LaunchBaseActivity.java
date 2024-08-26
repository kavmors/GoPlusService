package com.kavmors.goplus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class LaunchBaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MonitorService.start(this);

        Toast.makeText(getApplicationContext(), R.string.service_started, Toast.LENGTH_SHORT).show();
    }
}
