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
import android.view.View;
import android.widget.Chronometer;
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
import com.kakao.kakaolink.v2.KakaoLinkResponse;
import com.kakao.kakaolink.v2.KakaoLinkService;
import com.kakao.message.template.ContentObject;
import com.kakao.message.template.FeedTemplate;
import com.kakao.message.template.LinkObject;
import com.kakao.network.ErrorResult;
import com.kakao.network.callback.ResponseCallback;

import java.util.HashMap;
import java.util.Map;


public class MapsActivity extends AppCompatActivity
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
    private long stopTime=0;
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
    private int challengeLevel =0;
    private int goalTime = 0;
    private double goalDis = 0;
    private ProgressBar progressbar;
    private Chronometer subChrono;
    private TextView riddingTimeTitle;
    private TextView riddingDistanceTitle;
    private boolean result = false;
    private TextView resultText;
    private boolean resultCheck = false;

    private int getExp = 0;

    // 카메라 확대
    private float zoom = 18.0f;

    //지도
    Double goalLatitude = 0.0;
    Double goalLongitude = 0.0;
    LatLng goalLat;
    Boolean onPath = false;
    private FloatingActionButton pathAgainFab;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);



        //시작시 권한 체크
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            Toast.makeText(getApplicationContext(), "위치 권한 설정을 허용해주셔야 합니다", Toast.LENGTH_LONG).show();
            return;
        }


        startFab = (FloatingActionButton) findViewById(R.id.fab_start);
        pauseFab = (FloatingActionButton) findViewById(R.id.fab_pause);
        stopFab = (FloatingActionButton) findViewById(R.id.fab_stop);
        myLocationFab =(FloatingActionButton) findViewById(R.id.fab_myLocation);
        pathAgainFab = (FloatingActionButton) findViewById(R.id.fab_path_again);
        pathFab = (FloatingActionButton) findViewById(R.id.fab_path);
        chrono = (Chronometer) findViewById(R.id.chrono) ;
        speedText = (TextView) findViewById(R.id.text_speed);
        riddingDistance = (TextView) findViewById(R.id.text_ridingDistance);
        calText = (TextView) findViewById(R.id.text_calories);
        progressbar = (ProgressBar) findViewById(R.id.progressbar);
        smallRiddingDistance = (TextView) findViewById(R.id.text_ridingDistance_small);
        smallRiddingTime = (TextView) findViewById(R.id.text_ridingTime_small);
        subChrono = (Chronometer) findViewById(R.id.chrono_sub);
        riddingDistanceTitle = (TextView) findViewById(R.id.riddingDistance_title);
        riddingTimeTitle= (TextView) findViewById(R.id.riddingtime_title);
        resultText = (TextView) findViewById(R.id.result_text);

        // 라이딩 모드 받아오기
        resultText.setVisibility(View.GONE);
        Intent intent = getIntent();
        mode = intent.getStringExtra("mode");
        //Toast.makeText(getApplicationContext(), "mode"+mode, Toast.LENGTH_SHORT).show();

        if( !mode.equals("none")) {
            if( mode.equals("challenge") ) {
                challengeLevel = intent.getIntExtra("challengeLevel",0);

            }
            goalTime = intent.getIntExtra("time",0);
            goalDis = intent.getIntExtra("km", 5);
            //Toast.makeText(getApplicationContext(), "goal"+goalTime+"/"+goalDis, Toast.LENGTH_SHORT).show();
            chrono.setCountDown(true);
            riddingDistanceTitle.setText("남은거리");
            goalDistance = (float)goalDis;
            //goalDistance = 0.1f; //테스트 용
            riddingDistance.setText(String.format("%.1f", goalDistance));
            riddingTimeTitle.setText("남은시간");
            progressbar.setProgress(0);

        }else{
            progressbar.setVisibility(View.GONE);
            smallRiddingTime.setVisibility(View.GONE);
            subChrono.setVisibility(View.GONE);
            smallRiddingDistance.setVisibility(View.GONE);
        }



        startAnimation();






        ridingState=false;

        chrono.setFormat("00:%s");
        chrono.setTypeface(ResourcesCompat.getFont(this, R.font.pfstardust));
        chrono.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            public void onChronometerTick(Chronometer c) {
                long elapsedMillis = SystemClock.elapsedRealtime() -chrono.getBase();
                if(elapsedMillis > 3600000L){
                    chrono.setFormat("0%s");
                }else{
                    chrono.setFormat("00:%s");
                }
            }
        });



        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        startFab.setOnClickListener(new View.OnClickListener() {

            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View view) {
                changeWalkState();        //라이딩 상태 변경
                startFab.setVisibility(View.INVISIBLE);
                pauseFab.setVisibility(View.VISIBLE);

            }
        });

        pauseFab.setOnClickListener(new View.OnClickListener() {

            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View view) {
                changeWalkState();        //라이딩 상태 변경
                startFab.setVisibility(View.VISIBLE);
                pauseFab.setVisibility(View.INVISIBLE);




            }
        });

        pathAgainFab.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                // 경로 재 검색 코드
                if( mCurrentLocation != null && onPath) {
                    LatLng now = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude() );
                    MapPath.drawRoute(now, goalLat, getString(R.string.google_maps_key), mMap, getApplicationContext());
                }
            }
        });



        pathFab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),MapPathActivity.class);
                startActivityForResult(intent, 10);
            }
        });

        stopFab.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                getStringTime();
                wantFinish();
            }
        });

        myLocationFab.setOnClickListener( new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if(mMap != null && mCurrentLocation != null) {
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

                Location newLocation = locationResult.getLastLocation();

                double latitude = newLocation.getLatitude(), longtitude = newLocation.getLongitude();

                if( mCurrentLocation != null && ridingState == true  ) { //&&  newLocation.getSpeed()>0.3 - 속도 제한
                    distance += (double)(mCurrentLocation.distanceTo(newLocation))/1000f;

                    if(mode.equals("none") || mode.equals("finish")){
                        riddingDistance.setText(String.valueOf(Math.floor(distance*100)/100));
                    }else{
                        goalDistance -=  (double)(mCurrentLocation.distanceTo(newLocation))/1000f;
                        smallRiddingDistance.setText("주행거리 "+String.valueOf(Math.floor(distance*100)/100)+"km");
                        riddingDistance.setText(String.valueOf(Math.floor(goalDistance*100)/100));
                        progressbar.setProgress((int)((distance/goalDis)*100f));

                        if( !resultCheck && !result && goalDistance<= 0 &&  System.currentTimeMillis()- startTime < goalTime*60*1000 ){
                            result = true;
                            resultCheck = true;
                            int resultExp = 0;

                            if(mode.equals("challenge")){
                                switch(challengeLevel){
                                    case 1: resultExp = 100;
                                        break;
                                    case 2: resultExp = 200;
                                        break;
                                    case 3: resultExp = 300;
                                }

                            }else{
                                resultExp = 100;

                            }
                            Toast.makeText(getApplicationContext(), "목표를 달성하였습니다. 획득 경험치 "+resultExp+"exp", Toast.LENGTH_SHORT).show();
                            getExp += resultExp;
                            firebaseDB.mRootRef.child("회원").child(Integer.toString(firebaseDB.myUser.getId())).child("exp").setValue(firebaseDB.myUser.getExp()+resultExp);
                            LevelChecker.levelChecker(getApplicationContext());
                            firebaseDB.tempBase.put(firebaseDB.myUser.getEmail(), firebaseDB.myUser);
                            resultText.setText("★성공★");
                            resultText.setTextColor(Color.RED);


                        }else if(!resultCheck  &&  System.currentTimeMillis()- startTime > goalTime*60*1000 ) {
                            resultCheck = true;

                            Toast.makeText(getApplicationContext(), "목표 달성에 실패하셨습니다.", Toast.LENGTH_SHORT).show();

                            resultText.setText("★실패★");
                            resultText.setTextColor(Color.BLUE);


                        }

                        if( resultCheck ){
                            mode = "finish";
                            chrono.stop();
                            chrono.setCountDown(false);
                            chrono.setBase(subChrono.getBase());
                            chrono.start();
                            riddingDistanceTitle.setText("주행거리");
                            riddingDistance.setText(String.format("%.1f", distance ));
                            riddingTimeTitle.setText("주행거리");
                            smallRiddingDistance.setVisibility(View.GONE);
                            smallRiddingTime.setVisibility(View.GONE);
                            subChrono.setVisibility(View.GONE);
                            resultText.setVisibility(View.VISIBLE);

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

                if(ridingState){                        //라이딩 시작 버튼이 눌렸을 때
                    endLatLng = new LatLng(latitude, longtitude);        //현재 위치를 끝점으로 설정
                    drawPath();                                            //polyline 그리기
                    startLatLng = new LatLng(latitude, longtitude);        //시작점을 끝점으로 다시 설정

                }



                mCurrentLocation = newLocation;


            }
        };

    }

    private void changeWalkState(){
        if(!ridingState) {
            //Toast.makeText(getApplicationContext(), "라이딩 시작", Toast.LENGTH_SHORT).show();
            ridingState = true;
            if( reStart  == false ) {
                distance = 0;
                startTime = System.currentTimeMillis();
                if(!mode.equals("none")){
                    chrono.setBase( SystemClock.elapsedRealtime() + 1000*60*goalTime );
                    chrono.start();
                }

            }
            if(reStart == true) allStoppedTime +=  System.currentTimeMillis() - stopTime;
            hideFab();
            if( mode.equals("none")){
                chrono.setBase(SystemClock.elapsedRealtime() - stoppedTime);
                 chrono.start();

                subChrono.setBase(SystemClock.elapsedRealtime() -stoppedTime);
                subChrono.start();

            }else{
                subChrono.setBase(SystemClock.elapsedRealtime() -stoppedTime);
                subChrono.start();

            }
            startLatLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());        //현재 위치를 시작점으로 설정



        }else{
            showFab();
            //Toast.makeText(getApplicationContext(), "라이딩 멈춤", Toast.LENGTH_SHORT).show();
            stopTime = System.currentTimeMillis();

            ridingState = false;
            reStart = true;
            if( mode.equals("none")){
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
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startLatLng, 18));
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
        speedText.setText("속도 "+speed +"km/s");


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
        if( ridingState == true ){
            Toast.makeText(getApplicationContext(), "라이딩을 먼저 멈춰 주세요", Toast.LENGTH_SHORT).show();

        }else{
            wantFinish();

        }


    }

    public void wantFinish(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
        int newGetExp = (int)getRealTime()/1000/60/30*20;
        newGetExp += (int)distance*10;
        newGetExp += getExp;
        if( mode.equals("none")){
            builder.setTitle("정말 라이딩을 종료하시겠습니까?")
                    .setMessage("총주행거리: "+ (Math.floor(distance*100))/100+"km"+
                            "\n총주행시간:  "+  stringTime +
                            "\n평균 속도:  " + averageSpeed+
                            "\n획득한 경험치:  " + newGetExp+ "exp"
                    );
        }
        else{
            String resultText = "실패";
            if(result == true) {resultText ="★성공★";}
             builder.setTitle("정말 라이딩을 종료하시겠습니까?")
                .setMessage("총주행거리: "+(Math.floor(distance*100))/100+"km"+
                        "\n총주행시간:  "+  stringTime +
                        "\n평균 속도:  " + averageSpeed+
                        "\n획득한 경험치:  " + newGetExp +"exp"+
                        "\n챌린지 결과:  " + resultText
                );

        }



        builder.setPositiveButton("확인", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int id)
            {

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

                myExp = myExp + newGetExp;

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

                finish();
            }
        });

        builder.setNegativeButton("공유", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int id)
            {
                //Toast.makeText(getApplicationContext(), "Cancel Click", Toast.LENGTH_SHORT).show();

                //최고기록, 총기록
                long sec = getRealTime();
                int min = (int)sec/60;
                double myDistance = Math.floor(distance*100)/100;
                int myTime = min;




                Record myRecord = new Record((int)myDistance,myTime);

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

                myExp = myExp + newGetExp;

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

                //여기
                kakaoShare();
                finish();
            }
        });

        builder.setNeutralButton("취소", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int id)
            {
               // Toast.makeText(getApplicationContext(), "Neutral Click", Toast.LENGTH_SHORT).show();
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
        if( ridingState) {
           return (System.currentTimeMillis() - startTime - allStoppedTime) / 1000;
        }else{ return (stopTime- startTime - allStoppedTime) / 1000;}
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



}













