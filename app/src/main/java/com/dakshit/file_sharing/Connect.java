package com.dakshit.file_sharing;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.List;

public class Connect extends AppCompatActivity {
    private WifiManager wifiManager;
    private WifiBroadcastReceiver wifiBroadcastReceiver;
    private ListView listView;
    private Button retry;
    private TextView message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        listView = (ListView) findViewById(R.id.listPeers);
        retry = (Button) findViewById(R.id.btnRetry);
        message = (TextView) findViewById(R.id.tvMessage);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(this.WIFI_SERVICE);
        wifiBroadcastReceiver = new WifiBroadcastReceiver(this);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(wifiBroadcastReceiver, intentFilter);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
        }
        wifiManager.setWifiEnabled(true);

        if (wifiManager.isWifiEnabled()) wifiManager.startScan();


    }

    public void onScanSuccess() {
        message.setText("scanning sucsess");
        List<ScanResult> deviceNameList = wifiManager.getScanResults();
        ArrayAdapter<ScanResult> listAdapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, deviceNameList);
        listView.setAdapter(listAdapter);
    }

    public void onFail() {
        message.setText("scan fail kaytari karna mule");
    }


}

class WifiBroadcastReceiver extends BroadcastReceiver {
    public Connect activity;

    public WifiBroadcastReceiver(Connect activity) {
        this.activity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
            if (intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)) {
                activity.onScanSuccess();
            } else {
                activity.onFail();
            }
        }

    }
}