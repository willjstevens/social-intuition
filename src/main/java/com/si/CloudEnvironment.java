package com.si;

import org.springframework.stereotype.Component;

/**
 * {@link Environment} implementation that loads variables from a typical cloud PAAS manner which is usually from 
 * system environment variables.
 *
 * @author wstevens
 */
@Component
public class CloudEnvironment implements Environment 
{
	private String initLogLevel;
	private String databaseConfig;
	private String instanceId;
	
	/* (non-Javadoc)
	 * @see com.valloc.Environment#getInitLogLevel()
	 */
	@Override
	public String getInitLogLevel() {
		return initLogLevel;
	}

	@Override
	public String getInstanceId() {
		return instanceId;
	}

	@Override
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	/* (non-Javadoc)
	 * @see com.valloc.Environment#getDatabaseConfig()
	 */
	@Override
	public String getDatabaseConfig() {
		return databaseConfig;
	}

	/* (non-Javadoc)
	 * @see com.valloc.Environment#setInitLogLevel(java.lang.String)
	 */
	@Override
	public void setInitLogLevel(String initLogLevel) {
		this.initLogLevel = initLogLevel;
	}

	/* (non-Javadoc)
	 * @see com.valloc.Environment#setDatabaseConfig(java.lang.String)
	 */
	@Override
	public void setDatabaseConfig(String databaseUrl) {
		this.databaseConfig = databaseUrl;
	}
}