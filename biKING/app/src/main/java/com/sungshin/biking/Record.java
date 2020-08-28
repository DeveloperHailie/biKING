package com.sungshin.biking;

// 기록 Record class
// distance, time
// Record(),Record(distance,time),setRecord(distance,time),setDistance(distance),setTime(time),getDistance(),getTime()
public class Record{
    private double distance;
    private int time;
    public Record(){
        distance = 0;
        time = 0;
    }
    public Record(double distance, int time){
        this.distance =distance;
        this.time = time;
    }
    public void setRecord(double distance, int time){
        this.distance =distance;
        this.time = time;
    }
    public void compareSetRecord(Record r){
        // 거리를 기준으로 비교
        // 거리가 같으면 시간으로 비교
        if(this.distance>r.distance) return;
        if(this.distance<r.distance) {
            this.distance = r.distance;
            this.time = r.time;
        }
        else {
            if(this.time<r.time) return;
            else this.time=r.time;
        }
    }
    public void setDistance(double distance){
        this.distance =distance;
    }
    public void setTime(int time){
        this.time = time;
    }
    public double getDistance(){
        return distance;
    }
    public int getTime(){
        return time;
    }
}