package com.example.dailymap;

import java.util.Vector;

public class DiaryGroup {
    public String name;
    public Vector<String> userList; //email
    public Vector<DiaryDS> diaryList;

    public DiaryGroup(String name,String user){
        this.name=name;
        this.userList = new Vector<String>();
        this.diaryList=new Vector<DiaryDS>();
        addUser(user);
    }

    public void addUser(String user){
        this.userList.add(user); //user 추가
    }
}
