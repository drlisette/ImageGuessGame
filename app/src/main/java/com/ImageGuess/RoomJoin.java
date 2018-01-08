package com.ImageGuess;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * ImageGuess Game
 * Room join interface
 * Created by Stanislas, Lisette, Faustine on 2017/12 in SJTU.
 */

public class RoomJoin extends Activity {
    private Button confirmButton;
    private EditText roomNumber;
    private JSONObject joinRoomJSON =new JSONObject();
    private ClientSocket clientSocket;
    private String joinSuccess;
    private MyApp myApp;
    private String prompt;
    private String roomJoinState = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.room_join);
        confirmButton = (Button) findViewById(R.id.confirmJoinRoom);
        confirmButton.setBackgroundResource(R.drawable.buttonview);
        roomNumber = (EditText) findViewById(R.id.roomNumber);
        myApp=(MyApp)getApplication();
        myApp.setPlayerState("1006");
        clientSocket = new ClientSocket(this, myApp.getServerIP(), myApp.getServerPort());

        //处理网络线程传来的消息，判断加入房间是否成功
        final Handler joinHandler = new Handler(new Handler.Callback(){
            public boolean handleMessage(Message msg){
                if (msg.what == 1){
                    try{
                        switch (roomJoinState) {
                            case "102":
                                myApp.setRoomState("player");
                                prompt = "成功加入房间" + joinRoomJSON.get("roomNumber").toString();
                                Toast.makeText(RoomJoin.this, prompt, Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(RoomJoin.this, RoomWait.class);
                                startActivity(intent);
                                finish();
                                break;
                            case "103":
                                prompt = "房间号不存在，请重新输入";
                                Toast.makeText(RoomJoin.this, prompt, Toast.LENGTH_SHORT).show();
                                confirmButton.setBackgroundResource(R.drawable.buttonview);
                                break;
                            case "104":
                                prompt = "房间人数已满，请重新输入";
                                Toast.makeText(RoomJoin.this, prompt, Toast.LENGTH_SHORT).show();
                                confirmButton.setBackgroundResource(R.drawable.buttonview);
                                break;
                            default:
                                break;
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
                return false;
            }
        });

        roomNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    joinRoomJSON.put("roomNumber", s);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }


            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    confirmButton.setBackgroundResource(R.drawable.buttonview_down);
                    if (roomNumber.length() == 0){
                        prompt = "请输入房间号！";
                        Toast.makeText(RoomJoin.this, prompt, Toast.LENGTH_SHORT).show();
                        confirmButton.setBackgroundResource(R.drawable.buttonview);
                    }else {
                        //向服务器发送加入房间申请
                        joinRoomJSON.put("userName", myApp.getUserName());
                        joinRoomJSON.put("infoState", 3);
                        clientSocket.InfoToServer(joinRoomJSON.toString(), new ClientSocket.DataListener() {
                            @Override
                            public void transData() {
                                try {
                                    //接收服务器消息和房间状态
                                    joinSuccess = clientSocket.getServerMessage();
                                    JSONObject message = new JSONObject(joinSuccess);
                                    System.out.println(message.toString());
                                    roomJoinState = message.get("roomJoinState").toString();
                                    //接收消息传给handler
                                    Message msg = joinHandler.obtainMessage();
                                    msg.what = 1;
                                    joinHandler.sendMessage(msg);
                                    myApp.setRemoteIP(message.get("remoteIP").toString());
                                    myApp.setRemotePort(Integer.parseInt(message.get("remotePort").toString()));
                                    myApp.setRemoteName(message.get("hostName").toString());
                                    myApp.setRoomNumber(roomNumber.getText().toString());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                confirmButton.setBackgroundResource(R.drawable.buttonview);
            }
        });
    }

}
