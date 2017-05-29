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
package eu.simpaticoproject.adaptation.text;

import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.naming.OperationNotSupportedException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import eu.fbk.dh.tint.runner.TintPipeline;
import eu.fbk.dh.tint.runner.outputters.JSONOutputter;
import eu.fbk.dkm.pikes.twm.MachineLinking;
import eu.fbk.utils.core.PropertiesUtils;

/**
 * @author raman
 *
 */
@Component
public class Handler {

	@Value("${config.file}")
	private String config;
	
	@Value("${tae.mode.proxy.enabled}")
	private Boolean modeProxy = false;

	@Value("${tae.mode.proxy.endpoint}")
	private String proxyEndpoint = null;
	
	@Autowired 
	private ApplicationContext applicationContext;
	
    static Logger LOGGER = Logger.getLogger(Handler.class.getName());
    protected Properties itProps, enProps, esProps, allProps;

    protected static Set<String> supportedLanguages = Stream.of("it", "en", "es")
            .collect(Collectors.toCollection(HashSet::new));
    protected MachineLinking machineLinking;
	
	protected RestTemplate rest = new RestTemplate();

	@PostConstruct 
	public void init() throws IOException {
		if (modeProxy) return;
		
		Resource res = applicationContext.getResource(config);
		allProps = new Properties();
		if (res != null) {
			allProps.load(res.getInputStream());
		}
		
        enProps = PropertiesUtils.dotConvertedProperties(allProps, "en");
        itProps = PropertiesUtils.dotConvertedProperties(allProps, "it");
        esProps = PropertiesUtils.dotConvertedProperties(allProps, "es");

        LOGGER.info("Loading English pipeline");
//        StanfordCoreNLP enPipeline = new StanfordCoreNLP(enProps);

        LOGGER.info("Loading Spanish pipeline");
        StanfordCoreNLP esPipeline = new StanfordCoreNLP(esProps);

        LOGGER.info("Loading Italian pipeline");
        TintPipeline itPipeline = new TintPipeline();
        try {
            itPipeline.loadDefaultProperties();
            itPipeline.addProperties(itProps);
        } catch (IOException e) {
            e.printStackTrace();
        }
        itPipeline.load();

        Properties mlProperties = new Properties();
        mlProperties.setProperty("address", allProps.getProperty("ml_address"));
        mlProperties.setProperty("min_confidence", allProps.getProperty("ml_min_confidence"));
        machineLinking = new MachineLinking(mlProperties);
	}
	
	public String service(String lang, String text, Boolean doLex) throws Exception {
		if (modeProxy) {
			return rest.getForObject(proxyEndpoint+"?lang={lang}&doLex={doLex}&text={text}", String.class, lang, doLex, text);
		} else {
			return serviceLocal(lang, text, doLex);
		}
	}
	private String serviceLocal(String lang, String text, Boolean doLex) throws Exception {
        LOGGER.debug("Starting service");
//        request.setCharacterEncoding("UTF-8");
//        response.setCharacterEncoding("UTF-8");

        Annotation annotation = null;
        doLex = false;

        StanfordCoreNLP enPipeline = new StanfordCoreNLP(enProps);
        StanfordCoreNLP esPipeline = new StanfordCoreNLP(esProps);
        TintPipeline itPipeline = new TintPipeline();
        try {
            itPipeline.loadDefaultProperties();
            itPipeline.addProperties(itProps);
            String annotators = itPipeline.getProperty("annotators");
            if (doLex && !annotators.contains("lexenstein")) {
                itPipeline.setProperty("annotators", itProps.getProperty("annotators") + ", lexenstein");
                System.out.println("Annotators: " + itPipeline.getProperty("annotators"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        itPipeline.load();

        if (lang == null || !supportedLanguages.contains(lang)) {
            lang = machineLinking.lang(text);
        }

        switch (lang) {
        case "it":
            annotation = itPipeline.runRaw(text);
            break;
        case "es":
            annotation = new Annotation(text);
            esPipeline.annotate(annotation);
            break;
        case "en":
            annotation = new Annotation(text);
            enPipeline.annotate(annotation);
            break;
        }

        String json = "";
        if (annotation == null) {
        	throw new OperationNotSupportedException();
        } else {
            json = JSONOutputter.jsonPrint(annotation);
        }
        return json;
    }
	
}
