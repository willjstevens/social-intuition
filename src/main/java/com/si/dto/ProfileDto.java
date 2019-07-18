
/*
 * Property of Will Stevens
 * All rights reserved.
 */
package com.si.dto;

/**
 * @author wstevens
 */
public class ProfileDto
{
    private UserDto userDto = new UserDto();
    private ScoreDto scoreDto;
    private boolean isOwner;
    private boolean isCohort;
    private boolean hasSession;
    private boolean isCohortRequestSent;
    private boolean showCohortButtonSection;

    public boolean isShowCohortButtonSection() {
        return showCohortButtonSection;
    }

    public void setShowCohortButtonSection(boolean showCohortButtonSection) {
        this.showCohortButtonSection = showCohortButtonSection;
    }

    public boolean isCohortRequestSent() {
        return isCohortRequestSent;
    }

    public void setCohortRequestSent(boolean isCohortRequestSent) {
        this.isCohortRequestSent = isCohortRequestSent;
    }

    public boolean isHasSession() {
        return hasSession;
    }

    public void setHasSession(boolean hasSession) {
        this.hasSession = hasSession;
    }

    public UserDto getUserDto() {
        return userDto;
    }

    public void setUserDto(UserDto userDto) {
        this.userDto = userDto;
    }

    public boolean isOwner() {
        return isOwner;
    }

    public void setOwner(boolean isOwner) {
        this.isOwner = isOwner;
    }

    public ScoreDto getScoreDto() {
        return scoreDto;
    }

    public void setScoreDto(ScoreDto scoreDto) {
        this.scoreDto = scoreDto;
    }

    public void setIsOwner(boolean isOwner) {
        this.isOwner = isOwner;
    }

    public boolean isCohort() {
        return isCohort;
    }

    public void setIsCohort(boolean isCohort) {
        this.isCohort = isCohort;
    }

    public void setIsCohortRequestSent(boolean isCohortRequestSent) {
        this.isCohortRequestSent = isCohortRequestSent;
    }
}
