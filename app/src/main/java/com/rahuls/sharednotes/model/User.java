package com.rahuls.sharednotes.model;

import java.util.List;

public class User {
    private String UserName;
    private String UserEmail;
    private String UserId;
//    private String UserPhotoURL;
    private List<String> UserGroups;

    public User() {
    }

    public User(String UserName, String UserEmail, String UserId) {
        this.UserName = UserName;
        this.UserEmail = UserEmail;
        this.UserId = UserId;
    }

    public User(String UserName, String UserEmail, String UserId, List<String> UserGroups) {
        this.UserName = UserName;
        this.UserEmail = UserEmail;
        this.UserId = UserId;
        this.UserGroups = UserGroups;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String UserName) {
        this.UserName = UserName;
    }

    public String getUserEmail() {
        return UserEmail;
    }

    public void setUserEmail(String userEmail) {
        this.UserEmail = userEmail;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        this.UserId = userId;
    }

//    public String getUserPhotoURL() {
//        return UserPhotoURL;
//    }

//    public void setUserPhotoURL(String userPhotoURL) {
//        this.UserPhotoURL = userPhotoURL;
//    }

    public List<String> getUserGroups() {
        return UserGroups;
    }

    public void setUserGroups(List<String> userGroups) {
        this.UserGroups = userGroups;
    }
}
