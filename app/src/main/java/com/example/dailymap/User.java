package com.example.dailymap;

import java.util.Vector;

public class User {
    public String name;
    public String email;
    public Vector<String> diaryGroupList;

    public User(){

    }
    public User(String name, String email){
        this.name=name;
        this.email=email;
        this.diaryGroupList = new Vector<String>();
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Vector<String> getDiaryGroupList() {
        return diaryGroupList;
    }
}
