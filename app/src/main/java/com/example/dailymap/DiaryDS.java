package com.example.dailymap;

import android.content.Context;
import android.widget.Toast;

import java.util.Date;
import java.util.List;
import java.util.Vector;

//class DateDS{
//    public int year;
//    public int month;
//    public int day;
//
//    public DateDS(int y,int m,int d){
//        year=y;
//        month=m;
//        day=d;
//    }
//}
public class DiaryDS {
    public double locationX; //x,y 좌표 저장
    public double locationY; //x,y 좌표 저장
    public String date;
    public String writter; //
    public String content;
    public int feel; //0:good, 1: mid, 2:bad
    public String img; // 타입 바꿔야함

    public DiaryDS(){

    }
    public DiaryDS(String w){
        setWritter(w);
    }

    public void setLocation(double x, double y) {
        this.locationX=x;
        this.locationY=y;
    }

    public void setDate(int y,int m,int d) {
        this.date = y+""+m+""+d;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public void setContent(String content) {
        this.content = content;
    }
    void setWritter(String w){
        this.writter=w;
    }

    public void display(Context c){
        Toast.makeText(c,"writter "+this.writter,Toast.LENGTH_SHORT).show();
        Toast.makeText(c,"date "+this.date,Toast.LENGTH_SHORT).show();
        Toast.makeText(c,"location "+this.locationX,Toast.LENGTH_SHORT).show();
        Toast.makeText(c,"feel "+this.feel,Toast.LENGTH_SHORT).show();
        Toast.makeText(c,"img "+this.img,Toast.LENGTH_SHORT).show();
        Toast.makeText(c,"content "+this.content,Toast.LENGTH_SHORT).show();
    }

    public String getContent() {
        return content;
    }

    public double getLocationX() {
        return locationX;
    }

    public double getLocationY() {
        return locationY;
    }

    public int getFeel() {
        return feel;
    }

    public String getDate() {
        return date;
    }

    public String getImg() {
        return img;
    }

    public String getWritter() {
        return writter;
    }
    public void get(){

    }
}