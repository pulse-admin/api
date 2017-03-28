package gov.ca.emsa.pulse.broker.app;

import gov.ca.emsa.pulse.broker.auth.AcfLastAccessFilter;
import gov.ca.emsa.pulse.broker.cache.CacheCleanupJob;
import gov.ca.emsa.pulse.broker.cache.DirectoryRefreshManager;
import gov.ca.emsa.pulse.broker.manager.AlternateCareFacilityManager;
import gov.ca.emsa.pulse.broker.manager.EndpointManager;
import gov.ca.emsa.pulse.broker.manager.LocationManager;
import gov.ca.emsa.pulse.broker.manager.PatientManager;
import gov.ca.emsa.pulse.broker.manager.QueryManager;
import gov.ca.emsa.pulse.broker.manager.impl.DocumentQueryService;
import gov.ca.emsa.pulse.broker.manager.impl.DocumentRetrievalService;
import gov.ca.emsa.pulse.broker.manager.impl.PatientQueryService;

import java.util.Timer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@PropertySource("classpath:/application.properties")
@EnableTransactionManagement(proxyTargetClass=true)
@SpringBootApplication(scanBasePackages= {"gov.ca.emsa.pulse.broker.**",
		"gov.ca.emsa.pulse.common.**",
		"gov.ca.emsa.pulse.service.**"})
public class BrokerApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(BrokerApplication.class, args);
	}
	
	@Autowired private LocationManager locationManager;
	@Autowired private EndpointManager endpointManager;
	@Autowired private AlternateCareFacilityManager acfManager;
	@Autowired private PatientManager patientManager;
	@Autowired private QueryManager queryManager;
	
	@Value("${persistenceUnitName}")
	private String persistenceUnitName;
	@Value("${queryCacheExpireMinutes}")
	private String queryCacheExpireMinutes;
	@Value("${queryCacheCleanupMinutes}")
	private String queryCacheCleanupMinutes1;
	@Value("${acfCacheExpireMinutes}")
	private String acfCacheExpireMinutes;
	@Value("${acfCacheCleanupMinutes}")
	private String acfCacheCleanupMinutes1;
	@Value("${patientCacheExpireMinutes}")
	private String patientCacheExpireMinutes;
	@Value("${patientCacheCleanupMinutes}")
	private String patientCacheCleanupMinutes1;
	@Value("${directoryRefreshSeconds}")
	private String directoryRefreshSeconds;
	@Value("${locationDirectoryUrl}")
	private String locationDirectoryUrl;
	@Value("${endpointDirectoryUrl}")
	private String endpointDirectoryUrl;
	
	//TODO: the env is still null when the entity manager bean gets called and 
	//i really have no idea why. This is a short-term fix. The longer answer is that
	//spring boot really does not need the persistence.xml and the db stuff should be
	//configured in application.properties
	@Bean
	public org.springframework.orm.jpa.LocalEntityManagerFactoryBean entityManagerFactory(){
		org.springframework.orm.jpa.LocalEntityManagerFactoryBean bean = new org.springframework.orm.jpa.LocalEntityManagerFactoryBean();
		bean.setPersistenceUnitName(persistenceUnitName);
		return bean;
	}

	@Bean
	public org.springframework.orm.jpa.JpaTransactionManager transactionManager(){
		org.springframework.orm.jpa.JpaTransactionManager bean = new org.springframework.orm.jpa.JpaTransactionManager();
		bean.setEntityManagerFactory(entityManagerFactory().getObject());
		return bean;
	}

	@Bean
	public CacheCleanupJob queryCacheManager() {
		int queryCacheExpirationMinutes = new Integer(queryCacheExpireMinutes);
		long queryCacheExpirationMillis = queryCacheExpirationMinutes * 60 * 1000;
		int queryCacheCleanupMinutes = new Integer(queryCacheCleanupMinutes1);
		long queryCacheCleanupMillis = queryCacheCleanupMinutes * 60 * 1000;
		
		CacheCleanupJob task = null;

		if(queryCacheExpirationMinutes > 0) {
			task = new CacheCleanupJob();
			task.setExpirationMillis(queryCacheExpirationMillis);
			task.setManager(queryManager);
			
			Timer timer = new Timer();
			timer.scheduleAtFixedRate(task, 0, queryCacheCleanupMillis);

		}
		
		return task;
	}
	
	@Bean
	public CacheCleanupJob acfCacheManager() {
		int acfCacheExpirationMinutes = new Integer(acfCacheExpireMinutes);
		long acfCacheExpirationMillis = acfCacheExpirationMinutes * 60 * 1000;
		int acfCacheCleanupMinutes = new Integer(acfCacheCleanupMinutes1);
		long acfCacheCleanupMillis = acfCacheCleanupMinutes * 60 * 1000;
		
		CacheCleanupJob task = null;

		if(acfCacheExpirationMinutes > 0) {
			task = new CacheCleanupJob();
			task.setExpirationMillis(acfCacheExpirationMillis);
			task.setManager(acfManager);
			
			Timer timer = new Timer();
			timer.scheduleAtFixedRate(task, 0, acfCacheCleanupMillis);

		}
		
		return task;
	}
	
	@Bean
	public CacheCleanupJob patientCacheManager() {
		int patientCacheExpirationMinutes = new Integer(patientCacheExpireMinutes);
		long patientCacheExpirationMillis = patientCacheExpirationMinutes * 60 * 1000;
		int patientCacheCleanupMinutes = new Integer(patientCacheCleanupMinutes1);
		long patientCacheCleanupMillis = patientCacheCleanupMinutes * 60 * 1000;
		
		CacheCleanupJob task = null;

		if(patientCacheExpirationMinutes > 0) {
			task = new CacheCleanupJob();
			task.setExpirationMillis(patientCacheExpirationMillis);
			task.setManager(patientManager);
			
			Timer timer = new Timer();
			timer.scheduleAtFixedRate(task, 0, patientCacheCleanupMillis);

		}
		
		return task;
	}
	
	@Bean
	public DirectoryRefreshManager directoryRefreshManager() {
		int directoryRefresh = new Integer(directoryRefreshSeconds);
		long directoryRefreshExpirationMillis = directoryRefresh * 1000;
		
		DirectoryRefreshManager qcTask = null;

		if(directoryRefresh > 0) {
			qcTask = new DirectoryRefreshManager();
			qcTask.setLocationManager(locationManager);
			qcTask.setEndpointManager(endpointManager);
			qcTask.setExpirationMillis(directoryRefreshExpirationMillis);
			qcTask.setLocationDirectoryUrl(locationDirectoryUrl);
			qcTask.setEndpointDirectoryUrl(endpointDirectoryUrl);
			
			Timer timer = new Timer();
			timer.scheduleAtFixedRate(qcTask, 0, directoryRefreshExpirationMillis);

		}
		
		return qcTask;
	}
	
	   @Bean
	    public FilterRegistrationBean acfLastAccessFilter() {
		   AcfLastAccessFilter filter = new AcfLastAccessFilter();
		   filter.setAcfManager(acfManager);
	        FilterRegistrationBean registration = new FilterRegistrationBean();
	        registration.setFilter(filter);
	        registration.setOrder(2);
	        return registration;
	    }
	   
	@Bean
    @Scope(scopeName=ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public PatientQueryService patientQueryService() {
        return new PatientQueryService();
    }
	
	@Bean
    @Scope(scopeName=ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public DocumentQueryService documentQueryService() {
		return new DocumentQueryService();
	}
	
	@Bean
    @Scope(scopeName=ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public DocumentRetrievalService documentRetrievalService() {
		return new DocumentRetrievalService();
	}
}
