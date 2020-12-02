package com.example.dailymap;

import android.graphics.Color;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.util.ArrayList;
import java.util.Calendar;

public class AddDay implements DayViewDecorator {
    private FirebaseFirestore db;
    private String data;
    int Year;
    int Month;
    int Day;
    static ArrayList<CalendarDay> dates;
    Calendar calendar;

    public AddDay(String curDG) {
        db = FirebaseFirestore.getInstance();
        db.collection("DiaryGroupList").document(curDG)
                .collection("diaryList").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                //Toast.makeText(Calendar2.this,document.getData().get("date").toString(),Toast.LENGTH_LONG).show();
                                calendar = Calendar.getInstance();
                                dates = new ArrayList<>();
                                data = document.getData().get("date").toString();
                                Year= Integer.parseInt(data.substring(0,4));
                                Month= Integer.parseInt(data.substring(4,6));
                                Day= Integer.parseInt(data.substring(6));

                                calendar.set(Year,Month-1,Day);
                                CalendarDay day = CalendarDay.from(calendar);
                                dates.add(day);

                                //여기 있은땐 모든 정보 받아와짐
                            }

                            //여기서부터 처음것만
                        }

                    }

                });
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        Log.i("test1", dates + "");
        Log.i("test2", day + "");
        return false;
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.addSpan(new DotSpan(15, Color.parseColor("#345F53")));
    }

    /**
     * We're changing the internals, so make sure to call {@linkplain MaterialCalendarView#invalidateDecorators()}
     */

}

