/*
 * Property of Will Stevens
 * All rights reserved.
 */
package com.si.framework;

import java.io.InputStream;

/**
 * @author wstevens
 */
public class FileInfo
{
    public String name;
    public String contentType;
    public InputStream inputStream;

    public FileInfo(String name, String contentType, InputStream inputStream) {
        this.name = name;
        this.contentType = contentType;
        this.inputStream = inputStream;
    }
}
