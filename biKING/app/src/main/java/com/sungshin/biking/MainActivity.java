package com.sungshin.biking;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.kakao.kakaolink.v2.KakaoLinkResponse;
import com.kakao.kakaolink.v2.KakaoLinkService;
import com.kakao.message.template.ContentObject;
import com.kakao.message.template.FeedTemplate;
import com.kakao.message.template.LinkObject;
import com.kakao.network.ErrorResult;
import com.kakao.network.callback.ResponseCallback;
import com.kwabenaberko.openweathermaplib.implementation.OpenWeatherMapHelper;
import com.kwabenaberko.openweathermaplib.implementation.callbacks.CurrentWeatherCallback;
import com.kwabenaberko.openweathermaplib.models.currentweather.CurrentWeather;
import com.sungshin.biking.ListView.questListAdaptor;
import com.sungshin.biking.ListView.rankingListAdaptor;
import com.sungshin.biking.Quest.CreateQuest;
import com.sungshin.biking.Quest.QuestList;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;


public class MainActivity extends AppCompatActivity {
    Button homeButton;
    Button rankingButton;
    Button questButton;
    Button myPageButton;

    Button riddingButton;

    FrameLayout home;
    FrameLayout quest;
    FrameLayout myPage;
    FrameLayout ranking;

    FrameLayout riddingMode;

    Button challenge;
    Button challenge1;
    Button challenge2;
    Button challenge3;
    Button withMe;
    Button none;
    Button how;

    //권혜영
    TextView name;
    TextView exp;
    ProgressBar cpb;
    User[] rank;
    TextView myRanking;
    TextView rankingName;
    TextView rankingExp;
    TextView myPageTotalDistance;
    TextView myPageTotalTime;
    TextView myPageName;
    TextView myPageExp;
    TextView myPageRank;
    Button logoutButton;

    ImageView chaImage;
    ImageView levelIconImage;
    Activity now;
    int lid;
    int lidIcon;

    private ListView rankingListView;
    private rankingListAdaptor rankingAdapter;

    private ListView questListView;
    private questListAdaptor questAdapter;

    ImageView myPageRanking;
    ImageView myRankingImage;
    TextView myPageAverageSpeed;

    Button randomBattle;

    Adapter adapter;
    ViewPager viewPager;

    //날씨 아이콘
    private FusedLocationProviderClient fusedLocationClient;
    private String current_Weather;

    TextView myPage_text;
    ImageView myPage_image;


    @Override
    protected void onStart() {
        super.onStart();
        firebaseDB.updateTempBase();
        firebaseDB.competitionListener();
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        firebaseDB.updateTempBase();

        final int mainColor = getResources().getColor(R.color.main_color);
        final int subColor = getResources().getColor(R.color.sub_color);
        final int pointColor = getResources().getColor(R.color.point_color);

        homeButton = (Button)findViewById(R.id.main_button_home);
        rankingButton =(Button)findViewById(R.id.main_button_ranking);
        questButton = (Button)findViewById(R.id.main_button_quest);
        myPageButton = (Button)findViewById(R.id.main_button_my);
        riddingButton = (Button)findViewById(R.id.main_home_button_rriding);

        how = (Button)findViewById(R.id.how);
        home= (FrameLayout)findViewById(R.id.main);
        quest= (FrameLayout)findViewById(R.id.quest);
        myPage= (FrameLayout)findViewById(R.id.myPage);
        ranking= (FrameLayout)findViewById(R.id.ranking);

        riddingMode =(FrameLayout) findViewById(R.id.riddingMode);

        challenge = (Button) findViewById(R.id.main_button_riddingMode_challenge);
        challenge1 = (Button) findViewById(R.id.main_button_riddingMode_challenge_1);
        challenge2 = (Button) findViewById(R.id.main_button_riddingMode_challenge_2);
        challenge3 = (Button) findViewById(R.id.main_button_riddingMode_challenge_3);
        withMe = (Button) findViewById(R.id.main_button_riddingMode_WithMe);
        none = (Button) findViewById(R.id.main_button_riddingMode_none);
        randomBattle = (Button) findViewById(R.id.main_button_riddingMode_Battle);

        myPageRanking = (ImageView) findViewById(R.id.myPage_image_userRanking);
        myRankingImage = (ImageView) findViewById(R.id.ranking_image);
        myPageAverageSpeed = (TextView) findViewById(R.id.myPage_text_averageSpeed);
        logoutButton = (Button) findViewById(R.id.button_logout);

        //날씨 api 에 따른 아이콘 변경
        final ImageView image_weather = (ImageView)findViewById(R.id.image_weather);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        Log.e("weather","여기");
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {

                        if (location != null) {
                            Log.d("error", String.valueOf(current_Weather));

                            double longitude = location.getLongitude();
                            double latitude = location.getLatitude();
                            OpenWeatherMapHelper helper = new OpenWeatherMapHelper(BuildConfig.WeatherApiKey);
                            Log.e("weather","여기2");
                            helper.getCurrentWeatherByGeoCoordinates(latitude, longitude, new CurrentWeatherCallback() {
                                @Override
                                public void onSuccess(CurrentWeather currentWeather) {
                                    current_Weather = String.valueOf(currentWeather);
                                    Log.e("weather", "start");

                                    try {
                                        if (currentWeather.getWeather().get(0).getDescription().equals("mist")) {
                                            image_weather.setImageResource(R.drawable.mist);
                                            Log.e("weather", "mist");
                                        } else if (currentWeather.getWeather().get(0).getDescription().equals("sun")) {
                                            image_weather.setImageResource(R.drawable.sun);
                                            Log.e("weather", "sun");
                                        } else if (currentWeather.getWeather().get(0).getDescription().equals("rain")) {
                                            image_weather.setImageResource(R.drawable.rain);
                                            Log.e("weather", "rain");
                                        } else if (currentWeather.getWeather().get(0).getDescription().equals("broken clouds")) {
                                            image_weather.setImageResource(R.drawable.broken_clouds);
                                            Log.e("weather", "broken clouds");
                                        } else if (currentWeather.getWeather().get(0).getDescription().equals("scattered clouds")) {
                                            image_weather.setImageResource(R.drawable.cloud_sun);
                                            Log.e("weather", "scattered clouds");
                                        } else {
                                            image_weather.setImageResource(R.drawable.flag);
                                            Log.e("weather", "flag");
                                        }
                                    }
                                    catch( NullPointerException e){
                                        image_weather.setImageBitmap(null);
                                        Log.e("error", "error");

                                    }
                                }

                                @Override
                                public void onFailure(Throwable throwable) {
                                    Log.v(TAG, throwable.getMessage());
                                    image_weather.setImageBitmap(null);
                                }
                            });

                        }
                    }
                });

        //시간별로 배경 설정
        Calendar cal = Calendar.getInstance();
        int hour = 0;
        hour = cal.get(Calendar.HOUR_OF_DAY);

        final LinearLayout activity_main = (LinearLayout)findViewById(R.id.activity_main);
        Drawable morning = getResources().getDrawable(R.drawable.morning);
        Drawable night = getResources().getDrawable(R.drawable.night);


        if (6 <= hour && hour <= 18)
            activity_main.setBackground(morning);
        else
            activity_main.setBackground(night);


        Drawable building = getResources().getDrawable(R.drawable.building);
        Drawable namsan = getResources().getDrawable(R.drawable.namsan);
        Drawable yanghwa = getResources().getDrawable(R.drawable.yanghwa);
        Drawable lottetower = getResources().getDrawable(R.drawable.lottetower);

        myPage_text = (TextView)findViewById(R.id.myPage_text_userState) ;
        myPage_image = (ImageView)findViewById(R.id.myPage_image_userState);


        Random ram = new Random();
        int num = ram.nextInt(4);

        int distance = (int)firebaseDB.myUser.getTotalRecord().getDistance();
        int mdistance = distance*1000;

        switch(num)
        {
            case 0:
                myPage_image.setImageResource(R.drawable.building);
                myPage_text.setText("\"63빌딩 높이를 " + mdistance/249 + "회 달렸어요\"");
                break;

            case 1:
                myPage_image.setImageResource(R.drawable.namsan);
                myPage_text.setText("\"남산타워 끝까지 " + mdistance/236 + "번 다녀왔어요\"");
                break;

            case 2:
                myPage_image.setImageResource(R.drawable.yanghwa);
                myPage_text.setText("\"양화대교를 " + mdistance/1053 + "번 건넜어요\"");
                break;

            case 3:
                myPage_image.setImageResource(R.drawable.lottetower);
                myPage_text.setText("\"롯데타워 옥상까지 " + mdistance/555 + "회 다녀왔어요\"");
                break;

        }






        // 메인 캐릭터 이미지 설정
        now = this;

        LevelChecker.levelChecker(getApplicationContext());
        lid = now.getResources().getIdentifier("level"+firebaseDB.myUser.getLevel(), "drawable", "com.sungshin.biking");
        lidIcon = now.getResources().getIdentifier("levelicon"+firebaseDB.myUser.getLevel(), "drawable", "com.sungshin.biking");


        chaImage = (ImageView)findViewById(R.id.cha_image);
        levelIconImage = (ImageView)findViewById(R.id.level_icon);
        chaImage.setImageResource(lid);
        levelIconImage.setImageResource(lidIcon);
        GlideDrawableImageViewTarget gifImage = new GlideDrawableImageViewTarget(chaImage);
        Glide.with(now).load(lid).into(gifImage);

        //메인화면 사용자 정보 업데이트
        name = (TextView) findViewById(R.id.textView);
        exp = (TextView) findViewById(R.id.textView3);
        cpb = (ProgressBar) findViewById(R.id.cpb);
        myRanking = (TextView) findViewById(R.id.ranking_ranking);
        rankingName = (TextView) findViewById(R.id.ranking_username);
        rankingExp = (TextView) findViewById(R.id.ranking_exp);
        myPageTotalDistance = (TextView) findViewById(R.id.myPage_text_allDistance);
        myPageTotalTime = (TextView) findViewById(R.id.myPage_text_allTime);
        myPageName = (TextView) findViewById(R.id.myPage_text_userName);
        myPageExp = (TextView) findViewById(R.id.myPage_text_exp);
        myPageRank = (TextView) findViewById(R.id.myPage_text_userRanking);

        // home
        name.setText(firebaseDB.myUser.getUsername()+"님 환영합니다.");
        if(LevelChecker.getRemainExp()>0)
            exp.setText(LevelChecker.getRemainExp()+"exp");
        else
            exp.setText("최고 레벨 biKING 달성");
        cpb.setProgress((int)(100*((float)LevelChecker.getLevelExp()/(LevelChecker.levelList[firebaseDB.myUser.getLevel()]-LevelChecker.levelList[firebaseDB.myUser.getLevel()-1]))));


        // mypage

        homeButton.setBackgroundColor(pointColor); // 디폴트 메인 화면

        // 하단바 버튼 시작

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                firebaseDB.update();
                firebaseDB.myUser = firebaseDB.myData(firebaseDB.myUser.getEmail());

                LevelChecker.levelChecker(getApplicationContext());
                name.setText(firebaseDB.myUser.getUsername()+"님 환영합니다.");
                if(LevelChecker.getRemainExp()>0)
                    exp.setText(LevelChecker.getRemainExp()+"exp");
                else
                    exp.setText("최고 레벨 biKING 달성");
                //exp.setText(firebaseDB.myUser.getLevel());
                cpb.setProgress((int)(100*((float)LevelChecker.getLevelExp()/(LevelChecker.levelList[firebaseDB.myUser.getLevel()]-LevelChecker.levelList[firebaseDB.myUser.getLevel()-1]))));
                lid = now.getResources().getIdentifier("level"+firebaseDB.myUser.getLevel(), "drawable", "com.sungshin.biking");
                lidIcon = now.getResources().getIdentifier("levelicon"+firebaseDB.myUser.getLevel(), "drawable", "com.sungshin.biking");

                chaImage.setImageResource(lid);
                levelIconImage.setImageResource(lidIcon);
                GlideDrawableImageViewTarget gifImage = new GlideDrawableImageViewTarget(chaImage);
                Glide.with(now).load(lid).into(gifImage);

                home.setVisibility(View.VISIBLE);
                ranking.setVisibility(View.GONE);
                quest.setVisibility(View.GONE);
                myPage.setVisibility(View.GONE);
                riddingMode.setVisibility(View.GONE);


                homeButton.setBackgroundColor(pointColor);
                rankingButton.setBackgroundColor(mainColor);
                questButton.setBackgroundColor(mainColor);
                myPageButton.setBackgroundColor(mainColor);

            }
        });

        rankingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                firebaseDB.update();
                firebaseDB.myUser = firebaseDB.myData(firebaseDB.myUser.getEmail());

                rank = firebaseDB.sortingTempBase();
                home.setVisibility(View.GONE);
                ranking.setVisibility(View.VISIBLE);
                quest.setVisibility(View.GONE);
                myPage.setVisibility(View.GONE);
                riddingMode.setVisibility(View.GONE);


                homeButton.setBackgroundColor(mainColor);
                rankingButton.setBackgroundColor(pointColor);
                questButton.setBackgroundColor(mainColor);
                myPageButton.setBackgroundColor(mainColor);

                rankingListView = (ListView) findViewById(R.id.ranking_listview);
                rankingAdapter = new rankingListAdaptor();

                rankingListView.setAdapter(rankingAdapter);
                int index=1;
                for(int i = 0 ;i<rank.length; i++,index++){
                    int lidLevelIcon = now.getResources().getIdentifier("levelicon"+rank[i].getLevel(), "drawable", "com.sungshin.biking");
                    rankingAdapter.addItem(index,rank[i].getUsername(),Integer.toString(rank[i].getExp()),lidLevelIcon);
                    firebaseDB.tempBase.get(rank[i].getEmail()).setRanking(index);
                    if( i+1 < rank.length && rank[i].getExp() == rank[i+1].getExp()) index--;
                }

                rankingAdapter.notifyDataSetChanged();
                myRankingImage.setImageResource(lidIcon);
                firebaseDB.myUser.setRanking(firebaseDB.tempBase.get(firebaseDB.myUser.getEmail()).getRanking());
                myRanking.setText(Integer.toString(firebaseDB.myUser.getRanking())+"위");
                rankingName.setText(firebaseDB.myUser.getUsername()+"님");
                rankingExp.setText("누적경험치 "+Integer.toString(firebaseDB.myUser.getExp()));
            }
        });


        CreateQuest.makeQuest();

        questButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                home.setVisibility(View.GONE);
                ranking.setVisibility(View.GONE);
                quest.setVisibility(View.VISIBLE);
                myPage.setVisibility(View.GONE);
                riddingMode.setVisibility(View.GONE);


                homeButton.setBackgroundColor(mainColor);
                rankingButton.setBackgroundColor(mainColor);
                questButton.setBackgroundColor(pointColor);
                myPageButton.setBackgroundColor(mainColor);

                questListView = (ListView) findViewById(R.id.quest_listview);
                questAdapter = new questListAdaptor();

                QuestList.checkQuest();
                QuestList.sortList();
                questListView.setAdapter(questAdapter);
                questAdapter.addItem();


                questAdapter.notifyDataSetChanged();





            }
        });

        how.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), StartBikingExplanation.class);
                startActivity(intent);
                finish();
            }
            });




        myPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseDB.update();
                firebaseDB.myUser = firebaseDB.myData(firebaseDB.myUser.getEmail());

                lidIcon = now.getResources().getIdentifier("levelicon"+firebaseDB.myUser.getLevel(), "drawable", "com.sungshin.biking");
                myPageRanking.setImageResource(lidIcon);

                myPageName.setText("Lv."+firebaseDB.myUser.getLevel()+" "+firebaseDB.myUser.getUsername()+"님");
                myPageExp.setText(Integer.toString(firebaseDB.myUser.getExp())+"exp");
                myPageRank.setText(firebaseDB.myUser.checkingRanking()+"위");
                homeButton.setBackgroundColor(pointColor); // 디폴트 메인 화면

                home.setVisibility(View.GONE);
                ranking.setVisibility(View.GONE);
                quest.setVisibility(View.GONE);
                myPage.setVisibility(View.VISIBLE);
                riddingMode.setVisibility(View.GONE);

                homeButton.setBackgroundColor(mainColor);
                rankingButton.setBackgroundColor(mainColor);
                questButton.setBackgroundColor(mainColor);
                myPageButton.setBackgroundColor(pointColor);

                int sec = firebaseDB.myUser.getTotalRecord().getTime()*60;
                int speed = (int)firebaseDB.myUser.getTotalRecord().getDistance()*1000;

                myPageTotalDistance.setText("총 달린 거리 | "+(int)firebaseDB.myUser.getTotalRecord().getDistance()+ "km");
                myPageTotalTime.setText("총 달린 시간 | "+firebaseDB.myUser.getTotalRecord().getTime()/60 + "시간"
                        + firebaseDB.myUser.getTotalRecord().getTime()%60 + "분");
                if( sec ==0 ) sec = 1;
                myPageAverageSpeed.setText("평균 속도 | "+ (int)speed/sec + "m/s");

            }
        });
        //하단 바 버튼 종료

        logoutButton.setOnClickListener( new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);
                dlg.setTitle("로그 아웃");
                dlg.setMessage("로그아웃 하시겠습니까?");
                dlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
                dlg.setNegativeButton("취소", new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                dlg.show();
            }
        });

        //home -> 라이딩 기능 버튼
        riddingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                home.setVisibility(View.GONE);
                ranking.setVisibility(View.GONE);
                quest.setVisibility(View.GONE);
                myPage.setVisibility(View.GONE);
                riddingMode.setVisibility(View.VISIBLE);

                challenge.setVisibility(View.VISIBLE);

                homeButton.setBackgroundColor(pointColor);
                rankingButton.setBackgroundColor(mainColor);
                questButton.setBackgroundColor(mainColor);
                myPageButton.setBackgroundColor(mainColor);
                LocationManager mLocMan; // 위치 관리자

                mLocMan = (LocationManager)getSystemService(Context.LOCATION_SERVICE);


                if(!mLocMan.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("GPS 설정을 켜주세요")
                            .setMessage("현재 GPS 설정이 꺼져있습니다.\nGPS 설정이 꺼져있을 경우\n원활한 사용이 어렵습니다.\nGPS를 켜주세요.")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    // GPS 설정 화면으로 이동
                                    Intent gpsOptionsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    startActivity(gpsOptionsIntent);

                                }})
                            .show();


                }

            }
        });

        // home->rriding -> 내부 버튼

        challenge.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                challenge.setVisibility(View.GONE);
            }

        });

        challenge1.setOnClickListener( new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),MapsActivity.class);
                intent.putExtra("mode", "challenge");
                intent.putExtra("challengeLevel", 1);
                intent.putExtra("time",60);
                int goalKm = firebaseDB.myUser.getLevel();
                intent.putExtra("km",goalKm);
                startActivity(intent);

            }
        });

        challenge2.setOnClickListener( new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),MapsActivity.class);
                intent.putExtra("mode", "challenge");
                intent.putExtra("challengeLevel", 2);
                int goalKm = firebaseDB.myUser.getLevel()*3;
                intent.putExtra("time",60);
                intent.putExtra("km",goalKm);
                startActivity(intent);

            }
        });

        challenge3.setOnClickListener( new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),MapsActivity.class);
                intent.putExtra("mode", "challenge");
                intent.putExtra("challengeLevel", 3);
                intent.putExtra("time",60);
                int goalKm = firebaseDB.myUser.getLevel()*5;
                intent.putExtra("km",goalKm);
                startActivity(intent);

            }
        });

        withMe.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view){
                int time = firebaseDB.myUser.getBestRecord().getTime();
                double dis = firebaseDB.myUser.getBestRecord().getDistance();
                if(time == 0) time = 1;
                int goal = (int)dis/time*60;
                if(goal == 0) goal = 1;
                Intent intent = new Intent(getApplicationContext(),MapsActivity.class);
                intent.putExtra("mode", "withMe");
                intent.putExtra("time",60);
                intent.putExtra("km",goal);
                startActivity(intent);}

        });

        none.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view){

                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                intent.putExtra("mode", "none");
                startActivity(intent);


            }
        });

        randomBattle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Toast.makeText(MainActivity.this,"확인",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(),Maps2Activity.class);
                intent.putExtra("mode", "random");
                startActivity(intent);
            }
        });

    }



    //    public void shareKakao()
//    {
//        try {
//            final KakaoLink kakaoLink = KakaoLink.getKakaoLink(this);
//            final KakaoLinkMessageBuilder kakaoBuilder = kakaoLink.createKakaoTalkLinkMessageBuilder();
//
//           kakaoBuilder.addText("카카오링크 테스트입니다.");
//           //String url = http://k.kakaocdn.net/dn/qAlsC/btqBNLHeIeX/OUeeePf1PCOnkwPks0YGl1/img_640x640.jpg
//
//            kakaoBuilder.addAppButton("앱 실행 혹은 다운로드");
//            kakaoLink.sendMessage(kakaoBuilder,this);
//
//        } catch (KakaoParameterException e) {
//            e.printStackTrace();
//        }
//
//    }
    public void btnClick(View view){

        FeedTemplate params = FeedTemplate
                .newBuilder(ContentObject.newBuilder("\uD83D\uDC8C 최고의 라이딩 대결 어플 :: 바이킹",//\uD83D\uDEB2
                        "https://k.kakaocdn.net/dn/mIp9O/btqEKwmGXfC/ySQaYtQRrzjlDvubYad8QK/img_640x640.jpg",
                        LinkObject.newBuilder().setWebUrl("https://www.sungshin.ac.kr/sites/main_kor/main.jsp")
                                .setMobileWebUrl("https://www.sungshin.ac.kr/sites/main_kor/main.jsp").build())
                        .setDescrption(firebaseDB.myUser.getUsername()+"님은 현재 "+(firebaseDB.myUser.checkingRanking())+"위로"
                                +" 총 "+ (int)firebaseDB.myUser.getTotalRecord().getDistance()
                                +"km, 총 "+ (int)firebaseDB.myUser.getTotalRecord().getTime() + "분 동안 달렸어요! 우리 함께 달려볼까요?")
                        .build())
//            .addButton(new ButtonObject("확인하기", LinkObject.newBuilder()
//                    .setWebUrl("https://www.naver.com")
//                    .setMobileWebUrl("https://www.naver.com")
//                    .setAndroidExecutionParams("roomnum=$roomnum")//("key1=value1")
//                  //  .setIosExecutionParams("key1=value1")
//                    .build()))
//            .addButton(new ButtonObject("다운받기", LinkObject.newBuilder().setWebUrl("https://developers.kakao.com").setMobileWebUrl("https://developers.kakao.com").build()))
                .build();

        Map<String, String> serverCallbackArgs = new HashMap<String, String>();
        serverCallbackArgs.put("user_id", "${current_user_id}");
        serverCallbackArgs.put("product_id", "${shared_product_id}");


        KakaoLinkService.getInstance().sendDefault(this, params, new ResponseCallback<KakaoLinkResponse>() {
            @Override
            public void onFailure(ErrorResult errorResult) {
                Log.e("Kakao","Fail");
            }

            @Override
            public void onSuccess(KakaoLinkResponse result) {
                Log.e("Kakao","Success");
            }
        });

    }
}
