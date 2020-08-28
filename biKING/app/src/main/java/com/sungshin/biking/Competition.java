package com.sungshin.biking;

public class Competition {
    public int id;
    public int opponent;
    public int currentTime;
    public double currentDistance;
    public String email;

    public Competition(){
        this.id = 0;
        opponent = 0;
        currentTime = 0;
        currentDistance = 0;
        email = "";
    }
    public Competition(int id){
        this.id = id;
        opponent = 0;
        currentTime = 0;
        currentDistance = 0;
        email = "";
    }
    public Competition(String email){
        this.email=email;
        id = 0;
        opponent = 0;
        currentTime = 0;
        currentDistance =0;
    }
    // o,t,d 초기화
    public void reset(){
        opponent = 0;
        currentTime = 0;
        currentDistance = 0;
    }
    // 상대 매칭할 때 사용할 함수
    // 회원번호 != 인자값 && 경쟁상대 == 0 이면 true
    public boolean match(int id, int opponent){
            if(this.id!=id && opponent==0 && this.opponent==0) return true;
            return false;
    }
    public void setEmail(String email){
        this.email =email;
    }
    public void setId(int id){
        this.id=id;
    }
    public void setOpponent(int o){
        opponent=o;
    }
    public void setCurrentTime(int t){
        currentTime = t;
    }
    public void setCurrentDistance(double d){
        currentDistance = d;
    }
    public int getId(){
        return id;
    }
    public int getOpponent(){
        return opponent;
    }
    public int getCurrentTime(){
        return currentTime;
    }
    public double getCurrentDistance(){
        return currentDistance;
    }
    public String getEmail(){
        return email;
    }


}
