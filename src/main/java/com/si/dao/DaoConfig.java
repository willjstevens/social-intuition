/**
 * 
 */
package com.si.dao;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.si.Category;
import com.si.Environment;
import com.si.log.LogManager;
import com.si.log.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

/**
 * A configuration to load all necessary database related beans.
 * 
 * @author wstevens
 */
@Configuration
public class DaoConfig
{
	private static final Logger logger = LogManager.manager().newLogger(DaoConfig.class, Category.DATABASE);
	@Autowired private Environment environment;
	private boolean isInitialized;

	@Bean
	public MongoTemplate mongoTemplate() throws Exception {
		return new MongoTemplate(mongoDbFactory());
	}

	@Bean
	public MongoDbFactory mongoDbFactory() {
		// 1. Yank database config string and parse
		String databaseConfig = environment.getDatabaseConfig();

//		databaseUri = new URI("mongodb://admin:4nipLvYV7KwhZVyh@SG-sip-5733.servers.mongodirector.com:27017/admin");
//		databaseConfig = "mongodb://si:si765@cluster0-shard-00-00-0bnuy.mongodb.net:27017,cluster0-shard-00-01-0bnuy.mongodb.net:27017,cluster0-shard-00-02-0bnuy.mongodb.net:27017/test?ssl=true&replicaSet=cluster0-shard-0&authSource=admin&retryWrites=true";

        logger.info("Found database string \"%s\".", databaseConfig);

        MongoClientURI uri = new MongoClientURI(databaseConfig);
		MongoClient mongoClient = new MongoClient(uri);
		MongoDbFactory mongoDbFactory = new SimpleMongoDbFactory(mongoClient, "si");

		isInitialized = true;
        logger.info("Created the MongoDbFactory: " + mongoDbFactory);
		logger.info("Successfully connected to database.");

		return mongoDbFactory;
	}

	@Bean
	public ApplicationDao applicationDao() {
		return new ApplicationDao();
	}

	public boolean isInitialized() {
		return isInitialized;
	}
}