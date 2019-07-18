/*
 * Property of Will Stevens
 * All rights reserved.
 */
package com.si.framework;

import com.si.Category;
import com.si.Constants;
import com.si.dao.ApplicationDao;
import com.si.entity.DeviceSession;
import com.si.entity.User;
import com.si.log.LogManager;
import com.si.log.Logger;
import com.si.service.AccountService;
import com.si.service.ConfigurationService;
import com.si.service.UtilityService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 *
 *
 * 
 * @author wstevens
 */
public class SessionManager
{
	private static final Logger logger = LogManager.manager().newLogger(SessionManager.class, Category.SESSION);

	private final Map<String, Session> usernameToSessionLookup = new HashMap<>();
	private final Map<String, Session> cookieToSessionLookup = new HashMap<>();
	private final Map<String, Session> httpSessionIdLookup = new HashMap<>();
    private final Map<String, DeviceSession> httpSessionIdToDeviceSessionLookup = new HashMap<>();
    private final Map<DeviceSession, Session> deviceSessionLookup = new HashMap<>();
    @Autowired private AccountService accountService;
    @Autowired private ApplicationDao applicationDao;
    @Autowired private UtilityService utilityService;
    @Autowired private ConfigurationService configurationService;

    public Session initWebSession(User user) {
        Session session = loadSession(user);

        // load web specific lookups
        cookieToSessionLookup.put(user.getCookieValue(), session);
        usernameToSessionLookup.put(user.getUsername(), session);
        if (logger.isFine()) {
            logger.fine("Initialized web session for user %s.", user);
        }

        return session;
    }

    private Session initWebGuestSession(HttpSession httpSession) {
        User user = User.newGuestUser(configurationService.getUserGuestImageUrl());
        // currently assigning the HttpSession ID as the unique user ID, for the life of a web session
        String httpSessionId = httpSession.getId();
        user.setId(httpSessionId);
        Session session = new Session(user);
        session.setHttpSession(httpSession);
        httpSessionIdLookup.put(httpSessionId, session);
        httpSession.setAttribute(Constants.SISESSION_KEY, session);
        if (logger.isFine()) {
            logger.fine("Committed guest session for HttpSession and user ID \"%s\".", httpSessionId);
        }
        return session;
    }

    private Session initWebUnidentifiedSession(HttpSession httpSession) {
        User user = User.newUnidentifiedUser();
        // currently assigning the HttpSession ID as the unique user ID, for the life of a web session
        String httpSessionId = httpSession.getId();
        user.setId(httpSessionId);
        Session session = new Session(user);
        session.setHttpSession(httpSession);
        httpSessionIdLookup.put(httpSessionId, session);
        httpSession.setAttribute(Constants.SISESSION_KEY, session);
        if (logger.isFine()) {
            logger.fine("Committed unidentified session for HttpSession and user ID \"%s\".", httpSessionId);
        }
        return session;
    }

	public void commitWebSession(Session session, HttpSession httpSession) {
		session.setHttpSession(httpSession);
		httpSessionIdLookup.put(httpSession.getId(), session);
		httpSession.setAttribute(Constants.SISESSION_KEY, session);
        session.setCohorts(applicationDao.findCohorts(session.getUser()));
		if (logger.isFine()) {
			logger.fine("Committed session for user %s.", session.getUser());
		}
	}

    public Session initDeviceSession(User user) {
        Session session = loadSession(user);
        if (logger.isFine()) {
            logger.fine("Initialized device session for user %s.", user);
        }

        return session;
    }

    public Session commitDeviceSession(Session session, DeviceSession deviceSession) {
        final String httpSessionId = deviceSession.getHttpSessionId();
        // load web specific lookups
        httpSessionIdToDeviceSessionLookup.put(httpSessionId, deviceSession);
        deviceSessionLookup.put(deviceSession, session);
        session.addDeviceSession(deviceSession);
        session.setCohorts(applicationDao.findCohorts(session.getUser()));
        if (logger.isFine()) {
            logger.fine("Initialized device session for user %s.", session.getUser());
        }

        return session;
    }

    private Session loadSession(User user) {
        final String username = user.getUsername();
        Session session = usernameToSessionLookup.get(username);
        if (session == null) {
            session = new Session(user);
            usernameToSessionLookup.put(username, session);
        }
        return session;
    }

	public Session getSessionByUser(User user) {
		return usernameToSessionLookup.get(user.getUsername());
	}

	public Session getSession(HttpSession httpSession) {
		return httpSessionIdLookup.get(httpSession.getId());
	}

    public Session getSession(HttpSession httpSession, boolean isGuest) {
        Session session = getSession(httpSession);
        // session could be null if first time in and flagged as guest
        if (isGuest && session == null) {
            session = initWebGuestSession(httpSession);
        } else if (!isGuest && session == null) {
            session = initWebUnidentifiedSession(httpSession);
        }
        return session;
    }

    public boolean hasSessionForUser(User user) {
        return getSessionByUser(user) != null;
    }

    public boolean hasSessionForWeb(HttpSession httpSession) {
        return getSession(httpSession) != null;
    }

    public boolean hasSessionForDevice(String userId, String deviceId, String sessionId) {
        DeviceSession deviceSession = new DeviceSession(userId, deviceId, sessionId);
        return deviceSessionLookup.containsKey(deviceSession);
    }

    public boolean hasSessionForDevice(String userId, String deviceId, HttpSession httpSession) {
        DeviceSession deviceSession = new DeviceSession(userId, deviceId, httpSession.getId());
        return deviceSessionLookup.containsKey(deviceSession);
    }

    public User getUserForWeb(HttpSession httpSession) {
        return getSession(httpSession).getUser();
    }

    public User getUserForWeb(HttpSession httpSession, boolean isGuest) {
        User user = null;
        Session session = getSession(httpSession, isGuest);
        if (session != null) {
            user = session.getUser();
        }
        return user;
    }

	public boolean isWebSessionPresent(HttpSession httpSession) {
        return getSession(httpSession) != null;
	}

    public void verifySessionForWeb(HttpSession httpSession) {
        if (!isWebSessionPresent(httpSession)) {
            String message = String.format("Expected web session but none found for httpSession \"%s\".", httpSession.getId());
            logger.warn(message);
            throw new IllegalArgumentException(message);
        }
    }

    public Session getSessionForDevice(String userId, String deviceId, String httpSessionId) {
        return getSessionForDevice(new DeviceSession(userId, deviceId, httpSessionId));
    }

    public Session getSessionForDevice(DeviceSession deviceSession) {
        return deviceSessionLookup.get(deviceSession);
    }

    public boolean hasDeviceSession(String httpSessionId) {
        return getDeviceSession(httpSessionId) != null;
    }

    public DeviceSession getDeviceSession(String httpSessionId) {
        return httpSessionIdToDeviceSessionLookup.get(httpSessionId);
    }

    public Session getSessionForDevice(String userId, String deviceId, HttpSession httpSession) {
        return getSessionForDevice(userId, deviceId, httpSession.getId());
    }

    public User getUserForDevice(String userId, String deviceId, String sessionId) {
        return getSessionForDevice(userId, deviceId, sessionId).getUser();
    }

    public User getUserForDevice(String userId, String deviceId, HttpSession httpSession) {
        return getSessionForDevice(userId, deviceId, httpSession.getId()).getUser();
    }

    public void verifySessionForDevice(String userId, String deviceId, String sessionId) {
        DeviceSession deviceSession = new DeviceSession(userId, deviceId, sessionId);
        Session session = deviceSessionLookup.get(deviceSession);
        if (session == null) {
            String message = String.format("Expected device session but none found for userId \"%s\", deviceId \"%s\" and httpSessionId \"%s\".", userId, deviceId, sessionId);
            logger.warn(message);
            throw new IllegalArgumentException(message);
        }
    }

    public boolean removeWebSession(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        boolean didLogout = false;

        HttpSession httpSession = httpServletRequest.getSession();
        Session session = getSession(httpSession);

        // could be null if already logged out and hitting /logout GET
        if (session != null) {
            User user = session.getUser();
            cookieToSessionLookup.remove(user.getCookieValue());
            httpSessionIdLookup.remove(httpSession.getId());
            if (logger.isFine()) {
                logger.fine("Removed web HttpSession for user %s.", user);
            }
            didLogout = true;
        }

        Cookie cookie = utilityService.findSessionCookie(httpServletRequest.getCookies());
        if (cookie != null) {
            utilityService.deleteSessionCookie(cookie);
            httpServletResponse.addCookie(cookie);
        }

        // cleanup server is session is still valid
        if (httpServletRequest.isRequestedSessionIdValid()) {
            httpSession.removeAttribute(Constants.SISESSION_KEY);
            httpSession.invalidate();
        }

        // now cleanup session object if no other devices are using it
        cleanupSession(session);

        return didLogout;
    }

    public void removeDeviceSession(DeviceSession deviceSession) {
        // remove from lookup and device session from app session
        Session session = deviceSessionLookup.remove(deviceSession);
        httpSessionIdToDeviceSessionLookup.remove(deviceSession.getHttpSessionId());
        session.removeDeviceSession(deviceSession);
        if (logger.isFine()) {
            logger.fine("Removed device Session for user %s.", session.getUser());
        }

        cleanupSession(session);
    }

    private void cleanupSession(Session session) {
        if (session != null) {
            boolean hasWebSession = false;
            boolean hasDeviceSession = false;

            // detect if web session is present
            HttpSession httpSession = session.getHttpSession();
            if (httpSession != null) {
                hasWebSession = true;
            }

            // detect if device session
            if (session.hasDeviceIds()) {
                hasDeviceSession = true;
            }

            session.resetSession();

            // only do final / full cleanup if neither exist
            if (!hasWebSession && !hasDeviceSession) {
                String username = session.getUser().getUsername();
                usernameToSessionLookup.remove(username);
                if (logger.isFine()) {
                    logger.fine("Did final session cleanup for username: " + username);
                }
            }
        }
    }

//    public String newDeviceSessionId() {
//        String sessionId = "UNASSIGNED";
//        sessionId = utilityService.newUuidString();
//        logger.fine("New device session ID generated: %s", sessionId);
//        return sessionId;
//    }

}
