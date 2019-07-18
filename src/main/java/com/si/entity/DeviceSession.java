/*
 * Property of Will Stevens
 * All rights reserved.
 */
package com.si.entity;

import java.util.Date;

/**
 *
 *
 * @author wstevens
 */
public class DeviceSession
{
    private String id;
    private String userId;
    private String deviceId;
    private String httpSessionId;
    private Date insertTimestamp;

    public DeviceSession() {}

    public DeviceSession (String userId, String deviceId, String httpSessionId) {
        this.userId = userId;
        this.deviceId = deviceId;
        this.httpSessionId = httpSessionId;
    }

    public String getUserId () {
        return userId;
    }

    public void setUserId (String userId) {
        this.userId = userId;
    }

    public String getDeviceId () {
        return deviceId;
    }

    public void setDeviceId (String deviceId) {
        this.deviceId = deviceId;
    }

    public String getHttpSessionId() {
        return httpSessionId;
    }

    public void setHttpSessionId(String httpSessionId) {
        this.httpSessionId = httpSessionId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getInsertTimestamp() {
        return insertTimestamp;
    }

    public void setInsertTimestamp(Date insertTimestamp) {
        this.insertTimestamp = insertTimestamp;
    }

    @Override
    public boolean equals (Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DeviceSession deviceSession1 = (DeviceSession) o;

        if (!deviceId.equals(deviceSession1.deviceId)) {
            return false;
        }
        if (!httpSessionId.equals(deviceSession1.httpSessionId)) {
            return false;
        }
        if (!userId.equals(deviceSession1.userId)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode () {
        int result = Integer.MAX_VALUE;
        if (userId != null) {
            result = userId.hashCode();
        }
        result = 31 * result + deviceId.hashCode();
        result = 31 * result + httpSessionId.hashCode();
        if (userId != null) {
            result = 31 * result + userId.hashCode();
        }
        return result;
    }
}
