/*
 * Property of Will Stevens
 * All rights reserved.
 */
package com.si.dto;

import com.si.entity.Notification;

/**
 * @author wstevens
 */
public class NotificationDto {
    private Notification notification;
    private String prettyTimestamp;

    public NotificationDto(Notification notification, String prettyTimestamp) {
        this.notification = notification;
        this.prettyTimestamp = prettyTimestamp;
    }

    public Notification getNotification() {
        return notification;
    }

    public String getPrettyTimestamp() {
        return prettyTimestamp;
    }
}
