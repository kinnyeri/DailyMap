package com.example.dailymap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Diary extends AppCompatActivity {
    ImageView img;
    TextView loc;
    ImageView feel;
    TextView date;
    TextView content;
    TextView writter_tag;

    int []feelThumbs ={R.drawable.good,R.drawable.mid,R.drawable.bad};
    //FS
    private FirebaseFirestore db;
    //ST
    private FirebaseStorage storage;
    private StorageReference storageRef;
    //DiaryGroup 정보 유지
    String curDG;

    private Geocoder geocoder = new Geocoder(this);;
    String address= null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary);
        //DiaryGroup 정보 유지
        curDG=getIntent().getStringExtra("curDG");
        Log.d("DM","현재 DG : "+curDG);

        //CST
        storage= FirebaseStorage.getInstance("gs://daily-map-d47b1.appspot.com");
        storageRef = storage.getReference();

        //FST
        db = FirebaseFirestore.getInstance(); //Init Firestore

        // view
        img=(ImageView) findViewById(R.id.diaryImg);
        loc=(TextView)findViewById(R.id.diaryLoc);
        feel=(ImageView)findViewById(R.id.diaryFeel);
        date=(TextView)findViewById(R.id.diaryDate);
        content=(TextView)findViewById(R.id.diaryContent);
        writter_tag=(TextView)findViewById(R.id.writter_tag);

        //Intent
        Intent intent = getIntent();
        String locX = intent.getExtras().getString("locationX");
        String locY = intent.getExtras().getString("locationY");
        String feels = intent.getExtras().getString("feel");
        String wriiter = intent.getExtras().getString("writter");

        //String location = intent.getStringExtra("mLocation");
        // 역지오코딩 : 위도, 경도 -> 주소
        LatLng latLng = new LatLng(Double.parseDouble(locX), Double.parseDouble(locY));
        Address addr= ReverseGeocoding(latLng);
        if(addr!=null){
            address= addr.getAddressLine(0); // 주소
        }
        loc.setText(address);
        switch (feels){
            case "0":
                feel.setImageResource(feelThumbs[0]); break;
            case "1":
                feel.setImageResource(feelThumbs[1]); break;
            case "2":
                feel.setImageResource(feelThumbs[2]); break;
        }
        String tmpDate =intent.getExtras().getString("date"); //date 나중에
        date.setText(tmpDate.substring(0,4)+"/"+tmpDate.substring(4,6)+"/"+tmpDate.substring(6,8));
        content.setText(intent.getExtras().getString("content"));

        //이미지 올리기
        storageRef.child(curDG+"/"+intent.getExtras().getString("img"))
                .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            ImageView tmpIV=img;
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(Diary.this)
                        .load(uri)
                        .into(tmpIV);
                Log.d("DM","이미지 불러오기 성공");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println(e);
            }
        });
        //작성자
        db.collection("UserList")
                .whereEqualTo("email",wriiter)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            writter_tag.setText(document.getData().get("name").toString());

                            Log.d("DM","작성자 정보 불러오기 성공");
                        }
                    } else {

                    }
                }
        });
        Toast.makeText(Diary.this,"현재 : "+curDG,Toast.LENGTH_SHORT).show();
    }
    //역지오코딩 (위도,경도 -> 주소,지명)
    private Address ReverseGeocoding(LatLng point){
        Address address;
        List<Address> list = null;
        try {
            double d1 = point.latitude;
            double d2 = point.longitude;
            list = geocoder.getFromLocation(
                    d1, // 위도
                    d2, // 경도
                    10); // 얻어올 값의 개수
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("입출력 오류 - 서버에서 주소변환시 에러발생");
        }
        if (list != null) {
            if (list.size()==0) {
                System.out.println("해당되는 주소 정보는 없습니다");
            } else {
                System.out.println(list.get(0).toString());
                address= list.get(0);
                return address;
            }
        }
        return null;
    }
}