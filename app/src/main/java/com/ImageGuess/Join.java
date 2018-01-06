package com.ImageGuess;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

/**
 * ImageGuess Game
 * Join board interface
 * Created by Stanislas, Lisette, Faustine on 2017/12 in SJTU.
 */

public class Join extends Activity {

    private Paint drawPaint;
    private float posX, posY;
    private float paintWidth = 12;
    private int paintColor = Color.BLACK;
    private String gameData;
    private JSONObject gameDataJSON;
    private TextView currentDrawer;
    private LinearLayout paletteView;
    private TextView cueWord;
    private TextView timeShow;
    private TextView currentRoomNumber;
    private EditText answer;
    private Button sendAnswerButton;
    private ArrayList<TextView> guesserList;
    private ArrayList<TextView> scoreGuesserList;
    private ArrayList<Integer> scoreNumList; //各玩家分数
    private ArrayList<String> wordsUsed;
    private int localPort ;
    private int actionState = 10000;
    private String localIP;
    private String remoteIP;
    private ClientSocket clientSocket;
    private MyApp myApp;
    private JSONObject endJSON;
    private static final float ERASE_WIDTH = 150;
    private static final int ACTION_DOWN = 10000;
    private static final int ACTION_MOVE = 10001;
    private static final int ACTION_UP = 10002;
    private static final String CREATE_ROOM="create_room";
    private static final String CURRENT_DRAWER="current_drawer";
    private static final String SCORE_LIST="score_list";
    private static final String WORDS_USED="used_words";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play);
        cueWord = (TextView) findViewById(R.id.currentWord);
        currentRoomNumber = (TextView) findViewById(R.id.currentRoomNumber);
        //currentRoomNumber.setText(this.getIntent().getStringExtra(CREATE_ROOM));  // no intent. use myapp
        currentDrawer = (TextView) findViewById(R.id.playerNumber);
        sendAnswerButton = (Button) findViewById(R.id.sendAnswerButton);
        timeShow = (TextView) findViewById(R.id.timer);
        paletteView = (LinearLayout) findViewById(R.id.paletteView);
        answer = (EditText) findViewById(R.id.answer);
        final JoinView joinView=new JoinView(this);
        paletteView.addView(joinView);
        cueWord.setText("a kind of fruit");
        myApp=(MyApp)getApplication();
        //myApp.setPlayerState("1006");
        //myApp.setRoomState("1001");
        clientSocket = new ClientSocket(this, myApp.getServerIP(), myApp.getServerPort());
        init();
        timer.start();

        localIP = clientSocket.getIp(this);
        localPort=myApp.getPortNumber();
        remoteIP = myApp.getRemoteIP();
        clientSocket.InfoReceiver(localPort, new ClientSocket.DataListener() {
            @Override
            public void transData() {
                gameData = clientSocket.getGameData();
                Log.i(TAG, gameData);
                synchronized (this){
                    try{
                        gameDataJSON = new JSONObject(gameData);
                        posX = Float.valueOf(gameDataJSON.get("posX").toString());
                        posY = Float.valueOf(gameDataJSON.get("posY").toString());
                        paintWidth = Integer.valueOf(gameDataJSON.get("width").toString());
                        paintColor = Integer.valueOf(gameDataJSON.get("color").toString());
                        drawPaint.setColor(paintColor);
                        drawPaint.setStrokeWidth(paintWidth);
                        if (paintColor == Color.WHITE){
                            drawPaint.setStrokeWidth(ERASE_WIDTH);
                        }
                        actionState = Integer.valueOf(gameDataJSON.get("actionState").toString());
                        joinView.RefreshView(actionState);
                    } catch (JSONException e){
                        e.printStackTrace();
                    }
                }
                //Log.i(TAG, gameData);
            }
        });

    }

    /*
    private void updateGameStatus(){
        int currentDrawerId=this.getIntent().getIntExtra(CURRENT_DRAWER,1); //默认当前画画的是玩家1
        currentDrawer.setText(String.valueOf(currentDrawerId));
        guesserList=new ArrayList<TextView>();
        Log.i(TAG,"updateGameStatus");
        guesserList.add((TextView)findViewById(R.id.guesser_1));
        guesserList.add((TextView)findViewById(R.id.guesser_2));
        guesserList.add((TextView)findViewById(R.id.guesser_3));
        guesserList.add((TextView)findViewById(R.id.guesser_4));
        guesserList.add((TextView)findViewById(R.id.guesser_5));
        scoreGuesserList=new ArrayList<TextView>();
        scoreGuesserList.add((TextView)findViewById(R.id.score_guesser_1));
        scoreGuesserList.add((TextView)findViewById(R.id.score_guesser_2));
        scoreGuesserList.add((TextView)findViewById(R.id.score_guesser_3));
        scoreGuesserList.add((TextView)findViewById(R.id.score_guesser_4));
        scoreGuesserList.add((TextView)findViewById(R.id.score_guesser_5));

        scoreNumList=this.getIntent().getIntegerArrayListExtra(SCORE_LIST);  //6位玩家的分数，下标对应
        int j=0;
        for(int i=0;i<5;++i){
            if((j+1)==currentDrawerId)  //当前轮画画的玩家信息不在下方显示
                ++j;
            guesserList.get(i).setText(String.valueOf(j+1));
            scoreGuesserList.get(i).setText(String.valueOf(scoreNumList.get(j)));
            ++j;
        }
        wordsUsed=this.getIntent().getStringArrayListExtra(WORDS_USED);
    }
    */

    private CountDownTimer timer=new CountDownTimer(5000,1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            timeShow.setText(millisUntilFinished/1000+"秒");
        }

        @Override
        public void onFinish() { //时间到，换下一个玩家画画,销毁当前activity，新开activity
            timeShow.setText("时间到！");
            timeShow.setTextColor(Color.RED);
            if (myApp.getGameRound() > 0) {
                myApp.setGameRound(myApp.getGameRound() - 1);
                Intent intent = new Intent(Join.this, Play.class);
                startActivity(intent);
            } else{
                try{
                    endJSON.put("infoState",4);  //infoState 4 represents destroy of room.
                    endJSON.put("roomNumber",myApp.getRoomNumber());
                    clientSocket.InfoToServer(endJSON.toString(), new ClientSocket.DataListener(){
                        @Override
                        public void transData() {
                            try {
                                String endMessage = clientSocket.getServerMessage();
                                JSONObject message = new JSONObject(endMessage);
                                System.out.println(message.get("serverInfo").toString());  //Print server's return information.
                            }
                            catch (JSONException e){
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (JSONException e){
                    e.printStackTrace();
                }
            }
            finish();
        }
    };



    private void init() {
        drawPaint = new Paint();
        drawPaint.setColor(paintColor); // 设置颜色
        drawPaint.setStrokeWidth(paintWidth); //设置笔宽
        drawPaint.setAntiAlias(true); // 抗锯齿
        drawPaint.setDither(true); // 防抖动
        drawPaint.setStyle(Paint.Style.STROKE); // 设置画笔类型，STROKE空心
        drawPaint.setStrokeJoin(Paint.Join.ROUND); // 设置连接处样式
        drawPaint.setStrokeCap(Paint.Cap.ROUND); // 设置笔头样式
    }

    public class JoinView extends View {
        private Bitmap drawBitmap;
        private Canvas drawCanvas;
        private Path drawPath;
        private Paint drawBitmapPaint;

        public JoinView(Context context) {
            super(context);
            drawPath = new Path();
            drawBitmapPaint = new Paint(Paint.DITHER_FLAG); // 抗抖动选项
            //new Thread(new RefreshView()).start();
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            drawBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888); // 每个像素8bytes存储
            drawCanvas = new Canvas(drawBitmap);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawColor(Color.WHITE); // 设置背景颜色
            canvas.drawBitmap(drawBitmap, 0, 0, drawBitmapPaint);
            canvas.drawPath(drawPath, drawPaint);
        }

        private void touch_down(float x, float y) {
            drawPath.reset();
            drawPath.moveTo(x, y);
        }

        private void touch_move(float x, float y) {
            if (!drawPath.isEmpty()) {
                drawPath.quadTo(posX, posY, (x + posX) / 2, (y + posY) / 2);
            }
        }

        private void touch_up() {
            if (!drawPath.isEmpty()){
                drawPath.lineTo(posX, posY);
                drawCanvas.drawPath(drawPath, drawPaint);
                drawPath.reset();
            }
        }

        public void RefreshView (int actionState){
            switch (actionState) {
                case ACTION_DOWN:
                    touch_down(posX, posY);
                    break;
                case ACTION_MOVE:
                    touch_move(posX, posY);
                    break;
                case ACTION_UP:
                    touch_up();
                    break;
            }
            postInvalidate();
        }
    }
}
