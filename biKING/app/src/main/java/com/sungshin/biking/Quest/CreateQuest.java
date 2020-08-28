package com.sungshin.biking.Quest;

public class CreateQuest {
    public static boolean once = false;
    public static void makeQuest(){
        if( once == true ) return;
        once = true;

        for(int i = 1; i< 10; i+=2)
            QuestList.addQuest("총 달린 거리 "+i+"km 이상","TotalDistance" ,i,i*100);

        for(int i = 10; i<120; i+=20)
            QuestList.addQuest("총 달린 시간 "+i+"분 이상","TotalTime" ,i,i*50);

        for(int i = 1 ; i<10; i+=2)
            QuestList.addQuest("한번에 달린 거리 "+i+"km 이상","BestDistance" ,i,i*50);

        for(int i = 1 ; i<120; i+=20)
            QuestList.addQuest("한번에 달린 시간 "+i+"분 이상","BestTime" ,i,i*50);

    }
}
