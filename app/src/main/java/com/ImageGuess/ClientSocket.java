package com.ImageGuess;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import static android.content.ContentValues.TAG;

/**
 * ImageGuess Game
 * Client socket package
 * Created by Stanislas, Lisette, Faustine on 2017/12 in SJTU.
 */

public class ClientSocket {
    private String gameData;
    private String fromServer;
    private UDPReceiver udpReceiver;
    private String serverIP ;
    private int serverPort;


    ClientSocket(Context context, String serverIP, int serverPort){
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        Log.i(TAG, "IP address is: " + serverIP);
    }

    //基于TCP的对服务器通信
    public void InfoToServer(String sendMessage,DataListener dataListener){
        TCPSocket tcpSocket = new TCPSocket(serverIP, serverPort, sendMessage,dataListener);
        new Thread(tcpSocket).start();
    }

    //基于UDP的消息接收
    public void InfoReceiver(int portNumber, DataListener dataListener){
        udpReceiver = new UDPReceiver(portNumber, dataListener);
        new Thread(udpReceiver).start();
    }

    //基于UDP的消息发送
    public void InfoSender(int portNumber, String ipAddress, String gameData){
        UDPSender udpSender = new UDPSender(portNumber, ipAddress, gameData);
        new Thread(udpSender).start();
    }

    //消息回调接口
    public interface DataListener{
        void transData() throws IOException;
    }

    //获得服务器信息
    public  String getServerMessage(){
        synchronized (this){
            return  this.fromServer;
        }
    }

    //获得接收数据
    public  String getGameData(){
        synchronized(this){
            return this.gameData;
        }
    }

    //获取当前本机IP地址
    public static String getIp(final Context context) {
        String ip = null;
        ConnectivityManager conMan = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        // mobile 3G Data Network
        android.net.NetworkInfo.State mobile = conMan.getNetworkInfo(
                ConnectivityManager.TYPE_MOBILE).getState();
        // wifi
        android.net.NetworkInfo.State wifi = conMan.getNetworkInfo(
                ConnectivityManager.TYPE_WIFI).getState();

        // 如果3G网络和wifi网络都未连接，且不是处于正在连接状态 则进入Network Setting界面 由用户配置网络连接
        if (mobile == android.net.NetworkInfo.State.CONNECTED
                || mobile == android.net.NetworkInfo.State.CONNECTING) {
            ip =  getLocalIpAddress();
        }
        if (wifi == android.net.NetworkInfo.State.CONNECTED
                || wifi == android.net.NetworkInfo.State.CONNECTING) {
            //获取wifi服务
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            //判断wifi是否开启
            if (!wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
            }
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int ipAddress = wifiInfo.getIpAddress();
            ip =(ipAddress & 0xFF ) + "." +
                    ((ipAddress >> 8 ) & 0xFF) + "." +
                    ((ipAddress >> 16 ) & 0xFF) + "." +
                    ( ipAddress >> 24 & 0xFF) ;
        }
        return ip;
    }

    private static String getLocalIpAddress()
    {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) { //获取IPv4的IP地址
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    //TCP socket类
    public class TCPSocket implements Runnable {

        private int serverPort;
        private String serverIP;
        private String sendMessage;
        private DataListener dataListener;

        TCPSocket(String serverIP, int serverPort, String message,DataListener dataListener){
            this.serverIP = serverIP;
            this.serverPort = serverPort;
            this.sendMessage = message;
            this.dataListener =dataListener;
        }

        @Override
        public void run() {
            try{
                Socket socket = new Socket(serverIP, serverPort);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println(sendMessage);
                socket.shutdownOutput();
                //out.close();
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                fromServer =in.readLine();
                dataListener.transData();
                socket.close();
            } catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    //UDP 发送类
    public class UDPSender implements Runnable {

        private int portNumber;
        private byte[] rawData;
        private String ipAddress;
        private String gameData;

        public UDPSender(int portNumber, String ipAddress, String gameData) {
            this.portNumber = portNumber;
            this.ipAddress = ipAddress;
            this.gameData = gameData;
        }

        @Override
        public void run() {
            try{
                rawData = new byte[4096];
                DatagramSocket clientSocket = new DatagramSocket();
                InetAddress IP = InetAddress.getByName(ipAddress);
                try{
                    rawData = gameData.getBytes("UTF-8");
                    DatagramPacket sendPacket = new DatagramPacket(rawData, rawData.length, IP, portNumber);
                    clientSocket.send(sendPacket);
                    clientSocket.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }catch (SocketException | UnknownHostException s){
                s.printStackTrace();
            }
        }
    }

    //UDP接收类
    public class UDPReceiver implements Runnable {

        private int portNumber;
        private DataListener dataListener;
        private byte[] rawData;
        public volatile boolean exit = false;


        UDPReceiver(int portNumber, DataListener dataListener) {
            this.portNumber = portNumber;
            this.dataListener = dataListener;
        }

        @Override
        public void run() {
            try {
                DatagramSocket serverSocket = new DatagramSocket(portNumber);
                Log.i(TAG, "Receiving......");
                while (!exit) {
                    try {
                        rawData = new byte[4096];
                        DatagramPacket receivePacket = new DatagramPacket(rawData, rawData.length);
                        serverSocket.receive(receivePacket);
                        gameData = new String(receivePacket.getData(), 0, receivePacket.getLength(), "UTF-8");
                        if (gameData .equals("close")){
                            serverSocket.close();
                            Log.i(TAG, "Socket closed.");
                            break;
                        }
                        dataListener.transData();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            } catch (SocketException s) {
                s.printStackTrace();
            }
        }
    }
}
