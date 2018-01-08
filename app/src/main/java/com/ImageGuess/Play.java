package com.ImageGuess;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

import com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;

import static android.content.ContentValues.TAG;

/**
 * ImageGuess Game
 * Play board interface
 * Created by Stanislas, Lisette, Faustine on 2017/12 in SJTU.
 */

public class Play extends Activity {
    private Paint drawPaint;
    private TextView currentWord;
    private TextView timeShow;
    private EditText answer;
    private LinearLayout paletteView;
    private TextView try1;
    private TextView try2;
    private ImageView player1;
    private ImageView player2;
    private View countdownLayout;
    private float posX, posY;
    private float paintWidth = 12;
    private int paintColor = Color.BLACK;
    private String localIP;
    private String remoteIP;
    private String answerData;
    private String rightAnswer;
    private int localPort;
    private int remotePort;
    private int actionState;
    private ClientSocket clientSocket;
    private MyApp myApp;
    private JSONObject endJSON;
    private JSONObject answerJSON;
    private static final float TOUCH_TOLERANCE = 4; // 在屏幕上移动4个像素后响应
    private static final float ERASE_WIDTH = 150;
    private static final int ACTION_DOWN = 10000;
    private static final int ACTION_MOVE = 10001;
    private static final int ACTION_UP = 10002;
    private RelativeLayout answerFromOthers; //放置弹幕内容的父组件
    private DanmuBean danmuBean;  //弹幕内容
    private MyHandler handler;
    private GameDataThread gameDataThread = new GameDataThread();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play);

        //提示词显示，“到你画啦”
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
                countdownSecond.setText("到你画啦!");
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

        //界面初始化
        currentWord = (TextView) findViewById(R.id.currentWord);
        timeShow = (TextView) findViewById(R.id.timer);
        paletteView = (LinearLayout) findViewById(R.id.paletteView);
        paletteView.addView(new GameView(this));
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

        //获取当前词语
        answerFromOthers = (RelativeLayout) findViewById(R.id.danmu_container);
        danmuBean = new DanmuBean();
        handler = new MyHandler(this);
        JSONObject wordJSON = new JSONObject();
        JSONArray wordsData = myApp.getWordsData();
        rightAnswer = new String();
        try{
            wordJSON = wordsData.getJSONObject(myApp.getCurrentIndex());
            rightAnswer = wordJSON.get("Word").toString();
        }catch(JSONException e){
            e.printStackTrace();
        }
        currentWord.setText(rightAnswer);
        answer = (EditText) findViewById(R.id.answer);
        init();
        timer.start();
        clientSocket = new ClientSocket(this, myApp.getServerIP(), myApp.getServerPort());

        //画板初始化
        ImageView menu_icon = new ImageView(this);
        Drawable menu_img = ContextCompat.getDrawable(this, R.drawable.icon_menu);
        menu_icon.setImageDrawable(menu_img);
        final FloatingActionButton actionButton = new FloatingActionButton.Builder(this).setContentView(menu_icon)
                .setPosition(FloatingActionButton.POSITION_RIGHT_CENTER).build();

        SubActionButton.Builder itemBuilder = new SubActionButton.Builder(this);
        ImageView red_Icon = new ImageView(this);
        Drawable red_img = ContextCompat.getDrawable(this, R.drawable.icon_red);
        red_Icon.setImageDrawable(red_img);
        SubActionButton red_button = itemBuilder.setContentView(red_Icon).build();

        ImageView yellow_Icon = new ImageView(this);
        Drawable yellow_img = ContextCompat.getDrawable(this, R.drawable.icon_yellow);
        yellow_Icon.setImageDrawable(yellow_img);
        SubActionButton yellow_button = itemBuilder.setContentView(yellow_Icon).build();

        ImageView blue_Icon = new ImageView(this);
        Drawable blue_img = ContextCompat.getDrawable(this, R.drawable.icon_blue);
        blue_Icon.setImageDrawable(blue_img);
        SubActionButton blue_button = itemBuilder.setContentView(blue_Icon).build();

        ImageView green_Icon = new ImageView(this);
        Drawable green_img = ContextCompat.getDrawable(this, R.drawable.icon_green);
        green_Icon.setImageDrawable(green_img);
        SubActionButton green_button = itemBuilder.setContentView(green_Icon).build();

        ImageView black_Icon = new ImageView(this);
        Drawable black_img = ContextCompat.getDrawable(this, R.drawable.icon_black);
        black_Icon.setImageDrawable(black_img);
        SubActionButton black_button = itemBuilder.setContentView(black_Icon).build();

        ImageView erase_Icon = new ImageView(this);
        Drawable erase_img = ContextCompat.getDrawable(this, R.drawable.icon_erase);
        erase_Icon.setImageDrawable(erase_img);
        SubActionButton erase_button = itemBuilder.setContentView(erase_Icon).build();

        ImageView width_Icon = new ImageView(this);
        Drawable width_img = ContextCompat.getDrawable(this, R.drawable.icon_width);
        width_Icon.setImageDrawable(width_img);
        SubActionButton width_button = itemBuilder.setContentView(width_Icon).build();

        final FloatingActionMenu actionMenu = new FloatingActionMenu.Builder(this)
                .addSubActionView(erase_button)
                .addSubActionView(width_button)
                .addSubActionView(red_button)
                .addSubActionView(yellow_button)
                .addSubActionView(blue_button)
                .addSubActionView(green_button)
                .addSubActionView(black_button)
                .setRadius(300)
                .setStartAngle(90)
                .setEndAngle(270)
                .attachTo(actionButton).build();

        //获取IP地址和端口号
        localIP = clientSocket.getIp(this);
        localPort=myApp.getPortNumber();
        remoteIP = myApp.getRemoteIP();
        remotePort = myApp.getRemotePort();

        //处理网络线程传来的消息，判断回答是否正确
        final Handler answerHandler = new Handler(new Handler.Callback(){
            public boolean handleMessage(Message msg){
                int N = 0;
                if (msg.what == 1){
                    try{
                        if (rightAnswer.equals(answerJSON.get("answer").toString())){
                            timer.cancel();
                            myApp.setRemoteScore(myApp.getRemoteScore() + 5);
                            setContentView(R.layout.timeover);
                            TextView InfoText = (TextView)findViewById(R.id.info);
                            InfoText.setText("回答正确！");
                            InfoText.setTextColor(Color.BLACK);
                            TextView AnswerText = (TextView)findViewById(R.id.right_answer);
                            AnswerText.setText("正确答案：" + rightAnswer);
                            gameDataThread.exit = true;
                            clientSocket.InfoSender(myApp.getRemotePort(), myApp.getRemoteIP(), "right");
                            try{
                                Thread.sleep(500);
                            }catch(InterruptedException e){
                                e.printStackTrace();
                            }
                            if (myApp.getGameRound() > 0){
                                myApp.setGameRound(myApp.getGameRound() - 1);
                                myApp.setCurrentIndex(myApp.getCurrentIndex() + 1);
                                clientSocket.InfoSender(myApp.getRemotePort(), myApp.getRemoteIP(), "close");
                                Intent intent=new Intent(Play.this, Join.class);
                                startActivity(intent);
                                finish();
                            }else{
                                try{
                                    clientSocket.InfoSender(myApp.getRemotePort(), myApp.getRemoteIP(), "close");
                                    endJSON = new JSONObject();
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
                                    Intent intent = new Intent(Play.this, GameFinish.class);
                                    startActivity(intent);
                                    finish();
                                } catch (JSONException e){
                                    e.printStackTrace();
                                }
                            }
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                    if (danmuBean.getItems() != null){
                        N = danmuBean.getItems().length;
                    }
                    for (int i = 0; i < N; i++) {
                        handler.obtainMessage(1, i, 0).sendToTarget();
                    }
                }
                return false;
            }
        });

        clientSocket.InfoReceiver(localPort, new ClientSocket.DataListener() {
            @Override
            public void transData() {
                answerData = clientSocket.getGameData();
                synchronized (this){
                    try{
                        answerJSON = new JSONObject(answerData);
                        Log.i(TAG, "transData: " + answerData);
                        if (answerJSON.get("answer") != null){
                            Log.i(TAG, "Receive answer.");
                            danmuBean.setItems(new String[]{answerJSON.get("answer").toString()});
                            //接收消息传给handler，判断回答是否正确
                            Message msg = answerHandler.obtainMessage();
                            msg.what = 1;
                            answerHandler.sendMessage(msg);
                        }
                    } catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            }
        });

        //传输游戏数据
        new Thread(gameDataThread).start();

        //颜色、粗细、笔擦按钮
        red_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick: red_button");
                paintColor = Color.RED;
                drawPaint.setColor(paintColor);
                drawPaint.setStrokeWidth(paintWidth);
                drawPaint.setXfermode(null);
                actionMenu.close(true);
            }
        });

        yellow_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick: yellow_button");
                paintColor = Color.YELLOW;
                drawPaint.setColor(paintColor);
                drawPaint.setStrokeWidth(paintWidth);
                drawPaint.setXfermode(null);
                actionMenu.close(true);
            }
        });

        blue_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick: blue_button");
                paintColor = Color.BLUE;
                drawPaint.setColor(paintColor);
                drawPaint.setStrokeWidth(paintWidth);
                drawPaint.setXfermode(null);
                actionMenu.close(true);
            }
        });

        green_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick: green_button");
                paintColor = Color.GREEN;
                drawPaint.setColor(paintColor);
                drawPaint.setStrokeWidth(paintWidth);
                drawPaint.setXfermode(null);
                actionMenu.close(true);
            }
        });

        black_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick: black_button");
                paintColor = Color.BLACK;
                drawPaint.setColor(paintColor);
                drawPaint.setStrokeWidth(paintWidth);
                drawPaint.setXfermode(null);
                actionMenu.close(true);
            }
        });

        erase_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick: erase_button");
                drawPaint.setStrokeWidth(ERASE_WIDTH);
                paintColor = Color.WHITE;
                drawPaint.setColor(paintColor);
                actionMenu.close(true);
            }
        });

        width_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick: width_button");
                final WidthSeekBar widthSeekBar = new WidthSeekBar(Play.this, (int) paintWidth);
                widthSeekBar.widthSeekBar(new WidthSeekBar.WidthListener() {
                    @Override
                    public void transWidth() {
                        paintWidth = widthSeekBar.getWidth();
                        drawPaint.setStrokeWidth(paintWidth);
                    }
                });
                widthSeekBar.show();
                drawPaint.setXfermode(null);
                actionMenu.close(true);
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
            gameDataThread.exit = true;
            //根据游戏轮数跳转下一界面
            if (myApp.getGameRound() > 0){
                myApp.setGameRound(myApp.getGameRound() - 1);
                myApp.setCurrentIndex(myApp.getCurrentIndex() + 1);
                clientSocket.InfoSender(myApp.getRemotePort(), myApp.getRemoteIP(), "close");
                Intent intent=new Intent(Play.this, Join.class);
                startActivity(intent);
                finish();
            }else{
                try{
                    //游戏结束向服务器请求销毁房间
                    timer.cancel();
                    clientSocket.InfoSender(myApp.getRemotePort(), myApp.getRemoteIP(), "close");
                    endJSON = new JSONObject();
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
                    Intent intent = new Intent(Play.this, GameFinish.class);
                    startActivity(intent);
                    finish();
                } catch (JSONException e){
                    e.printStackTrace();
                }
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

    //当前“画”界面
    public class GameView extends View {
        private Bitmap drawBitmap;
        private Canvas drawCanvas;
        private Path drawPath;
        private Paint drawBitmapPaint;

        public GameView(Context context) {
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

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawColor(Color.WHITE); // 设置背景颜色
            canvas.drawBitmap(drawBitmap, 0, 0, drawBitmapPaint);
            canvas.drawPath(drawPath, drawPaint);
        }

        private void touch_down(float x, float y) {
            drawPath.reset();
            drawPath.moveTo(x, y);
            posX = x;
            posY = y;
        }

        private void touch_move(float x, float y) {
            float dx = Math.abs(x - posX);
            float dy = Math.abs(y - posY);
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                drawPath.quadTo(posX, posY, (x + posX) / 2, (y + posY) / 2);
                posX = x;
                posY = y;
            }
        }

        private void touch_up() {
            drawPath.lineTo(posX, posY);
            drawCanvas.drawPath(drawPath, drawPaint);
            drawPath.reset();
        }

        //监听不同触屏事件
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    actionState = ACTION_DOWN;
                    touch_down(x, y);
                    break;
                case MotionEvent.ACTION_MOVE:
                    actionState = ACTION_MOVE;
                    touch_move(x, y);
                    break;
                case MotionEvent.ACTION_UP:
                    actionState = ACTION_UP;
                    touch_up();
                    break;
            }
            postInvalidate();
            return true;
        }
    }

    //游戏数据传输线程
    public class GameDataThread implements Runnable {
        private JSONObject gameData;
        public boolean exit = false;

        @Override
        public void run() {
            while (!exit) {
                gameData = new JSONObject();
                try {
                    gameData.put("posX", posX);
                    gameData.put("posY", posY);
                    gameData.put("color", paintColor);
                    gameData.put("width", paintWidth);
                    gameData.put("actionState", actionState);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                clientSocket.InfoSender(remotePort, remoteIP, gameData.toString());
            }
        }
    }

    //弹幕显示线程
    private void showDanmu(String content, float textSize, int textColor) {
        final TextView textView = new TextView(this);
        textView.setTextSize(textSize);
        textView.setText(content);
        textView.setTextColor(textColor);
        int leftMargin = answerFromOthers.getRight() - answerFromOthers.getLeft() - answerFromOthers.getPaddingLeft();
        int verticalMargin = 0;
        textView.setTag(verticalMargin);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params.topMargin = verticalMargin;
        textView.setLayoutParams(params);
        Animation anim = AnimationHelper.createTranslateAnim(this, leftMargin, -ScreenUtils.getScreenWidth(this));
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        textView.startAnimation(anim);

        answerFromOthers.addView(textView);
    }

    //弹幕的handler，需要在主线程中添加组件
    private static class MyHandler extends Handler {
        private WeakReference<Play> ref;

        MyHandler(Play ac) {
            ref = new WeakReference<>(ac);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (msg.what == 1) {
                Play ac = ref.get();
                if (ac != null && ac.danmuBean != null) {
                    int index = msg.arg1;
                    String content = ac.danmuBean.getItems()[index];
                    float textSize = (float) (ac.danmuBean.getMinTextSize() * (1 + Math.random() * ac.danmuBean.getRange()));
                    int textColor = ac.danmuBean.getColor();

                    ac.showDanmu(content, textSize, textColor);
                    Log.i(TAG, content);
                }
            }
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