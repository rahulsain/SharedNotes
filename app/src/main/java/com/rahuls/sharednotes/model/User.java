package com.rahuls.sharednotes.model;

import java.util.Map;

public class User {
    private String UserName;
    private String UserEmail;
    private String UserId;
    private String UserPhotoURL;
    private Map<String, Object> UserGroups;

    public User() {
    }

    public User(String name, String UserEmail, String UserId) {
        this.UserName = name;
        this.UserEmail = UserEmail;
        this.UserId = UserId;
    }

    public User(String name, String UserEmail, String UserId, String UserPhotoURL, Map<String, Object> UserGroups) {
        this.UserName = name;
        this.UserEmail = UserEmail;
        this.UserId = UserId;
        this.UserPhotoURL = UserPhotoURL;
        this.UserGroups = UserGroups;
    }

    public String getName() {
        return UserName;
    }

    public void setName(String name) {
        this.UserName = name;
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

    public String getUserPhotoURL() {
        return UserPhotoURL;
    }

    public void setUserPhotoURL(String userPhotoURL) {
        this.UserPhotoURL = userPhotoURL;
    }

    public Map<String, Object> getUserGroups() {
        return UserGroups;
    }

    public void setUserGroups(Map<String, Object> userGroups) {
        this.UserGroups = userGroups;
    }
}
