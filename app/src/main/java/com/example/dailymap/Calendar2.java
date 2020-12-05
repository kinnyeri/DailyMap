package com.example.dailymap;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;


public class Calendar2 extends AppCompatActivity {
    private final OneDayDecorator oneDayDecorator = new OneDayDecorator();
    MaterialCalendarView materialCalendarView;

    //DiaryGroup 정보 유지
    String curDG;

    static final int REQ_ADD_CONTACT=1;
    String data;
    private FirebaseFirestore db;
    private static final int CALENDAR_DIARY_LIST = 526;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        //다어이리 유지
        curDG=getIntent().getStringExtra("curDG");
        Log.d("DM","현재 DG : "+curDG);

        materialCalendarView = (MaterialCalendarView) findViewById(R.id.calendarView);
        ImageButton add_btn = (ImageButton) findViewById(R.id.add_btn);
        add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Calendar2.this, AddDiary.class);
                intent.putExtra("curDG",curDG);
                startActivityForResult(intent,REQ_ADD_CONTACT);
            }
        });

        materialCalendarView.state().edit()
                .setFirstDayOfWeek(Calendar.SUNDAY)
                .setMinimumDate(CalendarDay.from(2017, 0, 1)) // 달력의 시작
                .setMaximumDate(CalendarDay.from(2030, 11, 31)) // 달력의 끝
                .setCalendarDisplayMode(CalendarMode.MONTHS)
                .commit();

        materialCalendarView.addDecorators(
                new SundayDecorator(),
                new SaturdayDecorator(),
                oneDayDecorator,
                new AddDay(curDG));


        materialCalendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                int Year = date.getYear();
                int Month = date.getMonth() + 1;
                int Day = date.getDay();

                String shot_Day = Year + "," + Month + "," + Day;
                materialCalendarView.clearSelection();

                Log.d("DM",shot_Day+" 선택, 해당 다이어리 리스트로 이동");

                Intent intent = new Intent(Calendar2.this, DiaryList.class);
                intent.putExtra("curDG",curDG);
                intent.putExtra("code",CALENDAR_DIARY_LIST);
                intent.putExtra("year",Year);
                intent.putExtra("month",Month);
                intent.putExtra("day",Day);
                startActivity(intent);
            }
        });

        Toast.makeText(Calendar2.this,"현재 : "+curDG,Toast.LENGTH_SHORT).show();


        db = FirebaseFirestore.getInstance();
        db.collection("DiaryGroupList").document(curDG)
                .collection("diaryList").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Calendar calendar = Calendar.getInstance();
                                ArrayList<CalendarDay> dates = new ArrayList<>();
                                data = document.getData().get("date").toString();
                                int Year= Integer.parseInt(data.substring(0,4));
                                int Month= Integer.parseInt(data.substring(4,6));
                                int Day= Integer.parseInt(data.substring(6));

                                calendar.set(Year,Month-1,Day);
                                CalendarDay day = CalendarDay.from(calendar);
                                dates.add(day);
                                materialCalendarView.addDecorator(new EventDecorator(Color.GRAY,dates,Calendar2.this));
                            }
                        }

                    }

                });
    }

}
