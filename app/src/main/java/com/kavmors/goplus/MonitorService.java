package com.kavmors.goplus;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import com.kavmors.goplus.port.PortCapability;
import com.kavmors.goplus.port.PortSender;
import com.kavmors.goplus.system.BatteryOperator;
import com.kavmors.goplus.system.BrightnessMonitor;
import com.kavmors.goplus.system.NetworkOperator;

import java.io.IOException;

public class MonitorService extends Service {

    private static final String TAG = "MonitorService";
    private static final String TAG_E = "MonitorEvent";

    private PortCapability mCapability;
    private BrightnessMonitor mBrightness;
    private BatteryOperator mBattery;
    private NetworkOperator mNetwork;

    public static void start(Context context) {
        context.startForegroundService(new Intent(context, MonitorService.class));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(TAG.hashCode(), createNotification());
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

        mBrightness = new BrightnessMonitor(this.getApplicationContext());
        mBattery = new BatteryOperator();
        mCapability = new PortCapability(PortSender.getInstance(this));
        mNetwork = new NetworkOperator();

        initBrightness();
        initBatteryAndCharging();
        initNetworkConfig();

        monitorBrightness();
        monitorBattery();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        mBrightness.unregisterObserver();
        mBattery.closeProcess();
    }

    private void monitorBrightness() {
        mBrightness.registerObserver(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG_E, String.format("brightness => %d (%d%%)",
                        mBrightness.getValue(), mBrightness.getProgress()));
                changeBrightness(mBrightness.getProgress());
            }
        });
    }

    private void changeBrightness(int progress) {
        int ttyValue = 2000 * progress / 100;
        try {
            mCapability.setBacklight(ttyValue);
        } catch (IOException e) {
            handleIOException(e);
        }
    }

    private void initBrightness() {
        try {
            int ttyValue = mCapability.getBacklight();
            int progress = ttyValue * 100 / 2000;
            mBrightness.initProgress(progress);
        } catch (IOException e) {
            handleIOException(e);
        }
    }

    private void monitorBattery() {
        mCapability.setOnBatteryListener(new PortCapability.OnBatteryChange() {
            @Override
            public void onChanged(int level, boolean isCharging) {
                Log.i(TAG_E, String.format("battery => %d%% %s",
                        level, isCharging ? "(charging)" : ""));
                updateBattery(level);
                updateCharging(isCharging);
            }
        });
    }

    private void updateBattery(int level) {
        try {
            mBattery.setBattery(level);
        } catch (IOException e) {
            handleIOException(e);
        }
    }

    private void initBatteryAndCharging() {
        try {
            Pair<Integer, Boolean> ttyState = mCapability.getBatteryState();
            updateBattery(ttyState.first);
            updateCharging(ttyState.second);
        } catch (IOException e) {
            handleIOException(e);
        }
    }

    private void updateCharging(boolean isCharging) {
        try {
            if (isCharging) {
                mBattery.setCharging();
            } else {
                mBattery.setUncharge();
            }
        } catch (IOException e) {
            handleIOException(e);
        }
    }

    private void initNetworkConfig() {
        try {
            mNetwork.initNtpServer();
            mNetwork.initCaptiveUrl();
        } catch (IOException e) {
            handleIOException(e);
        }
    }

    private void handleIOException(IOException e) {
        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Notification createNotification() {
        NotificationChannel channel = new NotificationChannel(
                TAG, getString(R.string.app_name),
                NotificationManager.IMPORTANCE_HIGH);
        NotificationManager manager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(channel);

        return new Notification.Builder(this, TAG)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.service_started))
                .build();
    }
}
