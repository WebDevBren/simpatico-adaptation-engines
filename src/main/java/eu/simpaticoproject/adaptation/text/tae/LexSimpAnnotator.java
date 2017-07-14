package eu.simpaticoproject.adaptation.text.tae;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.util.ArraySet;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.dh.tint.readability.ReadabilityAnnotations;
import eu.fbk.utils.core.PropertiesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;

/**
 * Created by alessio on 19/12/16.
 */

public class LexSimpAnnotator implements Annotator {

    private static final Logger LOGGER = LoggerFactory.getLogger(LexSimpAnnotator.class);
    private static final int DEFAULT_MIN_DIFF = 3;
    private static final int DEFAULT_DIFF = 4;
    private static final int DEFAULT_PORT = 8012;
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_LANGUAGE = "en";

    private String host;
    private Integer port;
    private Integer minDiff;
    private String language;
    private Integer position;

    public LexSimpAnnotator(String annotatorName, Properties props) {
        Properties localProperties = PropertiesUtils.dotConvertedProperties(props, annotatorName);
        this.host = localProperties.getProperty("host", DEFAULT_HOST);
        this.port = PropertiesUtils.getInteger(localProperties.getProperty("port"), DEFAULT_PORT);
        this.language = localProperties.getProperty("language", DEFAULT_LANGUAGE);
        this.minDiff = PropertiesUtils.getInteger(localProperties.getProperty("min_difficult"), DEFAULT_MIN_DIFF);
        this.position = PropertiesUtils.getInteger(localProperties.getProperty("offset"), -1);
    }

    @Override public void annotate(Annotation annotation) {

        List<RawSimplification> simplificationList = new ArrayList<>();

        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        for (CoreMap sentence : sentences) {
            Map<Integer, String> contentWords = new HashMap<>();
            StringBuffer buffer = new StringBuffer();
            List<CoreLabel> get = sentence.get(CoreAnnotations.TokensAnnotation.class);
            for (int i = 0; i < get.size(); i++) {
                CoreLabel token = get.get(i);
                String rawText = token.originalText().replace("\\s+", "_");
                buffer.append(rawText);
                buffer.append(" ");
                Boolean contentWord = token.get(ReadabilityAnnotations.ContentWord.class);
                Integer difficult = token.get(ReadabilityAnnotations.DifficultyLevelAnnotation.class);
                if (difficult == null) {
                    difficult = DEFAULT_DIFF;
                }

                if (this.position >= 0) {
                    Integer start = token.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
                    if (start.equals(this.position)) {
                        contentWords.put(i, rawText);
                    }
                } else if (contentWord && difficult >= minDiff) {
                    contentWords.put(i, rawText);
                }
            }
            String sentenceText = buffer.toString().trim();

            for (Integer contentWord : contentWords.keySet()) {
                Map<String, String> pars = new HashMap<>();
                pars.put("sentence", sentenceText);
                pars.put("target", contentWords.get(contentWord));
                pars.put("index", Integer.toString(contentWord));
                pars.put("lang", this.language);

                String simplifiedVersion;
                try {
                    simplifiedVersion = request(pars);
                } catch (IOException e) {
                    LOGGER.error(e.getMessage());
                    continue;
                }

                LOGGER.debug(simplifiedVersion);

                CoreLabel token = sentence.get(CoreAnnotations.TokensAnnotation.class).get(contentWord);
                RawSimplification simplification = new RawSimplification(
                        token.beginPosition(),
                        token.endPosition(),
                        simplifiedVersion
                );
                simplification.setOriginalValue(token.word());
                simplificationList.add(simplification);
                token.set(SimpaticoAnnotations.SimplifiedAnnotation.class, simplifiedVersion);

//                System.out.println(pars);
//                System.out.println(simplifiedVersion);
            }

        }

        annotation.set(SimpaticoAnnotations.SimplificationsAnnotation.class, simplificationList);
    }

    /**
     * Returns a set of requirements for which tasks this annotator can
     * provide.  For example, the POS annotator will return "pos".
     */
    @Override public Set<Class<? extends CoreAnnotation>> requirementsSatisfied() {
        return Collections.emptySet();
    }

    /**
     * Returns the set of tasks which this annotator requires in order
     * to perform.  For example, the POS annotator will return
     * "tokenize", "ssplit".
     */
    @Override public Set<Class<? extends CoreAnnotation>> requires() {
        return Collections.unmodifiableSet(new ArraySet<>(Arrays.asList(
                CoreAnnotations.TokensAnnotation.class,
                CoreAnnotations.SentencesAnnotation.class
        )));

    }

    protected String request(Map<String, String> pars) throws IOException {
        Gson gson = new GsonBuilder().create();
        String json = gson.toJson(pars);

        Socket echoSocket = new Socket(host, port);
        PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
//        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

        out.println(json);
        String responseLine;
        StringBuffer response = new StringBuffer();
        while ((responseLine = in.readLine()) != null) {
            response.append(responseLine);
            // continue...
        }
        in.close();
        out.close();
        echoSocket.close();

//        System.out.println(json);

        return response.toString();
    }
}
