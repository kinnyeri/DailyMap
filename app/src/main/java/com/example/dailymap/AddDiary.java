package com.example.dailymap;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Calendar;

public class AddDiary extends AppCompatActivity {
    TextView date;
    DatePickerDialog.OnDateSetListener callbackMethod;
    Calendar cal;
    int year, month,day;

    ImageView imgContent;
    ImageView submit;
    ImageView[] feels = new ImageView[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_diary);

        //date 지정
        date = (TextView) findViewById(R.id.editDate);

        cal=Calendar.getInstance();
        year =cal.get(Calendar.YEAR); month = cal.get(Calendar.MONTH)+1; day = cal.get(Calendar.DATE);
        date.setText(year+"/"+month+"/"+day);
        callbackMethod = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int tyear, int tmonth, int tday) {
                year = tyear; month=tmonth+1; day=tday;
                date.setText(year+"/"+month+"/"+day);
            }
        };

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog dialog = new DatePickerDialog(AddDiary.this, android.R.style.Theme_Holo_Light_Dialog_MinWidth
                        ,callbackMethod, year, month-1, day);
                dialog.getDatePicker().setCalendarViewShown(false);
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                dialog.show();
            }
        });

        //View 할당
        imgContent = (ImageView)findViewById(R.id.editImg);
        submit = (ImageView)findViewById(R.id.editSubmit);
        feels[0]= (ImageView)findViewById(R.id.good);
        feels[1]= (ImageView)findViewById(R.id.mid);
        feels[2]= (ImageView)findViewById(R.id.bad);

        //이미지 로딩
        imgContent.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, 101);
            }
        });
        //Submit
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(),"Submit OK", Toast.LENGTH_SHORT).show();
            }
        });
        //Feels
        for(int i=0;i<feels.length;i++){
            final int feelsCnt=i;
            feels[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(getApplicationContext(),"feels "+feelsCnt, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}