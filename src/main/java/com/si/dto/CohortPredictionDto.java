/*
 * Property of Will Stevens
 * All rights reserved.
 */
package com.si.dto;

import com.si.entity.User;

/**
 * @author wstevens
 */
public class CohortPredictionDto
{
    private User cohort;
    private boolean isOwner;

    public CohortPredictionDto(User cohort, boolean isOwner) {
        this.cohort = cohort;
        this.isOwner = isOwner;
    }

    public User getCohort() {
        return cohort;
    }

    public void setCohort(User cohort) {
        this.cohort = cohort;
    }

    public boolean isOwner() {
        return isOwner;
    }

    public void setOwner(boolean isOwner) {
        this.isOwner = isOwner;
    }
}
