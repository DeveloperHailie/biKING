package com.sungshin.biking;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.maps.android.PolyUtil;
import com.kakao.kakaolink.v2.KakaoLinkResponse;
import com.kakao.kakaolink.v2.KakaoLinkService;
import com.kakao.message.template.ContentObject;
import com.kakao.message.template.FeedTemplate;
import com.kakao.message.template.LinkObject;
import com.kakao.network.ErrorResult;
import com.kakao.network.callback.ResponseCallback;

import java.util.HashMap;
import java.util.Map;


public class Maps2Activity extends AppCompatActivity
        implements
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback{

    private GoogleMap mMap;
    private LocationRequest mLocationRequest;
    private Location mCurrentLocation;

    private FusedLocationProviderClient mFusedLocationClient;
    private Marker mCurrentMarker;
    private LatLng startLatLng = new LatLng(0, 0);        //polyline 시작점
    private LatLng endLatLng = new LatLng(0, 0);        //polyline 끝점
    private boolean ridingState = false;                    //걸음 상태


    private long startTime=0;
    private long waitingStartTime = 0;
    private long stopTime = 0;
    private long time;
    private boolean reStart = false;
    private long deltaTime = 0;
    private float distance = 0 ;
    private float goalDistance = 0 ;
    private double averageSpeed = 0;
    private long stoppedTime = 0;
    private long allStoppedTime = 0;
    private long speedCount = 0;
    private double sumSpeed = 0;
    private double speed;
    private String stringTime ="00:00:00";




    private FloatingActionButton pathFab;
    private FloatingActionButton startFab;
    private FloatingActionButton pauseFab;
    private FloatingActionButton stopFab;
    private FloatingActionButton myLocationFab;

    private TextView speedText;

    private TextView riddingDistance;
    private TextView calText;
    private Chronometer chrono;
    private TextView smallRiddingTime;
    private TextView smallRiddingDistance;

    private LocationCallback mLocationCallback;

    // 라이딩 모드 관련
    private String mode = "none";

    private int goalTime = 60;
    private double goalDis = 0;
    private ProgressBar progressbar;
    private Chronometer subChrono;
    private TextView riddingTimeTitle;
    private TextView riddingDistanceTitle;
    private boolean result = false;
    private TextView resultText;
    private boolean resultCheck = false;
    private LinearLayout distanceLayout;

    private int getExp = 0;

    // 카메라 확대
    private float zoom = 18.0f;

    //지도
    Double goalLatitude = 0.0;
    Double goalLongitude = 0.0;
    LatLng goalLat;
    Boolean onPath = false;
    private FloatingActionButton pathAgainFab;
    private LatLng mCurrentLatLng;

    // 대결 모드
    TextView waitingText;
    private boolean matching = false;
    int matchingUserId;
    private long matchingStartTime = 0;
    int w = 0;
    private double myDistance = 0;
    private int myTime = 0;
    private double otherDistance = 0;
    private int otherTime = 0;
    int comExp = 0 ;

    boolean finishActivityChecker = false;


    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map2);

        firebaseDB.maps2Condition = true;
        firebaseDB.comBase = new HashMap<String, Competition>();
        firebaseDB.idToEmail = new HashMap<Integer, String>();
        firebaseDB.email = null;

        //시작시 권한 체크
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "위치 권한 설정을 허용해주셔야 합니다", Toast.LENGTH_LONG).show();
            return;
        }


        startFab = (FloatingActionButton) findViewById(R.id.fab_start);
        pauseFab = (FloatingActionButton) findViewById(R.id.fab_pause);
        stopFab = (FloatingActionButton) findViewById(R.id.fab_stop);
        myLocationFab = (FloatingActionButton) findViewById(R.id.fab_myLocation);
        pathAgainFab = (FloatingActionButton) findViewById(R.id.fab_path_again);
        pathFab = (FloatingActionButton) findViewById(R.id.fab_path);
        chrono = (Chronometer) findViewById(R.id.chrono);
        speedText = (TextView) findViewById(R.id.text_speed);
        riddingDistance = (TextView) findViewById(R.id.text_ridingDistance);
        calText = (TextView) findViewById(R.id.text_calories);
        progressbar = (ProgressBar) findViewById(R.id.progressbar);
        smallRiddingDistance = (TextView) findViewById(R.id.text_ridingDistance_small);
        smallRiddingTime = (TextView) findViewById(R.id.text_ridingTime_small);
        subChrono = (Chronometer) findViewById(R.id.chrono_sub);
        riddingDistanceTitle = (TextView) findViewById(R.id.riddingDistance_title);
        riddingTimeTitle = (TextView) findViewById(R.id.riddingtime_title);
        resultText = (TextView) findViewById(R.id.result_text);
        distanceLayout = (LinearLayout) findViewById(R.id.distance_layout);
        waitingText = (TextView) findViewById(R.id.waiting_text);


        // 라이딩 모드 받아오기
        resultText.setVisibility(View.GONE);
        Intent intent = getIntent();
        mode = intent.getStringExtra("mode");

        if (mode.equals("random")) {

            waitingStartTime = System.currentTimeMillis();
            riddingDistanceTitle.setText("상대와");
            riddingDistance.setText(String.format("%.1f", 0.00f));
            riddingTimeTitle.setText("시작 버튼을 누르면 \n매칭이 시작됩니다");
            chrono.setVisibility(View.GONE);
            progressbar.setProgress(0);
            progressbar.setSecondaryProgress(0);
            distanceLayout.setVisibility(View.INVISIBLE);
            matching = false;

            distance = 0;



        }


        startAnimation();




        chrono.setFormat("00:%s");
        chrono.setTypeface(ResourcesCompat.getFont(this, R.font.pfstardust));
        chrono.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            public void onChronometerTick(Chronometer c) {
                long elapsedMillis = SystemClock.elapsedRealtime() - chrono.getBase();
                if (elapsedMillis > 3600000L) {
                    chrono.setFormat("0%s");
                } else {
                    chrono.setFormat("00:%s");
                }
            }
        });


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        startFab.setOnClickListener(new View.OnClickListener() {

            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View view) {

                android.app.AlertDialog.Builder dlg = new android.app.AlertDialog.Builder(Maps2Activity.this);
                dlg.setTitle("경쟁 모드");
                dlg.setMessage("경쟁을 시작하시겠습니까? 경쟁 시작시 취소, 멈춤이 불가능합니다.");
                dlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        changeWalkState();        //라이딩 상태 변경
                        chrono.setVisibility(View.VISIBLE);
                        chrono.setBase(SystemClock.elapsedRealtime());
                        chrono.start();
                        subChrono.setBase(SystemClock.elapsedRealtime());
                        subChrono.start();
                        riddingTimeTitle.setText("대기 시간");
                        startFab.setVisibility(View.INVISIBLE);
                        pauseFab.setVisibility(View.VISIBLE);
                        firebaseDB.startCompetition(firebaseDB.myUser);
                        startTime = System.currentTimeMillis();
                        waitingStartTime = System.currentTimeMillis();
                        if( !onPath ){
                            ObjectAnimator stopAnimation = ObjectAnimator.ofFloat(stopFab, "translationY", 550.0f);
                            stopAnimation.setDuration(1000);
                            stopAnimation.start();
                        }else{

                            ObjectAnimator stopAnimation = ObjectAnimator.ofFloat(stopFab, "translationY", 710.0f);
                            stopAnimation.setDuration(1000);
                            stopAnimation.start();

                        }

                    }
                });
                dlg.show();


            }
        });

        pauseFab.setOnClickListener(new View.OnClickListener() {

            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View view) {
                if(mode.equals("finish")){
                    changeWalkState();        //라이딩 상태 변경
                    startFab.setVisibility(View.VISIBLE);
                    pauseFab.setVisibility(View.INVISIBLE);
                }else{
                    Toast.makeText(getApplicationContext(),"경쟁 중 멈춤이 불가능 합니다.",Toast.LENGTH_SHORT).show();
                }


            }
        });

        pathAgainFab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // 경로 재 검색 코드
                if (isBreakAway() == false) {
                    if (mCurrentLocation != null && onPath) {
                        Toast.makeText(getApplicationContext(), "경로를 이탈했습니다. 경로를 재탐색합니다.", Toast.LENGTH_SHORT).show();
                        LatLng now = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                        MapPath.drawRoute(now, goalLat, getString(R.string.google_maps_key), mMap, getApplicationContext());
                    }
                }
            }
        });


        pathFab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MapPathActivity.class);
                startActivityForResult(intent, 10);
            }
        });

        stopFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getStringTime();
                wantFinish();
            }
        });

        myLocationFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMap != null && mCurrentLocation != null) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()), zoom));
                }

            }


        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        createLocationRequest();
        createLocationCallback();
        getCurrentLocation();

        firebaseDB.competitionListener();
        firebaseDB.clearPrevData(firebaseDB.myUser.getId());


        // 권혜영 추가 코드
        firebaseDB.updateCom();
        String message = "";
        String email[] = new String [firebaseDB.comBase.size()];

        firebaseDB.comBase.keySet().toArray(email);
        for(int j=0;j<firebaseDB.comBase.size();j++) {
            message = message + email[j] + " ";
        }
        Log.e("경쟁자",message);
       // Toast.makeText(Maps2Activity.this, "경쟁사용자:"+message, Toast.LENGTH_SHORT).show();




    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data ) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10 && resultCode == RESULT_OK){
            goalLatitude  = data.getDoubleExtra("glatitude",0);
            goalLongitude = data.getDoubleExtra("glongitude", 0);
            Double oriLatitude  = data.getDoubleExtra("slatitude",0);
            Double oriLongitude = data.getDoubleExtra("slongitude", 0);
            goalLat = new LatLng(goalLatitude, goalLongitude);
            LatLng ori = new LatLng(oriLatitude, oriLongitude);
            MapPath.drawRoute(ori, goalLat, getString(R.string.google_maps_key), mMap, getApplicationContext());
            onPath = true;
            ObjectAnimator pathAAnimation = ObjectAnimator.ofFloat(pathAgainFab, "translationY", 390.0f);
            pathAAnimation.setDuration(1000);
            pathAAnimation.start();
        }
        if(requestCode == 10 && resultCode == RESULT_CANCELED){
            Toast.makeText(getApplicationContext(),"경로 검색을 취소하셨습니다.",  Toast.LENGTH_SHORT).show();
        }



    }



    private void getCurrentLocation(){
        OnCompleteListener<Location> mCompleteListener = new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                {
                    if(task.isSuccessful() && task.getResult() != null) {
                        mCurrentLocation = task.getResult();
                    }
                }
            }
        };
        mFusedLocationClient.getLastLocation().addOnCompleteListener( this, mCompleteListener);
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,mLocationCallback,Looper.myLooper() );


    }

    private void createLocationCallback(){
        mLocationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult){
                super.onLocationResult(locationResult);

                //compUpdate
                firebaseDB.updateCom();


                Location newLocation = locationResult.getLastLocation();

                double latitude = newLocation.getLatitude(), longtitude = newLocation.getLongitude();

                if( mCurrentLocation != null ) { //&&  newLocation.getSpeed()>0.3 - 속도 제한
                    if(ridingState ==false ){
                        switch(w){
                            case 0: waitingText.setText("대기중");
                                break;
                            case 1: waitingText.setText("대기중.");
                                break;
                            case 2: waitingText.setText("대기중..");
                                break;
                            case 3: waitingText.setText("대기중...");
                                break;
                        }
                    }else {
                        if (mode.equals("finish")) {
                            distance += (double) (mCurrentLocation.distanceTo(newLocation)) / 1000f;
                            riddingDistance.setText(String.valueOf(Math.floor(distance * 100) / 100));
                        }

                        if (!matching && !resultCheck) {
                            distance += (double) (mCurrentLocation.distanceTo(newLocation)) / 1000f;
                            smallRiddingDistance.setText("주행거리 " + String.valueOf(Math.floor(distance * 100) / 100));

                            firebaseDB.findCompetition();
                            if (firebaseDB.otherCom != null && firebaseDB.otherCom.getId() != 0) {
                                matchingUserId = firebaseDB.otherCom.getId();
                                matching = true;
                            }

                            //상대방의 id를 찾아 matchingUserId에 저장
                            if (matchingUserId != 0) {
                                chrono.stop();
                                chrono.setCountDown(true);
                                chrono.setBase(SystemClock.elapsedRealtime() + 1000 * 60 * goalTime);
                                chrono.start();
                                matchingStartTime = System.currentTimeMillis();
                                matching = true;
                                riddingTimeTitle.setText("남은 시간");
                                progressbar.setProgress(0);
                                distanceLayout.setVisibility(View.VISIBLE);
                                waitingText.setVisibility(View.GONE);
                                Toast.makeText(getApplicationContext(), firebaseDB.otherCom.getEmail()+"님과의 경쟁이 시작되었습니다.", Toast.LENGTH_LONG).show();
                            }

                            switch (w) {
                                case 0:
                                    waitingText.setText("매칭중");
                                    break;
                                case 1:
                                    waitingText.setText("매칭중.");
                                    break;
                                case 2:
                                    waitingText.setText("매칭중..");
                                    break;
                                case 3:
                                    waitingText.setText("매칭중...");
                                    break;
                            }
                            w += 1;
                            w = (w) % 4;
                        } else {
                            if (!resultCheck) {
                                myDistance += (double) (mCurrentLocation.distanceTo(newLocation)) / 1000f;
                                if (firebaseDB.otherCom != null){
                                    otherDistance = firebaseDB.otherCom.getCurrentDistance();
                                    otherTime = firebaseDB.otherCom.getCurrentTime();
                                 }
                                myTime = (int) ((System.currentTimeMillis() - matchingStartTime) / 60 / 1000);

                                firebaseDB.mRootRef.child("경쟁").child(Integer.toString(firebaseDB.myUser.getId())).child("currentTime").setValue(String.valueOf(myTime));
                                firebaseDB.mRootRef.child("경쟁").child(Integer.toString(firebaseDB.myUser.getId())).child("currentDistance").setValue(String.valueOf(myDistance));
                                smallRiddingDistance.setText("주행거리 " + String.valueOf(Math.floor(distance * 100) / 100) + "km");
                                if (myDistance - otherDistance < 0) {
                                    riddingDistance.setText("- " + String.valueOf(Math.floor((otherDistance - myDistance) * 100) / 100));
                                } else {
                                    riddingDistance.setText("+ " + String.valueOf(Math.floor((myDistance - otherDistance) * 100) / 100));
                                }

                                double proAll = (otherDistance / otherTime + myDistance / myTime) * goalTime / 2; //Time은 분 단위라는 가정 하에
                                progressbar.setProgress((int) ((myDistance / proAll) * 100f));
                                progressbar.setSecondaryProgress((int) ((otherDistance / proAll) * 100f));

                                if (!resultCheck && System.currentTimeMillis() - matchingStartTime >= goalTime * 60 * 1000) {
                                    waitingText.setVisibility(View.VISIBLE);
                                    waitingText.setText("경기 종료");
                                    distanceLayout.setVisibility(View.GONE);
                                    resultText.setTextColor(Color.BLACK);
                                    resultText.setVisibility(View.VISIBLE);
                                    resultText.setText("상대 경기 종료 대기중");
                                    resultCheck = true;


                                    checkMatchingResult();

                                    // ui 변경
                                    mode = "finish";
                                    matching = false;
                                    chrono.stop();
                                    chrono.setCountDown(false);
                                    chrono.setBase(subChrono.getBase());
                                    chrono.start();
                                    riddingDistanceTitle.setText("주행거리");
                                    riddingDistance.setText(String.format("%.1f", distance));
                                    riddingTimeTitle.setText("주행시간");
                                    smallRiddingDistance.setVisibility(View.GONE);
                                    smallRiddingTime.setVisibility(View.GONE);
                                    subChrono.setVisibility(View.GONE);
                                    resultText.setVisibility(View.VISIBLE);
                                }

                            }
                        }
                    }
                    upDateSpeed(newLocation);
                    upDateAverageSpeed();
                    upDateCal();


                }

                if (mCurrentMarker != null) mCurrentMarker.remove();
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(new LatLng(latitude, longtitude));
                mCurrentMarker =  mMap.addMarker(markerOptions);

                mMap.animateCamera(CameraUpdateFactory.newLatLng(
                        new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude())));


                mCurrentLocation = newLocation;


            }
        };

    }

    private void changeWalkState(){
        if(!ridingState ) {
            //Toast.makeText(getApplicationContext(), "라이딩 시작", Toast.LENGTH_SHORT).show();
            ridingState= true;

            if(reStart == true && mode.equals("finish")) allStoppedTime +=  System.currentTimeMillis() - stopTime;
            hideFab();
            if( mode.equals("finish") ){
                ridingState = true;
                chrono.setBase(SystemClock.elapsedRealtime() - stoppedTime);
                 chrono.start();

                subChrono.setBase(SystemClock.elapsedRealtime() -stoppedTime);
                subChrono.start();

            }else{
                subChrono.setBase(SystemClock.elapsedRealtime() -stoppedTime);
                subChrono.start();

            }
            startLatLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());        //현재 위치를 시작점으로 설정



        }else {
            showFab();
            //Toast.makeText(getApplicationContext(), "라이딩 멈춤", Toast.LENGTH_SHORT).show();
            stopTime = System.currentTimeMillis();

            reStart = true;
            if( mode.equals("finish")){
                ridingState = false;
                chrono.stop();

                subChrono.stop();
                stoppedTime = SystemClock.elapsedRealtime() - subChrono.getBase();


            }else{
                subChrono.stop();
                stoppedTime = SystemClock.elapsedRealtime() - subChrono.getBase();

            }
        }
    }

    private void drawPath(){        //polyline을 그려주는 메소드
        PolylineOptions options = new PolylineOptions().add(startLatLng).add(endLatLng).width(15).color(Color.YELLOW).geodesic(true);
        //polylines.add(mMap.addPolyline(options));
        mMap.addPolyline(options);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(startLatLng));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if ((ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) &&
                    (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION))) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
                return;
            }
        }

    }


    public boolean isBreakAway() {
        mCurrentLatLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        MapPath mPolyline = new MapPath();
        if (PolyUtil.isLocationOnPath(mCurrentLatLng, mPolyline.getPolyline() , true, 200.0)) {
            return true;
        }
        else return false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        chrono.stop();
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            Toast.makeText(getApplicationContext(), "위치 권한 설정을 허용해주셔야 합니다", Toast.LENGTH_LONG).show();
            return;
        }
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.animateCamera(CameraUpdateFactory.zoomTo(zoom));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(zoom));
    }




    private void upDateAverageSpeed() {
        speedCount++;
        sumSpeed += speed;
        averageSpeed = Double.parseDouble(String.format("%.2f",sumSpeed/speedCount));

    }

    private void upDateCal(){
        long sec = getRealTime();
        double kcal =  420*sec/60/60; // 시간당 420 칼로리 소모
        kcal =  Double.parseDouble(String.format("%.2f", kcal));
        calText.setText("칼로리 "+kcal+"kcal");




    }
    private void upDateSpeed(Location location){
        speed = Double.parseDouble(String.format("%.2f", location.getSpeed()));
        speedText.setText("속도 "+speed +"km/h");


    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setMaxWaitTime(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public void showFab(){
        if( !onPath ){
            ObjectAnimator pathAnimation = ObjectAnimator.ofFloat(pathFab, "translationY", 390.0f);
            pathAnimation.setDuration(1000);
            pathAnimation.start();

            ObjectAnimator stopAnimation = ObjectAnimator.ofFloat(stopFab, "translationY", 550.0f);
            stopAnimation.setDuration(1000);
            stopAnimation.start();
        }else{


            ObjectAnimator pathAnimation = ObjectAnimator.ofFloat(pathFab, "translationY", 550.0f);
            pathAnimation.setDuration(1000);
            pathAnimation.start();

            ObjectAnimator stopAnimation = ObjectAnimator.ofFloat(stopFab, "translationY", 710.0f);
            stopAnimation.setDuration(1000);
            stopAnimation.start();

        }





    }

    public void hideFab(){

        ObjectAnimator pathAnimation = ObjectAnimator.ofFloat(pathFab, "translationY", 230.0f);
        pathAnimation.setDuration(1000);
        pathAnimation.start();

        ObjectAnimator stopAnimation = ObjectAnimator.ofFloat(stopFab, "translationY", 230.0f);
        stopAnimation.setDuration(1000);
        stopAnimation.start();

    }
    public void startAnimation(){
        showFab();

        ObjectAnimator myLocationAnimation = ObjectAnimator.ofFloat(myLocationFab, "translationY", 230.0f);
        myLocationAnimation.setDuration(1000);
        myLocationAnimation.start();
    }


    @Override
    public void onBackPressed() {
        if( matching ){
            Toast.makeText(getApplicationContext(), "대결 중에는 종료가 불가능 합니다.", Toast.LENGTH_SHORT).show();
        }
        else if( ridingState == true ){
            Toast.makeText(getApplicationContext(), "라이딩을 먼저 멈춰 주세요", Toast.LENGTH_SHORT).show();

        }else{
            wantFinish();

        }


    }

    public void wantFinish(){
        AlertDialog.Builder builder = new AlertDialog.Builder(Maps2Activity.this);
        int newGetExp = (int)getRealTime()/1000/60/30*20;
        newGetExp += (int)distance*10;
        newGetExp += comExp;
        if( matching ){
            builder.setTitle("현재 대결이 진행중입니다, 종료하실 경우 패배로 간주됩니다.")
                    .setMessage("총주행거리: "+ (Math.floor(distance*100))/100+"km"+
                            "\n총주행시간:  "+  stringTime +
                            "\n평균 속도:  " + averageSpeed+
                            "\n획득한 경험치:  " + newGetExp+ "exp"
                    );
        }
        else if( resultCheck ){
            String resultText = "패배";
            if(result == true) {resultText ="★승리★";}
             builder.setTitle("정말 라이딩을 종료하시겠습니까?")
                .setMessage("총주행거리: "+(Math.floor(distance*100))/100+"km"+
                        "\n총주행시간:  "+  stringTime +
                        "\n평균 속도:  " + averageSpeed+
                        "\n획득한 경험치:  " + newGetExp +"exp"+
                        "\n대결 결과:  " + resultText
                );

        }
        else {
            builder.setTitle("매칭 대기중입니다. 라이딩을 종료하시겠습니까?")
                    .setMessage("총주행거리: "+(Math.floor(distance*100))/100+"km"+
                            "\n총주행시간:  "+  stringTime +
                            "\n평균 속도:  " + averageSpeed+
                            "\n획득한 경험치:  " + newGetExp +"exp"
                    );
        }




        builder.setPositiveButton("확인", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int id)
            {

                if( !mode.equals("finish"))  firebaseDB.finishCompetition(firebaseDB.myUser);

                //최고기록, 총기록
                long sec = getRealTime();
                int min = (int)sec/60;
                double myDistance = Math.floor(distance*100)/100;
                int myTime = min;


                Record myRecord = new Record(myDistance,myTime);

                Record myBestRecord = firebaseDB.myUser.getBestRecord();
                myBestRecord.compareSetRecord(myRecord);
                Record myTotalRecord = firebaseDB.myUser.getTotalRecord();
                myTotalRecord.setRecord(myTotalRecord.getDistance()+myRecord.getDistance(), myTotalRecord.getTime()+myRecord.getTime());

                //myUser에 최고기록, 총기록 업뎃
                firebaseDB.myUser.setBestRecord(myBestRecord);
                firebaseDB.myUser.setTotalRecord(myTotalRecord);

                // 경험치, 레벨
                int newGetExp = (int)getRealTime()/1000/60/30*20;
                newGetExp += (int)distance*10;
                int myExp = firebaseDB.myUser.getExp();
                int myLevel = firebaseDB.myUser.getLevel();

                myExp = myExp + newGetExp + comExp;

                //myUser에 레벨, 경험치 업뎃
                firebaseDB.myUser.setExp(myExp);
                LevelChecker.levelChecker(getApplicationContext());

                //tempbase에 myUser값 업뎃
                firebaseDB.tempBase.put(firebaseDB.myUser.getEmail(), firebaseDB.myUser);

                //firebase에 myUser값 업뎃
                firebaseDB.mRootRef.child("회원").child(Integer.toString(firebaseDB.myUser.getId())).child("level").setValue(Integer.toString(firebaseDB.myUser.getLevel()));
                firebaseDB.mRootRef.child("회원").child(Integer.toString(firebaseDB.myUser.getId())).child("exp").setValue(Integer.toString(firebaseDB.myUser.getExp()));
                firebaseDB.mRootRef.child("회원").child(Integer.toString(firebaseDB.myUser.getId())).child("totalDistance").setValue(firebaseDB.myUser.getTotalRecord().getDistance());
                firebaseDB.mRootRef.child("회원").child(Integer.toString(firebaseDB.myUser.getId())).child("totalTime").setValue(firebaseDB.myUser.getTotalRecord().getTime());
                firebaseDB.mRootRef.child("회원").child(Integer.toString(firebaseDB.myUser.getId())).child("bestDistance").setValue(firebaseDB.myUser.getBestRecord().getDistance());
                firebaseDB.mRootRef.child("회원").child(Integer.toString(firebaseDB.myUser.getId())).child("bestTime").setValue(firebaseDB.myUser.getBestRecord().getTime());

                finishActivityChecker = true;
                firebaseDB.maps2Condition = false;
                finish();
            }
        });


        builder.setNeutralButton("취소", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int id)
            {
                //Toast.makeText(getApplicationContext(), "Neutral Click", Toast.LENGTH_SHORT).show();
            }
        });


        AlertDialog alertDialog = builder.create();

        alertDialog.show();

    }
    public void kakaoShare(){

        FeedTemplate params = FeedTemplate
                .newBuilder(ContentObject.newBuilder("\uD83D\uDC8C 최고의 라이딩 대결 어플 :: 바이킹",//\uD83D\uDEB2
                        "https://k.kakaocdn.net/dn/mIp9O/btqEKwmGXfC/ySQaYtQRrzjlDvubYad8QK/img_640x640.jpg",
                        LinkObject.newBuilder().setWebUrl("https://www.sungshin.ac.kr/sites/main_kor/main.jsp")
                                .setMobileWebUrl("https://www.sungshin.ac.kr/sites/main_kor/main.jsp").build())
                        .setDescrption(firebaseDB.myUser.getUsername()+"님이 "+(Math.floor(distance*100))/100+"km를 "
                                + stringTime+" 동안 평균속도 "+(int)averageSpeed+"km/h로 달렸어요!")
                        .build()).build();

        Map<String, String> serverCallbackArgs = new HashMap<String, String>();
        serverCallbackArgs.put("user_id", "${current_user_id}");
        serverCallbackArgs.put("product_id", "${shared_product_id}");


        KakaoLinkService.getInstance().sendDefault(this, params, new ResponseCallback<KakaoLinkResponse>() {
            @Override
            public void onFailure(ErrorResult errorResult) {}

            @Override
            public void onSuccess(KakaoLinkResponse result) {
            }
        });

    }
    public void checkMatchingResult(){
        if( finishActivityChecker ) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(Maps2Activity.this);

        if( myDistance >= otherDistance ) {
            result = true;
            resultText.setText("★승리★");
            resultText.setTextColor(Color.BLUE);
            builder.setTitle(firebaseDB.otherCom.getEmail()+"님과의 대결에서 승리하셨습니다");

            comExp = 100;
        }else{
            result = false;
            resultText.setText("패배");
            resultText.setTextColor(Color.RED);
            builder.setTitle(firebaseDB.otherCom.getEmail()+"님과의 대결에서 패배하셨습니다");

            comExp = 50;

        }

        builder.setMessage("나의주행거리: "+ (Math.floor(myDistance*100))/100+"km"+
                            "\n상대주행거리:  "+  (Math.floor(otherDistance*100))/100+"km" +
                            "\n나의 평균 속도:  " + averageSpeed+
                            "\n획득한 경험치:  " + comExp+ "exp"
                    );


        builder.setPositiveButton("종료", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int id)
            {
                firebaseDB.finishCompetition(firebaseDB.myUser);
                finishActivity(0);

                waitingText.setVisibility(View.GONE);
                distanceLayout.setVisibility(View.VISIBLE);
                resultText.setVisibility(View.VISIBLE);
            }
        });


        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    public void getStringTime(){
        long sec = getRealTime();
        long hour = sec/3600; //1시간 = 60분 = 3600초
        sec %= 3600;
        long min = sec/60;
        sec %= 60;
        stringTime = String.format("%02d:%02d:%02d",hour,min,sec);


    }

    public long getRealTime() {
        if(ridingState) {
           return (System.currentTimeMillis() - startTime - allStoppedTime) / 1000;
        }else{ return (stopTime- startTime - allStoppedTime) / 1000;}
    }



}













