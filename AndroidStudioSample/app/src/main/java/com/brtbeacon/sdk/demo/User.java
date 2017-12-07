package com.brtbeacon.sdk.demo;

/**
 * Created by yuewang on 2017/11/28.
 */

public class User {
    private String username;
    private String userpassword;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserpassword() {
        return userpassword;
    }

    public void setUserpassword(String userpassword) {
        this.userpassword = userpassword;
    }

    User(String username, String userpassword){
        this.username=username;
        this.userpassword=userpassword;

    }

}
