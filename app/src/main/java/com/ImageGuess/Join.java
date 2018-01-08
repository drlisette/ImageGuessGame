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
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    private String rightAnswer;
    private JSONObject gameDataJSON;
    private JSONObject answerJSON;
    private LinearLayout paletteView;
    private TextView try1;
    private TextView try2;
    private ImageView player1;
    private ImageView player2;
    private View countdownLayout;
    private TextView cueWord;
    private TextView timeShow;
    private EditText answer;
    private Button sendAnswerButton;
    private int localPort ;
    private int remotePort;
    private int actionState = 10000;
    private String localIP;
    private String remoteIP;
    private String prompt;
    private ClientSocket clientSocket;
    private MyApp myApp;
    private static final float ERASE_WIDTH = 150;
    private static final int ACTION_DOWN = 10000;
    private static final int ACTION_MOVE = 10001;
    private static final int ACTION_UP = 10002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.join);

        //提示词显示，“到你猜啦”
        LayoutInflater inflater=getLayoutInflater();
        countdownLayout=inflater.inflate(R.layout.countdown_before_play, null);
        final TextView countdownSecond=(TextView) countdownLayout.findViewById(R.id.remainingSecondsBeforeStart);
        CountDownTimer second=new CountDownTimer(3200,800) {
            @Override
            public void onTick(long millisUntilFinished) {
                countdownSecond.setText(millisUntilFinished/800+"秒");
                countdownSecond.setTextColor(Color.parseColor("#363636"));
                Log.i(TAG,countdownSecond.getText().toString());
            }

            @Override
            public void onFinish() {
                countdownSecond.setText("到你猜啦!");
                countdownSecond.setTextColor(Color.parseColor("#F08080"));
                this.cancel();
                timer.start();
            }
        };

        Toast newToast=new Toast(getApplicationContext());
        countdownSecond.getBackground().setAlpha(150);
        newToast.setView(countdownLayout);
        newToast.setGravity(Gravity.CENTER,0,0);
        newToast.setDuration(Toast.LENGTH_LONG);
        newToast.show();
        second.start();

        myApp=(MyApp)getApplication();
        //根据个人信息设置头像，名字和得分
        if (myApp.getPlayerState().equals("1005")){
            try1=(TextView) findViewById(R.id.try1);
            try2=(TextView) findViewById(R.id.try2);
            player1 = (ImageView) findViewById(R.id.leftPlayer);
            player2 = (ImageView) findViewById(R.id.rightPlayer);
            try1.setBackgroundResource(R.drawable.scoretextview);
            try2.setBackgroundResource(R.drawable.scoretextview_2);
            player1.setBackgroundResource(R.drawable.picture1);
            player2.setBackgroundResource(R.drawable.picture2);
            try1.setText(Integer.toString(myApp.getUserScore()) + "分");
            try2.setText(Integer.toString(myApp.getRemoteScore()) + "分");
        }else{
            try1=(TextView) findViewById(R.id.try2);
            try2=(TextView) findViewById(R.id.try1);
            player1 = (ImageView) findViewById(R.id.rightPlayer);
            player2 = (ImageView) findViewById(R.id.leftPlayer);
            try1.setBackgroundResource(R.drawable.scoretextview_2);
            try2.setBackgroundResource(R.drawable.scoretextview);
            player1.setBackgroundResource(R.drawable.picture2);
            player2.setBackgroundResource(R.drawable.picture1);
            try1.setText(Integer.toString(myApp.getUserScore()) + "分");
            try2.setText(Integer.toString(myApp.getRemoteScore()) + "分");
        }

        cueWord = (TextView) findViewById(R.id.currentWord);
        sendAnswerButton = (Button) findViewById(R.id.sendAnswerButton);
        sendAnswerButton.setBackgroundResource(R.drawable.buttonview);
        timeShow = (TextView) findViewById(R.id.timer);
        paletteView = (LinearLayout) findViewById(R.id.paletteView);
        answer = (EditText) findViewById(R.id.answer);

        //界面初始化
        final JoinView joinView=new JoinView(this);
        paletteView.addView(joinView);
        ViewGroup.LayoutParams paletteViewLp=paletteView.getLayoutParams();
        int pixelHeight=ScreenUtils.getScreenHeight(this);
        int cutHeight=ScreenUtils.dp2px(this,(float)250);
        int pixelWidth=ScreenUtils.getScreenWidth(this);
        int cutWidth=ScreenUtils.dp2px(this,(float)70);
        paletteViewLp.height=pixelHeight-cutHeight;
        paletteViewLp.width=pixelWidth-cutWidth;
        paletteView.setLayoutParams(paletteViewLp);

        //加入延时用于等待双方同步
        try{
            Thread.sleep(3000);
        }catch(InterruptedException e){
            e.printStackTrace();
        }

        //获取当前词语和提示
        JSONObject wordJSON = new JSONObject();
        String tip = new String();
        JSONArray wordsData = myApp.getWordsData();
        try{
            wordJSON = wordsData.getJSONObject(myApp.getCurrentIndex());
            tip = wordJSON.get("Tip").toString();
            rightAnswer = wordJSON.get("Word").toString();
        }catch(JSONException e){
            e.printStackTrace();
        }
        cueWord.setText(tip);
        clientSocket = new ClientSocket(this, myApp.getServerIP(), myApp.getServerPort());
        init();
        timer.start();

        //获取IP地址和端口号
        localIP = clientSocket.getIp(this);
        localPort=myApp.getPortNumber();
        remoteIP = myApp.getRemoteIP();
        remotePort = myApp.getRemotePort();

        //处理网络线程传来的消息，判断回答是否正确
        final Handler answerHandler = new Handler(new Handler.Callback(){
            public boolean handleMessage(Message msg){
                if (msg.what == 1){
                        timer.cancel();
                        setContentView(R.layout.timeover);
                        TextView InfoText = (TextView)findViewById(R.id.info);
                        InfoText.setText("回答正确！");
                        InfoText.setTextColor(Color.BLACK);
                        TextView AnswerText = (TextView)findViewById(R.id.right_answer);
                        AnswerText.setText("正确答案：" +  rightAnswer);
                        myApp.setUserScore(myApp.getUserScore() + 5);
                        if (myApp.getGameRound() > 0){
                            myApp.setGameRound(myApp.getGameRound() - 1);
                            myApp.setCurrentIndex(myApp.getCurrentIndex() + 1);
                            clientSocket.InfoSender(myApp.getRemotePort(), myApp.getRemoteIP(), "close");
                            Intent intent=new Intent(Join.this, Play.class);
                            startActivity(intent);
                            finish();
                        }else {
                            clientSocket.InfoSender(myApp.getRemotePort(), myApp.getRemoteIP(), "close");
                            Intent intent = new Intent(Join.this, GameFinish.class);
                            startActivity(intent);
                            finish();
                        }
                }
                return false;
            }
        });

        //接收位置和画笔状态信息
        clientSocket.InfoReceiver(localPort, new ClientSocket.DataListener() {
            @Override
            public void transData() {
                gameData = clientSocket.getGameData();
                synchronized (this){
                    try{
                        if (gameData.equals("right")){
                            //接收消息传给handler，判断回答是否正确
                            Message msg = answerHandler.obtainMessage();
                            msg.what = 1;
                            answerHandler.sendMessage(msg);
                        }else{
                            //接收游戏数据
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
                            //根据游戏数据更新画板
                            actionState = Integer.valueOf(gameDataJSON.get("actionState").toString());
                            joinView.RefreshView(actionState);
                        }
                    } catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            }
        });

        sendAnswerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (answer.length() == 0){
                    prompt = "当前内容为空！";
                    Toast.makeText(Join.this, prompt, Toast.LENGTH_SHORT).show();
                }else{
                    try{
                        answerJSON = new JSONObject();
                        answerJSON.put("answer", answer.getText().toString());
                        answer.getText().clear();
                        Log.i(TAG, "Send answer.");
                        clientSocket.InfoSender(remotePort, remoteIP, answerJSON.toString());
                    }catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    //游戏倒计时一分钟
    private CountDownTimer timer=new CountDownTimer(60000,1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            timeShow.setText(millisUntilFinished/1000+"秒");
        }

        //时间到，换下一个玩家画画,销毁当前activity，新开activity
        @Override
        public void onFinish() {
            setContentView(R.layout.timeover);
            TextView AnswerText = (TextView)findViewById(R.id.right_answer);
            AnswerText.setText(rightAnswer);
            timer.cancel();
            //根据游戏轮数跳转下一界面
            if (myApp.getGameRound() > 0) {
                myApp.setGameRound(myApp.getGameRound() - 1);
                myApp.setCurrentIndex(myApp.getCurrentIndex() + 1);
                clientSocket.InfoSender(myApp.getRemotePort(), myApp.getRemoteIP(), "close");
                Intent intent = new Intent(Join.this, Play.class);
                startActivity(intent);
                finish();
            } else {
                clientSocket.InfoSender(myApp.getRemotePort(), myApp.getRemoteIP(), "close");
                Intent intent = new Intent(Join.this, GameFinish.class);
                startActivity(intent);
                finish();
            }
        }
    };

    //画板初始化
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

    //当前“猜”界面
    public class JoinView extends View {
        private Bitmap drawBitmap;
        private Canvas drawCanvas;
        private Path drawPath;
        private Paint drawBitmapPaint;

        public JoinView(Context context) {
            super(context);
            drawPath = new Path();
            drawBitmapPaint = new Paint(Paint.DITHER_FLAG); // 抗抖动选项
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            drawBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888); // 每个像素8bytes存储
            drawCanvas = new Canvas(drawBitmap);
        }

        //根据游戏数据模拟触屏事件
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
