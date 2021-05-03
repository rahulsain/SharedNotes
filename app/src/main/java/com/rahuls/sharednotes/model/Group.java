package com.rahuls.sharednotes.model;

import java.util.Map;

public class Group {
    private String GroupName;
    private String CreatedBy;
    private String CreatedAt;
    private String GroupId;
    private Map<String, Object> GroupMembers;

    public Group() {
    }

    public Group(String groupName, String createdBy, String createdAt, String groupId, Map<String, Object> groupMembers) {
        GroupName = groupName;
        CreatedBy = createdBy;
        CreatedAt = createdAt;
        GroupId = groupId;
        GroupMembers = groupMembers;
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

    public String getCreatedAt() {
        return CreatedAt;
    }

    public void setCreatedAt(String createdAt) {
        CreatedAt = createdAt;
    }

    public String getGroupId() {
        return GroupId;
    }

    public void setGroupId(String groupId) {
        GroupId = groupId;
    }

    public Map<String, Object> getGroupMembers() {
        return GroupMembers;
    }

    public void setGroupMembers(Map<String, Object> groupMembers) {
        GroupMembers = groupMembers;
    }

}
