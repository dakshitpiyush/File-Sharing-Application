package com.dakshit.file_sharing;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ShowFiles extends ListActivity {
    protected File curDirectory;
    protected ArrayAdapter<String> listOfFileAdapter;
    private File root=new File(Environment.getExternalStorageDirectory().getAbsolutePath());
    private List<String> listOfFiles=new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState){
        //TODO: add header with back & next button and title as file name
        //TODO: add footer as no of selected file and nextbutton here(or in header)
        super.onCreate(savedInstanceState);
        LayoutInflater inflator = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View emptyView = inflator.inflate(R.layout.filelist, null);
        ((ViewGroup) getListView().getParent()).addView(emptyView);
        getListView().setEmptyView(emptyView);
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
            refreshList();
        } else {
            //TODO: add file into sending queue/array
        }
        super.onListItemClick(fileListView, view, position, id);
    }

    @Override
    public void onBackPressed(){
        if(!curDirectory.equals(root)) {
            curDirectory = curDirectory.getParentFile();
            refreshList();
        }
    }
}
