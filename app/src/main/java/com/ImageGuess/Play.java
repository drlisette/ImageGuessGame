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
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
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
    private TextView currentDrawer;
    private Paint drawPaint;
    private TextView currentWord;
    private TextView timeShow;
    private TextView currentRoomNumber;
    private EditText answer;
    private LinearLayout paletteView;
    private ArrayList<TextView> guesserList;
    private ArrayList<TextView> scoreGuesserList;
    private ArrayList<Integer> scoreNumList; //各玩家分数
    private ArrayList<String> wordsUsed;

    private float posX, posY;
    private float paintWidth = 12;
    private int paintColor = Color.BLACK;
    private String localIP;
    private String remoteIP;
    private int localPort;
    private int remotePort;
    private int actionState;
    private ClientSocket clientSocket;
    private MyApp myApp;
    private JSONObject endJSON;
    private static final float TOUCH_TOLERANCE = 4; // 在屏幕上移动4个像素后响应
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
        Log.i(TAG,"onCreate");
        myApp=(MyApp)getApplication();
        currentWord = (TextView) findViewById(R.id.currentWord);
        currentRoomNumber = (TextView) findViewById(R.id.currentRoomNumber);
        currentRoomNumber.setText(this.getIntent().getStringExtra(CREATE_ROOM));
        timeShow = (TextView) findViewById(R.id.timer);
        paletteView = (LinearLayout) findViewById(R.id.paletteView);
        paletteView.addView(new GameView(this));
        currentDrawer = (TextView) findViewById(R.id.playerNumber);

        currentWord.setText("apple");
        answer = (EditText) findViewById(R.id.answer);
        init();
        timer.start();

        clientSocket = new ClientSocket(this, myApp.getServerIP(), myApp.getServerPort());
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

        init();

        localIP = clientSocket.getIp(this);
        remoteIP = myApp.getRemoteIP();
        remotePort = myApp.getRemotePort();
        new Thread(new GameDataThread()).start();

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
        //
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

    private CountDownTimer timer=new CountDownTimer(5000,1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            timeShow.setText(millisUntilFinished/1000+"秒");
        }

        @Override
        public void onFinish() { //时间到，换下一个玩家画画,销毁当前activity，新开activity
            timeShow.setText("时间到！");
            timeShow.setTextColor(Color.RED);
            if (myApp.getGameRound() > 0){
                myApp.setGameRound(myApp.getGameRound() - 1);
                Intent intent=new Intent(Play.this, Join.class);
                startActivity(intent);
            }else{
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

    public class GameDataThread implements Runnable {
        private JSONObject gameData;

        @Override
        public void run() {
            while (true) {
                while (true) {
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
                    Log.i(TAG, gameData.toString());
                    clientSocket.InfoSender(remotePort, remoteIP, gameData.toString());
                }
            }
        }
    }
}