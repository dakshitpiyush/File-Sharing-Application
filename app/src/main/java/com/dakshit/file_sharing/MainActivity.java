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
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
    private TextView tvUserName;
    private ImageView imageView;
    private AlertDialog.Builder builder;
    private AlertDialog dialog;
    private String homeFolder;
    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.v("start", "Activity is start");
        tvUserName = findViewById(R.id.textView);
        imageView= findViewById(R.id.profilePic);

        final SharedPreferences prefs = getApplicationContext().getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        boolean is_first_time = prefs.getBoolean("first_time", true);

        if(is_first_time){
            Log.v("start", "ya ya ya su swagatam");
            tvUserName.setText("ya ya ya su swagatam");
            Intent intent = new Intent(getApplicationContext(), Landing.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
            startActivity(intent);
        }
        else{
            Log.v("start", "khar bol tu pahile pn ala ahes na");
            String uname = prefs.getString("username", "kahichnahi");
            Log.v("start", "uanem is:" + uname);
            tvUserName.setText(uname);
            imageView.setImageResource(prefs.getInt("profilePic", R.drawable.profile3));
            homeFolder=prefs.getString("homeFolder", getApplicationContext().getExternalFilesDir(null).getPath());
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
        }else{
            Toast.makeText(getApplicationContext(), "you need to give permission of storage", Toast.LENGTH_LONG).show();
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

            final View customLayout = getLayoutInflater().inflate( R.layout.display_location, null);
            builder.setView(customLayout);
            TextView tt = customLayout.findViewById(R.id.files_location);
            tt.setText(homeFolder);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener(){

                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();

                }
            });
            dialog = builder.create();
        }


        dialog.show();
//        File pdfFile = new File(homeFolder);//File path
//        if (pdfFile.exists()) //Checking if the file exists or not
//        {
//            Uri path = Uri.fromFile(pdfFile);
//            Intent objIntent = new Intent(Intent.ACTION_VIEW);
//            //objIntent.addCategory(Intent.CATEGORY_OPENABLE);
//            objIntent.setDataAndType(path, DocumentsContract.Document.MIME_TYPE_DIR);
//            //objIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            startActivity(objIntent);//Starting the pdf viewer
//        } else {
//
//            Toast.makeText(getApplicationContext(), "The file not exists! ", Toast.LENGTH_SHORT).show();
//
//        }

    }

}

class ShowLocation extends AppCompatDialogFragment{
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());


    }


}

