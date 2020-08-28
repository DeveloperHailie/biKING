package com.sungshin.biking.ListView;

public class rankingListItem {
    private int ranking;
    private String name;
    private int imageId;
    private String exp;


    public int getRanking(){
        return ranking;
    }
    public void setRanking(int ranking){
        this.ranking = ranking;
    }

    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }

    public int getImage(){
        return imageId;
    }

    public void setImage(int imageId){
        this.imageId=imageId;
    }

    public String getExp(){
        return exp;
    }

    public void setExp(String exp){
        this.exp = exp;
    }
}
