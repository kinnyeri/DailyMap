package com.example.dailymap;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public void onClickDiary(View v){
        Intent intent = new Intent(this,Diary.class);
        startActivity(intent);
    }
    public void onClickCalendar(View v){
        Intent intent = new Intent(this,CalendarList.class);
        startActivity(intent);
    }
}