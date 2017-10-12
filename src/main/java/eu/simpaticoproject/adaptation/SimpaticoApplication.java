package eu.simpaticoproject.adaptation;

import java.net.UnknownHostException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.filter.CharacterEncodingFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;

import eu.simpaticoproject.adaptation.workflow.storage.RepositoryManager;

@SpringBootApplication
@Configuration
public class SimpaticoApplication {

	@Value("${db.name}")
	private String dbName;
	
	@Value("${db.host}")
	private String dbHost;

	@Value("${db.port}")
	private int dbPort;
	
	@Value("${db.username}")
	private String dbUsername;
	
	@Value("${db.password}")
	private String dbPassword;
	
	public String generateDbUrl() {
		String dbUrl = "";
		if( StringUtils.isNotEmpty(dbUsername) && StringUtils.isNotEmpty(dbPassword) ) {
			String dbUrl = String.format("mongodb://%s:%s@%s:%s/%s", dbUsername, dbPassword, dbHost, dbPort, dbName);
		} else {
			String dbUrl = String.format("mongodb://%s:%s/%s", dbHost, dbPort, dbName);
		}
		
		return dbUrl;
	}
	
	public static void main(String[] args) {
		SpringApplication.run(SimpaticoApplication.class, args);
	}
	
	@Bean
	public FilterRegistrationBean filterRegistrationBean() {
	    FilterRegistrationBean registrationBean = new FilterRegistrationBean();
	    CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
	    characterEncodingFilter.setForceEncoding(true);
	    characterEncodingFilter.setEncoding("UTF-8");
	    registrationBean.setFilter(characterEncodingFilter);
	    return registrationBean;
	}
	
	@Bean
	public MongoTemplate getMongo() throws UnknownHostException, MongoException {
		return new MongoTemplate(new MongoClient(generateDbUrl());
	}
	
	@Bean
	RepositoryManager getRepositoryManager() throws UnknownHostException, MongoException {
		return new RepositoryManager(getMongo());
	}

	@Bean
	public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
	    ObjectMapper mapper = new ObjectMapper();
	    mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
	    MappingJackson2HttpMessageConverter converter = 
	        new MappingJackson2HttpMessageConverter(mapper);
	    return converter;
	}
}
