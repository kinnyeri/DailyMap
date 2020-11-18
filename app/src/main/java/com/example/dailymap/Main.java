package com.example.dailymap;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Main extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseAuth mAuth; //auth
    @Override //Auth 확인
    protected void onStart() {
        super.onStart();
        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);

        //FirebaseUser user = mAuth.getCurrentUser(); //에러 남 > 로그아웃 안됨
        Toast.makeText(Main.this,"start",Toast.LENGTH_LONG).show();
        if(signInAccount!=null){ //login 중
            Toast.makeText(Main.this,signInAccount.getDisplayName(),Toast.LENGTH_SHORT).show();
        }
        else{ //user 없으면 signin page로 넘어가기
            Toast.makeText(Main.this,"no one",Toast.LENGTH_LONG).show();
            Intent intent = new Intent(getApplicationContext(),SignIn.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {

        mMap = googleMap;

        LatLng SEOUL = new LatLng(37.57, 126.97);

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(SEOUL);
        markerOptions.title("경복궁");
        markerOptions.snippet("투어&박물관이 있는 역사적인 궁전");
        mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(SEOUL, 10));

        // 메인화면 버튼 클릭 이벤트
        // 계정 버튼
        ImageButton accountButton = (ImageButton) findViewById(R.id.account_btn);
        accountButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Toast.makeText(getApplicationContext(),"계정관리 버튼", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Main.this,Account.class);
                startActivity(intent);
            }
        });

        // 다이어리 확인 추가 버튼 // 지울거임
        ImageButton diaryButton = (ImageButton) findViewById(R.id.diary_btn);
        diaryButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Toast.makeText(getApplicationContext(),"기록장 추가 버튼", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Main.this,DiaryList.class);
                startActivity(intent);
            }
        });

        // 달력 버튼
        ImageButton calendarButton = (ImageButton) findViewById(R.id.calendar_btn);
        calendarButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Toast.makeText(getApplicationContext(),"달력 버튼", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Main.this,Calendar.class);
                startActivity(intent);
            }
        });

        // 기록장 추가 버튼
        ImageButton addButton = (ImageButton) findViewById(R.id.add_btn);
        addButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Toast.makeText(getApplicationContext(),"기록장 추가 버튼", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Main.this,AddDiary.class);
                startActivity(intent);
            }
        });

    }
}