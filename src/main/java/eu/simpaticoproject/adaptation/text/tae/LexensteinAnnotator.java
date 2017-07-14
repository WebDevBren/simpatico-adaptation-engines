package eu.simpaticoproject.adaptation.text.tae;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by alessio on 03/11/16.
 */

@Deprecated
public class LexensteinAnnotator implements Annotator {

    private static final Logger LOGGER = LoggerFactory.getLogger(LexensteinAnnotator.class);
    int DEFAULT_PORT = 8057;

    String inputFile, outputFile, server;
    LexensteinModel lexensteinModel;
    int port;
    int MIN_LENGTH = 3;

    static Set<String> acceptgpos = Stream.of("S", "A", "V", "B").collect(Collectors.toCollection(HashSet::new));
    static Set<String> ignorepos = Stream.of("SP").collect(Collectors.toCollection(HashSet::new));

    public LexensteinAnnotator(String annotatorName, Properties props) {
        Properties selfProperties = PropertiesUtils.dotConvertedProperties(props, annotatorName);

        port = PropertiesUtils.getInteger(selfProperties.getProperty("port"), DEFAULT_PORT);
        inputFile = selfProperties.getProperty("inputFile");
        outputFile = selfProperties.getProperty("outputFile");
        server = selfProperties.getProperty("server");

        lexensteinModel = LexensteinModel.getInstance(selfProperties.getProperty("skipLemmaFile"));
    }

    @Override public void annotate(Annotation annotation) {

        LinkedHashMap<Integer, Map<Integer, String>> sentenceWords = new LinkedHashMap<>();
        List<RawSimplification> simplificationList = new ArrayList<>();

        Map<String, String> skipLemmaList = lexensteinModel.getLemmaList();

        try {

            BufferedWriter writer = new BufferedWriter(new FileWriter(inputFile));
            int sentenceID = 0;
            for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                int tokenID = 0;
                LinkedHashMap<Integer, String> words = new LinkedHashMap<>();
                StringBuffer buffer = new StringBuffer();
                for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                    String word = token.word();
                    String lemma = token.lemma();

                    buffer.append(" ").append(word);

                    String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                    String gpos = pos.substring(0, 1);

                    boolean simplify = true;

                    if (!acceptgpos.contains(gpos) || ignorepos.contains(pos)) {
                        simplify = false;
                    }
                    Integer difficultyLevel = token.get(ReadabilityAnnotations.DifficultyLevelAnnotation.class);
                    if (difficultyLevel != null && difficultyLevel < 3) {
                        simplify = false;
                    }
                    if (skipLemmaList.containsKey(lemma)) {
                        simplify = false;
                    }
                    if (skipLemmaList.containsKey(word)) {
                        simplify = false;
                    }
                    if (word.length() < MIN_LENGTH) {
                        simplify = false;
                    }
                    if (simplify) {
                        words.put(tokenID, word);
                    }
                    tokenID++;
                }
                for (Integer key : words.keySet()) {
                    writer.append(buffer.toString().trim()).append("\t");
                    writer.append(words.get(key)).append("\t");
                    writer.append(Integer.toString(key)).append("\n");
                }

                sentenceWords.put(sentenceID, words);
                sentenceID++;
            }

            writer.flush();
            writer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Call Lexenstein

        try {
            URL url = new URL(String.format("http://%s:%d", server, port));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            int code = connection.getResponseCode();

            if (code == 200) {
                List<String> simplifications = Files.readLines(new File(outputFile), Charsets.UTF_8);
                int index = 0;
                for (Integer sentenceID : sentenceWords.keySet()) {
                    for (Integer tokenID : sentenceWords.get(sentenceID).keySet()) {
                        CoreLabel token = annotation.get(CoreAnnotations.SentencesAnnotation.class).get(sentenceID)
                                .get(
                                        CoreAnnotations.TokensAnnotation.class).get(tokenID);
                        String simplifiedVersion = simplifications.get(index);
                        if (simplifiedVersion.length() > 0) {
                            RawSimplification simplification = new RawSimplification(
                                    token.beginPosition(),
                                    token.endPosition(),
                                    simplifiedVersion
                            );
                            simplification.setOriginalValue(token.word());
                            simplificationList.add(simplification);
                            token.set(SimpaticoAnnotations.SimplifiedAnnotation.class, simplifiedVersion);
                        }
                        index++;
                    }
                }

                annotation.set(SimpaticoAnnotations.SimplificationsAnnotation.class, simplificationList);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

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
                CoreAnnotations.PartOfSpeechAnnotation.class,
                CoreAnnotations.TokensAnnotation.class,
                CoreAnnotations.SentencesAnnotation.class,
                ReadabilityAnnotations.ReadabilityAnnotation.class
        )));
    }

}
