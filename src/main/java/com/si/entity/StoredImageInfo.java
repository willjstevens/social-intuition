/*
 * Property of Will Stevens
 * All rights reserved.
 */
package com.si.entity;

/**
 * @author wstevens
 */
public class StoredImageInfo
{
    private String fileName;
    private String secureUrl;

    public StoredImageInfo() {}

    public StoredImageInfo(String fileName, String secureUrl) {
        this.fileName = fileName;
        this.secureUrl = secureUrl;
    }

    public String getSecureUrl() {
        return secureUrl;
    }

    public void setSecureUrl(String secureUrl) {
        this.secureUrl = secureUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
