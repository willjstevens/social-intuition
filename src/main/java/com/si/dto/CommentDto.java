/*
 * Property of Will Stevens
 * All rights reserved.
 */
package com.si.dto;

import com.si.entity.Comment;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wstevens
 */
public class CommentDto
{
    private Comment comment;
    private boolean isOwner;
    private LikeDto selfLikeDto;
    private List<LikeDto> likeDtos = new ArrayList<>();
    private List<LikeDto> guestLikeDtos = new ArrayList<>();
    private String displayTimestamp;
    private String postPrettyTimestamp;

    public CommentDto(Comment comment, boolean isOwner, String postPrettyTimestamp) {
        this.comment = comment;
        this.isOwner = isOwner;
        this.postPrettyTimestamp = postPrettyTimestamp;
    }

    public void addLikeDto(LikeDto likeDto) {
        likeDtos.add(likeDto);
    }

    public void setSelfLikeDto(LikeDto selfLikeDto) {
        this.selfLikeDto = selfLikeDto;
    }

    public Comment getComment() {
        return comment;
    }

    public void setComment(Comment comment) {
        this.comment = comment;
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

    public List<LikeDto> getLikeDtos() {
        return likeDtos;
    }

    public void setLikeDtos(List<LikeDto> likeDtos) {
        this.likeDtos = likeDtos;
    }

    public LikeDto getSelfLikeDto() {
        return selfLikeDto;
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

    public String getDisplayTimestamp() {
        return displayTimestamp;
    }

    public void setDisplayTimestamp(String displayTimestamp) {
        this.displayTimestamp = displayTimestamp;
    }
}
