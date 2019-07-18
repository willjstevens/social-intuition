/*
 * Property of Will Stevens
 * All rights reserved.
 */
package com.si.web;

import com.si.Category;
import com.si.framework.Response;
import com.si.framework.Session;
import com.si.framework.SessionManager;
import com.si.log.LogManager;
import com.si.log.Logger;
import com.si.service.AccountService;
import com.si.service.UtilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 *
 * 
 * @author wstevens
 */
@Component
public class SessionRequiredInterceptor extends HandlerInterceptorAdapter
{
	private static final Logger logger = LogManager.manager().newLogger(SessionRequiredInterceptor.class, Category.INTERCEPTOR_SECURE_RESERVED);
	@Autowired private AccountService accountService;
    @Autowired private UtilityService utilityService;
	@Autowired private SessionManager sessionManager;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		HttpSession httpSession = request.getSession();

		boolean isLoggedIn = sessionManager.isWebSessionPresent(httpSession);
		// if not logged in, make an attempt
		if (!isLoggedIn) {
			// otherwise if not logged in, try and sign-in via cookie
			Cookie cookie = utilityService.findSessionCookie(request.getCookies());
			if (cookie != null) {
				// cookie is present, so try and sign in via session cookie
                Response<Session> serviceResult = accountService.webLoginByCookie(cookie.getValue());
				if (serviceResult.isSuccess()) {
					Session session = serviceResult.getData();
					sessionManager.commitWebSession(session, httpSession);
					if (logger.isFine()) {
						logger.fine("Session created for username %s", session.getUsername());
					}
                    isLoggedIn = true;
				}
			}
		}

        // set to pass request only if was logged in or attempt to login succeeded
        boolean doPassRequest = isLoggedIn;

		return doPassRequest;
	}
		
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
		super.postHandle(request, response, handler, modelAndView);
	}
}
