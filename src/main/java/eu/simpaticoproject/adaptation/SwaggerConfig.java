/*******************************************************************************
 * Copyright 2015 Fondazione Bruno Kessler
 * 
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 ******************************************************************************/
package eu.simpaticoproject.adaptation;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.common.base.Predicates;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author raman
 *
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {                                    
    @Bean
    public Docket api() { 
        return new Docket(DocumentationType.SWAGGER_2)  
          .select()                                  
          	.apis(RequestHandlerSelectors.any())              
          	.paths(Predicates.not(PathSelectors.ant("/error/**")))    
          	.build()
          .apiInfo(apiInfo())
          .useDefaultResponseMessages(false)                                   
          .globalResponseMessage(RequestMethod.GET,                     
        		  Stream.of(new ResponseMessageBuilder()   
        	              .code(500)
        	              .message("Internal Processing Error")
        	              .responseModel(new ModelRef("Error"))
        	              .build(),
        	              new ResponseMessageBuilder() 
        	                .code(501)
        	                .message("Configuration not supported")
        	                .build())
                  .collect(Collectors.toCollection(ArrayList::new)));
    }
     
    private ApiInfo apiInfo() {
        ApiInfo apiInfo = new ApiInfo(
          "SIMPATICO TAE e WAE API",
          "SIMPATICO Text Adaptation Engine and Workflow Adaptation Engine REST API",
          null,
          null,
          new Contact("SCO and DH", "http://fbk.eu", null),
          "APACHE LICENSE 2.0",
          "http://www.apache.org/licenses/LICENSE-2.0");
        return apiInfo;
    }}