package com.dakshit.file_sharing;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {
    private int STORAGE_PERMISSION_CODE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    public void sendFile(View view) {
        if (requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Intent selectFileIntent = new Intent(this, ShowFiles.class);
            startActivity(selectFileIntent);
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