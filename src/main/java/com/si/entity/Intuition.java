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
public class Intuition
{
    private String id;
    private User user;
    private String intuitionText;
    private String visibility;
    private String predictionType;
    private List<StoredImageInfo> imageInfos;
    private List<Outcome> potentialOutcomes = new ArrayList<>();
    private Outcome predictedOutcome;
    private Outcome outcome;
    private boolean scoreIntuition;
    private boolean displayPrediction;
    private boolean allowPredictedOutcomeVoting;
    private boolean allowCohortsToContributePredictedOutcomes;
    private String activeWindow;
    private List<Like> likes = new ArrayList<>();
    private List<Comment> comments = new ArrayList<>();
    private String insertTimestamp;
    private String expirationTimestamp;
    private long expirationMillis;

    public long getExpirationMillis() {
        return expirationMillis;
    }

    public void setExpirationMillis(long expirationMillis) {
        this.expirationMillis = expirationMillis;
    }

    public String getInsertTimestamp() {
        return insertTimestamp;
    }

    public void setInsertTimestamp(String insertTimestamp) {
        this.insertTimestamp = insertTimestamp;
    }

    public String getExpirationTimestamp() {
        return expirationTimestamp;
    }

    public void setExpirationTimestamp(String expirationTimestamp) {
        this.expirationTimestamp = expirationTimestamp;
    }

    public List<StoredImageInfo> getImageInfos() {
        return imageInfos;
    }

    public void setImageInfos(List<StoredImageInfo> imageInfos) {
        this.imageInfos = imageInfos;
    }

    public void addImageInfo(StoredImageInfo imageInfo) {
        if (imageInfos == null) {
            imageInfos = new ArrayList<>();
        }
        imageInfos.add(imageInfo);
    }

    public boolean hasImageInfos() {
        return imageInfos != null && !imageInfos.isEmpty();
    }

    public String getIntuitionText() {
        return intuitionText;
    }

    public void setIntuitionText(String intuitionText) {
        this.intuitionText = intuitionText;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public String getPredictionType() {
        return predictionType;
    }

    public void setPredictionType(String predictionType) {
        this.predictionType = predictionType;
    }

    public Outcome getOutcome() {
        return outcome;
    }

    public void setOutcome(Outcome outcome) {
        this.outcome = outcome;
    }

    public boolean doScoreIntuition() {
        return scoreIntuition;
    }

    public void setScoreIntuition(boolean scoreIntuition) {
        this.scoreIntuition = scoreIntuition;
    }

    public boolean isDisplayPrediction() {
        return displayPrediction;
    }

    public void setDisplayPrediction(boolean displayPrediction) {
        this.displayPrediction = displayPrediction;
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

    public String getActiveWindow() {
        return activeWindow;
    }

    public void setActiveWindow(String activeWindow) {
        this.activeWindow = activeWindow;
    }

    public List<Outcome> getPotentialOutcomes() {
        return potentialOutcomes;
    }

    public void setPotentialOutcomes(List<Outcome> potentialOutcomes) {
        this.potentialOutcomes = potentialOutcomes;
    }

    public Outcome getPredictedOutcome() {
        return predictedOutcome;
    }

    public void setPredictedOutcome(Outcome predictedOutcome) {
        this.predictedOutcome = predictedOutcome;
    }

    public boolean isAllowPredictedOutcomeVoting() {
        return allowPredictedOutcomeVoting;
    }

    public void setAllowPredictedOutcomeVoting(boolean allowPredictedOutcomeVoting) {
        this.allowPredictedOutcomeVoting = allowPredictedOutcomeVoting;
    }

    public boolean isAllowCohortsToContributePredictedOutcomes() {
        return allowCohortsToContributePredictedOutcomes;
    }

    public void setAllowCohortsToContributePredictedOutcomes(boolean allowCohortsToContributePredictedOutcomes) {
        this.allowCohortsToContributePredictedOutcomes = allowCohortsToContributePredictedOutcomes;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
