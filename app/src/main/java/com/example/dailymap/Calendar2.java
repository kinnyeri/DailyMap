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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        curDG=getIntent().getStringExtra("curDG"); // 이동이 있으면 intent에 붙여서 보내줘야함

        materialCalendarView = (MaterialCalendarView) findViewById(R.id.calendarView);
        ImageButton add_btn = (ImageButton) findViewById(R.id.add_btn);


        add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "기록장 추가 버튼", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Calendar2.this, AddDiary.class);
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
                oneDayDecorator);


        materialCalendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                int Year = date.getYear();
                int Month = date.getMonth() + 1;
                int Day = date.getDay();

                Log.i("Year test", Year + "");
                Log.i("Month test", Month + "");
                Log.i("Day test", Day + "");

                String shot_Day = Year + "," + Month + "," + Day;

                Log.i("shot_Day test", shot_Day + "");
                materialCalendarView.clearSelection();

                Toast.makeText(getApplicationContext(), shot_Day, Toast.LENGTH_SHORT).show();
            }
        });

        Toast.makeText(Calendar2.this,"현재 : "+curDG,Toast.LENGTH_LONG).show();


        db = FirebaseFirestore.getInstance();
        String dgKey = FirebaseAuth.getInstance().getCurrentUser().getUid()+"000";
        db.collection("DiaryGroupList").document(dgKey)
                .collection("diaryList").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                //Toast.makeText(Calendar2.this,document.getData().get("date").toString(),Toast.LENGTH_LONG).show();
                                data=document.getData().get("date").toString();
                                TextView textViewNo = (TextView) findViewById(R.id.textViewNo);
                                textViewNo.setText(data);

                            }

                        }
                    }
                });

    }


    class ApiSimulator extends AsyncTask<Void, Void, List<CalendarDay>> {


        @Override
        protected List<CalendarDay> doInBackground(@NonNull Void... voids) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Calendar calendar = Calendar.getInstance();
            ArrayList<CalendarDay> dates = new ArrayList<>();
            /*특정날짜 달력에 점표시해주는곳*/
            /*월은 0이 1월 년,일은 그대로*/
            //string 문자열인 Time_Result 을 받아와서 ,를 기준으로짜르고 string을 int 로 변환

            //calendar.set(year, month - 1, dayy);
            CalendarDay day = CalendarDay.from(calendar);

            Intent intent = getIntent();
            String date = intent.getStringExtra("date");
            Toast.makeText(Calendar2.this,date,Toast.LENGTH_LONG).show();

            // date를 받아와서 20201129이면
            // 2020을 int year로 저장
            // 11을 int month로 저장
            // 29를 int dayy로 저장 시켜야함 -> 현재 date 자체가 안받아와짐
            dates.add(day);
            return dates;
        }

        @Override
        protected void onPostExecute(@NonNull List<CalendarDay> calendarDays) {
            super.onPostExecute(calendarDays);
            if (isFinishing()) {
                return;
            }
            materialCalendarView.addDecorator(new EventDecorator(Color.RED, calendarDays, Calendar2.this));
        }
    }
}
