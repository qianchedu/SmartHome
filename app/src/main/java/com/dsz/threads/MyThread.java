package com.dsz.threads;

import android.os.Bundle;
import android.os.Message;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import static android.R.attr.port;

/**
 * Created by Administrator on 2016/10/12.
 */
public class MyThread extends Thread{
    private String txt;
    private Socket socket;


    private int port = 30000;
    private String ip;
    public MyThread(String txt,String ip) {
        this.txt = txt;
        this.ip = ip;
    }

    @Override
    public void run() {
        Message msg = new Message();
        msg.what = 0x11;
        Bundle bundle = new Bundle();
        bundle.clear();
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(ip,port),1000);

            OutputStream out = socket.getOutputStream();
//            DataInputStream dataInput = new DataInputStream(socket.getInputStream());

            out.write(txt.getBytes("gbk"));
            out.flush();

            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
