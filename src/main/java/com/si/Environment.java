package com.si;

/**
 * Variables and interfacing with outside container and environment.  Variables sourced from
 * here should be kept from a minimum and typically used to bootstrap the application; after
 * which they should be sourced from something like a database.
 *  
 * @author wstevens
 */
public interface Environment 
{
	/**
	 * Contains the boostrapping log level; typically loaded from an environment variable.
	 * 
	 * @return String The log level.
	 */
	public String getInitLogLevel();
	
	/**
	 * Contains the database configuration string which contains the host, port, database name, 
	 * username and password.  The DAO config should reference this and parse it into meaningful 
	 * pieces.  This is NOT meant to be a database connection/URL string. Example:
	 * 
	 * 		postgres://acme:acne123@localhost:5432/acme
	 * 
	 * @return String The configuration string
	 */
	public String getDatabaseConfig();
	
	/**
	 * The unique instance of a container's Spring application running in the cloud.
	 * @return
	 */
	public String getInstanceId();
	
	public void setInstanceId(String instanceId);
	public void setInitLogLevel(String initLogLevel);
	public void setDatabaseConfig(String databaseConfig);
}