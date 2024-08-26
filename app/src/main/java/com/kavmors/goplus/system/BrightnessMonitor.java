package com.kavmors.goplus.system;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;

import com.yujing.serialport.SerialPort;

import java.io.OutputStream;
import java.nio.charset.Charset;

public final class BrightnessMonitor {

    private static final int BRIGHTNESS_MIN = 0;
    private static final int BRIGHTNESS_MAX = 255;
    private static final int BRIGHTNESS_DEFAULT = BRIGHTNESS_MAX;

    private final ContentResolver mCr;
    private final ContentObserver mOb;

    private Runnable mCallback;

    public BrightnessMonitor(Context context) {
        mCr = context.getContentResolver();
        mOb = new ContentObserver(new Handler(Looper.getMainLooper())) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                if (mCallback != null) {
                    mCallback.run();
                }
            }
        };
    }

    public void registerObserver(Runnable callback) {
        mCallback = callback;
        mCr.registerContentObserver(
                Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS), false, mOb);
    }

    public void unregisterObserver() {
        mCallback = null;
        mCr.unregisterContentObserver(mOb);
    }

    public int getValue() {
        return Settings.System.getInt(mCr, Settings.System.SCREEN_BRIGHTNESS, BRIGHTNESS_DEFAULT);
    }

    public int getProgress() {
        return getValue() * 100 / BRIGHTNESS_MAX;
    }

    public void initProgress(int progress) {
        // su : settings put system $SCREEN_BRIGHTNESS $progress
        String cmd = String.format("settings put system %s %d",
                Settings.System.SCREEN_BRIGHTNESS, progress);

        try {
            Process su = Runtime.getRuntime().exec(SerialPort.getSuPath());
            OutputStream os = su.getOutputStream();

            os.write(cmd.getBytes(Charset.defaultCharset()));
            os.write('\n');

            os.write("exit".getBytes(Charset.defaultCharset()));
            os.write('\n');

            os.flush();

            su.waitFor();
            os.close();
            su.destroy();
        } catch (Exception ignored) {}
    }
}
