/*
 * Property of Will Stevens
 * All rights reserved.
 */
package com.si.framework;

import com.si.entity.Cohort;
import com.si.entity.DeviceSession;
import com.si.entity.User;

import javax.servlet.http.HttpSession;
import java.util.*;

/**
 *
 *
 * 
 * @author wstevens
 */
public class Session {
    private User user;
    private HttpSession httpSession;
    private Set<DeviceSession> deviceSessions = new HashSet();
    private List<Cohort> cohorts = new ArrayList<>();
    private List<String> cohortIds = new ArrayList<>();
    private Map<String, String> usernameToCohortLookup = new HashMap();

    public Session() { /* for stub usage */ }

    Session(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getUsername() {
        return user.getUsername();
    }

    public HttpSession getHttpSession() {
        return httpSession;
    }

    public void setHttpSession(HttpSession httpSession) {
        this.httpSession = httpSession;
    }

    public void addDeviceSession(DeviceSession deviceSession) {
        deviceSessions.add(deviceSession);
    }

    public void removeDeviceSession(DeviceSession deviceSession) {
        deviceSessions.remove(deviceSession);
    }

    public Set<DeviceSession> getDeviceSessions() {
        return deviceSessions;
    }

//    public DeviceSession getDeviceSession(String deviceId) {
//        return deviceSessions.stream()
//                .filter(deviceSession -> deviceSession.getDeviceId().equals(deviceId))
//                .findFirst()
//                .get();
//    }

    public boolean hasDeviceIds() {
        return !deviceSessions.isEmpty();
    }

    public List<Cohort> getCohorts() {
        return cohorts;
    }

    public void setCohorts(List<Cohort> cohorts) {
        this.cohorts = cohorts;
        // reset cohort ID
        cohortIds.clear();
        final String thisUserId = user.getId();
        for (Cohort cohort : cohorts) {
            if (cohort.isAccepted()) {
                boolean thisUserIsInviter = thisUserId.equals(cohort.getInviterUserId());
                if (thisUserIsInviter) {
                    // if this user is the inviter then the cohort is the consenter
                    cohortIds.add(cohort.getConsenterUserId());
                    usernameToCohortLookup.put(cohort.getConsenterUsername(), cohort.getConsenterUserId());
                } else {
                    // if this user is not the inviter then he is the consenter, and the cohort is the inviter
                    cohortIds.add(cohort.getInviterUserId());
                    usernameToCohortLookup.put(cohort.getInviterUsername(), cohort.getInviterUserId());
                }
            }
        }
    }

    public boolean hasCohort(String username) {
        return usernameToCohortLookup.containsKey(username);
    }

    public String getCohortIdByUsername(String username) {
        return usernameToCohortLookup.get(username);
    }

    public List<String> getCohortIds() {
        return cohortIds;
    }

    public void resetSession() {
        cohortIds.clear();
        cohorts.clear();
    }

    @Override
    public boolean equals (Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Session session = (Session) o;

        if (!user.equals(session.user)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode () {
        return user.hashCode();
    }

    @Override
	public String toString() {
		return "Session for [username=" + user.getUsername() + "]";
	}
}
