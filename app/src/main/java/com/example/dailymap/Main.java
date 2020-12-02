package com.example.dailymap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Main extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Geocoder geocoder;
    private FirebaseAuth mAuth; //auth

    //++ ====================================
    private View mapView;
    private MapFragment mMapFragment;
    private View mMyLocationButtonView = null;
    private Marker currentMarker = null;

    private static final String TAG = "project";
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int UPDATE_INTERVAL_MS = 1000;  // 1초
    private static final int FASTEST_UPDATE_INTERVAL_MS = 500; // 0.5초

    // onRequestPermissionsResult에서 수신된 결과에서 ActivityCompat.requestPermissions를 사용한 퍼미션 요청을 구별하기 위해 사용됩니다.
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    boolean needRequest = false;

    // 앱을 실행하기 위해 필요한 퍼미션을 정의합니다.
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};  // 외부 저장소

    Location mCurrentLocation;
    LatLng currentPosition;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private Location location;

    private Marker searchMarker= null; // 검색한 위치를 표시하는 마커
    private String clickLocation= null;// 기존에 존재하는 마커 중 선택한 마커 정보
    private double clickLatitude, clickLongitude;

    String mLocation; // 기록장 추가 화면으로 넘겨줄 데이터 저장
    double mLatitude, mLongitude;

    private View mLayout; // Snackbar 사용하기 위해서는 View가 필요

    //DiaryGroup 정보 유지
    String curDG;
    FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    @Override //Auth 확인
    protected void onStart() {
        super.onStart();
        //FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser(); //에러 남 > 로그아웃 안됨
        Toast.makeText(Main.this,"start",Toast.LENGTH_LONG).show();
        if(user!=null){ //login 중
            Toast.makeText(Main.this,user.getDisplayName(),Toast.LENGTH_SHORT).show();
        }
        else{ //user 없으면 signin page로 넘어가기
            Toast.makeText(Main.this,"no one",Toast.LENGTH_LONG).show();
            Intent intent = new Intent(getApplicationContext(),SignIn.class);
            startActivity(intent);
        }

        // ++ ===============================================
        if (checkPermission()) {

            System.out.println("onStart : call mFusedLocationClient.requestLocationUpdates");
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);

            if (mMap!=null)
                mMap.setMyLocationEnabled(true);
            //mMyLocationButtonView = mMapFragment.getView().findViewWithTag("GoogleMapMyLocationButton");
            //mMyLocationButtonView.setBackgroundColor(Color.GREEN);
        }
        // ==================================================
        //다이어리 정보 유지
        curDG = getIntent().getStringExtra("curDG"); //signin에서 받아온거 사용
        Toast.makeText(Main.this,"1현재 : "+curDG,Toast.LENGTH_LONG).show();
        if(curDG==null) curDG = user.getDisplayName()+"'s diary";
        Toast.makeText(Main.this,"현재 : "+curDG,Toast.LENGTH_LONG).show();

        // DB에서 현재 DiaryGroup의 diaryList 정보 가져오기
        db.collection("DiaryGroupList").document(curDG)
                .collection("diaryList")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            ArrayList<Map<String,Object>> dataList = new ArrayList<Map<String, Object>>();
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        dataList.add(document.getData());
                        System.out.println(curDG+ "dataList!!!!!: "+dataList);
                        System.out.println(curDG+ "- 위도: "+dataList.get(dataList.size()-1).get("locationX"));
                        System.out.println(curDG+ "- 경도: "+dataList.get(dataList.size()-1).get("locationY"));
                    }
                    addMarkerFromDB(dataList);
                } else {
                    Toast.makeText(Main.this,"Failed to get img",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();

        mLayout = findViewById(R.id.layout_main);
        locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);


        LocationSettingsRequest.Builder builder =
                new LocationSettingsRequest.Builder();

        builder.addLocationRequest(locationRequest);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        // ====================================

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapView = mapFragment.getView();
        mapFragment.getMapAsync(this);


    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {

        mMap = googleMap;
        geocoder = new Geocoder(this);

        // ++ ====================================================
        //런타임 퍼미션 요청 대화상자나 GPS 활성 요청 대화상자 보이기전에
        //지도의 초기위치를 서울로 이동
        setDefaultLocation();

        // 런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED   ) {

            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)
            startLocationUpdates(); // 3. 위치 업데이트 시작
        }
        else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.
            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])) {
                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Snackbar.make(mLayout, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.",
                        Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                        ActivityCompat.requestPermissions( Main.this, REQUIRED_PERMISSIONS,
                                PERMISSIONS_REQUEST_CODE);
                    }
                }).show();


            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions( this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }

        }

        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        // 현재 위치 표시 버튼 위치 조정
        if (mapView != null &&
                mapView.findViewById(Integer.parseInt("1")) != null) {
            // Get the button view
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            // and next place it, on bottom right (as Google Maps app)
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                    locationButton.getLayoutParams();

            // rule 제거 position on left bottom
            int[] ruleList = layoutParams.getRules();
            for (int i = 0; i < ruleList.length; i ++) {
                layoutParams.removeRule(i);
            }
            //
            //layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            layoutParams.setMargins(1500, 0, 50, 150);
        }

        // ====================================================


        LatLng SEOUL = new LatLng(37.57, 126.97);
        // 마커 추가 ( DB에서 가져오기 전 테스트 용이었음 )
        //AddMarker("경복궁","투어&박물관이 있는 역사적인 궁전",SEOUL);
        // SEOUL로 카메라 시점 이동
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(SEOUL, 10));

        // 메인화면 버튼 핸들러 : 계정관리, (다이어리확인), 달력, 기록장추가 버튼
        MainUIBtnHandler();

        // 검색 핸들러
        SearchHandler();

        // 슬라이딩 애니메이션 적용 레이아웃
        final LinearLayout slidePage = findViewById(R.id.sliding_page); //기록장 미리보기 화면
        final LinearLayout slideButtons = findViewById(R.id.sliding_buttonPanel); // 버튼 패널 (기록장 추가, 달력 버튼)

        // 슬라이딩 화면 업데이트할 텍스트뷰
        final TextView placeText =findViewById(R.id.place_text);
        final TextView diaryNum = findViewById(R.id.recordNum_text);

        // 마커 클릭에 대한 이벤트 처리
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {

                // 마커 정보 표시
                marker.showInfoWindow();

                // 마커에 해당하는 기록장 정보 가져오기
                db.collection("DiaryGroupList").document(curDG)
                        .collection("diaryList")
                        .whereEqualTo("locationX",marker.getPosition().latitude)
                        .whereEqualTo("locationY",marker.getPosition().longitude)
                        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    ArrayList<Map<String,Object>> diaryLists = new ArrayList<Map<String, Object>>();
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                diaryLists.add(document.getData());
                                System.out.println(curDG+ "diaryLists!!!!!: "+diaryLists);
                                System.out.println("contents!!"+ diaryLists.get(diaryLists.size()-1).get("content"));
                                placeText.setText(marker.getSnippet());
                                diaryNum.setText("기록 +" + diaryLists.size());

                                //Storage
                                storage= FirebaseStorage.getInstance("gs://daily-map-d47b1.appspot.com");
                                storageRef = storage.getReference();

                                storageRef.child(curDG+"/"+diaryLists.get(diaryLists.size()-1).get("img").toString())
                                        .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    ImageView tmpIV=findViewById(R.id.diary_image_1);
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Glide.with(Main.this)
                                                .load(uri)
                                                .into(tmpIV);
                                        Toast.makeText(Main.this,"이미지 불러오기 성공",Toast.LENGTH_LONG).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        System.out.println(e);
                                        Toast.makeText(Main.this,"이미지 불러오기 실패",Toast.LENGTH_LONG).show();
                                    }
                                });
                            }

                            // 해당 마커 위치에 기록장 정보가 없는 경우
                            if(diaryLists.size()<=0){
                                Toast.makeText(Main.this,"클릭하신 마커 위치에는 기록장이 없습니다..",Toast.LENGTH_LONG).show();
                                System.out.println("클릭한 마커 정보 dairylist Size 0!!!"+marker.getSnippet());
                            }
                            // 해당 마커 위치에 기록장 정보가 있는 경우
                            else{
                                // 기록장 미리보기 화면 띄우기
                                // if(slidePage.getVisibility()==View.GONE){ }
                                ShowLayout(slidePage, slideButtons);
                                System.out.println("클릭한 마커 정보!!!"+marker.getSnippet());

                                clickLocation= marker.getSnippet();
                                clickLatitude = marker.getPosition().latitude;
                                clickLongitude = marker.getPosition().longitude;

                            }

                        } else {
                            System.out.println("Failed to get img");
                            Toast.makeText(Main.this,"Failed to get img",Toast.LENGTH_SHORT).show();
                        }
                    }
                });

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

                // 검색창 커서 활성화 되어있는 경우 비활성화 시키기
                if(searchBox.isCursorVisible()){
                    searchBox.setCursorVisible(false);
                }

                // 슬라이딩 화면(마커 클릭 시 나온 화면) 보이는 경우 다시 내려가게 하기
                if(slidePage.getVisibility()==View.VISIBLE){
                    HideLayout(slidePage, slideButtons);
                }

                // 슬라이딩 화면(마커 클릭 시 나온 화면) 안 보이는 경우 마커 추가
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
                        /*
                    Address addr= ReverseGeocoding(point);

                    if(addr!=null){
                        String locationName = addr.getFeatureName(); // 주소이름,
                        String address = addr.getAddressLine(0); // 주소

                        // 지도에서 클릭한 위치에 마커 추가
                        AddMarker(locationName,address,point);
                    }
                         */
                }
            }
        });
    }

    // ++ ===================================================
    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            List<Location> locationList = locationResult.getLocations();

            if (locationList.size() > 0) {
                location = locationList.get(locationList.size() - 1);
                //location = locationList.get(0);

                currentPosition
                        = new LatLng(location.getLatitude(), location.getLongitude());

                if (currentMarker != null) currentMarker.remove();
                /*
                String markerTitle = getCurrentAddress(currentPosition);
                String markerSnippet = "위도:" + String.valueOf(location.getLatitude())
                        + " 경도:" + String.valueOf(location.getLongitude());

                System.out.println("onLocationResult : " + markerSnippet);

                //현재 위치에 마커 생성하고 이동
                setCurrentLocation(location, markerTitle, markerSnippet);
                 */
                mCurrentLocation = location;
            }
        }
    };

    @Override
    protected void onStop() {
        super.onStop();

        if (mFusedLocationClient != null) {

            System.out.println("onStop : call stopLocationUpdates");
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    // ++ ===================================================

    public void addMarkerFromDB(ArrayList<Map<String,Object>> data){
        for(int i=0; i<data.size(); i++){
            double latitude; // 위도
            double longitude; // 경도
            latitude= (double) data.get(i).get("locationX");
            longitude = (double) data.get(i).get("locationY");

            LatLng latLng = new LatLng(latitude, longitude);
            System.out.println(latitude+", " +longitude);
            Address addr= ReverseGeocoding(latLng);

            if(addr!=null){
                String locationName = addr.getFeatureName(); // 주소이름,
                String address = addr.getAddressLine(0); // 주소

                // DB에서 가져온 기록장 정보 위치에 마커 추가
                AddMarker(0, locationName,address,latLng);
            }
        }
    }

    // 지도에서 초기 위치 설정
    public void setDefaultLocation() {

        //디폴트 위치, Seoul
        LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);
        String markerTitle = "위치정보 가져올 수 없음";
        String markerSnippet = "위치 퍼미션과 GPS 활성 요부 확인하세요";

        if (currentMarker != null) currentMarker.remove();

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(DEFAULT_LOCATION);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        currentMarker = mMap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 15);
        mMap.moveCamera(cameraUpdate);

    }

    // 현재 위치 업데이트
    private void startLocationUpdates() {

        if (!checkLocationServicesStatus()) {

            System.out.println("startLocationUpdates : call showDialogForLocationServiceSetting");
            showDialogForLocationServiceSetting();
        }else {

            int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION);



            if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED ||
                    hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED   ) {

                System.out.println("startLocationUpdates : 퍼미션 안가지고 있음");
                return;
            }


            System.out.println("startLocationUpdates : call mFusedLocationClient.requestLocationUpdates");

            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

            if (checkPermission())
                mMap.setMyLocationEnabled(true);
            //mMyLocationButtonView = mMapFragment.getView().findViewWithTag("GoogleMapMyLocationButton");
            //mMyLocationButtonView.setBackgroundColor(Color.GREEN);

        }

    }

    // 현재 위치 주소 반환
    public String getCurrentAddress(LatLng latlng) {

        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {

            addresses = geocoder.getFromLocation(
                    latlng.latitude,
                    latlng.longitude,
                    1);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";

        }


        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";

        } else {
            Address address = addresses.get(0);
            return address.getAddressLine(0).toString();
        }

    }

    // 현재 위치에 마커 생성
    /*
    public void setCurrentLocation(Location location, String markerTitle, String markerSnippet) {
        if (currentMarker != null) currentMarker.remove();

        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(currentLatLng);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);

        currentMarker = mMap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng);
        mMap.moveCamera(cameraUpdate);
    }
     */

    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    // 위치 서비스 상태 확인
    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    //여기부터는 런타임 퍼미션 처리을 위한 메소드들
    private boolean checkPermission() {

        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);



        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED   ) {
            return true;
        }

        return false;

    }

    //ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메소드입니다.
    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        if ( permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면

            boolean check_result = true;


            // 모든 퍼미션을 허용했는지 체크합니다.

            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }


            if ( check_result ) {

                // 퍼미션을 허용했다면 위치 업데이트를 시작합니다.
                startLocationUpdates();
            }
            else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {


                    // 사용자가 거부만 선택한 경우에는 앱을 다시 실행하여 허용을 선택하면 앱을 사용할 수 있습니다.
                    Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {

                            finish();
                        }
                    }).show();

                }else {


                    // "다시 묻지 않음"을 사용자가 체크하고 거부를 선택한 경우에는 설정(앱 정보)에서 퍼미션을 허용해야 앱을 사용할 수 있습니다.
                    Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {

                            finish();
                        }
                    }).show();
                }
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case GPS_ENABLE_REQUEST_CODE:
                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {
                        System.out.println("onActivityResult : GPS 활성화 되있음");
                        needRequest = true;
                        return;
                    }
                }
                break;
        }
    }


    // ===================================================


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
                //다이어리 정보 유지
                intent.putExtra("curDG",curDG);
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
                //다이어리 정보 유지
                intent.putExtra("curDG",curDG);
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
                //다이어리 정보 유지
                intent.putExtra("curDG",curDG);

                // 검색한 장소 정보 넘겨주기
                if(searchMarker!=null){
                    mLocation = searchMarker.getSnippet();
                    mLatitude = searchMarker.getPosition().latitude;
                    mLongitude = searchMarker.getPosition().longitude;
                    System.out.println("검색한 장소 위치값 디폴트로 넘겨줌!!! "+mLocation);
                }
                // 검색한 장소가 없다면 현재위치를 디폴트 값으로 넘겨주기
                else if(searchMarker==null){
                    Address addr= ReverseGeocoding(currentPosition);
                    String address=null;
                    if(addr!=null){
                        address = addr.getAddressLine(0); // 주소
                    }
                    mLocation = address;
                    mLatitude = currentPosition.latitude;
                    mLongitude = currentPosition.longitude;
                    System.out.println("현재 위치값 디폴트로 넘겨줌!!! "+mLocation);
                }
                System.out.println("위도, 경도 : "+ mLatitude+ ", " +mLongitude);
                intent.putExtra("mLocation", mLocation);
                intent.putExtra("mLatitude", mLatitude);
                intent.putExtra("mLongitude", mLongitude);

                startActivity(intent);
            }
        });

        // 슬라이딩 화면 기록장 추가 버튼
        ImageButton addButton_slide = (ImageButton) findViewById(R.id.sliding_add_btn);
        addButton_slide.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Toast.makeText(getApplicationContext(),"슬라이딩 패널 기록장 추가 버튼", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Main.this,AddDiary.class);
                //다이어리 정보 유지
                intent.putExtra("curDG",curDG);

                // 마커 위치 정보 디폴트로 넘겨줌
                mLocation = clickLocation;
                mLatitude = clickLatitude;
                mLongitude = clickLongitude;
                System.out.println("클릭한 마커 위치값 디폴트로 넘겨줌!!! "+mLocation);
                intent.putExtra("mLocation", mLocation);
                intent.putExtra("mLatitude", mLatitude);
                intent.putExtra("mLongitude", mLongitude);

                startActivity(intent);
            }
        });

        // 슬라이딩 패널 -> 누르면 다이어리 확인 창으로 넘어감
        LinearLayout slidingPanel = (LinearLayout) findViewById(R.id.sliding_page);
        slidingPanel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Toast.makeText(getApplicationContext(),"슬라이딩 패널 클릭", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Main.this,DiaryList.class);
                //다이어리 정보 유지
                intent.putExtra("curDG",curDG);

                // 마커 위치 정보 디폴트로 넘겨줌
                mLocation = clickLocation;
                mLatitude = clickLatitude;
                mLongitude = clickLongitude;
                System.out.println("클릭한 마커 위치값 디폴트로 넘겨줌!!! "+mLocation);
                intent.putExtra("mLocation", mLocation);
                intent.putExtra("mLatitude", mLatitude);
                intent.putExtra("mLongitude", mLongitude);

                startActivity(intent);
            }
        });

        //+========================================================
        //+=========================================================

    }

    // 검색 핸들러
    @SuppressLint("ClickableViewAccessibility")
    public void SearchHandler(){
        final LinearLayout slidePage = findViewById(R.id.sliding_page); //기록장 미리보기 화면
        final LinearLayout slideButtons = findViewById(R.id.sliding_buttonPanel); // 버튼 패널 (기록장 추가, 달력 버튼)

        // 검색창
        final EditText searchBox= findViewById(R.id.edit_text);

        searchBox.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                searchBox.setCursorVisible(true);
            }
        });

        // 구글맵 검색하는 부분
        searchBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH){
                    // 커서 비활성화
                    searchBox.setCursorVisible(false);

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
                        if (searchMarker != null) searchMarker.remove();
                        AddMarker(1, locationName,address,point);

                        // 해당 좌표로 화면 줌
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point,15));
                    }

                    // 토스트 메시지 띄우기 (검색창 텍스트 내용)
                    Toast.makeText(getApplicationContext(),searchBox.getText()+"구글맵 검색!!", Toast.LENGTH_LONG).show();

                    // 검색창 내용 초기화
                    //searchBox.getText().clear();

                    // 가상 키보드 숨기기
                    HideKeyboard(searchBox);

                    return true;
                }
                return false;
            }
        });

        // 검색창 x 버튼 기능
        searchBox.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(event.getRawX() >= (searchBox.getRight() - searchBox.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        // 검색창 내용 초기화
                        searchBox.getText().clear();

                        // 가상키보드 숨기기
                        HideKeyboard(searchBox);

                        // 커서 비활성화
                        if(searchBox.isCursorVisible()){
                            searchBox.setCursorVisible(false);
                        }

                        // 검색 위치 표시 마커 지우기
                        if (searchMarker != null) {
                            searchMarker.remove();
                            searchMarker=null;
                        }
                        return true;
                    }
                }
                return false;
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

    // 기록장 미리보기화면 위로 슬라이딩, 기존 버튼 숨기기
    private void ShowLayout(LinearLayout page, final LinearLayout buttons ){
        // 기록장 미리보기 화면 위로 슬라이딩
        final Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        page.setVisibility(View.VISIBLE);
        page.startAnimation(slideUp);

        // 버튼 숨기기 (알파값 1->0)
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

    // 기록장 미리보기화면 아래로 슬라이딩, 기존 버튼 보이게
    private void HideLayout(LinearLayout page, LinearLayout buttons){
        // 기록장 미리보기 화면 아래로 슬라이딩
        final Animation slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);
        page.startAnimation(slideDown);
        page.setVisibility(View.GONE); // 화면 안 보이도록 설정

        // 버튼 보이게 (알파값 0->1)
        final Animation btnSlideDown = AnimationUtils.loadAnimation(this, R.anim.btn_slide_down);
        buttons.startAnimation(btnSlideDown);
    }

    // 가상 키보드 숨기기
    private void HideKeyboard(EditText et) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
    }

    // 마커 추가
    private void AddMarker(int type, String name, String snippet, LatLng point){
        // 마커 옵션 설정
        MarkerOptions mOptions = new MarkerOptions();
        mOptions.title(name);
        mOptions.snippet(snippet);
        mOptions.position(point);
        // 마커 아이콘 변경 (비트맵 이미지만 가능)
        if(type ==0 ){ // 기록장 표시 마커
            mOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_leaf_32));
            // 마커 추가
            mMap.addMarker(mOptions);
        }
        else{ // 검색한 위치 표시 마커
            mOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_leaf2_32));
            // 마커 추가
            searchMarker=mMap.addMarker(mOptions);
        }

        // 추가한 마커 정보 표시
        // marker.showInfoWindow();
    }
}