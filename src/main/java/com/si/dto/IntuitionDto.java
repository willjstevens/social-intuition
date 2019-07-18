/*
 * Property of Will Stevens
 * All rights reserved.
 */
package com.si.dto;

import com.si.entity.Intuition;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wstevens
 */
public class IntuitionDto
{
    private Intuition intuition;
    private boolean isOwner;
    private boolean isInteractive;
    private boolean isActive;
    private boolean isCorrect;
    private boolean canVote;
    private boolean canContributeOutcome;
    private boolean canMakeSocialContributions;
    private OutcomeDto cohortVotedOutcomeDto;
    private LikeDto selfLikeDto;
    private List<LikeDto> likeDtos = new ArrayList<>();
    private List<LikeDto> guestLikeDtos = new ArrayList<>();
    private List<CommentDto> commentDtos = new ArrayList<>();
    private List<OutcomeDto> potentialOutcomeDtos = new ArrayList<>();
    private OutcomeDto outcomeDto;
    private String postPrettyTimestamp;
    private String expirationPrettyTimestamp;
    private String postTimestamp;
    private String expirationTimestamp;

    public IntuitionDto(Intuition intuition, boolean isOwner, String postPrettyTimestamp, String expirationPrettyTimestamp) {
        this.intuition = intuition;
        this.isOwner = isOwner;
        this.postPrettyTimestamp = postPrettyTimestamp;
        this.expirationPrettyTimestamp = expirationPrettyTimestamp;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setIsCorrect(boolean isCorrect) {
        this.isCorrect = isCorrect;
    }

    public boolean isInteractive() {
        return isInteractive;
    }

    public void setInteractive(boolean isInteractive) {
        this.isInteractive = isInteractive;
    }

    public void addLikeDto(LikeDto likeDto) {
        likeDtos.add(likeDto);
    }

    public void addCommentDto(CommentDto commentDto) {
        commentDtos.add(commentDto);
    }

    public void addPotentialOutcomeDto(OutcomeDto potentialOutcomeDto) {
        potentialOutcomeDtos.add(potentialOutcomeDto);
    }

    public void setOutcomeDto(OutcomeDto outcomeDto) {
        this.outcomeDto = outcomeDto;
    }

    public void setSelfLikeDto(LikeDto selfLikeDto) {
        this.selfLikeDto = selfLikeDto;
    }

    public Intuition getIntuition() {
        return intuition;
    }

    public void setIntuition(Intuition intuition) {
        this.intuition = intuition;
    }

    public boolean isOwner() {
        return isOwner;
    }

    public void setOwner(boolean isOwner) {
        this.isOwner = isOwner;
    }

    public String getPostPrettyTimestamp() {
        return postPrettyTimestamp;
    }

    public void setPostPrettyTimestamp(String postPrettyTimestamp) {
        this.postPrettyTimestamp = postPrettyTimestamp;
    }

    public LikeDto getSelfLikeDto() {
        return selfLikeDto;
    }

    public List<LikeDto> getLikeDtos() {
        return likeDtos;
    }

    public void setLikeDtos(List<LikeDto> likeDtos) {
        this.likeDtos = likeDtos;
    }

    public List<CommentDto> getCommentDtos() {
        return commentDtos;
    }

    public void setCommentDtos(List<CommentDto> commentDtos) {
        this.commentDtos = commentDtos;
    }

    public List<OutcomeDto> getPotentialOutcomeDtos() {
        return potentialOutcomeDtos;
    }

    public void setPotentialOutcomeDtos(List<OutcomeDto> potentialOutcomeDtos) {
        this.potentialOutcomeDtos = potentialOutcomeDtos;
    }

    public OutcomeDto getOutcomeDto() {
        return outcomeDto;
    }

    public String getExpirationPrettyTimestamp() {
        return expirationPrettyTimestamp;
    }

    public OutcomeDto getCohortVotedOutcomeDto() {
        return cohortVotedOutcomeDto;
    }

    public void setCohortVotedOutcomeDto(OutcomeDto cohortVotedOutcomeDto) {
        this.cohortVotedOutcomeDto = cohortVotedOutcomeDto;
    }

    public boolean isCanVote() {
        return canVote;
    }

    public void setCanVote(boolean canVote) {
        this.canVote = canVote;
    }

    public void setExpirationPrettyTimestamp(String expirationPrettyTimestamp) {
        this.expirationPrettyTimestamp = expirationPrettyTimestamp;
    }

    public boolean isCanContributeOutcome() {
        return canContributeOutcome;
    }

    public void setCanContributeOutcome(boolean canContributeOutcome) {
        this.canContributeOutcome = canContributeOutcome;
    }

    public boolean isCanMakeSocialContributions() {
        return canMakeSocialContributions;
    }

    public void setCanMakeSocialContributions(boolean canMakeSocialContributions) {
        this.canMakeSocialContributions = canMakeSocialContributions;
    }

    public String getPostTimestamp() {
        return postTimestamp;
    }

    public void setPostTimestamp(String postTimestamp) {
        this.postTimestamp = postTimestamp;
    }

    public String getExpirationTimestamp() {
        return expirationTimestamp;
    }

    public void setExpirationTimestamp(String expirationTimestamp) {
        this.expirationTimestamp = expirationTimestamp;
    }

    public List<LikeDto> getGuestLikeDtos() {
        return guestLikeDtos;
    }

    public void setGuestLikeDtos(List<LikeDto> guestLikeDtos) {
        this.guestLikeDtos = guestLikeDtos;
    }

    public void addGuestLikeDto(LikeDto guestLikeDto) {
        guestLikeDtos.add(guestLikeDto);
    }
}
