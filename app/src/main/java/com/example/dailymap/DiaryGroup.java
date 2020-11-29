package com.example.dailymap;

import java.util.Vector;

public class DiaryGroup {
    public Vector<String> userList; //email
    //collection으로 다이어리 저장

    public DiaryGroup(){

    }
    public DiaryGroup(String user){
        this.userList = new Vector<String>();
        userList.add(user);
    }

    public DiaryGroup(Vector<String> user){
        userList= (Vector<String>) user.clone();
    }

    public Vector<String> getUserList() {
        return userList;
    }
}
