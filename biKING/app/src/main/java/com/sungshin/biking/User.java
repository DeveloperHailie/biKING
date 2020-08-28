package com.sungshin.biking;

import com.google.firebase.database.IgnoreExtraProperties;
// 회원정보 저장할 User class
// email, custId, username, kakao, level, exp, totalRecord, bestRecord
// User(),User(email,username),setUser(~~),get~~()
@IgnoreExtraProperties
public class User implements Comparable<User>{
    private String email; // 키
    static public int custId = 0; //총 사용자 인원수
    private String username;
    private String kakao;
    private int level;
    private int exp;
    private Record totalRecord;
    private Record bestRecord;
    public int id;
    public int ranking;

    public User() {
        email = null;
        username = null;
        kakao = null;
        level = 1;
        exp = 0;
        totalRecord = new Record(0,0);
        bestRecord = new Record(0,0);
    }
    public User(String email, String username) {
        this.email = email;
        this.username = username;
        kakao = "없음";
        level = 1;
        exp = 0;
        totalRecord = new Record(0,0);
        bestRecord = new Record(0,0);
        custId++;
    }
    //public User(String email, int custId, String username,String kakao, int level, int exp, Record totalRecord, Record bestRecord){  }
    public void setUser(String email, int custId, String username,String kakao, int level, int exp, Record totalRecord, Record bestRecord){
        this.email=email;
        this.custId=custId;
        this.username=username;
        this.kakao=kakao;
        this.level=level;
        this.exp=exp;
        this.totalRecord=totalRecord;
        this.bestRecord=bestRecord;
    }
    public String getEmail(){return email;}
    public void setEmail(String email){this.email = email;}
    public static int getCustId(){return custId;}
    public String getUsername(){return username;}
    public String getKakao(){return kakao;}
    public int getLevel(){return level;}
    public void setLevel(int i){this.level=i;}
    public void setExp(int i){this.exp=i;}
    public int getExp(){return exp;}
    public Record getTotalRecord(){return totalRecord;}
    public Record getBestRecord(){return bestRecord;}
    public void setTotalRecord(Record record){
        this.totalRecord.setRecord(record.getDistance(),record.getTime());
    }
    public void setBestRecord(Record record){
        this.bestRecord.setRecord(record.getDistance(),record.getTime());
    }

    public int getId(){ return id;   }
    public void setId(int id) { this.id = id;}
    public int getRanking() {return ranking;}
    public void setRanking(int ranking) {this.ranking=ranking;}

    @Override
    public int compareTo(User u){
        int myExp = this.getExp();
        int uExp = u.getExp();
        if(myExp > uExp) return -1;
        else if(myExp<uExp) return 1;
        else return 0;
    }

    public int checkingRanking(){
        // 메인 랭킹 업데이트
        User[] rank = firebaseDB.sortingTempBase();

        int index=1;
        for(int i = 0 ;i<rank.length; i++,index++){
            firebaseDB.tempBase.get(rank[i].getEmail()).setRanking(index);
            if( i+1 < rank.length && rank[i].getExp() == rank[i+1].getExp()) index--;
        }
       return firebaseDB.tempBase.get(email).getRanking();

    }
}

