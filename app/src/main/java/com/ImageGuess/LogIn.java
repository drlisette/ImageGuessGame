package com.ImageGuess;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import javax.security.auth.login.LoginException;

/**
 * ImageGuess Game
 * Login interface
 * Created by Stanislas, Lisette, Faustine on 2017/12 in SJTU.
 */

public class LogIn extends Activity {
    private Button loginButton;
    private Button registerButton;
    private EditText userName;
    private EditText passWord;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private CheckBox rememberPass;
    private JSONObject loginJSON=new JSONObject();
    private ClientSocket clientSocket;
    private String createUser;
    private int portNumber;
    private MyApp myApp;
    private String prompt;
    private String loginState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_in);

        loginButton = (Button)findViewById(R.id.confirmLogIn);
        loginButton.setBackgroundResource(R.drawable.buttonview);
        registerButton = (Button)findViewById(R.id.register);
        registerButton.setBackgroundResource(R.drawable.buttonview);
        userName  = (EditText) findViewById(R.id.userName);
        passWord = (EditText)findViewById(R.id.passWord);
        pref= PreferenceManager.getDefaultSharedPreferences(this);
        rememberPass=(CheckBox)findViewById(R.id.rememberPassword);

        //记住密码
        boolean isRemember = pref.getBoolean("remember_password",false);
        if(isRemember){
            String account=pref.getString("account","");
            String password=pref.getString("password","");
            userName.setText(account);
            passWord.setText(password);
            rememberPass.setChecked(true);
        }
        myApp = (MyApp)getApplication();
        clientSocket = new ClientSocket(this, myApp.getServerIP(), myApp.getServerPort());

        //处理网络线程传来的消息，判断登陆是否成功
        final Handler loginHandler = new Handler(new Handler.Callback(){
            public boolean handleMessage(Message msg){
                if (msg.what == 1) {
                    switch (loginState) {
                        case "001":
                            prompt = "登陆成功！";
                            myApp.setPortNumber(portNumber);
                            Toast.makeText(LogIn.this, prompt, Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LogIn.this, Welcome.class);
                            loginButton.setBackgroundResource(R.drawable.buttonview);
                            startActivity(intent);
                            finish();
                            break;
                        case "002":
                            prompt = "用户名不存在，请重新输入！";
                            Toast.makeText(LogIn.this, prompt, Toast.LENGTH_SHORT).show();
                            loginButton.setBackgroundResource(R.drawable.buttonview);
                            break;
                        case "003":
                            prompt = "密码错误，请重新输入！";
                            Toast.makeText(LogIn.this, prompt, Toast.LENGTH_SHORT).show();
                            loginButton.setBackgroundResource(R.drawable.buttonview);
                            break;
                        default:
                            break;
                    }
                }
                return false;
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                loginButton.setBackgroundResource(R.drawable.buttonview_down);
                try{
                    if (userName.length() == 0 | passWord.length() == 0){
                        prompt = "请输入正确的用户名和密码！";
                        Toast.makeText(LogIn.this, prompt, Toast.LENGTH_SHORT).show();
                        loginButton.setBackgroundResource(R.drawable.buttonview);
                    }else{
                        //向服务器发送登陆申请
                        loginJSON.put("passWord",passWord.getText());
                        loginJSON.put("userName",userName.getText());
                        loginJSON.put("infoState",1);  //infoState 1 represents a login behaviour.
                        myApp.setUserName(userName.getText().toString());
                        clientSocket.InfoToServer(loginJSON.toString(), new ClientSocket.DataListener(){
                            @Override
                            public void transData() {
                                try {
                                    //接收服务器消息和登陆状态
                                    createUser = clientSocket.getServerMessage();
                                    JSONObject message = new JSONObject(createUser);
                                    System.out.println(message.get("serverInfo").toString());  //Print server's return information.
                                    loginState = message.get("loginState").toString();
                                    //接收消息传给handler
                                    Message msg = loginHandler.obtainMessage();
                                    msg.what = 1;
                                    loginHandler.sendMessage(msg);
                                    portNumber = Integer.parseInt(message.get("portNumber").toString());  //Get port number for UDP receiver.
                                }
                                catch (JSONException e){
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                } catch(JSONException e) {
                    e.printStackTrace();
                }
                editor=pref.edit();
                if(rememberPass.isChecked()){
                    editor.putBoolean("remember_password",true);
                    editor.putString("account",userName.getText().toString());
                    editor.putString("password",passWord.getText().toString());
                }else{
                    editor.clear();
                }
                editor.apply();
            }
        });
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LogIn.this, Register.class);
                registerButton.setBackgroundResource(R.drawable.buttonview_down);
                startActivity(intent);
                registerButton.setBackgroundResource(R.drawable.buttonview);
            }
        });
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