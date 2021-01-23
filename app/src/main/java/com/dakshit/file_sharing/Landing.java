package com.dakshit.file_sharing;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Landing extends AppCompatActivity {
    TextView name;
    private RadioGroup radio_group;
    private RadioButton radio_button;
    private Button btn_display;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome);
        name = (TextView) findViewById(R.id.username);

        addListenerOnRadio();

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
            editor.apply();
            Log.v("welcome", "username : " + prefs.getString("username", "nopeeee"));
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }
    }

    public void addListenerOnRadio(){
        radio_group = (RadioGroup) findViewById(R.id.profiles);
        radio_button = (RadioButton) findViewById(R.id.profile3);
        radio_button.setForeground(getDrawable(R.drawable.border));
        radio_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                radio_button.setForeground(null);
                radio_button = (RadioButton) findViewById(i);
                radio_button.setForeground(getDrawable(R.drawable.border));
            }
        });
    }

}
