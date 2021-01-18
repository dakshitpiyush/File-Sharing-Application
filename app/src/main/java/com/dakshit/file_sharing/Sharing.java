package com.dakshit.file_sharing;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
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
    private LinearLayout scroll;
    private WifiP2pInfo info;
    private ArrayList<String> selectedFileList = new ArrayList<>();
    private Handler handler;
    private LayoutInflater layoutInflater;
    private int curSend = 0;
    private ArrayList<View> sendViewList = new ArrayList<>();
    private View curReceiveView;
    public final int BUFFER_SIZE=4096;
    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel channel;
    private Socket socket;
    private ServerSocket sc=null;

    public static final int SOCKET_CREATED=1;
    public static final int FILE_RECEIVING=2;
    public static final int FILE_SENT=3;
    public static final int DATA_PART_RECEIVED=4;
    public static final int DATA_PART_SENT=5;

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

        wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = wifiP2pManager.initialize(this, getMainLooper(), null);
        Thread session=new Thread(){
            @Override
            public void run() {
                try {
                    if (info.groupFormed && info.isGroupOwner) {
                        Log.v("sc","server created");
                        sc = new ServerSocket();
                        sc.setReuseAddress(true);
                        sc.bind(new InetSocketAddress(8069));
                        socket = sc.accept();

                    } else {
                        socket = new Socket();
                        Log.v("sc","client created");
                        //todo:decide best statergy to avoid port already used situation
                         socket.connect(new InetSocketAddress(info.groupOwnerAddress.getHostName(), 8069), 1000);

                    }
                    Message msg = handler.obtainMessage(SOCKET_CREATED, socket);
                    msg.setTarget(handler);
                    msg.sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                switch (msg.what) {
                    case SOCKET_CREATED:
                        startSharing();
                        break;
                    case FILE_RECEIVING:
                        FileR fileR = (FileR) msg.obj;
                        receive(fileR.fileName, fileR.fileSize);
                        break;
                    case FILE_SENT:
                        //Todo: kaytari kara file gelyavar
                        break;
                    case DATA_PART_RECEIVED:
                        makeProgress(false);
                        break;
                    case DATA_PART_SENT:
                        makeProgress(true);
                        break;
                    case 6:
//                        Toast.makeText(getApplicationContext(), "tuza sender palala", Toast.LENGTH_LONG).show();
//                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//                        navigateUpTo(intent);
                        break;
                }
                return true;
            }

        });
        if(selectedFileList!=null)
        for(int i=0; i<selectedFileList.size();i++){
            drawSend(selectedFileList.get(i));
        }


        session.start();
        Log.v("starting", "sharing activty created");
    }

    private void startSharing() {
        InputStream inputStream=null;
        OutputStream outputStream=null;
        DataInputStream dis=null;
        DataOutputStream dos = null;
        String parentFolder;
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
//            parentFolder="/";
            parentFolder = getApplicationContext().getExternalFilesDir(null).getPath();
        }else{
            parentFolder=Environment.getExternalStorageDirectory().getAbsolutePath() + "/fileSharing";
            File file = new File(parentFolder);
            if(!file.exists()){
                file.mkdir();
            }
            parentFolder += "/";
        }


        try{
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            dos = new DataOutputStream(outputStream);
            dis = new DataInputStream(inputStream);
        }
        catch(Exception e){
            e.printStackTrace();
        }

        DataInputStream finalDis = dis;
        InputStream finalInputStream = inputStream;
        String finalParentFolder = parentFolder;
        Thread receive=new Thread(){
            @Override
            public void run() {

                int bytes;
                byte[] data = new byte[BUFFER_SIZE];
                while (socket!=null && !socket.isClosed()) {
                    try {
                        long fileSize = finalDis.readLong();
                        String fileName = finalDis.readUTF();
                        FileR fileR = new FileR(fileName, fileSize);
                        Message msg = handler.obtainMessage(FILE_RECEIVING, fileR);
                        msg.setTarget(handler);
                        msg.sendToTarget();
                        File file = new File(finalParentFolder + fileName);
                        FileOutputStream fos = new FileOutputStream(file);
                        while ((bytes = finalInputStream.read(data, 0, (int)Math.min(BUFFER_SIZE, fileSize))) !=-1 && fileSize>0) {
                            fileSize -= bytes;
                            fos.write(data, 0, bytes);
                            handler.obtainMessage(DATA_PART_RECEIVED).sendToTarget();
                        }
                        fos.close();
                    }catch (EOFException e){
                        handler.obtainMessage(6).sendToTarget();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        DataOutputStream finalDos = dos;
        OutputStream finalOutputStream = outputStream;
        Thread send=new Thread(){
            @Override
            public void run() {
                File file;
                while(socket!=null && !socket.isClosed()) {
                    try {
                        if(selectedFileList==null || curSend>=selectedFileList.size()){
                            break;
                        }
                        file=new File(selectedFileList.get(curSend));
                        if(!file.exists()){
                            curSend++;
                            continue;
                        }
                        long fileSize = file.length();
                        String fileName = file.getName();
                        finalDos.writeLong(fileSize);
                        finalDos.writeUTF(fileName);
                        FileInputStream fis = new FileInputStream(file);
                        byte[] data = new byte[BUFFER_SIZE];

                        while (fis.read(data) != -1) {
                            finalOutputStream.write(data, 0, (int)Math.min(fileSize, data.length));
                            fileSize-=BUFFER_SIZE;
                            Message msg = handler.obtainMessage(DATA_PART_SENT);
                            msg.setTarget(handler);
                            msg.sendToTarget();
                        }
                        fis.close();
                        Message msgS = handler.obtainMessage(FILE_SENT);
                        msgS.setTarget(handler);
                        msgS.sendToTarget();
                        curSend++;
                    } catch (IOException e) {
                            e.printStackTrace();
                    }
                }
            }
        };

        receive.start();
        send.start();
    }
    public void addFile(View view){
        Log.v("btn presse","file add call");
        //Todo: code for adding file

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
                if(icons.containsKey(fileName.substring(pos + 1))){
                    icon.setImageResource(icons.get(fileName.substring(pos +1)));
                }
                else{
                    icon.setImageResource(R.drawable.unknown);
                }
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
            if(icons.containsKey(fileName.substring(pos + 1))){
                icon.setImageResource(icons.get(fileName.substring(pos +1)));
            }
            else{
                icon.setImageResource(R.drawable.unknown);
            }
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }



    private void makeProgress(boolean isSend) {
        if(selectedFileList!=null && curSend>=selectedFileList.size()){
            return;
        }
        int progress;
        View fileSharingView;
        fileSharingView = isSend ? sendViewList.get(curSend) : curReceiveView;
        ProgressBar progressBar = (ProgressBar) fileSharingView.findViewById(R.id.pbrSent);
        progress = Math.min(progressBar.getProgress() + BUFFER_SIZE, progressBar.getMax());
        progressBar.setProgress(progress);
        TextView sentView = fileSharingView.findViewById(R.id.tvSent);
        sentView.setText(getSize(progress));
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.v("restarting", "restarting sharing activity but whayii");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (socket !=  null){
                socket.close();
            }
            if(sc != null){
                sc.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        wifiP2pManager.removeGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(getApplicationContext(), "group removed", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(getApplicationContext(), "Not removed error code"+ String.valueOf(reason), Toast.LENGTH_LONG).show();
            }
        });
        Log.v("destroy", "activity destroye");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.v("sharing", "activity is stoping");
//        try {
//            if (socket != null) socket.close();
//            if (sc != null) sc.close();
//        }catch(IOException e){
//
//        }
//        wifiP2pManager.removeGroup(channel, new WifiP2pManager.ActionListener() {
//            @Override
//            public void onSuccess() {
//                Toast.makeText(getApplicationContext(), "group removed", Toast.LENGTH_LONG).show();
//            }
//
//            @Override
//            public void onFailure(int reason) {
//                Toast.makeText(getApplicationContext(), "Not removed error code"+ String.valueOf(reason) , Toast.LENGTH_LONG).show();
//            }
//        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v("sharing", "actvity resumed");
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
