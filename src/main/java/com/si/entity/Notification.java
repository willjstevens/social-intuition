/*
 * Property of Will Stevens
 * All rights reserved.
 */
package com.si.entity;

/**
 * @author wstevens
 */
public class Notification<T>
{
    private String id;
    private String userId;
    private String type;
    private String message;
    private T data;
    private boolean isHandled;
    private String insertTimestamp;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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

    public boolean isHandled() {
        return isHandled;
    }

    public void setHandled(boolean isHandled) {
        this.isHandled = isHandled;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}