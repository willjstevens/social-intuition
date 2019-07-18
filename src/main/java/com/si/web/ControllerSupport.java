package com.si.web;

import com.si.Category;
import com.si.framework.*;
import com.si.log.LogManager;
import com.si.log.Logger;
import com.si.service.ApplicationService;
import com.si.service.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Handles requests for the application home page.
 */
@ControllerAdvice
public class ControllerSupport
{
	private static final Logger logger = LogManager.manager().newLogger(ControllerSupport.class, Category.CONTROLLER);
	@Autowired private ConfigurationService configurationService;
	@Autowired private ApplicationService applicationService;


	@ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public String handleException(Exception e) {
		logger.error(e.getMessage(), e);
		return "ERROR";
	}

	@ExceptionHandler(ServerErrorPageResultException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public void handleServerErrorPageResultException(ServerErrorPageResultException e) {
		logger.error(e.getMessage(), e);
//        Response response = e.getResponse();
//        response.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
//        return response;
	}

	@ExceptionHandler(ServerErrorAjaxResultException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ResponseBody
	public Response handleServerErrorAjaxResultException(ServerErrorAjaxResultException e) {
		logger.error(e.getMessage(), e);
        Response response = e.getResponse();
        response.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return response;
	}

	@ExceptionHandler(BadRequestPageResultException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public void handleBadRequestPageResultException(BadRequestPageResultException e) {
		logger.warn(e.getMessage());
//        Response response = e.getResponse();
//        response.setCode(HttpStatus.BAD_REQUEST.value());
//        return response;
	}

	@ExceptionHandler(BadRequestAjaxResultException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ResponseBody
    public Response handleBadRequestAjaxResultException(BadRequestAjaxResultException e) {
		logger.warn(e.getMessage());
        Response response = e.getResponse();
        response.setCode(HttpStatus.BAD_REQUEST.value());
        return response;
	}

	@ExceptionHandler(ForbiddenPageResultException.class)
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public void handleForbiddenPageResultException(ForbiddenPageResultException e) {
        logger.warn(e.getMessage());
//        Response response = e.getResponse();
//        response.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
//        return response;
	}

	@ExceptionHandler(ForbiddenAjaxResultException.class)
	@ResponseStatus(HttpStatus.FORBIDDEN)
	@ResponseBody
	public Response handleForbiddenAjaxResultException(ForbiddenAjaxResultException e) {
		logger.warn(e.getMessage());
        Response response = e.getResponse();
        response.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return response;
	}


	@ExceptionHandler(PageNotFoundPageResultException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String handleCustomPageNotFoundPageResultException(PageNotFoundPageResultException e) {

		  return "redirect:/#/404";
	}

	@ExceptionHandler(PageNotFoundAjaxResultException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ResponseBody
	public Response handlePageNotFoundAjaxResultException(PageNotFoundAjaxResultException e) {
        Response response = e.getResponse();
        response.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return response;
	}

}