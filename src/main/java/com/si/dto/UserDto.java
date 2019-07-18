
/*
 * Property of Will Stevens
 * All rights reserved.
 */
package com.si.dto;

import com.si.entity.User;

/**
 * @author wstevens
 */
public class UserDto
{
    private User user;
    private boolean isCohort;

    public boolean isCohort() {
        return isCohort;
    }

    public void setCohort(boolean isCohort) {
        this.isCohort = isCohort;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
