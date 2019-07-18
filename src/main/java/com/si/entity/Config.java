/*
 * Property of Will Stevens
 * All rights reserved.
 */
package com.si.entity;

import com.si.Category;

/**
 * 
 *
 * 
 * @author wstevens
 */
public class Config
{
    private String id;
	private Category category;
	private String key;
	private String value;
	private String description;
	
	public Category getCategory() {
		return category;
	}
	public void setCategory(Category category) {
		this.category = category;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
