package com.dakshit.file_sharing;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

public class Connect extends AppCompatActivity {
    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel channel;
    private WifiManager wifiManager;
    private BroadcastReceiver broadcastReceiver;
    private IntentFilter intentFilter;
    private ListView peerList;
    private Button retry;
    public TextView message;
    private ArrayAdapter<String> listAdapter;
    public ArrayList<String> deviceList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        peerList = (ListView) findViewById(R.id.listPeers);
        retry = (Button) findViewById(R.id.btnRetry);
        message = (TextView) findViewById(R.id.tvMessage);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);


        wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = wifiP2pManager.initialize(this, getMainLooper(), null);
        broadcastReceiver = new WifiDirectBroadcastReceiver(wifiP2pManager, channel, this);
        intentFilter = new IntentFilter();

        wifiP2pManager.removeGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int reason) {

            }
        });

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);


        registerReceiver(broadcastReceiver, intentFilter);

        discover(null);
    }

    public void discover(View view) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
        }
        wifiP2pManager.discoverPeers(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                message.setText("Sucsess");
            }

            @Override
            public void onFailure(int reason) {
                message.setText("searching fails, Retry error code"+ String.valueOf(reason));
            }
        });
        peerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = deviceList.get(position);
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions((Activity) getApplicationContext(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
                }
                wifiP2pManager.connect(channel, config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        //message.setText("sucsess Fully connected to "+parent.getItemAtPosition(position));
                    }

                    @Override
                    public void onFailure(int reason) {
                        String getRes;
                        if (reason == WifiP2pManager.P2P_UNSUPPORTED)
                            getRes = "device is unsupported";
                        else if (reason == wifiP2pManager.BUSY) getRes = "device is busy";
                        else getRes = "unknown error occured try again";
                        message.setText("Fail to connect device " + getRes);
                    }
                });

            }
        });
    }


    public WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peers) {
            List<String> deviceNameList = new ArrayList();
            for (WifiP2pDevice device : peers.getDeviceList()) {
                deviceNameList.add(device.deviceName);
                deviceList.add(device.deviceAddress);
            }

            listAdapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, deviceNameList);
            peerList.setAdapter(listAdapter);

        }
    };

    public Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            String fileName;
            switch (msg.what) {
                case 1:
                    fileName = (String) msg.obj;
                    message.setText("sneding fails of file" + fileName);
                    break;
                case 2:
                    fileName = (String) msg.obj;
                    message.setText("sneding sucsess file" + fileName);
                    break;
            }
            return true;
        }
    });

    public WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info) {
            Intent parentIntent = getIntent(), sharing = new Intent(getApplicationContext(), Sharing.class);
            ArrayList<String> selectedFileList = null;
            if (parentIntent.hasExtra("fileList"))
                selectedFileList = parentIntent.getStringArrayListExtra("fileList");
            sharing.putExtra("selectedFileList", selectedFileList);
            sharing.putExtra("wifiP2pInfo", info);
            startActivity(sharing);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }


    private static class WifiDirectBroadcastReceiver extends BroadcastReceiver {
        private WifiP2pManager wifiP2pManager;
        private WifiP2pManager.Channel channel;
        private Connect activity;

        public WifiDirectBroadcastReceiver(WifiP2pManager wifiP2pManager, WifiP2pManager.Channel channel, Connect activity) {
            this.wifiP2pManager = wifiP2pManager;
            this.channel = channel;
            this.activity = activity;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                if (intent.getIntExtra(wifiP2pManager.EXTRA_WIFI_STATE, -1) == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    Toast.makeText(context, "wifi is on", Toast.LENGTH_LONG).show();
                    activity.discover(null);
                } else {
                    Toast.makeText(context, "wifi is off", Toast.LENGTH_LONG).show();
                }

            } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
                }
                wifiP2pManager.requestPeers(channel, activity.peerListListener);
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



