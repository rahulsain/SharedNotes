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

    public Group(String groupName, String createdBy, Timestamp createdAt, String groupId, List<String> groupMembers, List<String> groupMemberUId) {
        GroupName = groupName;
        CreatedBy = createdBy;
        CreatedAt = createdAt;
        GroupId = groupId;
        GroupMembers = groupMembers;
        GroupMemberUId = groupMemberUId;
    }

    public String getGroupName() {
        return GroupName;
    }

    public void setGroupName(String groupName) {
        GroupName = groupName;
    }

    public String getCreatedBy() {
        return CreatedBy;
    }

    public void setCreatedBy(String createdBy) {
        CreatedBy = createdBy;
    }

    public Timestamp getCreatedAt() {
        return CreatedAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        CreatedAt = createdAt;
    }

    public String getGroupId() {
        return GroupId;
    }

    public void setGroupId(String groupId) {
        GroupId = groupId;
    }

    public List<String> getGroupMembers() {
        return GroupMembers;
    }

    public void setGroupMembers(List<String> groupMembers) {
        GroupMembers = groupMembers;
    }

    public List<String> getGroupMemberUId() { return GroupMemberUId; }

    public void setGroupMemberUId(List<String> groupMemberUId) { GroupMemberUId = groupMemberUId; }

}
