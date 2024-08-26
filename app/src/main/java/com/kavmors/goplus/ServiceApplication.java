package com.kavmors.goplus;

import android.app.Application;

import com.kavmors.goplus.port.PortSender;

public class ServiceApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        PortSender.getInstance(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        PortSender.getInstance(this).destroy();
    }
}
