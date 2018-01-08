package com.ImageGuess;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static android.content.ContentValues.TAG;

/**
 * ImageGuess Game
 * Room wait interface
 * Created by Stanislas, Lisette, Faustine on 2017/12 in SJTU.
 */

public class RoomWait extends Activity{
    private Button startGame;
    private TextView roomNumber;
    private TextView nameHost;
    private TextView namePlayer;
    private ImageView headHost;
    private ImageView headPlayer;
    private String localIP;
    private int localPort ;
    private static ClientSocket clientSocket;
    private MyApp myApp;
    private String gameData;
    private String prompt;
    private JSONArray wordsData = new JSONArray();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wait_game_start);
        roomNumber = (TextView) findViewById(R.id.currentRoomNumber);
        nameHost=(TextView)findViewById(R.id.hostName);
        namePlayer=(TextView)findViewById(R.id.playerName);
        headHost=(ImageView)findViewById(R.id.host);
        headPlayer=(ImageView)findViewById(R.id.player);
        myApp = (MyApp) getApplication();
        roomNumber.setText(myApp.getRoomNumber());
        clientSocket = new ClientSocket(this, myApp.getServerIP(), myApp.getServerPort());
        localPort = myApp.getPortNumber();
        localIP = clientSocket.getIp(this);

        //处理网络线程传来的消息，根据玩家状态处理事件
        final Handler startHandler = new Handler(new Handler.Callback(){
            public boolean handleMessage(Message msg){
                if (msg.what == 1){
                    //添加其他玩家头像
                    if (myApp.getRoomState().equals("host")) {
                        headPlayer.setBackgroundResource(R.drawable.picture2);
                        namePlayer.setText(myApp.getRemoteName());
                    }else if (myApp.getRoomState().equals("player")){
                        //开始游戏
                        myApp.setWordsData(wordsData);
                        setContentView(R.layout.main_activity);
                        Intent intent = new Intent(RoomWait.this, Join.class);
                        startActivity(intent);
                        finish();
                    }
                }
                return false;
            }
        });

        clientSocket.InfoReceiver(localPort, new ClientSocket.DataListener() {
            @Override
            public void transData() {
                try {
                    gameData = clientSocket.getGameData();
                    JSONObject message = new JSONObject(gameData);
                    System.out.println(message.toString());
                    //房主接收其他玩家端口号
                    if (myApp.getRoomState().equals("host")) {
                        if (message.get("remoteIP") != null && message.get("remotePort") != null){
                            myApp.setRemoteIP(message.get("remoteIP").toString());
                            myApp.setRemotePort(Integer.parseInt(message.get("remotePort").toString()));
                            myApp.setRemoteName(message.get("userName").toString());
                            Message msg = startHandler.obtainMessage();
                            msg.what = 1;
                            startHandler.sendMessage(msg);
                        }
                    }
                    //其他玩家接收开始指令
                    if(myApp.getRoomState().equals("player")){
                        if(message.get("startState").toString().equals("start")) {
                            wordsData = new JSONArray(message.get("words").toString());
                            clientSocket.InfoSender(myApp.getRemotePort(), myApp.getRemoteIP(), "close");
                            Message msg = startHandler.obtainMessage();
                            msg.what = 1;
                            startHandler.sendMessage(msg);
                        }
                    }
                }catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        if(myApp.getRoomState().equals("player")) {
            try {
                //其他玩家发送个人信息给房主，IP，端口，名字
                headPlayer.setBackgroundResource(R.drawable.picture2);
                namePlayer.setText(myApp.getUserName());
                nameHost.setText(myApp.getRemoteName());
                headHost.setBackgroundResource(R.drawable.picture1);
                JSONObject playerInfo = new JSONObject();
                playerInfo.put("remoteIP", localIP);
                playerInfo.put("remotePort",myApp.getPortNumber());
                playerInfo.put("userName",myApp.getUserName());
                clientSocket.InfoSender(myApp.getRemotePort(),myApp.getRemoteIP(),playerInfo.toString());
            }catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if(myApp.getRoomState().equals("host")) {
            headHost.setBackgroundResource(R.drawable.picture1);
            nameHost.setText(myApp.getUserName());
            startGame = (Button) findViewById(R.id.startGame);
            startGame.setBackgroundResource(R.drawable.buttonview);
            startGame.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        startGame.setBackgroundResource(R.drawable.buttonview_down);
                        if (myApp.getRemoteName().equals("undefine")){
                            startGame.setBackgroundResource(R.drawable.buttonview);
                            prompt = "等待其他玩家加入，请稍后再试！";
                            Toast.makeText(RoomWait.this, prompt, Toast.LENGTH_SHORT).show();
                        }else{
                            //房主发送开始指令
                            JSONObject playerInfo = new JSONObject();
                            playerInfo.put("startState", "start");
                            playerInfo.put("words", myApp.getWordsData().toString());
                            clientSocket.InfoSender(myApp.getRemotePort(), myApp.getRemoteIP(), playerInfo.toString());
                            try{
                                Thread.sleep(500);
                            }catch(InterruptedException e){
                                e.printStackTrace();
                            }
                            clientSocket.InfoSender(myApp.getRemotePort(), myApp.getRemoteIP(), "close");
                            setContentView(R.layout.main_activity);
                            Intent intent = new Intent(RoomWait.this, Play.class);
                            startActivity(intent);
                            finish();
                        }
                    }catch (JSONException e) {
                        e.printStackTrace();
                    }
                    startGame.setBackgroundResource(R.drawable.buttonview);
                }
            });
        }
    }

    //屏蔽返回键
    @Override
    public boolean onKeyDown(int keycode, KeyEvent event){
        switch(keycode){
            case KeyEvent.KEYCODE_BACK:
                return true;
            default:
                return false;
        }
    }
}
