/*
 * Property of Will Stevens
 * All rights reserved.
 */
package com.si.dto;

import com.si.entity.Outcome;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wstevens
 */
public class OutcomeDto
{
    private Outcome outcome;
    private boolean isOwner;
    private boolean isPredicted;
    private String postPrettyTimestamp;
    private LikeDto selfLikeDto;
    private List<LikeDto> likeDtos = new ArrayList<>();
    private List<LikeDto> guestLikeDtos = new ArrayList<>();
    private List<CommentDto> commentDtos = new ArrayList<>();

    public OutcomeDto(Outcome outcome, boolean isOwner, String postPrettyTimestamp) {
        this.outcome = outcome;
        this.isOwner = isOwner;
        this.postPrettyTimestamp = postPrettyTimestamp;
    }

    public void addLikeDto(LikeDto likeDto) {
        likeDtos.add(likeDto);
    }

    public void addCommentDto(CommentDto commentDto) {
        commentDtos.add(commentDto);
    }

    public void setSelfLikeDto(LikeDto selfLikeDto) {
        this.selfLikeDto = selfLikeDto;
    }

    public Outcome getOutcome() {
        return outcome;
    }

    public void setOutcome(Outcome outcome) {
        this.outcome = outcome;
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

    public List<LikeDto> getGuestLikeDtos() {
        return guestLikeDtos;
    }

    public void setGuestLikeDtos(List<LikeDto> guestLikeDtos) {
        this.guestLikeDtos = guestLikeDtos;
    }

    public void addGuestLikeDto(LikeDto guestLikeDto) {
        guestLikeDtos.add(guestLikeDto);
    }

    public boolean isPredicted() {
        return isPredicted;
    }

    public void setPredicted(boolean predicted) {
        isPredicted = predicted;
    }
}
