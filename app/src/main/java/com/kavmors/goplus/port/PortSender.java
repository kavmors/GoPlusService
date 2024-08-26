package com.kavmors.goplus.port;

import android.content.Context;
import android.util.Log;

import com.yujing.serialport.SerialPort;
import com.yujing.yserialport.DataListener;
import com.yujing.yserialport.ThreadMode;
import com.yujing.yserialport.YSerialPort;

import java.io.File;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class PortSender {

    private static PortSender sInstance;

    public static PortSender getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new PortSender(createSerialPort(context));
        }
        return sInstance;
    }

    private static final String TAG = "PortSender";
    private static final long CMD_REC_TIMEOUT = 2000L;
    private static final Charset CHARSET = Charset.defaultCharset();

    static {
        ensureSu();
        unsetEnforce();
    }

    private static void ensureSu() {
        SerialPort.setSuPath("/system/xbin/su");
    }

    private static void unsetEnforce() {
        String cmd = "setenforce 0";
        try {
            Process su = Runtime.getRuntime().exec(SerialPort.getSuPath());
            OutputStream os = su.getOutputStream();

            os.write(cmd.getBytes(CHARSET));
            os.write('\n');

            os.write("exit".getBytes(CHARSET));
            os.write('\n');

            os.flush();

            su.waitFor();
            os.close();
            su.destroy();
        } catch (Exception ignored) {}
    }

    private static void ensureTTYPermission(String tty) {
        File device = new File(tty);

        String cmd = "chmod 666 " + device.getAbsolutePath();
        if (!device.canRead() || !device.canWrite()) {
            try {
                Process su = Runtime.getRuntime().exec(SerialPort.getSuPath());
                OutputStream os = su.getOutputStream();

                os.write(cmd.getBytes(CHARSET));
                os.write('\n');

                os.write("exit".getBytes(CHARSET));
                os.write('\n');

                os.flush();

                su.waitFor();
                os.close();
                su.destroy();
            } catch (Exception ignored) {
            }
        }
    }

    public static YSerialPort createSerialPort(Context context) {
        ensureTTYPermission("/dev/ttyACM0");
        return YSerialPort.getInstance(context.getApplicationContext(), "/dev/ttyACM0", "115200");
    }

    public interface OnData {
        void onData(String received);
    }

    private final YSerialPort mPort;

    private PortSender(YSerialPort port) {
        mPort = port;

        // receive default
        port.addDataListener(new DataListener() {   // Log Listener
            @Override
            public void value(String hexString, byte[] bytes) {
                String rec = new String(bytes, CHARSET).trim();
                Log.i(TAG, "REC < " + rec);
            }
        });
        port.setThreadMode(ThreadMode.IO);
        port.setToAuto();
        port.start();
    }

    public synchronized void observeData(OnData callback, String focusOn) {
        DataListener listener = new DataListener() {
            @Override
            public void value(String hexString, byte[] bytes) {
                String rec = new String(bytes, CHARSET).trim();
                if (focusOn == null || focusOn.isEmpty() || rec.contains(focusOn)) {
                    callback.onData(rec);
                }
            }
        };
        mPort.addDataListener(listener);
    }

    public synchronized void sendCmd(String cmd, OnData callback, String focusOn) {
        DataListener listener = new DataListener() {
            @Override
            public void value(String hexString, byte[] bytes) {
                String rec = new String(bytes, CHARSET).trim();
                if (focusOn == null || focusOn.isEmpty() || rec.contains(focusOn)) {
                    mPort.removeDataListener(this);
                    callback.onData(rec);
                }
            }
        };
        mPort.addDataListener(listener);

        Log.i(TAG, "SND > " + cmd);
        mPort.send((cmd + "\r\n").getBytes(CHARSET));
    }

    public synchronized String sendCmdSync(String cmd, String focusOn) {
        final CountDownLatch count = new CountDownLatch(1);

        final String[] dataTmp = new String[1];
        dataTmp[0] = "";

        sendCmd(cmd, new OnData() {
            @Override
            public void onData(String received) {
                dataTmp[0] = received;
                count.countDown();
            }
        }, focusOn);

        String rec;
        try {
            count.await(CMD_REC_TIMEOUT, TimeUnit.MILLISECONDS);
            rec = dataTmp[0];
        } catch (InterruptedException e) {
            rec = "";
        }
        Log.i(TAG, "SYN - " + cmd + " <> " + rec);
        return rec;
    }

    public void destroy() {
        mPort.clearDataListener();
        mPort.onDestroy();
    }
}
