/*
 * Property of Will Stevens
 * All rights reserved.
 */
package com.si.web;

import com.si.Category;
import com.si.entity.DeviceSession;
import com.si.framework.SessionManager;
import com.si.log.LogManager;
import com.si.log.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 *
 *
 * 
 * @author wstevens
 */
public class WebSessionListener implements HttpSessionListener
{

    private static final Logger logger = LogManager.manager().newLogger(WebSessionListener.class, Category.SESSION);
	private static final int DEFAULT_SESSION_TIMEOUT_SECONDS = 60 * 60 * 6; // 6 hours is plenty before we cleanup resources


//	private static final int DEFAULT_SESSION_TIMEOUT_SECONDS = 360; // TODO: 5 min; get rid of test value


	private int sessionTimeoutSeconds = DEFAULT_SESSION_TIMEOUT_SECONDS;
    private ApplicationContext applicationContext;
	private SessionManager sessionManager;

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSessionListener#sessionCreated(javax.servlet.http.HttpSessionEvent)
	 */
	@Override
	public void sessionCreated(HttpSessionEvent event) {
        if (sessionManager == null) {
            sessionManager = (SessionManager) applicationContext.getBean("sessionManager");
        }

		HttpSession httpSession = event.getSession();
		httpSession.setMaxInactiveInterval(sessionTimeoutSeconds);
        if (logger.isFiner()) {
            logger.finer("HttpSession with ID \"%s\" was created with session timeout set to %d seconds.", httpSession.getId(), sessionTimeoutSeconds);
        }
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSessionListener#sessionDestroyed(javax.servlet.http.HttpSessionEvent)
	 */
	@Override
	public void sessionDestroyed(HttpSessionEvent event) {
		HttpSession httpSession = event.getSession();
		// for now only expire and cleanup device sessions
		if (sessionManager.hasDeviceSession(httpSession.getId())) {
			DeviceSession deviceSession = sessionManager.getDeviceSession(httpSession.getId());
			sessionManager.removeDeviceSession(deviceSession);
			// TODO: this needed?
			httpSession.invalidate();

			if (logger.isFiner()) {
				logger.finer("Device session with ID \"%s\" was destroyed and removed from the session manager.", httpSession.getId());
			}
		}
	}

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
	}

	public void setSessionTimeoutSeconds(int sessionTimeoutSeconds) {
		this.sessionTimeoutSeconds = sessionTimeoutSeconds;
	}
	
}
