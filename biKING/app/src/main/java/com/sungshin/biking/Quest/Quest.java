package com.sungshin.biking.Quest;

public class Quest {
    String name;
    String type;
    int goal;
    int exp;
    int condition;


    public Quest (String name, String type, int goal, int exp){
        this.name = name;
        this.type = type;
        this.goal = goal;
        this.exp = exp;
        this.condition = 1;
    }

    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }

    public int getExp(){
        return exp;
    }

    public void setExp(int exp){
        this.exp=exp;
    }

    public int getCondition(){
        return condition;
    }

    public void setCondition(int condition){
        this.condition= condition;
    }

    public String getType(){
        return type;
    }
}
