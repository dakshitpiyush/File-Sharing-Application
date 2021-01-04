package com.dakshit.file_sharing;

import android.net.wifi.p2p.WifiP2pInfo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Session extends Thread {
    ArrayList<String> selectedFileList;
    WifiP2pInfo info;

    public Session(WifiP2pInfo info, ArrayList<String> selectedFileList) {
        this.selectedFileList = selectedFileList;
        this.info = info;
    }

    @Override
    public void run() {
        Socket socket = null;
        try {
            if (info.groupFormed && info.isGroupOwner) {
                ServerSocket sc = new ServerSocket();
                socket = sc.accept();
            } else {
                socket = new Socket();
                socket.connect(new InetSocketAddress(info.groupOwnerAddress.getHostName(), 8888), 500);
            }
            SendReciveFile sendReciveFile = new SendReciveFile(socket);
            if (sendReciveFile.status) {
                sendReciveFile.start();
                if (selectedFileList != null) {
                    for (int i = 0; i < selectedFileList.size(); i++) {
                        String url = selectedFileList.get(i);
                        boolean status = sendReciveFile.send(url);
                        //todo:set and handler and give info about success or failure
                    }
                }
            }
        } catch (IOException e) {
            //todo:set an handler and show message via handler
        }
    }
}
