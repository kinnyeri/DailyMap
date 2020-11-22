package com.example.dailymap;

import java.util.Date;

public class DiaryDS {
    public float location[]; //x,y 좌표 저장
    public Date date;
    public String writter;
    public String content;
    public int feel; //0:good, 1: mid, 2:bad
    public int img; // 타입 바꿔야함
}