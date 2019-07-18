/*
 * Property of Will Stevens
 * All rights reserved.
 */
package com.si.dto;

import com.si.entity.Like;

/**
 * @author wstevens
 */
public class LikeDto
{
    private Like like;
    private boolean isOwner;

    public LikeDto(Like like, boolean isOwner) {
        this.like = like;
        this.isOwner = isOwner;
    }

    public Like getLike() {
        return like;
    }

    public void setLike(Like like) {
        this.like = like;
    }

    public boolean isOwner() {
        return isOwner;
    }

    public void setOwner(boolean isOwner) {
        this.isOwner = isOwner;
    }
}
