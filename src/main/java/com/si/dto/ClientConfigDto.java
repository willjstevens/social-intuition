/*
 * Property of Will Stevens
 * All rights reserved.
 */
package com.si.dto;

/**
 * @author wstevens
 */
public class ClientConfigDto
{
    private String facebookAppId;
    private String googleClientId;
    private String userGuestImageUrl;
    private String userDefaultImageUrl;
    private String siteDomainHttps;
    private String siteDomainOverride;

    public String getSiteDomainHttps() {
        return siteDomainHttps;
    }

    public void setSiteDomainHttps(String siteDomainHttps) {
        this.siteDomainHttps = siteDomainHttps;
    }

    public String getFacebookAppId() {
        return facebookAppId;
    }

    public void setFacebookAppId(String facebookAppId) {
        this.facebookAppId = facebookAppId;
    }

    public String getGoogleClientId() {
        return googleClientId;
    }

    public void setGoogleClientId(String googleClientId) {
        this.googleClientId = googleClientId;
    }

    public String getUserDefaultImageUrl() {
        return userDefaultImageUrl;
    }

    public void setUserDefaultImageUrl(String userDefaultImageUrl) {
        this.userDefaultImageUrl = userDefaultImageUrl;
    }

    public String getUserGuestImageUrl() {
        return userGuestImageUrl;
    }

    public void setUserGuestImageUrl(String userGuestImageUrl) {
        this.userGuestImageUrl = userGuestImageUrl;
    }

    public String getSiteDomainOverride() {
        return siteDomainOverride;
    }

    public void setSiteDomainOverride(String siteDomainOverride) {
        this.siteDomainOverride = siteDomainOverride;
    }
}
