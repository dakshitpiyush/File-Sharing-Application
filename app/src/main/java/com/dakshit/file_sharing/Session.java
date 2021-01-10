package com.dakshit.file_sharing;

import android.net.wifi.p2p.WifiP2pInfo;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


public class Session extends Thread {
    private ArrayList<String> selectedFileList;
    private WifiP2pInfo info;
    private Handler handler;

    public Session(WifiP2pInfo info, ArrayList<String> selectedFileList, Handler handler) {
        this.selectedFileList = selectedFileList;
        this.info = info;
        this.handler = handler;
    }

    @Override
    public void run() {
        Socket socket = null;
        try {
            if (info.groupFormed && info.isGroupOwner) {
                ServerSocket sc = new ServerSocket(8000);
                socket = sc.accept();
            } else {
                socket = new Socket();
                //todo:decide best statergy to avoid port already used situation
                socket.connect(new InetSocketAddress(info.groupOwnerAddress.getHostName(), 8000), 1000);
            }
            Message msg = handler.obtainMessage(1, socket);
            msg.setTarget(handler);
            msg.sendToTarget();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
