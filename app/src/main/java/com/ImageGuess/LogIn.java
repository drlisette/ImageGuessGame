package com.ImageGuess;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * ImageGuess Game
 * Login interface
 * Created by Stanislas, Lisette, Faustine on 2017/12 in SJTU.
 */

public class LogIn extends Activity {
    private Button confirmButton;
    private EditText userName;
    private EditText password;
    private JSONObject loginJSON=new JSONObject();
    private ClientSocket clientSocket;
    private String createUser;
    private int portNumber;
    private MyApp myApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_in);

        confirmButton = (Button)findViewById(R.id.confirmLogIn);
        userName  = (EditText) findViewById(R.id.userName);
        password = (EditText)findViewById(R.id.password);
        myApp = (MyApp)getApplication();
        clientSocket = new ClientSocket(this, myApp.getServerIP(), myApp.getServerPort());

        confirmButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(LogIn.this,Welcome.class);
                try{
                    loginJSON.put("infoState",1);  //infoState 1 represents a login behaviour.
                    loginJSON.put("passWord",password.getText());
                    loginJSON.put("userName",userName.getText());
                    myApp.setUserName(userName.getText().toString());
                    clientSocket.InfoToServer(loginJSON.toString(), new ClientSocket.DataListener(){
                        @Override
                        public void transData() {
                            try {
                                createUser = clientSocket.getServerMessage();
                                JSONObject message = new JSONObject(createUser);
                                System.out.println(message.get("serverInfo").toString());  //Print server's return information.
                                portNumber = Integer.parseInt(message.get("portNumber").toString());  //Get port number for UDP receiver.
                                myApp.setPortNumber(portNumber);
                            }
                            catch (JSONException e){
                                e.printStackTrace();
                            }
                        }
                    });
                }
                catch(JSONException e) {
                    e.printStackTrace();
                }
                startActivity(intent);
            }
        });
    }
}
