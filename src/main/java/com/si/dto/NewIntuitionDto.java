/*
 * Property of Will Stevens
 * All rights reserved.
 */
package com.si.dto;

import com.si.entity.ActiveWindow;
import com.si.entity.Outcome;

import java.util.List;

/**
 * @author wstevens
 */
public class NewIntuitionDto
{
    private String defaultVisibility;
    private String defaultPredictionType;
    private List<ActiveWindow> activeWindows;
    private List<Outcome> predictionChoicesYesNo;
    private List<Outcome> predictionChoicesTrueFalse;
    private boolean scoreIntuition;
    private boolean displayPrediction;
    private boolean displayCohortsPredictions;
    private boolean allowPredictedOutcomeVoting;
    private boolean allowCohortsToContributePredictedOutcomes;

    public List<ActiveWindow> getActiveWindows() {
        return activeWindows;
    }

    public void setActiveWindows(List<ActiveWindow> activeWindows) {
        this.activeWindows = activeWindows;
    }

    public String getDefaultVisibility() {
        return defaultVisibility;
    }

    public void setDefaultVisibility(String defaultVisibility) {
        this.defaultVisibility = defaultVisibility;
    }

    public String getDefaultPredictionType() {
        return defaultPredictionType;
    }

    public void setDefaultPredictionType(String defaultPredictionType) {
        this.defaultPredictionType = defaultPredictionType;
    }

    public List<Outcome> getPredictionChoicesYesNo() {
        return predictionChoicesYesNo;
    }

    public void setPredictionChoicesYesNo(List<Outcome> predictionChoicesYesNo) {
        this.predictionChoicesYesNo = predictionChoicesYesNo;
    }

    public List<Outcome> getPredictionChoicesTrueFalse() {
        return predictionChoicesTrueFalse;
    }

    public void setPredictionChoicesTrueFalse(List<Outcome> predictionChoicesTrueFalse) {
        this.predictionChoicesTrueFalse = predictionChoicesTrueFalse;
    }

    public boolean isDisplayPrediction() {
        return displayPrediction;
    }

    public void setDisplayPrediction(boolean displayPrediction) {
        this.displayPrediction = displayPrediction;
    }

    public boolean isDisplayCohortsPredictions() {
        return displayCohortsPredictions;
    }

    public void setDisplayCohortsPredictions(boolean displayCohortsPredictions) {
        this.displayCohortsPredictions = displayCohortsPredictions;
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

    public boolean isScoreIntuition() {
        return scoreIntuition;
    }

    public void setScoreIntuition(boolean scoreIntuition) {
        this.scoreIntuition = scoreIntuition;
    }
}
