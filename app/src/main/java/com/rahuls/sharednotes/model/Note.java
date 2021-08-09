package com.rahuls.sharednotes.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

public class Note {
    private String title;
    private String content;
    @ServerTimestamp
    private Timestamp createdOn;
    @ServerTimestamp
    private Timestamp lastEditedOn;
    private String createdBy;
    private String createdByEmail;
    private String createdByName;

    public Note() {
    }

    public Note(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public Note(String title, String content,Timestamp createdOn,String createdBy,Timestamp lastEditedOn,String createdByEmail,String createdByName) {
        this.title = title;
        this.content = content;
        this.createdOn = createdOn;
        this.createdBy = createdBy;
        this.lastEditedOn = lastEditedOn;
        this.createdByEmail = createdByEmail;
        this.createdByName = createdByName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Timestamp getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Timestamp createdOn) {
        this.createdOn = createdOn;
    }

    public String getCreatedBy() { return createdBy; }

    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public Timestamp getLastEditedOn() { return lastEditedOn; }

    public void setLastEditedOn(Timestamp lastEditedOn) { this.lastEditedOn = lastEditedOn; }

    public String getCreatedByEmail() { return createdByEmail; }

    public void setCreatedByEmail(String createdByEmail) { this.createdByEmail = createdByEmail; }

    public String getCreatedByName() { return createdByName; }

    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }
}
