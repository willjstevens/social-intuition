package com.si.entity;


import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author wstevens
 */
public class Score
{
    private String userId;
	private List<Intuition> ownedCorrect = new ArrayList<>();
    private List<Intuition> ownedIncorrect = new ArrayList<>();
    private List<Intuition> cohortCorrect = new ArrayList<>();
    private List<Intuition> cohortIncorrect = new ArrayList<>();

    public void addOwnedCorrect(Intuition intuition) {
        ownedCorrect.add(intuition);
    }

    public void addOwnedInorrect(Intuition intuition) {
        ownedIncorrect.add(intuition);
    }

    public void addCohortCorrect(Intuition intuition) {
        cohortCorrect.add(intuition);
    }

    public void addCohortIncorrect(Intuition intuition) {
        cohortIncorrect.add(intuition);
    }

    public List<Intuition> getOwnedCorrect() {
        return ownedCorrect;
    }

    public List<Intuition> getOwnedIncorrect() {
        return ownedIncorrect;
    }

    public List<Intuition> getCohortCorrect() {
        return cohortCorrect;
    }

    public List<Intuition> getCohortIncorrect() {
        return cohortIncorrect;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


}
