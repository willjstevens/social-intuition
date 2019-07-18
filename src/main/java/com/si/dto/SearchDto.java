/*
 * Property of Will Stevens
 * All rights reserved.
 */
package com.si.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wstevens
 */
public class SearchDto
{
    private boolean isRequestingUserLoggedIn;
    private List<UserDto> userResults = new ArrayList();

    public boolean isRequestingUserLoggedIn() {
        return isRequestingUserLoggedIn;
    }

    public void setRequestingUserLoggedIn(boolean isRequestingUserLoggedIn) {
        this.isRequestingUserLoggedIn = isRequestingUserLoggedIn;
    }

    public List<UserDto> getUserResults() {
        return userResults;
    }

    public void setUserResults(List<UserDto> userResults) {
        this.userResults = userResults;
    }
}
