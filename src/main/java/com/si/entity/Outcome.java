/*
 * Property of Will Stevens
 * All rights reserved.
 */
package com.si.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wstevens
 */
public class Outcome
{
    private String id;
    private String predictionText;
    private boolean isIntuitionOwnerContributed;
    private User contributorUser;
    private boolean isCorrect;
    private boolean wrongByExpiration;
    private List<User> outcomeVoters = new ArrayList<>();
    private List<Like> likes = new ArrayList<>();
    private List<Comment> comments = new ArrayList<>();
    private String insertTimestamp;

    public String getInsertTimestamp() {
        return insertTimestamp;
    }
    public void setInsertTimestamp(String insertTimestamp) {
        this.insertTimestamp = insertTimestamp;
    }

    public Outcome() {}

    public Outcome(String predictionText) {
        this.predictionText = predictionText;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean isCorrect) {
        this.isCorrect = isCorrect;
    }

    public boolean isWrongByExpiration() {
        return wrongByExpiration;
    }

    public void setWrongByExpiration(boolean wrongByExpiration) {
        this.wrongByExpiration = wrongByExpiration;
    }

    public String getPredictionText() {
        return predictionText;
    }

    public void setPredictionText(String predictionText) {
        this.predictionText = predictionText;
    }

    public List<Like> getLikes() {
        return likes;
    }

    public void setLikes(List<Like> likes) {
        this.likes = likes;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isIntuitionOwnerContributed() {
        return isIntuitionOwnerContributed;
    }

    public void setIntuitionOwnerContributed(boolean isIntuitionOwnerContributed) {
        this.isIntuitionOwnerContributed = isIntuitionOwnerContributed;
    }

    public User getContributorUser() {
        return contributorUser;
    }

    public void setContributorUser(User contributorUser) {
        this.contributorUser = contributorUser;
    }

    public List<User> getOutcomeVoters() {
        return outcomeVoters;
    }

    public void setOutcomeVoters(List<User> outcomeVoters) {
        this.outcomeVoters = outcomeVoters;
    }
}
