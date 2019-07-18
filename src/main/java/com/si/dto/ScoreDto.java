/*
 * Property of Will Stevens
 * All rights reserved.
 */
package com.si.dto;

import com.si.entity.Intuition;
import com.si.entity.Score;

import java.util.List;

/**
 * @author wstevens
 */
public class ScoreDto
{
    private Score score;
    private int ownedPercent;
    private int cohortPercent;
    private int totalPercent;
    private int total;
    private int totalCorrect;
    private int ownedTotal;
    private int cohortTotal;
    private List<Intuition> allOwned;
    private List<Intuition> allCohort;

    public void calculateSums() {
        ownedTotal = score.getOwnedCorrect().size() + score.getOwnedIncorrect().size();
        cohortTotal = score.getCohortCorrect().size() + score.getCohortIncorrect().size();
        totalCorrect = score.getOwnedCorrect().size() + score.getCohortCorrect().size();
        total = ownedTotal + cohortTotal;
    }

    public int getOwnedPercent() {
        return ownedPercent;
    }

    public void setOwnedPercent(int ownedPercent) {
        this.ownedPercent = ownedPercent;
    }

    public int getCohortPercent() {
        return cohortPercent;
    }

    public void setCohortPercent(int cohortPercent) {
        this.cohortPercent = cohortPercent;
    }

    public Score getScore() {
        return score;
    }

    public void setScore(Score score) {
        this.score = score;
    }

    public int getTotalPercent() {
        return totalPercent;
    }

    public void setTotalPercent(int totalPercent) {
        this.totalPercent = totalPercent;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getTotalCorrect() {
        return totalCorrect;
    }

    public void setTotalCorrect(int totalCorrect) {
        this.totalCorrect = totalCorrect;
    }

    public int getOwnedTotal() {
        return ownedTotal;
    }

    public void setOwnedTotal(int ownedTotal) {
        this.ownedTotal = ownedTotal;
    }

    public int getCohortTotal() {
        return cohortTotal;
    }

    public void setCohortTotal(int cohortTotal) {
        this.cohortTotal = cohortTotal;
    }

    public List<Intuition> getAllOwned() {
        return allOwned;
    }

    public List<Intuition> getAllCohort() {
        return allCohort;
    }

    public void setAllOwned(List<Intuition> allOwned) {
        this.allOwned = allOwned;
    }

    public void setAllCohort(List<Intuition> allCohort) {
        this.allCohort = allCohort;
    }
}
