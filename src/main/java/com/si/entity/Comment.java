/*
 * Property of Will Stevens
 * All rights reserved.
 */
package com.si.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wstevens
 */
public class Comment
{
    private String id;
    private String commentText;
    private List<Like> likes = new ArrayList<>();
    private String insertTimestamp;
    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public String getInsertTimestamp() {
        return insertTimestamp;
    }

    public void setInsertTimestamp(String insertTimestamp) {
        this.insertTimestamp = insertTimestamp;
    }

    public List<Like> getLikes() {
        return likes;
    }

    public void setLikes(List<Like> likes) {
        this.likes = likes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
