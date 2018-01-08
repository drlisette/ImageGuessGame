package com.ImageGuess;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;


/**
 * ImageGuess Game
 * Screen utils class
 * Created by Stanislas, Lisette, Faustine on 2017/12 in SJTU.
 */

//获取屏幕相关信息
public class ScreenUtils {

        private ScreenUtils()
        {
            throw new UnsupportedOperationException("cannot be instantiated");
        }

        //获取屏幕宽度
        public static int getScreenWidth(Context context)
        {
            WindowManager wm = (WindowManager) context
                    .getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics outMetrics = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(outMetrics);
            return outMetrics.widthPixels;
        }

        //获取屏幕高度
        public static int getScreenHeight(Context context)
        {
            WindowManager wm = (WindowManager) context
                    .getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics outMetrics = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(outMetrics);
            return outMetrics.heightPixels;
        }

    //单位转换，dp到px
    public static int dp2px(Context context, float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, context.getResources().getDisplayMetrics());
    }
}
