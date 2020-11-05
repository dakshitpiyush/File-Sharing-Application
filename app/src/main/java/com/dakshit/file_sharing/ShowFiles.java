package com.dakshit.file_sharing;

import android.app.ListActivity;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ShowFiles extends ListActivity {
    protected File curDirectory;
    protected ArrayAdapter<String> listOfFileAdapter;
    private File root=new File(Environment.getExternalStorageDirectory().getAbsolutePath());
    private List<String> listOfFiles=new ArrayList<String>();
    private Set<String> selectedFilesSet=new HashSet();
    private TextView fileCountView;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //LayoutInflater inflator = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        setContentView(R.layout.filelist);
        fileCountView=findViewById(R.id.noSelectedFile);
        curDirectory=root;
        for(String listItem:curDirectory.list()){
            listOfFiles.add(listItem);
        }
        listOfFileAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, listOfFiles);
        setListAdapter(listOfFileAdapter);
    }
    private void refreshList(){
        listOfFiles.clear();
        for(String listItem:curDirectory.list()){
            listOfFiles.add(listItem);
        }
        listOfFileAdapter.notifyDataSetChanged();
    }
    @Override
    protected void onListItemClick(ListView fileListView, View view, int position, long id){
        String selectedFileName=curDirectory+"/"+fileListView.getItemAtPosition(position);
        File selectedFile=new File(selectedFileName);
        if(selectedFile.isDirectory()){
            curDirectory=selectedFile;
            selectedFilesSet.clear();
            refreshList();
        } else {
            if(selectedFilesSet.add(selectedFileName)){
                fileCountView.setText(selectedFilesSet.size()+" files selected");
            } else{
                selectedFilesSet.remove(selectedFileName);
                fileCountView.setText(selectedFilesSet.size()+" files selected");
            }
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
    public void goBackDir(View view){
        if(!curDirectory.equals(root)) moveBack();
    }
    public void moveBack(){
        curDirectory = curDirectory.getParentFile();
        selectedFilesSet.clear();
        fileCountView.setText(selectedFilesSet.size()+" files selected");
        refreshList();
    }
}
