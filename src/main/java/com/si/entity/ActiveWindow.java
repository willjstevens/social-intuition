/*
 * Property of Will Stevens
 * All rights reserved.
 */
package com.si.entity;

/**
 * @author wstevens
 */
public class ActiveWindow
{
    private String code;
    private String text;

    public ActiveWindow() {}

    public ActiveWindow(String text, String code) {
        this.text = text;
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
