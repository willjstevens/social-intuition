
/*
 * Property of Will Stevens
 * All rights reserved.
 */
package com.si.dto;

import com.si.entity.DeviceSession;
import com.si.entity.User;

/**
 * @author wstevens
 */
public class DeviceLoginDto
{
    private User user;
    private DeviceSession deviceSession;

    public DeviceSession getDeviceSession() {
        return deviceSession;
    }

    public void setDeviceSession(DeviceSession deviceSession) {
        this.deviceSession = deviceSession;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
