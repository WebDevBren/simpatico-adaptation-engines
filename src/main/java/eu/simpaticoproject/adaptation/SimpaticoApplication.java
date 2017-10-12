package eu.simpaticoproject.adaptation;

import java.net.UnknownHostException;

import org.apache.commons.lang.StringUtils;

import org.apache.log4j.Logger;
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
import com.mongodb.MongoClientURI;
import com.mongodb.MongoException;

import eu.simpaticoproject.adaptation.text.Handler;
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
		if( dbUsername != "" && dbPassword != "") {
			dbUrl = String.format("mongodb://%s:%s@%s:%s", dbUsername, dbPassword, dbHost, dbPort);
		} else {
			dbUrl = String.format("mongodb://%s:%s", dbHost, dbPort);
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
		String dbUrl = generateDbUrl();
		return new MongoTemplate(new MongoClient(new MongoClientURI(dbUrl)), dbName);
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
