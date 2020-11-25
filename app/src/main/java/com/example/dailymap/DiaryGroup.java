package com.example.dailymap;

import java.util.Vector;

public class DiaryGroup {
    public String name;
    public Vector<String> userList; //email

    public DiaryGroup(){

    }
    public DiaryGroup(String name,String user){
        this.name=name;
        this.userList = new Vector<String>();
        userList.add(user);
    }

    public String getName() {
        return name;
    }

    public Vector<String> getUserList() {
        return userList;
    }
}
