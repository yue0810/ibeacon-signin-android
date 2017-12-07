package com.brtbeacon.sdk.demo;

/**
 * Created by yuewang on 2017/11/16.
 */

public class MyBeacon {
    private String username;
    private String classroom;
    private String intime;

    MyBeacon(String username, String classroom,String intime) {
        this.username=username;
        this.classroom=classroom;
        this.intime=intime;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getClassroom() {
        return classroom;
    }

    public void setClassroom(String classroom) {
        this.classroom = classroom;
    }

    public String getIntime() {
        return intime;
    }

    public void setIntime(String intime) {
        this.intime = intime;
    }
}
