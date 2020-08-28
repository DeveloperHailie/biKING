package com.sungshin.biking.Quest;


import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static com.sungshin.biking.firebaseDB.myUser;

public class QuestList {

    static private ArrayList<Quest> questList = new ArrayList<Quest>();
    static public int listSize;

    static public void addQuest(String name, String type, int goal, int exp) {
        Quest quest = new Quest(name,type, goal, exp);
        questList.add(quest);
        listSize++;
    }

    static public int getSize() {

        return listSize;
    }

    static public ArrayList<Quest> getList() {

        return questList;
    }

    static public void sortList(){
        Comparator<Quest> typeComparator = new Comparator<Quest>() {
            @Override
            public int compare(Quest q1, Quest q2) {
                return q1.type.compareTo(q2.type);
            }
        };

        Comparator<Quest> conditionComparator = new Comparator<Quest>() {
            @Override
            public int compare(Quest q1, Quest q2) {
                return q1.condition - (q2.condition);
            }
        };

        Collections.sort(questList, typeComparator);
        Collections.sort(questList, conditionComparator);

    }

    static public Quest getQuest(String name){
        for(int i = 0; i< questList.size(); i++)
            if( name.equals(questList.get(i).getName())) return questList.get(i);
         return null;

    }

    static public void checkQuest(){
        Quest item;
        ArrayList<Quest> questList = QuestList.getList();

        double userTotalDIstance = myUser.getTotalRecord().getDistance();
        int userTotalTime =  myUser.getTotalRecord().getTime();
        double userBestDistance = myUser.getBestRecord().getDistance();
        int userBestTime = myUser.getBestRecord().getTime();

        String questfile = "data/data/com.sungshin.biking/files/"+myUser.getId()+".txt";

        try{
            BufferedReader br = new BufferedReader(new FileReader(questfile));
            String str = null;
            while(((str = br.readLine()) != null)){
                Log.d("TAG","FILE"+str.trim()+"완료");
                Quest q = QuestList.getQuest(str.trim());
                if( null != q ){
                    q.setCondition(2);
                    Log.d("TAG","FILE"+q.getName()+" 저장 완료");
                }
            }
            br.close();

        }catch (FileNotFoundException e){
            Log.d("TAG","noFile----------------------------------");

        }catch (IOException e) {
            e.printStackTrace();
        }


        for( int i = 0; i < questList.size() ; i++ ) {
            item = questList.get(i);
            if( item.getCondition() == 2 ) continue;
            int goal = item.goal;
            switch(item.getType()){
                case "TotalDistance":
                    if( userTotalDIstance >= goal ) item.setCondition(0);
                    break;
                case "TotalTime":
                    if( userTotalTime >= goal ) item.setCondition(0);
                    break;
                case "BestDistance":
                    if( userBestDistance>= goal ) item.setCondition(0);
                    break;
                case "BestTime":
                    if( userBestTime>= goal ) item.setCondition(0);
                    break;

            }




        }
    }
}
