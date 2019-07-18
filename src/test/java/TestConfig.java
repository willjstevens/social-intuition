import com.si.Category;
import com.si.CloudEnvironment;
import com.si.Environment;
import com.si.log.Level;
import com.si.log.LogManager;
import com.si.log.Logger;
import com.si.service.ConfigurationService;
import com.si.service.UtilityService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

/**
 * A configuration which is imported by all tier configurations (web, service, dao); this allows 
 * for common loading when breaking tiers apart for unit tests.
 * 
 * @author wstevens
 */
@Configuration
public class TestConfig
{
	private static final Logger logger = LogManager.manager().newLogger(TestConfig.class, Category.FRAMEWORK);
	private static final String ENV_VAR_LOG_INITLEVEL 	= "si_log_initlevel";
	private static final String ENV_VAR_DB_CONFIG		= "si_db_config_test";


	@Bean
	public Environment environment() {
		Environment environment = new CloudEnvironment();
		
		final String initLogLevel = System.getenv(ENV_VAR_LOG_INITLEVEL);
		logger.info("Init log level config %s is set to \"%s\"", ENV_VAR_LOG_INITLEVEL, initLogLevel);
		if (initLogLevel != null) {
			environment.setInitLogLevel(initLogLevel.toUpperCase());
			LogManager.manager().setLevel(Level.toLogLevel(environment.getInitLogLevel()));
		} else {
			System.out.println(String.format("Could NOT find critical initLogLevel value for configuration %s.", ENV_VAR_LOG_INITLEVEL));
		}
		
		final String databaseConfig = System.getenv(ENV_VAR_DB_CONFIG);
		if (logger.isFine()) {
			logger.fine("Init database config %s is set to \"%s\"", ENV_VAR_DB_CONFIG, databaseConfig);
		}
		if (databaseConfig != null) {
			environment.setDatabaseConfig(databaseConfig);
		} else {
			System.out.println(String.format("Could NOT find critical database config value for configuration %s.", ENV_VAR_DB_CONFIG));
		}
		
		String instanceId = UUID.randomUUID().toString();
		environment.setInstanceId(instanceId);
		if (logger.isInfo()) {
			logger.info("Container instance ID is %s.", instanceId);
		}
		return environment;
	}


	@Bean
	public UtilityService utilityService() {
		return new UtilityService();
	}

	@Bean
	public ConfigurationService configurationService() {
		return new ConfigurationService();
	}
}
