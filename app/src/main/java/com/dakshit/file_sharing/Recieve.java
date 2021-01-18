package com.dakshit.file_sharing;

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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class Recieve extends AppCompatActivity {

    private WifiManager wifiManager;
    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel channel;
    private IntentFilter intentFilter;
    private TextView message;


    private WifiBroadcastReciever wifiBroadcastReciever;
    //  public Handler handler = new Handler(new Handler.Callback() {
//        @Override
//        public boolean handleMessage(@NonNull Message msg) {
//            String fileName;
//            switch (msg.what) {
//                case 1:
//                    fileName = (String) msg.obj;
//                    message.setText("seneding fails of file" + fileName);
//                    break;
//                case 2:
//                    fileName = (String) msg.obj;
//                    message.setText("seneding sucsess file" + fileName);
//                    break;
//                case 3:
//                    //sended part of data
//                    fileName = (String) msg.obj;
//                    break;
//                case 4:
//                    //recieved some part of data
//                    fileName = (String) msg.obj;
//                    break;
//
//            }
//            return true;
//        }
//    });
    private Button retry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recieve);

        message = (TextView) findViewById(R.id.tvRecived);
        retry = (Button) findViewById(R.id.btnRecRetry);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(this.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);
        wifiP2pManager = (WifiP2pManager) getApplicationContext().getSystemService(this.WIFI_P2P_SERVICE);
        channel = wifiP2pManager.initialize(this, getMainLooper(), null);

        wifiP2pManager.removeGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int reason) {

            }
        });
        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        wifiBroadcastReciever = new WifiBroadcastReciever(wifiP2pManager, channel, this);

        registerReceiver(wifiBroadcastReciever, intentFilter);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
        }
        wifiP2pManager.discoverPeers(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                message.setText("Succsess, Waiting for Sender");
            }

            @Override
            public void onFailure(int reason) {
                message.setText("searching fails, Retry error code"+ String.valueOf(reason));
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(wifiBroadcastReciever, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(wifiBroadcastReciever);
    }

    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info) {
            Intent sharing = new Intent(getApplicationContext(), Sharing.class);
            sharing.putExtra("wifiP2pInfo", info);
            startActivity(sharing);
        }
    };


    private static class WifiBroadcastReciever extends BroadcastReceiver {
        private Recieve activity;
        private WifiP2pManager wifiP2pManager;
        private WifiP2pManager.Channel channel;

        public WifiBroadcastReciever(WifiP2pManager wifiP2pManager, WifiP2pManager.Channel channel, Recieve activity) {
            this.channel = channel;
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