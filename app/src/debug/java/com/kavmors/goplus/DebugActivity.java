package com.kavmors.goplus;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.kavmors.goplus.port.PortSender;

public class DebugActivity extends LaunchBaseActivity {

    private TextView mCmd;
    private Button mSend;
    private TextView mRec;

    private PortSender mPort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        mPort = PortSender.getInstance(this);

        mCmd = findViewById(R.id.cmd);
        mSend = findViewById(R.id.send);
        mRec = findViewById(R.id.rec);

        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRec.setText("");
                mPort.sendCmd(mCmd.getText().toString(), new PortSender.OnData() {
                    @Override
                    public void onData(String received) {
                        mRec.setText(received);
                    }
                }, null);
                mCmd.setText("");
            }
        });
    }
}
