package com.rahuls.sharednotes.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.List;

public class Group {
    private String GroupName;
    private String CreatedBy;
    @ServerTimestamp
    private Timestamp CreatedAt;
    private String GroupId;
    private List<String> GroupMembers;
    private List<String> GroupMemberUId;

    public Group() {
    }

    public Group(String GroupName, String CreatedBy, Timestamp CreatedAt, String GroupId, List<String> GroupMembers, List<String> GroupMemberUId) {
        this.GroupName = GroupName;
        this.CreatedBy = CreatedBy;
        this.CreatedAt = CreatedAt;
        this.GroupId = GroupId;
        this.GroupMembers = GroupMembers;
        this.GroupMemberUId = GroupMemberUId;
    }

    public String getGroupName() {
        return GroupName;
    }

    public void setGroupName(String GroupName) {
        this.GroupName = GroupName;
    }

    public String getCreatedBy() {
        return CreatedBy;
    }

    public void setCreatedBy(String CreatedBy) {
        this.CreatedBy = CreatedBy;
    }

    public Timestamp getCreatedAt() {
        return CreatedAt;
    }

    public void setCreatedAt(Timestamp CreatedAt) {
        this.CreatedAt = CreatedAt;
    }

    public String getGroupId() {
        return GroupId;
    }

    public void setGroupId(String GroupId) {
        this.GroupId = GroupId;
    }

    public List<String> getGroupMembers() {
        return GroupMembers;
    }

    public void setGroupMembers(List<String> GroupMembers) {
        this.GroupMembers = GroupMembers;
    }

    public List<String> getGroupMemberUId() { return GroupMemberUId; }

    public void setGroupMemberUId(List<String> GroupMemberUId) { this.GroupMemberUId = GroupMemberUId; }

}
