package com.dakshit.file_sharing;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Recieve extends AppCompatActivity {

    private WifiManager wifiManager;
    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel channel;
    private IntentFilter intentFilter;
    private TextView message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recieve);

        message = (TextView) findViewById(R.id.tvRecived);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(this.WIFI_SERVICE);
        wifiP2pManager = (WifiP2pManager) getApplicationContext().getSystemService(this.WIFI_P2P_SERVICE);
        wifiManager.setWifiEnabled(true);

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        WifiBroadcastReciever wifiBroadcastReciever = new WifiBroadcastReciever(wifiManager, wifiP2pManager, this);

        registerReceiver(wifiBroadcastReciever, intentFilter);
    }

    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info) {
            final InetAddress groupOwnerAddress = info.groupOwnerAddress;
            startSession(groupOwnerAddress, info);
        }
    };

    public void startSession(InetAddress groupOwner, WifiP2pInfo info) {
        Socket socket = null;
        try {
            if (info.groupFormed && info.isGroupOwner) {
                ServerSocket serverSocket = new ServerSocket(8888);
                socket = serverSocket.accept();
            } else {
                socket = new Socket();
                socket.connect(new InetSocketAddress(groupOwner.getHostName(), 8888), 500);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        SendReciveFile sendReciveFile = new SendReciveFile(socket);
        if (sendReciveFile.status) {
            sendReciveFile.start();

            Intent parentIntent = getIntent();
            if (parentIntent.hasExtra("fileList")) {
                ArrayList<String> fileList = (ArrayList) parentIntent.getParcelableArrayListExtra("fileList");
                for (int i = 0; i < fileList.size(); i++) {
                    String url = fileList.get(i);
                    if (!sendReciveFile.send(url)) {
                        message.setText(message.getText() + "\npromblem while " + url + "transferred");
                    } else {
                        message.setText(message.getText() + "\nfile having url " + url + "transferred");
                    }
                }
            }
        }

    }

    private class WifiBroadcastReciever extends BroadcastReceiver {
        private Recieve activity;
        private WifiManager wifiManager;
        private WifiP2pManager wifiP2pManager;

        public WifiBroadcastReciever(WifiManager wifiManager, WifiP2pManager wifiP2pManager, Recieve activity) {
            this.wifiManager = wifiManager;
            this.wifiP2pManager = wifiP2pManager;
            this.activity = activity;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                if (intent.getIntExtra(wifiP2pManager.EXTRA_WIFI_STATE, -1) == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    Toast.makeText(context, "wifi is on", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, "wifi is off", Toast.LENGTH_LONG).show();
                }

            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                if (wifiP2pManager == null) return;
                NetworkInfo networkInf = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                if (networkInf.isConnected()) {
                    activity.message.setText("connected");
                    wifiP2pManager.requestConnectionInfo(channel, activity.connectionInfoListener);
                } else {
                    activity.message.setText("Disconnected");
                }

            } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {

            }
        }
    }

}