/*
 * Property of Will Stevens
 * All rights reserved.
 */
package com.si.web;

import com.si.Category;
import com.si.dto.DeviceLoginDto;
import com.si.entity.DeviceSession;
import com.si.framework.Response;
import com.si.framework.SessionManager;
import com.si.log.LogManager;
import com.si.log.Logger;
import com.si.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashSet;
import java.util.Set;

import static com.si.Constants.*;
import static com.si.UrlConstants.*;

/**
 *
 *
 * 
 * @author wstevens
 */
@Component
public class DeviceSessionInterceptor extends HandlerInterceptorAdapter
{
	private static final Logger logger = LogManager.manager().newLogger(DeviceSessionInterceptor.class, Category.INTERCEPTOR_SECURE_RESERVED);
	private static final String LOGIN_URI = REST_API_PATH + LOGIN;
	private Set<String> excludedUris = new HashSet<>();
	@Autowired private SessionManager sessionManager;
	@Autowired private AccountService accountService;

	public DeviceSessionInterceptor() {
		excludedUris.add(REST_API_PATH + SIGNUP);
		excludedUris.add(REST_API_PATH + LOGIN);
		excludedUris.add(REST_API_PATH + LOGIN_DEVICE_SESSION);
		excludedUris.add(REST_API_PATH + SEARCH_USERNAME);
		excludedUris.add(REST_API_PATH + SEARCH_EMAIL);
//		excludedUris.add(REST_API_PATH + NOTIFICATION);
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		// Default to true because all device and web requests will flow through here; only device requests (containing
		//		at least a "deviceId" header need to be analyzed for an active session; if no active session then this
		//		flag is set false
		boolean doPassRequest = true;

		// check for a device request
		final String userId = request.getHeader(USER_ID);
		final String deviceId = request.getHeader(DEVICE_ID);
		if (deviceId != null) {
			// and this is not device login request, hence, it should already have a session located with it
//			if (!excludedUris.contains(request.getRequestURI())) {
			if (!containsPath(request.getRequestURI())) {
				final HttpSession httpSession = request.getSession();
				if (sessionManager.hasSessionForDevice(userId, deviceId, httpSession)) {
					if (logger.isFiner()) {
						logger.finer("Found active device session for user ID \"%s\"");
					}
				} else {
					// first re-login with new device information
					final String httpSessionId = httpSession.getId();
					DeviceSession deviceSession = new DeviceSession(userId, deviceId, httpSessionId);
					Response<DeviceLoginDto> serviceResponse = accountService.deviceLoginByDeviceSession(deviceSession);
					// now set new httpSessionId in response
					response.setHeader(HTTP_SESSION_ID, httpSessionId);
					// continue no further
					doPassRequest = false;

					if (logger.isFiner()) {
						logger.finer("Found NO active device session for user ID \"%s\" so returning renewed HttpSessionId.");
					}
				}
			}
		}

        return doPassRequest;
	}

	private boolean containsPath(String requestUri) {
		boolean containsPath = false;
		for (String exclusionPattern : excludedUris) {
			String requestUriBase = requestUri;
			int pathVariableIndex = exclusionPattern.indexOf("/{");
			if (pathVariableIndex >= 0) {
				exclusionPattern = exclusionPattern.substring(0, pathVariableIndex);
				if (requestUri.contains("/{")) {
					requestUriBase = requestUri.substring(0, pathVariableIndex);
				}
			}

			if (exclusionPattern.contains(requestUriBase)) {
				containsPath = true;
				break;
			}
		}
		return containsPath;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
		super.postHandle(request, response, handler, modelAndView);
	}
}
