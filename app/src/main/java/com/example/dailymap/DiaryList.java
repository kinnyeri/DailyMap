package com.example.dailymap;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

public class DiaryList extends AppCompatActivity {
    int[] thumbnails = {R.drawable.gallery,R.drawable.gallery,R.drawable.gallery,
            R.drawable.gallery,R.drawable.jeju00};
            //사이즈 줄여서 넣기
    /*R.drawable.gallery,R.drawable.gallery,
            R.drawable.gallery,R.drawable.jeju00,R.drawable.gallery,R.drawable.gallery,
            R.drawable.gallery,R.drawable.gallery,R.drawable.gallery,R.drawable.gallery,
            R.drawable.gallery,R.drawable.gallery,R.drawable.gallery,R.drawable.gallery,
            R.drawable.gallery,R.drawable.gallery,R.drawable.gallery,R.drawable.gallery*/
    GridView gv;
    DisplayMetrics metrics;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_list);

        gv = (GridView) findViewById(R.id.gridList);
        gv.setAdapter(new ImgAdapter(this));

        metrics= new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
    }
    public class ImgAdapter extends BaseAdapter {
        private Context context;

        public ImgAdapter(Context c){
            context=c;
        }

        public int getCount(){
            return thumbnails.length;
        }
        public Object getItem(int pos){
            return thumbnails[pos];
        }
        public long getItemId(int pos){
            return pos;
        }
        public View getView(int pos,View convertView, ViewGroup parent){
            int rowWidth = (metrics.widthPixels)/3;
            ImageView imageView;
//            TextView textView;
//            FrameLayout container = new FrameLayout(context);
//            container.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,FrameLayout.LayoutParams.WRAP_CONTENT));
            if(convertView==null ){
                imageView=new ImageView(context);
                imageView.setLayoutParams(new GridView.LayoutParams(rowWidth,rowWidth));
                imageView.setPadding(1,1,1,1);
            }else{
                imageView =(ImageView)convertView;
            }

//            textView=new TextView(context);
//            textView.setLayoutParams(new FrameLayout.LayoutParams(
//                    FrameLayout.LayoutParams.WRAP_CONTENT,FrameLayout.LayoutParams.WRAP_CONTENT));
//            textView.setText("2020년 11월 1일\n제주도");
//            textView.setTextSize(20);

            imageView.setImageResource(thumbnails[pos]);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(DiaryList.this,Diary.class);
                    startActivity(intent);
                }
            });
//            container.addView(imageView);
//            container.addView(textView);

            return imageView;
        }
    };
}