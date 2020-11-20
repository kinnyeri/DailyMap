package com.example.dailymap;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.util.List;

public class Main extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Geocoder geocoder;
    private FirebaseAuth mAuth; //auth

    @Override //Auth 확인
    protected void onStart() {
        super.onStart();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser(); //에러 남 > 로그아웃 안됨
        Toast.makeText(Main.this,"start",Toast.LENGTH_LONG).show();
        if(user!=null){ //login 중
            Toast.makeText(Main.this,user.getDisplayName(),Toast.LENGTH_SHORT).show();
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
        geocoder = new Geocoder(this);

        LatLng SEOUL = new LatLng(37.57, 126.97);
        // 마커 추가
        AddMarker("경복궁","투어&박물관이 있는 역사적인 궁전",SEOUL);
        // SEOUL로 카메라 시점 이동
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(SEOUL, 10));

        // 메인화면 버튼 핸들러 : 계정관리, (다이어리확인), 달력, 기록장추가 버튼
        MainUIBtnHandler();

        // 검색 핸들러
        SearchHandler();

        // 슬라이딩 애니메이션 적용 레이아웃
        final LinearLayout slidePage = findViewById(R.id.sliding_page); //기록장 미리보기 화면
        final LinearLayout slideButtons = findViewById(R.id.sliding_buttonPanel); // 버튼 패널 (기록장 추가, 달력 버튼)

        // 마커 클릭에 대한 이벤트 처리
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                // 마커 정보 표시
                marker.showInfoWindow();

                // 마커 클릭한 경우, 기록장 미리보기 화면 띄우기
                if(slidePage.getVisibility()==View.GONE){
                    ShowLayout(slidePage, slideButtons);
                }

                return true;
            }
        });

        // 맵 터치에 대한 이벤트 처리
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            // 지도 영역 클릭한 경우
            @Override
            public void onMapClick(LatLng point) {
                // 가상키보드 숨기기
                final EditText searchBox= findViewById(R.id.edit_text);
                HideKeyboard(searchBox);

                // 슬라이딩 화면(마커 클릭 시 나온 화면) 보이는 경우 다시 내려가게 하기
                if(slidePage.getVisibility()==View.VISIBLE){
                    HideLayout(slidePage, slideButtons);
                }

                // 슬라이딩 화면(마커 클릭 시 나온 화면), 가상키보드 안 보이는 경우 마커 추가
                else if(slidePage.getVisibility()==View.GONE ){
                    // ReverseGeocoding 함수 : 역지오코딩 (위도,경도 -> 주소,지명)
                    // 반환값 예시
                        /* Address[
                                addressLines=[0:"대한민국 서귀포시 성산읍 성산 일출봉"],
                                feature=성산 일출봉,
                                admin=null,
                                sub-admin=null,
                                locality=서귀포시,
                                thoroughfare=null,
                                postalCode=699-900,
                                countryCode=KR,
                                countryName=대한민국,
                                hasLatitude=true,latitude=33.458056000000006,
                                hasLongitude=true,longitude=126.94250000000001,
                                phone=null,url=null,
                                extras=null]
                         */
                    Address addr= ReverseGeocoding(point);

                    if(addr!=null){
                        String locationName = addr.getFeatureName(); // 주소이름,
                        String address = addr.getAddressLine(0); // 주소

                        // 지도에서 클릭한 위치에 마커 추가
                        AddMarker(locationName,address,point);
                    }
                }
            }
        });
    }

    // 메인화면 UI 핸들러
    public void MainUIBtnHandler(){
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
                Toast.makeText(getApplicationContext(),"다이어리 확인 버튼", Toast.LENGTH_SHORT).show();
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
                Intent intent = new Intent(Main.this,Calendar2.class);
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

    // 검색 핸들러
    public void SearchHandler(){
        final LinearLayout slidePage = findViewById(R.id.sliding_page); //기록장 미리보기 화면
        final LinearLayout slideButtons = findViewById(R.id.sliding_buttonPanel); // 버튼 패널 (기록장 추가, 달력 버튼)

        // 검색창
        final EditText searchBox= findViewById(R.id.edit_text);
        // 구글맵 검색하는 부분
        searchBox.setOnKeyListener(new View.OnKeyListener() {

            // 엔터키 눌렀을 경우에 검색
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                switch (keyCode){
                    case KeyEvent.KEYCODE_ENTER:
                        // 슬라이드 패널 가리기
                        HideLayout(slidePage,slideButtons);

                        // 지오코딩 (주소,지명 -> 위도,경도)
                        String searchAddress =searchBox.getText().toString();
                        System.out.println(searchAddress);

                        Address addr = Geocoding(searchAddress);

                        if(addr!=null){
                            // 지오코딩 반환값 처리
                            String []splitStr = addr.toString().split(",");
                            String address = splitStr[0].substring(splitStr[0].indexOf("\"") + 1,splitStr[0].length() - 2); // 주소
                            System.out.println(address);

                            String latitude = splitStr[10].substring(splitStr[10].indexOf("=") + 1); // 위도
                            String longitude = splitStr[12].substring(splitStr[12].indexOf("=") + 1); // 경도
                            System.out.println(latitude);
                            System.out.println(longitude);

                            String locationName = addr.getFeatureName(); // 주소 이름
                            System.out.println(locationName);

                            LatLng point = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude)); // 좌표(위도, 경도) 생성

                            // 마커 생성
                            AddMarker(locationName,address,point);

                            // 해당 좌표로 화면 줌
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point,15));
                        }

                        // 토스트 메시지 띄우기 (검색창 텍스트 내용)
                        Toast.makeText(getApplicationContext(),searchBox.getText()+"구글맵 검색!!", Toast.LENGTH_LONG).show();

                        // 검색창 내용 초기화
                        searchBox.getText().clear();

                        // 가상 키보드 숨기기
                        HideKeyboard(searchBox);
                }
                return true;
            }
        });
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

    // 지오코딩 (주소,지명 -> 위도,경도)
    private Address Geocoding(String searchAddress){
        Address address;
        List<Address> list = null;

        try {
            list = geocoder.getFromLocationName(
                    searchAddress, // 지역 이름
                    10); // 읽을 개수
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("입출력 오류 - 서버에서 주소변환시 에러발생");
        }

        if (list != null) {
            if (list.size() == 0) {
                System.out.println("해당되는 주소 정보는 없습니다");
            } else {
                System.out.println("성공!!!:"+list.get(0).toString());
                address= list.get(0);
                return address;
            }
        }
        return null;
    }

    // 기록장 미리보기화면, 버튼들 위로 슬라이딩
    private void ShowLayout(LinearLayout page, final LinearLayout buttons ){
        // 기록장 미리보기 화면 위로 슬라이딩
        final Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        page.setVisibility(View.VISIBLE);
        page.startAnimation(slideUp);

        // 버튼도 위로 슬라이딩
        final Animation btnSlideUp = AnimationUtils.loadAnimation(this, R.anim.btn_slide_up);
        btnSlideUp.setFillAfter(true);
        buttons.startAnimation(btnSlideUp);
        /*
        btnSlideUp.setAnimationListener(new Animation.AnimationListener() {
            float startposY;
            float endposY;

            @Override
            public void onAnimationStart(Animation animation) {
                startposY=buttons.getY();
                System.out.println("Start:"+startposY);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                buttons.setY(startposY-convertDpIntoPx(300));
                endposY=buttons.getY();
                System.out.println("End:"+endposY);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
         */
    }

    // 기록장 미리보기화면, 버튼들 아래로 슬라이딩
    private void HideLayout(LinearLayout page, LinearLayout buttons){
        // 기록장 미리보기 화면 아래로 슬라이딩
        final Animation slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);
        page.startAnimation(slideDown);
        page.setVisibility(View.GONE); // 화면 안 보이도록 설정

        // 버튼도 아래로 슬라이딩
        final Animation btnSlideDown = AnimationUtils.loadAnimation(this, R.anim.btn_slide_down);
        buttons.startAnimation(btnSlideDown);
    }

    // 가상 키보드 숨기기
    private void HideKeyboard(EditText et) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
    }

    // 마커 추가
    private void AddMarker(String name, String snippet, LatLng point){
        // 마커 옵션 설정
        MarkerOptions mOptions = new MarkerOptions();
        mOptions.title(name);
        mOptions.snippet(snippet);
        mOptions.position(point);
        // 마커 아이콘 변경 (비트맵 이미지만 가능)
        mOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_leaf_32));
        // 마커 추가
        mMap.addMarker(mOptions);
    }
}