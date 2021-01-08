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
            SendReciveFile sendReciveFile = new SendReciveFile(socket, handler);
            if (sendReciveFile.status) {
                sendReciveFile.start();
                if (selectedFileList != null) {
                    for (int i = 0; i < selectedFileList.size(); i++) {
                        String url = selectedFileList.get(i);
                        Message msg;
                        if (sendReciveFile.send(url)) {
                            msg = handler.obtainMessage(1, url);
                        } else {
                            msg = handler.obtainMessage(2, url);
                        }
                        msg.setTarget(handler);
                        msg.sendToTarget();
                    }
                }
            }
        } catch (IOException e) {
            //todo:set an handler and show message via handler
            e.printStackTrace();
        }
    }
}
