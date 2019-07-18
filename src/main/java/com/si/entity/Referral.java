/*
 * Property of Will Stevens
 * All rights reserved.
 */
package com.si.entity;

/**
 * @author wstevens
 */
public class Referral
{
    private String id;
    private String referralCode;
    private String title;
    private String buttonTitle;
    private String targetUrl;
    private boolean requiresSession;
    private boolean isGuestAllowed;
    private boolean requestRegistration;
    private String progressMessage;
    private String insertTimestamp;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isRequestRegistration() {
        return requestRegistration;
    }

    public void setRequestRegistration(boolean requestRegistration) {
        this.requestRegistration = requestRegistration;
    }

    public String getReferralCode() {
        return referralCode;
    }

    public void setReferralCode(String referralCode) {
        this.referralCode = referralCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getButtonTitle() {
        return buttonTitle;
    }

    public void setButtonTitle(String buttonTitle) {
        this.buttonTitle = buttonTitle;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public boolean isRequiresSession() {
        return requiresSession;
    }

    public void setRequiresSession(boolean requiresSession) {
        this.requiresSession = requiresSession;
    }

    public String getProgressMessage() {
        return progressMessage;
    }

    public void setProgressMessage(String progressMessage) {
        this.progressMessage = progressMessage;
    }

    public boolean isGuestAllowed() {
        return isGuestAllowed;
    }

    public void setGuestAllowed(boolean guestAllowed) {
        isGuestAllowed = guestAllowed;
    }

    public String getInsertTimestamp() {
        return insertTimestamp;
    }

    public void setInsertTimestamp(String insertTimestamp) {
        this.insertTimestamp = insertTimestamp;
    }
}
