package com.dakshit.file_sharing;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.lang.reflect.Method;

public class Recieve extends AppCompatActivity {

    private WifiManager wifiManager;
    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel channel;
    private IntentFilter intentFilter;
    private TextView message;

    private LocationManager locationManager;
    private WifiBroadcastReciever wifiBroadcastReciever;
    private Button retry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recieve);

        message = findViewById(R.id.tvRecived);
        retry = findViewById(R.id.btnRecRetry);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);
        wifiP2pManager = (WifiP2pManager) getApplicationContext().getSystemService(WIFI_P2P_SERVICE);
        locationManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
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
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q) {
            intentFilter.addAction(LocationManager.MODE_CHANGED_ACTION);
        }
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        wifiBroadcastReciever = new WifiBroadcastReciever(wifiP2pManager, channel, this);

        registerReceiver(wifiBroadcastReciever, intentFilter);
        if(!wifiManager.isP2pSupported()){
            message.setText("your device is not supported p2p uninstall this app ");
        }else{
            changeDeviceName();
            discover(null);
        }


    }

    public void discover(View view){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
        }
        if(!wifiManager.isWifiEnabled() ) {
            message.setText("Promblem while turning on wifi please turn on wifi manually");
            wifiManager.setWifiEnabled(true);
            return;
        }else if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.Q && !locationManager.isLocationEnabled()){
            message.setText("please on loction");
        }
        wifiP2pManager.discoverPeers(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                message.setText("Succsess, Waiting for Sender");
            }

            @Override
            public void onFailure(int reason) {
                message.setText("searching fails, Retry error code"+ reason);
            }
        });
    }

    public void changeDeviceName() {
        final SharedPreferences prefs = getApplicationContext().getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        String username=prefs.getString("username", "kahichnahi");
        int profilePic=prefs.getInt("profilePic", 3);
        String deviceNewName="receiver"+":"+username+":"+ profilePic;

        try {
            Method method = wifiP2pManager.getClass().getMethod("setDeviceName", WifiP2pManager.Channel.class, String.class, WifiP2pManager.ActionListener.class);

            method.invoke(wifiP2pManager, channel, deviceNewName, new WifiP2pManager.ActionListener() {

                @Override
                public void onSuccess() {
                    Log.v("namechange", "name change sucsessfully"+deviceNewName);
                }

                @Override
                public void onFailure(int reason) {

                    Log.d("namefail", "Name change failed: " + reason);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
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

    protected void onDestroy() {
        super.onDestroy();
        wifiP2pManager.removeGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                //Toast.makeText(getApplicationContext(), "group removed", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(int reason) {
                //Toast.makeText(getApplicationContext(), "Not removed error code"+ String.valueOf(reason), Toast.LENGTH_LONG).show();
            }
        });
        Log.v("destroy", "activity destroye");
    }

    private static class WifiBroadcastReciever extends BroadcastReceiver {
        private final Recieve activity;
        private final WifiP2pManager wifiP2pManager;
        private final WifiP2pManager.Channel channel;

        public WifiBroadcastReciever(WifiP2pManager wifiP2pManager, WifiP2pManager.Channel channel, Recieve activity) {
            this.channel = channel;
            this.wifiP2pManager = wifiP2pManager;
            this.activity = activity;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                if (intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1) == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    Toast.makeText(context, "wifi is on", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, "wifi is off", Toast.LENGTH_LONG).show();
                }
                activity.discover(null);

            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                if (wifiP2pManager == null) return;
                NetworkInfo networkInf = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                if (networkInf.isConnected()) {
                    activity.message.setText("connected");
                    wifiP2pManager.requestConnectionInfo(channel, activity.connectionInfoListener);
                } else {
                    //activity.message.setText("Disconnected");
                }

            } else if (LocationManager.MODE_CHANGED_ACTION.equals(action)) {
                activity.discover(null);
            }
        }
    }

}