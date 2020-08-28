package com.sungshin.biking;

import android.content.Context;
import android.widget.Toast;

public class LevelChecker {
    static int levelList[] = { 0,100,500,1000,2000,4000,8000,15000,30000,50000};
    static public void levelChecker(Context context){
        User user = firebaseDB.myUser;
        int exp  = user.getExp();
        int level = user.getLevel();
        if( user.getExp()< 0) {
            firebaseDB.myUser.setExp(0);
            firebaseDB.mRootRef.child("회원").child(Integer.toString(user.getId())).child("exp").setValue(Integer.toString(0));
        }
        for( int i=  0 ; i< levelList.length; i++){
            if( exp < levelList[i] ){
                firebaseDB.mRootRef.child("회원").child(Integer.toString(user.getId())).child("level").setValue(Integer.toString(i));
                firebaseDB.tempBase.put(firebaseDB.myUser.getEmail(), firebaseDB.myUser);
                if( level < i ) Toast.makeText(context, "★ 레벨 " + level+ "->" + i+"로 업하셨습니다 ★", Toast.LENGTH_SHORT).show();
                user.setLevel(i);
                return;

            }
        }
    }

    static public int getRemainExp(){
        User user = firebaseDB.myUser;
        int level = user.getLevel();
        if( level >= levelList.length ) return 0;
        int remainExp = levelList[level]-levelList[level-1];

        return remainExp - getLevelExp();
    }

    static public int getLevelExp(){
        User user = firebaseDB.myUser;
        int level = user.getLevel();
        int exp = user.getExp()- levelList[level-1];
        return exp;
    }


}
