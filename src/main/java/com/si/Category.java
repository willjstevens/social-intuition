/*
 * Property of Will Stevens
 * All rights reserved.
 */
package com.si;

/**
 *
 *
 * @author wstevens
 */
public enum Category
{
	UNINITIALIZED					("UNINITIALIZED"),
	ALL								("all"),
	CLIENT							("client"),
	CONFIGURATION					("configuration"),
	CONTROLLER						("controller"),
	CORE							("core"),
	DATABASE						("database"),
	ENVIRONMENT						("environment"),
	FILE							("file"),
	FRAMEWORK						("framework"),
	INITIALIZATION					("initialization"),
	INTERCEPTOR						("interceptor"),
	INTERCEPTOR_SECURE_RESERVED		("interceptor.secure-reserved"),
    IO                              ("io"),
	LOCALIZATION					("localization"),
	LOG								("log"),
	SECURITY						("security"),
	SECURITY_CRYPTO					("security.crypto"),
    SECURITY_OAUTH  				("security.oauth"),
	SECURITY_TRANSPORT				("security.transport"),
	SERVICE							("service"),
	SERVICE_ADMIN					("service.admin"),
	SERVICE_ACCOUNT					("service.account"),
	SERVICE_APPLICATION				("service.application"),
	SERVICE_DASHBOARD				("service.dashboard"),
	SERVICE_EMAIL					("service.email"),
	SERVICE_LIFECYCLE				("service.lifecycle"),
	SERVICE_OBJECT					("service.object"),
	SERVICE_PAGE					("service.page"),
	SERVICE_SYSTEM					("service.system"),
	SERVICE_PAGE_SERVE				("service.user-page-serve"),
	SERVICE_VALIDATION				("service.validation"),
	SESSION 						("session"),
	STATE_TRANSITION				("state-transition"),
	TEST							("test"),
	UTILITY							("utility");

	private String id;

	private Category(final String id) {
		this.id = id;
	}

	public String id() {
		return id;
	}
	
	public static Category toCategory(String categoryStr) {
		Category category = UNINITIALIZED;
		
		if (categoryStr == null || categoryStr.isEmpty()) {
			throw new IllegalArgumentException();
		}
		for (Category candidate : Category.values()) {
			if (candidate.id.equals(categoryStr)) {
				category = candidate;
				break;
			}
		}
		
		return category;
	}
}
