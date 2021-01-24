package com.dakshit.file_sharing;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    private final int STORAGE_PERMISSION_CODE = 1;
    private TextView tt;
    private ImageView imageView;
    private HorizontalScrollView locationscroll;
    private AlertDialog.Builder builder;
    private AlertDialog dialog;
    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        requestPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        requestPermission(Manifest.permission.CHANGE_WIFI_STATE);
        requestPermission(Manifest.permission.MANAGE_EXTERNAL_STORAGE);
        try {
            String path = Environment.getExternalStorageDirectory().getAbsolutePath();
            File rooter = new File(path + "/Mazfolder");
            rooter.mkdir();
            if(rooter.isDirectory()){
                Log.v("start", "ata dir ahe ");
            }
            else{
                Log.v("start", "ny zali create bhava");
            }
            Log.v("start", path);
        }
        catch (Exception e){
            Log.v("start", "nay chalat ahe bhava");
            e.printStackTrace();

        }
        Log.v("start", "Activity is start");
        tt = (TextView) findViewById(R.id.textView);
        imageView=(ImageView)findViewById(R.id.profilePic);
        final SharedPreferences prefs = getApplicationContext().getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        boolean is_first_time = prefs.getBoolean("first_time", true);
        if(is_first_time){
            Log.v("start", "ya ya ya su swagatam");
            tt.setText("ya ya ya su swagatam");
            Intent intent = new Intent(getApplicationContext(), Landing.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
            startActivity(intent);
        }
        else{
            Log.v("start", "khar bol tu pahile pn ala ahes na");
            String uname = prefs.getString("username", "kahichnahi");
            Log.v("start", "uanem is:" + uname);
            tt.setText(uname);
            imageView.setImageResource(prefs.getInt("profilePic", R.drawable.profile3));
        }
    }

    @SuppressLint("ResourceType")
    @Override
    protected void onResume() {
        super.onResume();



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
        super.onActivityResult(requestCode, resultCode, data);
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
            ArrayList<String> selectedFileList = data.getStringArrayListExtra("fileList");
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

    public void showLocation(View view){
        if(builder == null){
            builder = new AlertDialog.Builder(MainActivity.this);

            final View customLayout
                    = getLayoutInflater()
                    .inflate(
                            R.layout.display_location,
                            null);
            builder.setView(customLayout);
            TextView tt = (TextView)customLayout.findViewById(R.id.files_location);
            tt.setText(getApplicationContext().getExternalFilesDir(null).getPath());
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener(){

                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();

                }
            });
            dialog = builder.create();
        }


        dialog.show();

    }
}

class ShowLocation extends AppCompatDialogFragment{
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());


    }


}

