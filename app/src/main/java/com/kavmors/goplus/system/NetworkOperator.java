package com.kavmors.goplus.system;

import com.yujing.serialport.SerialPort;

import java.io.IOException;
import java.io.OutputStream;

public final class NetworkOperator {

    private static final String NTP_SERVER = "time.asia.apple.com";
    private static final String HTTP_URL = "http://connect.rom.miui.com/generate_204";
    private static final String HTTPS_URL = "https://connect.rom.miui.com/generate_204";

    private Process mProcess;

    public void closeProcess() {
        try {
            if (mProcess != null) {
                mProcess.getOutputStream().close();
                mProcess.destroy();
            }
        } catch (IOException ignored) {}
    }

    public void initNtpServer() throws IOException {
        ensureProcess();
        exec("settings put global ntp_server " + NTP_SERVER);
    }

    public void initCaptiveUrl() throws IOException {
        ensureProcess();
        exec("settings put global captive_portal_http_url " + HTTP_URL);
        exec("settings put global captive_portal_https_url " + HTTPS_URL);
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
