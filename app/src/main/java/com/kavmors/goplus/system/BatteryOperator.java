package com.kavmors.goplus.system;

import com.yujing.serialport.SerialPort;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

public final class BatteryOperator {

    private Process mProcess;

    public void closeProcess() {
        try {
            if (mProcess != null) {
                mProcess.getOutputStream().close();
                mProcess.destroy();
            }
        } catch (IOException ignored) {}
    }

    public void setBattery(int level) throws IOException {
        ensureProcess();
        exec("dumpsys battery set level " + level);
    }

    public void setCharging() throws IOException {
        ensureProcess();
        exec("dumpsys battery set ac 1");
    }

    public void setUncharge() throws IOException {
        ensureProcess();
        exec("dumpsys battery set ac 0");
        exec("dumpsys battery unplug");
    }

    private void exec(String cmd) throws IOException {
        OutputStream os = mProcess.getOutputStream();
        os.write(cmd.getBytes());
        os.write('\n');
        os.flush();
    }

    private void ensureProcess() throws IOException {
        if (mProcess == null || !mProcess.isAlive()) {
            mProcess = Runtime.getRuntime().exec(SerialPort.getSuPath());
        }
    }
}
