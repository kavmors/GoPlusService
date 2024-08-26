package com.kavmors.goplus.port;

import android.util.Pair;

import java.io.IOException;

public class PortCapability {

    public interface OnBatteryChange {
        void onChanged(int level, boolean isCharging);
    }

    private static final String REC_BKL = "+BKL=";
    private static final String REC_BAT = "+BAT=";
    private static final String REC_BATCG = "+BATCG=";

    private final PortSender mSender;

    private OnBatteryChange mBatteryListener;

    public PortCapability(PortSender sender) {
        mSender = sender;
        observerBatteryChanged();
    }

    public void setBacklight(int value) throws IOException {
        int res = parseBacklightResult(mSender.sendCmdSync("AT+BKL=" + value, REC_BKL));
        if (res != value) throw new IOException("BKL not expected: " + res);
    }

    public int getBacklight() throws IOException {
        return parseBacklightResult(mSender.sendCmdSync("AT+BKL", REC_BKL));
    }

    public Pair<Integer, Boolean> getBatteryState() throws IOException {
        return parseBattery(mSender.sendCmdSync("AT+BAT", REC_BAT), false);
    }

    public void setOnBatteryListener(OnBatteryChange listener) {
        mBatteryListener = listener;
    }

    private void observerBatteryChanged() {
        mSender.observeData(new PortSender.OnData() {
            @Override
            public void onData(String received) {
                try {
                    Pair<Integer, Boolean> bat = parseBattery(received, true);
                    if (mBatteryListener != null) {
                        mBatteryListener.onChanged(bat.first, bat.second);
                    }
                } catch (IOException ignored) {}
            }
        }, REC_BATCG);
    }

    private int parseBacklightResult(String res) throws IOException {
        // Empty
        if (res.isEmpty()) throw new IOException("No BKL reply");

        // +BKL=700
        // +BKL=+ERROR=100
        int split = res.indexOf(REC_BKL);
        if (split < 0) throw new IOException("Invalid BKL reply: " + res);
        res = res.substring(split + REC_BKL.length());

        // +ERROR=xx
        if (res.startsWith("+ERROR")) throw new IOException("BKL reply with ERR: " + res);

        try {
            return Integer.parseInt(res);
        } catch (NumberFormatException e) {
            throw new IOException("BKL reply not number: " + res);
        }
    }

    // return: <battery percent, is charging>
    private Pair<Integer, Boolean> parseBattery(String res, boolean changed) throws IOException {
        // Empty
        if (res.isEmpty()) throw new IOException("No BAT reply");

        // +BAT=voltage,percent,charging,current,temperature
        // +BAT=4000,63,1,2903,389,2
        // +BATCG=4000,63,1,2903,389,2
        int split = res.indexOf(changed ? REC_BATCG : REC_BAT);
        if (split < 0) throw new IOException("Invalid BAT reply: " + res);
        res = res.substring(split + (changed ? REC_BATCG : REC_BAT).length());

        // +ERROR=xx
        if (res.startsWith("+ERROR")) throw new IOException("BAT reply with ERR: " + res);

        try {
            String[] infos = res.split(",");
            int level = Integer.parseInt(infos[1]);
            int charge = Integer.parseInt(infos[2]);

            return new Pair<>(level, charge == 1);
        } catch (IndexOutOfBoundsException e) {
            throw new IOException("BAT info invalid: " + res);
        } catch (NumberFormatException e) {
            throw new IOException("BAT info not number: " + res);
        }
    }
}
