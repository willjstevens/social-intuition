/*
 * Property of Will Stevens
 * All rights reserved.
 */
package com.si.web;

import com.si.Category;
import com.si.UrlBuilder;
import com.si.framework.Response;
import com.si.framework.Session;
import com.si.framework.SessionManager;
import com.si.log.LogManager;
import com.si.log.Logger;
import com.si.service.AccountService;
import com.si.service.ConfigurationService;
import com.si.service.UtilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static com.si.Constants.LOGIN_PROCESS_FLAG;
import static com.si.Constants.TARGET_URL;
import static com.si.UrlConstants.LOGIN;
import static com.si.UrlConstants.TEMPLATES;


/**
 * Intercepts access to select secured resources.
 *
 * 
 * @author wstevens
 */
@Component
public class SecureReservedPageInterceptor extends HandlerInterceptorAdapter
{
	private static final Logger logger = LogManager.manager().newLogger(SecureReservedPageInterceptor.class, Category.INTERCEPTOR_SECURE_RESERVED);
    private static final int TEMPLATES_URL_LENGTH = TEMPLATES.length();
	@Autowired private AccountService accountService;
	@Autowired private ConfigurationService configurationService;
    @Autowired private UtilityService utilityService;
	@Autowired private SessionManager sessionManager;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		boolean doPassRequest = false;
        if (logger.isFiner()) {
            logger.finer("Handling a secured page intercept for request URI: %s", request.getRequestURI());
        }
		HttpSession httpSession = request.getSession();
		// First check if we are in some login phase for session establishment.
		boolean isWithinLoginProcess = httpSession.getAttribute(LOGIN_PROCESS_FLAG) != null;
		if (isWithinLoginProcess) {
			if (logger.isFiner()) {
				logger.finer("Within login process so sending request along.");
			}
			// If so implicitly pass all login requests on for validation and login.
			return super.preHandle(request, response, handler);
		}

		//	first check if session is present for user on server, then he is already logged in
		boolean isLoggedIn = sessionManager.isWebSessionPresent(httpSession);
		if (!isLoggedIn) {
			// otherwise if not logged in, try and sign-in via cookie
			Cookie cookie = utilityService.findSessionCookie(request.getCookies());
			if (logger.isFiner()) {
				logger.finer("Not logged in and found session cookie (possibly null): %s.", cookie);
			}
			if (cookie != null) {
				// cookie is present, so try and sign in via session cookie
                Response<Session> serviceResult = accountService.webLoginByCookie(cookie.getValue());
				if (serviceResult.isSuccess()) {
					if (logger.isFiner()) {
						logger.finer("Found cookie and successful login by cookie.");
					}
					Session session = serviceResult.getData();
					sessionManager.commitWebSession(session, httpSession);
					isLoggedIn = true;
				} else {
					if (logger.isFiner()) {
						logger.finer("Failed login by cookie.");
					}
				}
			}
		}

		if (isLoggedIn) {
			// proceed into controllers if logged in
			if (logger.isFiner()) {
				logger.finer("Logged in so passing request.");
			}
			doPassRequest = super.preHandle(request, response, handler);
		} else {
			String targetLocation = request.getRequestURI();
			boolean isLoginRequest = targetLocation.endsWith(LOGIN);
			if (isLoginRequest) {
				// pass it to controller to serve up GET
				doPassRequest = super.preHandle(request, response, handler);
			} else if (!isLoggedIn && targetLocation.startsWith(TEMPLATES)) {
				// if user is requesting a screen needing login, and still not logged in (via session or cookie),
				// 		then redirect to login screen
				UrlBuilder urlBuilder = new UrlBuilder(configurationService);
				urlBuilder.setDomainPrefixSecure();
				urlBuilder.setSpaPrefix();
				urlBuilder.appendActionPathPart(LOGIN);
				String loginRedirectUrl = urlBuilder.buildUrl();
				// save off target URL and mark for login process
				String targetUrl = targetLocation.substring(TEMPLATES.length());
				if (logger.isFiner()) {
					logger.finer("Not logged in and setting target redirect URL to %s.", targetUrl);
				}

				httpSession.setAttribute(TARGET_URL, targetUrl);
				httpSession.setAttribute(LOGIN_PROCESS_FLAG, new Object());
				response.setHeader("Redirect-Url", LOGIN);
			}
		}

		return doPassRequest;
	}
		
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
		super.postHandle(request, response, handler, modelAndView);
	}
}
