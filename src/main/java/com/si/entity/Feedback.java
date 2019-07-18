/*
 * Property of Will Stevens
 * All rights reserved.
 */
package com.si.entity;

/**
 * @author wstevens
 */
public class Feedback
{
    private String id;
    private String name;
    private String email;
    private String comment;
    private String insertTimestamp;

    public Feedback() {}

    public Feedback(String name, String email, String type, String comment) {
        this.name = name;
        this.email = email;
        this.comment = comment;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getInsertTimestamp() {
        return insertTimestamp;
    }

    public void setInsertTimestamp(String insertTimestamp) {
        this.insertTimestamp = insertTimestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
