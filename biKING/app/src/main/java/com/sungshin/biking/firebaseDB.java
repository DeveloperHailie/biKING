package com.sungshin.biking;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

public class firebaseDB{
    public static HashMap<String,User> tempBase = new HashMap<String, User>();
    public static HashMap<String, Competition> comBase = new HashMap<String, Competition>();
    public static HashMap<Integer, String> idToEmail = new HashMap<Integer, String>();
    public static User newUser = null;
    public static User myUser = null;
    public static Competition myCom = null;
    public static Competition otherCom = null;
    public static String email[] = null;
    static DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();

    public static boolean maps2Condition = false;

    public static boolean matchingState = false;

    //회원가입 시 firebase database로 새 회원 data 전송
    public static  void signUpFirebase(String email, String username) {
        newUser = new User(email, username);
        mRootRef.child("회원").child(Integer.toString(newUser.getCustId())).setValue(newUser.getCustId());
        mRootRef.child("회원").child(Integer.toString(newUser.getCustId())).child("email").setValue(email);
        mRootRef.child("회원").child(Integer.toString(newUser.getCustId())).child("userName").setValue(username);
        if(email.contains("@"))
            mRootRef.child("회원").child(Integer.toString(newUser.getCustId())).child("kakao").setValue("없음");
        else
            mRootRef.child("회원").child(Integer.toString(newUser.getCustId())).child("kakao").setValue("있음");

        // 혹은 user.get~~로 setVlaue
        mRootRef.child("회원").child(Integer.toString(newUser.getCustId())).child("level").setValue(1);
        mRootRef.child("회원").child(Integer.toString(newUser.getCustId())).child("exp").setValue(0);
        mRootRef.child("회원").child(Integer.toString(newUser.getCustId())).child("totalDistance").setValue(0);
        mRootRef.child("회원").child(Integer.toString(newUser.getCustId())).child("totalTime").setValue(0);
        mRootRef.child("회원").child(Integer.toString(newUser.getCustId())).child("bestDistance").setValue(0);
        mRootRef.child("회원").child(Integer.toString(newUser.getCustId())).child("bestTime").setValue(0);
        mRootRef.child("회원").child(Integer.toString(newUser.getCustId())).child("custId").setValue(Integer.toString(newUser.getCustId()));
        mRootRef.child("사용자수").setValue(newUser.getCustId());
    }
    //firebae database에서 tempBase로 data 가져오기
    public static void updateTempBase(){

        mRootRef.child("회원").addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.e("로그","onDataChange");
                String number = Long.toString(dataSnapshot.getChildrenCount());
                User.custId=Integer.parseInt(number);
                Log.e("로그", "총 사용자 수 : " + number);
                int id = 0; boolean error = true;
                try{
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if(id==0){id++;continue;}
                        User user = new User();

                        DecimalFormat df = new DecimalFormat();
                        Number num = null;
                        double bestDis = 0;
                        double totalDis = 0;
                        try {
                            totalDis = df.parse(""+snapshot.child("totalDistance").getValue()).doubleValue();
                            bestDis = df.parse(""+snapshot.child("bestDistance").getValue()).doubleValue();

                        } catch (ParseException e) {
                            e.printStackTrace();
                            Log.e("double"," DOUBLE WRONG");
                        }

                        Record totalRecord = new Record(totalDis,Integer.parseInt(""+snapshot.child("totalTime").getValue()));
                        Record bestRecord = new Record(bestDis ,Integer.parseInt(""+snapshot.child("bestTime").getValue()));
                        user.setUser(""+snapshot.child("email").getValue(),Integer.parseInt(""+snapshot.child("custId").getValue()),""+snapshot.child("userName").getValue(),
                                ""+snapshot.child("kakao").getValue(), Integer.parseInt(""+snapshot.child("level").getValue()), Integer.parseInt(""+snapshot.child("exp").getValue()),
                                totalRecord, bestRecord);
                        user.setId(id++);
                        Log.e("로그",user.getUsername());
                        tempBase.put(user.getEmail(),user);
                        //tempBase.put(""+snapshot.child("email").getValue(),user);
                    }
                    if(myUser!=null){
                        myUser = tempBase.get(myUser.getEmail());
                    }

                    error = false;
                    Log.e("로그: 에러","error"+Boolean.toString(error));
                }
                catch(Exception e){
                    Log.e("로그: 에러", "Exception");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        mRootRef.child("사용자수").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int number;
                if(dataSnapshot.getValue()==null){
                    number = 0;
                }
                else{
                    number = Integer.parseInt(""+dataSnapshot.getValue());
                }
                Log.d("MainActivity", "스냅샷 : " + dataSnapshot.getValue());
                if(newUser!=null)
                    tempBase.put(newUser.getEmail(),newUser);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    //email 입력하면 해당하는 User Data 반환 (tempBase에서 가져옴), 없는 사용자면 null 반환
    //내부 myUser에도 저장
    public static  User myData(String email){
        try{
            myUser = tempBase.get(email);
        }
        catch(Exception e){
            return null;
        }
        return myUser;
    }

    //각 정보를 입력하면 firebase에 update, user update, tempbase update
    //firebase database의 내용 수정+tempBase의 내용 수정
    public static void updateDatabase(String email, String username, String kakao, int level, int exp, Record totalRecord, Record bestRecord){
        // tempBase에서 email(키)로 custId를 찾고, 그 custId를 key로 firebase database 수정
        User user = tempBase.get(email);
        mRootRef.child("회원").child(Integer.toString(user.getId())).child("userName").setValue(username);
        mRootRef.child("회원").child(Integer.toString(user.getId())).child("kakao").setValue(kakao);
        mRootRef.child("회원").child(Integer.toString(user.getId())).child("level").setValue(level);
        mRootRef.child("회원").child(Integer.toString(user.getId())).child("exp").setValue(exp);
        mRootRef.child("회원").child(Integer.toString(user.getId())).child("totalDistance").setValue(totalRecord.getDistance());
        mRootRef.child("회원").child(Integer.toString(user.getId())).child("totalTime").setValue(totalRecord.getTime());
        mRootRef.child("회원").child(Integer.toString(user.getId())).child("bestDistance").setValue(bestRecord.getDistance());
        mRootRef.child("회원").child(Integer.toString(user.getId())).child("bestTime").setValue(bestRecord.getTime());
        //mRootRef.child("회원").child(Integer.toString(user.getCustId())).child("custId").setValue(Integer.toString(user.getCustId()));
        user.setUser(email,user.getCustId(),username,kakao,level,exp,totalRecord,bestRecord);
        tempBase.put(email,user);
    }
    public static String printUserInfo(User user){
        String print = "총사용자수:"+ Integer.toString(user.getCustId())+" 이메일:"+user.getEmail()+" 카카오:"+user.getKakao()+" 닉네임:"+user.getUsername()+" 총거리:"
                +Double.toString(user.getTotalRecord().getDistance()) +" 총시간:"+Integer.toString(user.getTotalRecord().getTime())+" 최고거리:"
                +Double.toString(user.getBestRecord().getDistance())+" 최고시간:"+Integer.toString(user.getBestRecord().getTime())+
                " 레벨:"+Integer.toString(user.getLevel()) +" 경험치:"+Integer.toString(user.getExp());
        return print;
    }

    public static void competitionListener(){
        mRootRef.child("경쟁").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (maps2Condition) {
                    if (!matchingState) {
                        Log.e("로그5", "경쟁 onDataChange");

                        String number = Long.toString(dataSnapshot.getChildrenCount());
                        Log.e("로그5", "경쟁모드 이용자 수 : " + number);

                        //emailSet 은 기존 comBase에 있던 email들
                        Set<String> emailSet;
                        if (comBase.size() > 0) { //기존 comBase에 competition이 있었다면
                            emailSet = comBase.keySet();
                        } else {
                            emailSet = null;
                            //안써주면 밑에서 에러발생
                        }

                        int index = 0;

                        //email은 firebase에 있는 competition들의 email을 저장
                        if (dataSnapshot.getChildrenCount() > 1) //firebase에 competition이 있다면
                            email = new String[(int) dataSnapshot.getChildrenCount()];
                        else
                            email = new String[1]; //이거 안써주면 에러 나서 그냥 추가함. 필요 없는 코드임.

                        try {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                if (index == 0) {
                                    index++;
                                    continue;
                                } //index가 0인 경우는 data update할 때 쓰는 자식이므로 pass
                                // firebase에 있는 competition 저장하기
                                Competition comUser = new Competition("" + snapshot.child("email").getValue());
                                // competition의 id값 받아오기 위해 tempBase에서 확인
                                User tempUser = tempBase.get(comUser.getEmail());
                                if (tempUser == null) {
                                    Log.e("로그", Integer.toString(comUser.getId()) + "경쟁모드 null 발생");
                                    index++;
                                    continue;
                                }
                                // firebase에 있는 comptition 저장하기2
                                Log.e("로그2: op", snapshot.child("opponent").getValue() + "");
                                Log.e("로그2: ct", snapshot.child("currentTime").getValue() + "");
                                Log.e("로그2: cd", snapshot.child("currentDistance").getValue() + "");
                                comUser.setId(tempUser.getId());
                                comUser.setOpponent(Integer.parseInt("" + snapshot.child("opponent").getValue()));
                                comUser.setCurrentTime(Integer.parseInt("" + snapshot.child("currentTime").getValue()));
                                DecimalFormat df = new DecimalFormat();
                                Number num = df.parse("" + snapshot.child("currentDistance").getValue());
                                comUser.setCurrentDistance(num.doubleValue());
                                //combase에 위에서 받아온 competiton 저장하기
                                comBase.put(comUser.getEmail(), comUser);
                                idToEmail.put(comUser.getId(), comUser.getEmail());

                                // 삭제된 comUser를 comBase에 반영하기 위한 작업
                                // firebase에 competition이 1명 이상 있으면 그 competition의 email값을 저장
                                if (dataSnapshot.getChildrenCount() > 1)
                                    email[index++] = comUser.getEmail();

                            }
                        } catch (Exception e) {
                        }
                    } else {

                        try {
                            otherCom.setCurrentTime(Integer.parseInt(dataSnapshot.child("" + otherCom.getId()).child("currentTime").getValue() + ""));
                            DecimalFormat df = new DecimalFormat();
                            Number num = null;
                            try {
                                num = df.parse(("" + dataSnapshot.child("" + otherCom.getId()).child("currentDistance").getValue()));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            otherCom.setCurrentDistance(num.doubleValue());
                        } catch (Exception e) {
                        }
                    }

                }
            }



            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public static void startCompetition(User user){

        myCom = new Competition(user.getEmail());
        myCom.setId(myUser.getId());
        //comBase.put(user.getEmail(),myCom);
        Log.e("로그5","starCompetition"+user.getEmail());
        mRootRef.child("경쟁").child(Integer.toString(user.getId())).child("email").setValue(user.getEmail());
        mRootRef.child("경쟁").child(Integer.toString(user.getId())).child("opponent").setValue("0");
        mRootRef.child("경쟁").child(Integer.toString(user.getId())).child("currentTime").setValue("0");
        mRootRef.child("경쟁").child(Integer.toString(user.getId())).child("currentDistance").setValue("0");
    }
    public static void finishCompetition(User user){
        mRootRef.child("경쟁").child(Integer.toString(user.getId())).removeValue();
        myCom = null;
        otherCom = null;
    }


    public static void printComBaseAndEmail(){
        for( String e : email){
            Log.e("로그6","EMAIL"+ e);
        }

        Log.e("로그6","COMBASE SIZE"+ comBase.size());
        int i = 0;

        for( String s : comBase.keySet() ){
            Log.e("로그6","COMBASE"+ s +" / " +comBase.get(s).getId());
        }
    }

    public static void clearPrevData(int id){
        if( comBase == null || email == null ) return;

        for( String e : email ) {
            if (comBase.get(e) == null) continue;
            if(comBase.get(e).getOpponent() == id) mRootRef.child("경쟁").child(Integer.toString(comBase.get(e).getId())).removeValue();
        }

    }

    public static void findCompetition(){
        if( comBase == null || email == null ) return;
        if( comBase.size() <= 1 || email.length <= 1 ) return;

        if( comBase.get(myUser.getEmail()).getOpponent() != 0 ) {
            int op = comBase.get(myUser.getEmail()).getOpponent();
            otherCom = comBase.get(idToEmail.get(op));
            Log.e("로그7","경쟁자 결정2 "+ otherCom.getId());
            matchingState = true;
        }

        for( String e : email ){
            if( comBase.get(e) == null ) continue;
            if(comBase.get(e).getId() == 0 || comBase.get(e).getId() == myUser.getId()) continue;

            if( myCom.match(comBase.get(e).getId(), comBase.get(e).getOpponent())){
                Log.e("로그7","경쟁자 결정 "+ comBase.get(e).getEmail());
                otherCom = comBase.get(e);
                myCom.setOpponent(otherCom.getId());
                comBase.get(e).setOpponent(myCom.getId());
                otherCom.setOpponent(myCom.getId());
                mRootRef.child("경쟁").child(Integer.toString(myCom.getId())).child("opponent").setValue(otherCom.getId());
                mRootRef.child("경쟁").child(Integer.toString(otherCom.getId())).child("opponent").setValue(myCom.getId());
                matchingState = true;
                break;
            }

        }

    }

    //HashMap<String,User> tempBase에 저장된 User들을 정렬해서 반환해주는 함수
    public static User[] sortingTempBase(){
        Collection<User> value = tempBase.values();
        User[] rank = new User[value.size()];
        value.toArray(rank);
        //내림차순 정렬
        Arrays.sort(rank);
        return rank;
    }

    // firebase에 저장된 내용으로 기기 내에 저장된 정보(다른 사용자의 정보) 업뎃하고 싶을 시 호출하는 함수
    // 이 함수를 호출하는 경우는 다른 사용자가 업데이트한 내용을 받아오고 싶을 때 사용
    // 기기 내에 저장된 myUser의 내용을 재업데이트하여 가져오는 방식
    public static void update(){
        mRootRef.child("회원").child("0").setValue(Double.toString(Math.random()));
    }

    public static void updateCom(){
        mRootRef.child("경쟁").child("0").setValue(Double.toString(Math.random()));
    }
}