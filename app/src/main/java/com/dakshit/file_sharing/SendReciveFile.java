package com.dakshit.file_sharing;

import android.os.Environment;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class SendReciveFile extends Thread {
    private Socket socket;
    public boolean status = false;
    private InputStream inputStream;
    private OutputStream outputStream;
    private DataOutputStream dos;
    private DataInputStream dis;
    public static final int BUFFER_SIZE = 4096;  //4kb at time
    private static final String PARENT_FOLDER = Environment.getExternalStorageDirectory().getAbsolutePath() + "/fileSharing";

    public SendReciveFile(Socket sock) {
        this.socket = sock;
        status = socket != null;
        try {
            if (status) {
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
                dos = new DataOutputStream(outputStream);
                dis = new DataInputStream(inputStream);
            }
        } catch (IOException e) {

        }
        File parent = new File(PARENT_FOLDER);
        if (!parent.exists()) {
            parent.mkdir();
        }
    }

    @Override
    public void run() {
        super.run();
        int bytes;
        byte[] data = new byte[BUFFER_SIZE];


        while (socket != null) {
            try {
                long fileSize = dis.readLong();
                String fileName = dis.readUTF();
                File file = new File(PARENT_FOLDER + "/" + fileName);
                FileOutputStream fos = new FileOutputStream(file);
                while ((bytes = inputStream.read(data, 0, (int) (Math.min(fileSize, BUFFER_SIZE)))) > 0) {
                    fileSize -= bytes;
                    fos.write(data, 0, bytes);
                }
                fos.close();
                dos.writeBoolean(true);
            } catch (IOException e) {

            }
        }
    }

    public boolean send(String url) {
        File file = new File(url);
        boolean isSucssesTransfer = false;
        if (!file.exists()) return false;
        try {
            long fileSize = file.length();
            String fileName = file.getName();
            dos.writeLong(fileSize);
            dos.writeUTF(fileName);
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[BUFFER_SIZE];

            while (fis.read(data) > 0) {
                outputStream.write(data);
            }
            isSucssesTransfer = dis.readBoolean();
            fis.close();
        } catch (IOException e) {

        }
        return isSucssesTransfer;
    }
}
