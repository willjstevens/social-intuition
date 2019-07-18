/*
 * Property of Will Stevens
 * All rights reserved.
 */
package com.si.framework;


/**
 * @author wstevens
 */
public class Response<T>
{

    private boolean isSuccess;
    private String message;
    private int code;
    private T data;
    private String error;
    private String targetUrl;
    private String lastUpdateTimestamp;

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getTargetUrl () {
        return targetUrl;
    }

    public void setTargetUrl (String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public String getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    public void setLastUpdateTimestamp(String lastUpdateTimestamp) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }
}
