package com.dakshit.file_sharing;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class ShowFiles extends ListActivity {
    protected File curDirectory;
    private TextView directoryPath;
    protected MyListAdapter listOfFileAdapter;
    private File root;
    private ArrayList<File> listOfFiles = new ArrayList();
    private HashSet<String> selectedFilesSet = new HashSet<>();
    private TextView fileCountView;
    ArrayList<String> filenames=new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filelist);
        Intent filesIntent;
        filesIntent = new Intent(Intent.ACTION_GET_CONTENT);
        filesIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        filesIntent.addCategory(Intent.CATEGORY_OPENABLE);
        filesIntent.setType("*/*");  //use image/* for photos, etc.
        startActivityForResuIntent filesIntent;
        filesIntent = new Intent(Intent.ACTION_GET_CONTENT);
        filesIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        filesIntent.addCategory(Intent.CATEGORY_OPENABLE);
        filesIntent.setType("*/*");  //use image/* for photos, etc.
        startActivityForResult(filesIntelt(filesIntent, 1001);

        fileCountView = findViewById(R.id.noSelectedFile);
        root = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
        directoryPath=findViewById(R.id.dirName);


        curDirectory = root;

        directoryPath.setText(curDirectory.getPath());

        for (File listItem : curDirectory.listFiles()) {
            listOfFiles.add(listItem);
            filenames.add(listItem.getName());
        }
        subs = new ArrayList<>();
        for(int i = 0 ;i < curDirectory.length();i++){
            subs.add("piyush");
        }
        listOfFileAdapter = new MyListAdapter(this, listOfFiles, subs, filenames);
        setListAdapter(listOfFileAdapter);
    }
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 1001:
                    // Checking whether data is null or not
                    if (data != null) {

                        // Checking for selection multiple files or single.
                        if (data.getClipData() != null){

                            // Getting the length of data and logging up the logs using index
                            for (int index = 0; index < data.getClipData().getItemCount(); index++) {

                                // Getting the URIs of the selected files and logging them into logcat at debug level
                                Uri uri = data.getClipData().getItemAt(index).getUri();
                                /*
                                 * Get the file's content URI from the incoming Intent,
                                 * then query the server app to get the file's display name
                                 * and size.
                                 */
                                String ss = FileUtils.getPath(getApplicationContext(), uri);
                                File filee = new File(ss);
                                boolean bool = filee.exists();
                                String  m = "asach";

                            }
                        }else{

                            // Getting the URI of the selected file and logging into logcat at debug level
                            Uri uri = data.getData();
                            Log.d("fileUri: ", String.valueOf(uri));
                        }
                    }
                    break;
            }
        }
    }

    private void refreshList(){
        listOfFiles.clear();
        filenames.clear();
        for(File listItem:curDirectory.listFiles()){
            listOfFiles.add(listItem);
            filenames.add(listItem.getName());
        }
        directoryPath.setText(curDirectory.getPath());
        listOfFileAdapter.notifyDataSetChanged();
    }
    @Override
    protected void onListItemClick(ListView fileListView, View view, int position, long id){
        File selectedFile=listOfFiles.get(position);
        if(selectedFile.isDirectory()){
            curDirectory=selectedFile;
            selectedFilesSet.clear();
            refreshList();
        } else {
            CheckBox checkBox = view.findViewById(R.id.checkBox);
            if (selectedFilesSet.add(selectedFile.getAbsolutePath())) checkBox.setChecked(true);
            else checkBox.setChecked(false);

        }
        super.onListItemClick(fileListView, view, position, id);
    }

    @Override
    public void onBackPressed(){
        if(!curDirectory.equals(root)) {
            moveBack();
        } else{
            super.onBackPressed();
        }
    }

    public void goBackDir(View view) {
        if (!curDirectory.equals(root)) moveBack();
    }

    public void moveBack() {
        curDirectory = curDirectory.getParentFile();
        selectedFilesSet.clear();
        fileCountView.setText(selectedFilesSet.size() + " files selected");
        refreshList();
    }

    public void connect(View view) {
        Intent connect = new Intent(this, Connect.class);
        ArrayList arr = new ArrayList(selectedFilesSet);
        connect.putExtra("fileList", arr);
        startActivity(connect);

    }
}

    private class MyListAdapter extends ArrayAdapter<String> {

        private final Activity context;
        private final ArrayList<File> maintitle;
        private final ArrayList<String> filenames;
        private final HashMap<String, Integer> icons = new HashMap() {{
            put("png", R.drawable.image);
            put("jpeg", R.drawable.image);
            put("jpg", R.drawable.image);
            put("svg", R.drawable.image);
            put("gif", R.drawable.image);
            put("mp4", R.drawable.video);
            put("mpeg", R.drawable.video);
            put("mkv", R.drawable.video);
            put("avi", R.drawable.video);
            put("flv", R.drawable.video);
            put("wmv", R.drawable.video);
            put("webm", R.drawable.video);
            put("pdf", R.drawable.pdf);
            put("doc", R.drawable.worddoc);
            put("docx", R.drawable.worddoc);
            put("xlsx", R.drawable.video);
            put("txt", R.drawable.txt);
            put("mp3", R.drawable.audio);
            put("wav", R.drawable.audio);
            put("m4a", R.drawable.audio);
            put("zip", R.drawable.zip);
            put("tar", R.drawable.zip);
            put("rar", R.drawable.zip);
        }};

        public MyListAdapter(Activity context, ArrayList<File> maintitle, ArrayList<String> filenames) {
            super(context, R.layout.iconlist, filenames);
            this.context = context;
            this.maintitle = maintitle;
            this.filenames = filenames;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            View rowView = inflater.inflate(R.layout.iconlist, null, true);
            TextView titleText = (TextView) rowView.findViewById(R.id.title);
            ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
            TextView subtitleText = (TextView) rowView.findViewById(R.id.subtitle);
            CheckBox checkBox = (CheckBox) rowView.findViewById(R.id.checkBox);

            titleText.setText(filenames.get(position));
            if (maintitle.get(position).isDirectory()) {
                imageView.setImageResource(R.drawable.folder);
            } else {
                int pos = maintitle.get(position).getName().lastIndexOf(".");
                String filecha = maintitle.get(position).getName();
                if (pos != -1) {
                    imageView.setImageResource(icons.getOrDefault(maintitle.get(position).getName().substring(pos + 1), R.drawable.unknown));
                } else {
                    imageView.setImageResource(R.drawable.unknown);
                }
            }
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    String selectedFile = maintitle.get(position).getAbsolutePath();
                    if (isChecked) selectedFilesSet.add(selectedFile);
                    else selectedFilesSet.remove(selectedFile);
                    fileCountView.setText(selectedFilesSet.size() + " files selected");
                }
            });

            subtitleText.setText(getSize(maintitle.get(position)));

            return rowView;

        }


        private String getSize(File file) {
            double length = (double) file.length();
            if (length < 1024) {
                return String.valueOf(length) + " B";
            } else if (length / 1024 < 1024) {
                return String.format("%.1f", length / 1024) + " KB";
            } else if (length / (1024 * 1024) < 1024) {
                return String.format("%.1f", length / (1024 * 1024)) + " MB";
            } else {
                return String.format("%.1f", length / (1024 * 1024 * 1024)) + " GB";
            }
        }
    }

}
