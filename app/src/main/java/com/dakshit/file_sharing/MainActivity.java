package com.dakshit.file_sharing;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {
    private int STORAGE_PERMISSION_CODE = 1;
    private WifiManager wifiManager;
    private WifiManager.LocalOnlyHotspotReservation hotspotReservation;
    private WifiConfiguration currentConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        requestPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        requestPermission(Manifest.permission.CHANGE_WIFI_STATE);
    }

    public void sendReceiveFile(View view) {
        if (requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            if (view.getId() == R.id.btnSend) {
                Intent selectFileIntent = new Intent(this, ShowFiles.class);
                startActivity(selectFileIntent);
            } else if (view.getId() == R.id.btnRecive) {
                Intent receiveFileIntent = new Intent(this, Recieve.class);
                startActivity(receiveFileIntent);
            }
        } else {
            TextView forTest = findViewById(R.id.test);
            forTest.setText("you do not have permission");
        }
    }


    //request for permissions
    private boolean requestPermission(String resource) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, resource) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{resource}, STORAGE_PERMISSION_CODE);
            return ContextCompat.checkSelfPermission(MainActivity.this, resource) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

}