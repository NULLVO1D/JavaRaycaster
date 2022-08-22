package com.company;

import java.awt.*;

public class Rect {

    public int x;
    public int y;
    public int width;
    public int height;
    public Color color;

    public Rect(int x, int y, int w, int h, Color c) {
        this.x = x;
        this.y = y;
        width = w;
        height = h;
        color = c;
    }

    public void setTo(int x, int y, int w, int h, Color c) {
        this.x = x;
        this.y = y;
        width = w;
        height = h;
        color = c;
    }


}
