/*
 * Property of Will Stevens
 * All rights reserved.
 */
package com.si.framework;

/**
 *
 *
 * 
 * @author wstevens
 */
public abstract class ResultException extends RuntimeException
{
	private static final long serialVersionUID = 5849985795981906759L;
    private Response response;

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }
}
