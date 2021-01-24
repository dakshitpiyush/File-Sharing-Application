package com.dakshit.file_sharing;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.util.HashMap;

public class Landing extends AppCompatActivity {
    TextView name;
    private RadioButton radio_button;
    private final HashMap<Integer, Integer> proToImg=new HashMap(){{
        put(R.id.profile1, R.drawable.profile1);
        put(R.id.profile2, R.drawable.profile2);
        put(R.id.profile3, R.drawable.profile3);
        put(R.id.profile4, R.drawable.profile4);
        put(R.id.profile5, R.drawable.profile5);
        put(R.id.profile6, R.drawable.profile6);
        put(R.id.profile7, R.drawable.profile7);
        put(R.id.profile8, R.drawable.profile8);
        put(R.id.profile9, R.drawable.profile9);
    }};
    private RadioGroup profile_group;

    private Drawable border;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome);

        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.MANAGE_EXTERNAL_STORAGE
        }, 1);

        name = findViewById(R.id.username);
        profile_group = findViewById(R.id.profiles);
        radio_button = findViewById(R.id.profile2);
        border = getDrawable(R.drawable.border);

        radio_button.setForeground(border);
        profile_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Log.v("id", String.valueOf(checkedId));
                radio_button.setForeground(null);
                radio_button = findViewById(checkedId);
                radio_button.setForeground(border);

            }
        });
        radio_button.setChecked(true);

    }
    public void gotoMain(View view){
        if(name.getText() == "" || name.getText().length() <= 2){
            name.setText("");
            Toast.makeText(getApplicationContext(), "bhai nav tri neet tak", Toast.LENGTH_LONG).show();
        }else{
            final SharedPreferences prefs = getApplicationContext().getSharedPreferences("my_preferences", Context.MODE_PRIVATE);

            SharedPreferences.Editor editor = prefs.edit();
            String ss = String.valueOf(name.getText());
            editor.putString("username", ss);
            editor.putBoolean("first_time", false);
            editor.putInt("profilePic", proToImg.get(profile_group.getCheckedRadioButtonId()));
            editor.putString("homeFolder", getHomeFolder());
            editor.apply();
            Log.v("welcome", "username : " + prefs.getString("username", "nopeeee"));
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    public String getHomeFolder(){
        String homeFolder=Environment.getExternalStorageDirectory().getAbsolutePath()+"/FileSharing/";
        try {
            File homeFold=new File(homeFolder);
            if(!homeFold.exists()) {
                homeFold.mkdir();
            }
        }
        catch (Exception e){
            Log.v("start", "nay chalat ahe bhava");
            e.printStackTrace();
            homeFolder=getApplicationContext().getExternalFilesDir(null).getPath();
        }
        return homeFolder;
    }

}
