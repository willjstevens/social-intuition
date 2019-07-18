package com.si.service;

import com.si.framework.*;

/**
 * Standard implementation for account operations.
 *
 * @author wstevens
 */
public abstract class AbstractServiceImpl
{

	ServerErrorPageResultException getServerErrorPageResultException(Response response) {
        ServerErrorPageResultException serverErrorPageResultException = new ServerErrorPageResultException();
        serverErrorPageResultException.setResponse(response);
		return serverErrorPageResultException;
	}
	
	ServerErrorAjaxResultException getServerErrorAjaxResultException(Response response) {
        ServerErrorAjaxResultException serverErrorAjaxResultException = new ServerErrorAjaxResultException();
        serverErrorAjaxResultException.setResponse(response);
		return serverErrorAjaxResultException;
	}
	
	ForbiddenPageResultException getForbiddenPageResultException(Response response) {
        ForbiddenPageResultException forbiddenPageResultException = new ForbiddenPageResultException();
        forbiddenPageResultException.setResponse(response);
		return forbiddenPageResultException;
	}

	ForbiddenAjaxResultException getForbiddenAjaxResultException(Response response) {
        ForbiddenAjaxResultException forbiddenAjaxResultException = new ForbiddenAjaxResultException();
        forbiddenAjaxResultException.setResponse(response);
		return forbiddenAjaxResultException;
	}
	
	PageNotFoundPageResultException getPageNotFoundPageResultException(Response response) {
        PageNotFoundPageResultException pageNotFoundPageResultException = new PageNotFoundPageResultException();
        pageNotFoundPageResultException.setResponse(response);
		return pageNotFoundPageResultException;
	}

	PageNotFoundAjaxResultException PageNotFoundAjaxResultException(Response response) {
        PageNotFoundAjaxResultException pageNotFoundAjaxResultException = new PageNotFoundAjaxResultException();
        pageNotFoundAjaxResultException.setResponse(response);
		return pageNotFoundAjaxResultException;
	}

}
