package com.dakshit.file_sharing;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;


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
        Log.v("start", "Activity is start");
    }

    public void sendReceiveFile(View view) {
        if (requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            if (view.getId() == R.id.btnSend) {
                Intent selectFileIntent;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                    selectFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    selectFileIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    selectFileIntent.addCategory(Intent.CATEGORY_OPENABLE);
                    selectFileIntent.setType("*/*");  //use image/* for photos, etc.
                    startActivityForResult(selectFileIntent, 1001);

                } else {
                    selectFileIntent = new Intent(this, ShowFiles.class);
                    startActivityForResult(selectFileIntent, 1002);
                }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && requestCode == 1001 && data != null) {
            ArrayList<String> selectedFileList = new ArrayList<>();
            if (data.getClipData() != null) {
                // Getting the length of data and logging up the logs using index
                for (int index = 0; index < data.getClipData().getItemCount(); index++) {
                    // Getting the URIs of the selected files and logging them into logcat at debug level
                    Uri uri = data.getClipData().getItemAt(index).getUri();
                    selectedFileList.add(FileUtils.getPath(getApplicationContext(), uri));
                }
            } else {
                // Getting the URI of the selected file and logging into logcat at debug level
                Uri uri = data.getData();
                selectedFileList.add(FileUtils.getPath(getApplicationContext(), uri));
            }
            Intent connect = new Intent(this, Connect.class);
            connect.putExtra("fileList", selectedFileList);
            startActivity(connect);
        }
        if (resultCode == RESULT_OK && requestCode == 1002 && data != null) {
            ArrayList<String> selectedFileList=data.getStringArrayListExtra("fileList");
            Intent connect = new Intent(this, Connect.class);
            connect.putExtra("fileList", selectedFileList);
            startActivity(connect);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.v("stop", "Activity is stoping");
    }
}