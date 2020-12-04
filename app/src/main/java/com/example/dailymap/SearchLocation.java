package com.example.dailymap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class SearchLocation extends AppCompatActivity implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback{

    private GoogleMap mMap;
    private Geocoder geocoder;

    private View mapView;
    private Marker currentMarker = null; // 현재 위치 표시 마커

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int UPDATE_INTERVAL_MS = 1000;  // 1초
    private static final int FASTEST_UPDATE_INTERVAL_MS = 500; // 0.5초

    // onRequestPermissionsResult에서 수신된 결과에서 ActivityCompat.requestPermissions를 사용한 퍼미션 요청을 구별하기 위해 사용됨
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    boolean needRequest = false;

    // 앱을 실행하기 위해 필요한 퍼미션을 정의합니다.
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};  // 외부 저장소

    Location mCurrentLocation;
    LatLng currentPosition;

    private Marker searchMarker= null; // 검색한 위치 표시 마커

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private Location location;

    private View mLayout; // Snackbar 사용하기 위해서는 View가 필요

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_location);

        locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);


        LocationSettingsRequest.Builder builder =
                new LocationSettingsRequest.Builder();

        builder.addLocationRequest(locationRequest);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapView = mapFragment.getView();
        mapFragment.getMapAsync((OnMapReadyCallback) this);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {

        mMap = googleMap;
        geocoder = new Geocoder(this);

        // ++ ====================================================
        //런타임 퍼미션 요청 대화상자나 GPS 활성 요청 대화상자 보이기전에 지도의 초기위치를 서울로 이동
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
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식)
            startLocationUpdates(); // 3. 위치 업데이트 시작
        }
        else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요. 2가지 경우(3-1, 4-1) 존재.
            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])) {
                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명
                Snackbar.make(mLayout, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.",
                        Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        // 3-3. 사용자에게 퍼미션 요청. 요청 결과는 onRequestPermissionResult에서 수신됨.
                        ActivityCompat.requestPermissions( SearchLocation.this, REQUIRED_PERMISSIONS,
                                PERMISSIONS_REQUEST_CODE);
                    }
                }).show();


            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 함
                // 요청 결과는 onRequestPermissionResult에서 수신됨
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
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 50, 150);
        }

        // ====================================================

        LatLng SEOUL = new LatLng(37.57, 126.97);
        // 마커 추가
        //AddMarker("경복궁","투어&박물관이 있는 역사적인 궁전",SEOUL);
        // SEOUL로 카메라 시점 이동
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(SEOUL, 10));

        // 검색 핸들러
        SearchHandler();

        // 마커 클릭에 대한 이벤트 처리
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Toast.makeText(getApplicationContext(),"마커 클릭!!", Toast.LENGTH_SHORT).show();
                System.out.println("마커 클릭!!!!");

                // 마커 클릭한 경우
                // 위치 정보 기록장 추가 화면으로 넘겨주기
                //String location = marker.getTitle();
                String location = marker.getSnippet();
                double latitude = marker.getPosition().latitude;
                double longitude = marker.getPosition().longitude;

                // 주소, 위도, 경도 값 AddDiary 화면으로 넘겨주기
                Intent intent = new Intent(getApplicationContext(), AddDiary.class);
                intent.putExtra("location", location);
                intent.putExtra("latitude", latitude);
                intent.putExtra("longitude", longitude);

                setResult(RESULT_OK, intent);
                finish();

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
        });
    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            List<Location> locationList = locationResult.getLocations();

            if (locationList.size() > 0) {
                location = locationList.get(locationList.size() - 1);
                //location = locationList.get(0);

                // 현재 위치 (위도, 경도)
                currentPosition
                        = new LatLng(location.getLatitude(), location.getLongitude());


                // 현재 위치 주소 가지고 역 지오코딩
                Address addr= ReverseGeocoding(currentPosition);
                String markerTitle = addr.getFeatureName(); // 주소이름,
                String address = addr.getAddressLine(0); // 주소

                // 현재 위치에 마커 추가
                setCurrentLocation(location, markerTitle, address);

                mCurrentLocation = location;
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();

        System.out.println("onStart");

        if (checkPermission()) {

            System.out.println("onStart : call mFusedLocationClient.requestLocationUpdates");
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);

            if (mMap!=null)
                mMap.setMyLocationEnabled(true);
        }
    }


    @Override
    protected void onStop() {
        super.onStop();

        if (mFusedLocationClient != null) {

            System.out.println("onStop : call stopLocationUpdates");
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }


    // ++ ===================================================
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
    public void setCurrentLocation(Location location, String markerTitle, String markerSnippet) {
        if (currentMarker != null) currentMarker.remove();

        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(currentLatLng);
        //markerOptions.title(markerTitle);
        markerOptions.title("현재 위치");
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);

        currentMarker = mMap.addMarker(markerOptions);

        // 마커 정보 표시
        currentMarker.showInfoWindow();

        // 마커로 카메라 이동
        // CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng);
        // mMap.moveCamera(cameraUpdate);
    }

    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(SearchLocation.this);
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

            // 모든 퍼미션을 허용했는지 체크
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
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료 (2 가지 경우 존재)
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {

                    // 사용자가 거부만 선택한 경우 : 앱을 다시 실행하여 허용을 선택하면 앱 사용 가능
                    Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {

                            finish();
                        }
                    }).show();

                }else {
                    // "다시 묻지 않음"을 사용자가 체크하고 거부를 선택한 경우 : 설정(앱 정보)에서 퍼미션을 허용해야 앱 사용 가능
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


    // 검색 핸들러
    @SuppressLint("ClickableViewAccessibility")
    public void SearchHandler(){

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
                        AddMarker(locationName,address,point);

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
        mOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_dm_green_32));
        // 마커 추가
        searchMarker =mMap.addMarker(mOptions);

        // 추가한 마커 정보 표시
        // marker.showInfoWindow();
    }
}

