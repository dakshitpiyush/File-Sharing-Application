package com.dakshit.file_sharing;

import android.content.Intent;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Sharing extends AppCompatActivity {
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
    private ViewGroup scroll;
    private WifiP2pInfo info;
    private ArrayList<String> selectedFileList = null;
    private Session session;
    private Handler handler;
    private SendReciveFile sendReciveFile;
    private LayoutInflater layoutInflater;
    private int curSend = 0;
    private ArrayList<View> sendViewList = new ArrayList<>();
    private View curReceiveView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sharing);
        Intent parent = getIntent();
        scroll = findViewById(R.id.scrShare);
        layoutInflater = getLayoutInflater();
        info = (WifiP2pInfo) parent.getParcelableExtra("wifiP2pInfo");
        if (parent.hasExtra("selectedFileList"))
            selectedFileList = parent.getStringArrayListExtra("selectedFileList");
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                switch (msg.what) {
                    case 1:
                        Socket socket = (Socket) msg.obj;
                        startSharing(socket);
                        break;
                    case 2:
                        FileR fileR = (FileR) msg.obj;
                        receive(fileR.fileName, fileR.fileSize);
                        break;
                    case 3:
                        makeProgress(false);
                        break;
                    case 4:
                        makeProgress(true);
                        break;
                    case 5:
                        curSend++;
                        sendNext();
                }
                return true;
            }

        });

        session = new Session(info, selectedFileList, handler);
        session.start();
    }

    private void startSharing(Socket socket) {
        sendReciveFile = new SendReciveFile(socket, handler);
        sendReciveFile.start();
        if (selectedFileList != null) {
            for (int i = 0; i < selectedFileList.size(); i++) {
                drawSend(selectedFileList.get(i));
            }
            sendNext();
        }
    }

    public void drawSend(String url) {
        File curFile = new File(url);
        View fileShareView = layoutInflater.inflate(R.layout.file_trans, null, true);
        ImageView icon = fileShareView.findViewById(R.id.iconS);
        TextView fileNameView = fileShareView.findViewById(R.id.tvFileName),
                fileSizeView = fileShareView.findViewById(R.id.tvSize);
        ProgressBar progressBar = fileShareView.findViewById(R.id.pbrSent);
        String fileName = curFile.getName();
        if (curFile.isDirectory()) {
            icon.setImageResource(R.drawable.folder);
        } else {
            int pos = fileName.lastIndexOf(".");
            if (pos != -1) {
                icon.setImageResource(icons.getOrDefault(fileName.substring(pos + 1), R.drawable.unknown));
            } else {
                icon.setImageResource(R.drawable.unknown);
            }
        }
        progressBar.setMax((int) curFile.length());
        fileNameView.setText(fileName);
        fileSizeView.setText(getSize((double) curFile.length()));
        scroll.addView(fileShareView);
        sendViewList.add(fileShareView);
    }

    private void receive(String fileName, long size) {
        View fileShareView = layoutInflater.inflate(R.layout.file_trans, null, true);
        ImageView icon = fileShareView.findViewById(R.id.iconS);
        TextView fileNameView = fileShareView.findViewById(R.id.tvFileName),
                fileSizeView = fileShareView.findViewById(R.id.tvSize);
        ProgressBar progressBar = fileShareView.findViewById(R.id.pbrSent);
        int pos = fileName.lastIndexOf(".");
        if (pos != -1) {
            icon.setImageResource(icons.getOrDefault(fileName.substring(pos + 1), R.drawable.unknown));
        } else {
            icon.setImageResource(R.drawable.unknown);
        }
        fileNameView.setText(fileName);
        fileSizeView.setText(getSize((double) size));
        progressBar.setMax((int) size);
        scroll.addView(fileShareView);
        curReceiveView = fileShareView;
    }

    private String getSize(double length) {
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

    private void sendNext() {
        if (curSend >= selectedFileList.size()) {
            return;
        }
        File curFile = new File(selectedFileList.get(curSend));
        sendReciveFile.send(curFile);
    }

    private void makeProgress(boolean isSend) {
        int progress;
        View fileSharingView;
        fileSharingView = isSend ? sendViewList.get(curSend) : curReceiveView;
        ProgressBar progressBar = (ProgressBar) fileSharingView.findViewById(R.id.pbrSent);
        progress = Math.min(progressBar.getProgress() + SendReciveFile.BUFFER_SIZE, progressBar.getMax());
        progressBar.setProgress(progress);
        TextView sentView = fileSharingView.findViewById(R.id.tvSent);
        sentView.setText(getSize(progress));
    }


}

class FileR {
    public String fileName;
    public long fileSize;

    public FileR(String fileName, long fileSize) {
        this.fileName = fileName;
        this.fileSize = fileSize;
    }
}
