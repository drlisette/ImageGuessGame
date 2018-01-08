package com.ImageGuess;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * ImageGuess Game
 * Game finish page
 * Created by Stanislas, Lisette, Faustine on 2017/12 in SJTU.
 */


public class GameFinish extends Activity {
    private Button returnButton;
    private TextView name1, name2;
    private TextView score1, score2;
    private ImageView logo1, logo2;
    private MyApp myApp;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_finish);
        myApp = (MyApp) getApplication();
        returnButton=(Button)findViewById(R.id.returnButton);
        returnButton.setBackgroundResource(R.drawable.buttonview);
        name1 = (TextView)findViewById(R.id.name1);
        name2 = (TextView)findViewById(R.id.name2);
        logo1 = (ImageView)findViewById(R.id.logo1);
        logo2 = (ImageView)findViewById(R.id.logo2);
        //根据个人信息设置头像，名字和得分
        if (myApp.getPlayerState().equals("1005")){
            logo1.setBackgroundResource(R.drawable.picture1);
            logo2.setBackgroundResource(R.drawable.picture2);
        }else{
            logo1.setBackgroundResource(R.drawable.picture2);
            logo2.setBackgroundResource(R.drawable.picture1);
        }
        name1.setText(myApp.getUserName());
        name2.setText(myApp.getRemoteName());
        score1 = (TextView)findViewById(R.id.score1);
        score2 = (TextView)findViewById(R.id.score2);
        score1.setText(Integer.toString(myApp.getUserScore()) + "分");
        score2.setText(Integer.toString(myApp.getRemoteScore()) + "分");

        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GameFinish.this, Welcome.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
