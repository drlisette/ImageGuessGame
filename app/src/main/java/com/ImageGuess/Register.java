package com.ImageGuess;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * ImageGuess Game
 * Register interface
 * Created by Stanislas, Lisette, Faustine on 2017/12 in SJTU.
 */

public class Register extends Activity {
    Button regButton;
    EditText userName;
    EditText passWord;
    JSONObject loginJSON=new JSONObject();
    private ClientSocket clientSocket;
    private String createUser;
    private String portNumber;
    private MyApp myApp;
    private String prompt;
    private String registerState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        regButton=(Button)findViewById(R.id.confirmRegister);
        regButton.setBackgroundResource(R.drawable.buttonview);
        userName=(EditText) findViewById(R.id.userName);
        passWord=(EditText)findViewById(R.id.passWord);
        myApp=(MyApp)getApplication();
        clientSocket = new ClientSocket(this, myApp.getServerIP(), myApp.getServerPort());

        //处理网络线程传来的消息，判断注册是否成功
        final Handler registerHandler = new Handler(new Handler.Callback(){
            public boolean handleMessage(Message msg){
                if (msg.what == 1) {
                    switch (registerState) {
                        case "001":
                            prompt = "新用户创建成功，请重新登录";
                            Toast.makeText(Register.this, prompt, Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(Register.this, LogIn.class);
                            startActivity(intent);
                            finish();
                            break;
                        case "002":
                            prompt = "用户名已存在，请重新输入";
                            Toast.makeText(Register.this, prompt, Toast.LENGTH_SHORT).show();
                            regButton.setBackgroundResource(R.drawable.buttonview);
                            break;
                        default:
                            break;
                    }
                }
                return false;
            }
        });

        regButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                regButton.setBackgroundResource(R.drawable.buttonview_down);
                try{
                    if (userName.length() == 0 | passWord.length() == 0){
                        prompt = "请输入正确的用户名和密码！";
                        Toast.makeText(Register.this, prompt, Toast.LENGTH_SHORT).show();
                        regButton.setBackgroundResource(R.drawable.buttonview);
                    }else{
                        //向服务器发送注册申请
                        loginJSON.put("passWord",passWord.getText());
                        loginJSON.put("userName",userName.getText());
                        loginJSON.put("infoState",0);
                        myApp.setUserName(userName.getText().toString());
                        clientSocket.InfoToServer(loginJSON.toString(),new ClientSocket.DataListener(){
                            @Override
                            public void transData() {
                                try {
                                    //接收服务器消息和注册状态
                                    createUser = clientSocket.getServerMessage();
                                    JSONObject message = new JSONObject(createUser);
                                    System.out.println(message.get("serverInfo").toString());
                                    registerState = message.get("loginState").toString();
                                    //接收消息传给handler
                                    Message msg = registerHandler.obtainMessage();
                                    msg.what = 1;
                                    registerHandler.sendMessage(msg);
                                }
                                catch (JSONException e){
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }
                catch(JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}

