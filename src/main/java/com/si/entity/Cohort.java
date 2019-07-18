/*
 * Property of Will Stevens
 * All rights reserved.
 */
package com.si.entity;

/**
 * @author wstevens
 */
public class Cohort
{
    private String id;
    private String inviterUserId;
    private String inviterFullName;
    private String inviterUsername;
    private StoredImageInfo inviterImageInfo;
    private String consenterUserId;
    private String consenterFullName;
    private String consenterUsername;
    private StoredImageInfo consenterImageInfo;
    private boolean isAccepted;
    private boolean isIgnored;
    private String insertTimestamp;
    private String updateTimestamp;

    public String getConsenterUsername() {
        return consenterUsername;
    }

    public void setConsenterUsername(String consenterUsername) {
        this.consenterUsername = consenterUsername;
    }

    public String getInviterUsername() {
        return inviterUsername;
    }

    public void setInviterUsername(String inviterUsername) {
        this.inviterUsername = inviterUsername;
    }


    public StoredImageInfo getInviterImageInfo() {
        return inviterImageInfo;
    }

    public void setInviterImageInfo(StoredImageInfo inviterImageInfo) {
        this.inviterImageInfo = inviterImageInfo;
    }

    public StoredImageInfo getConsenterImageInfo() {
        return consenterImageInfo;
    }

    public void setConsenterImageInfo(StoredImageInfo consenterImageInfo) {
        this.consenterImageInfo = consenterImageInfo;
    }

    public String getInviterUserId() {
        return inviterUserId;
    }

    public void setInviterUserId(String inviterUserId) {
        this.inviterUserId = inviterUserId;
    }

    public String getInviterFullName() {
        return inviterFullName;
    }

    public void setInviterFullName(String inviterFullName) {
        this.inviterFullName = inviterFullName;
    }

    public String getConsenterUserId() {
        return consenterUserId;
    }

    public void setConsenterUserId(String consenterUserId) {
        this.consenterUserId = consenterUserId;
    }

    public String getConsenterFullName() {
        return consenterFullName;
    }

    public void setConsenterFullName(String consenterFullName) {
        this.consenterFullName = consenterFullName;
    }

    public boolean isAccepted() {
        return isAccepted;
    }

    public void setAccepted(boolean isAccepted) {
        this.isAccepted = isAccepted;
    }

    public boolean isIgnored() {
        return isIgnored;
    }

    public void setIgnored(boolean isIgnored) {
        this.isIgnored = isIgnored;
    }

    public String getInsertTimestamp() {
        return insertTimestamp;
    }

    public void setInsertTimestamp(String insertTimestamp) {
        this.insertTimestamp = insertTimestamp;
    }

    public String getUpdateTimestamp() {
        return updateTimestamp;
    }

    public void setUpdateTimestamp(String updateTimestamp) {
        this.updateTimestamp = updateTimestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


}
