package com.ImageGuess;

import android.graphics.Color;

/**
 * ImageGuess Game
 * Danmu class
 * Created by Stanislas, Lisette, Faustine on 2017/12 in SJTU.
 */

public class DanmuBean {
    private String[] items;
    private int color;
    private int minTextSize;
    private float range;

    public DanmuBean() {
        //init default value
        color = Color.parseColor("#ff6666");
        minTextSize = 16;
        range = 0.5f;
    }

    public String[] getItems() {
        return items;
    }

    public void setItems(String[] items) {
        this.items = items;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getMinTextSize() {
        return minTextSize;
    }

    public void setMinTextSize(int minTextSize) {
        this.minTextSize = minTextSize;
    }

    public float getRange() {
        return range;
    }

    public void setRange(float range) {
        this.range = range;
    }
}
