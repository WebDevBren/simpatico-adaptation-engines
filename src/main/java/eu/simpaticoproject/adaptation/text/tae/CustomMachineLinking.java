package eu.simpaticoproject.adaptation.text.tae;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import eu.fbk.dkm.pikes.twm.Linking;
import eu.fbk.dkm.pikes.twm.LinkingTag;
import eu.fbk.twm.utils.analysis.HardTokenizer;
import eu.fbk.twm.utils.analysis.Token;
import eu.fbk.utils.core.PropertiesUtils;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: alessio
 * Date: 21/07/14
 * Time: 17:15
 * To change this template use File | Settings | File Templates.
 */

public class CustomMachineLinking extends Linking {

    public static final double ML_CONFIDENCE = 0.5;

    private static String LABEL = "ml-annotate";
    private Double minWeight;
    private String lang;
    private LinkingSkipModel skipModel;

    public CustomMachineLinking(Properties properties) {
        super(properties, properties.getProperty("address"));
        minWeight = PropertiesUtils.getDouble(properties.getProperty("min_confidence"), ML_CONFIDENCE);
        lang = properties.getProperty("lang", null);
        skipModel = LinkingSkipModel.getInstance(properties.getProperty("skiplist"));
    }

    @Override
    public List<LinkingTag> tag(String text) throws Exception {

        ArrayList<LinkingTag> ret = new ArrayList<>();

        try {
            Map<String, String> pars;

            pars = new HashMap<>();
            pars.put("min_weight", minWeight.toString());
            pars.put("disambiguation", "1");
            pars.put("topic", "1");
            pars.put("include_text", "0");
            pars.put("image", "1");
            pars.put("class", "1");
            pars.put("app_id", "0");
            pars.put("app_key", "0");
            pars.put("text", text);
            if (lang != null) {
                pars.put("lang", lang);
            }

            LOGGER.debug("Text length: {}", text.length());
            LOGGER.debug("Pars: {}", pars);

            HardTokenizer hardTokenizer = HardTokenizer.getInstance();
            Token[] tokens = hardTokenizer.tokenArray(text);

            Set<Integer> startIndexes = new HashSet<>();
            Set<Integer> endIndexes = new HashSet<>();
            Map<Integer, Integer> skipIndexes = new HashMap<>();

            for (Token token : tokens) {
                startIndexes.add(token.getStart());
                endIndexes.add(token.getEnd());
            }

            for (Integer startIndex : startIndexes) {
                for (String string : skipModel.getSkipList()) {
                    int endIndex = startIndex + string.length();
                    if (endIndex > text.length()) {
                        continue;
                    }
                    String myString = text.substring(startIndex, endIndex).toLowerCase();
                    if (myString.equals(string)) {
                        skipIndexes.put(startIndex, endIndex);
                    }
                }
                for (String string : skipModel.getReplaceList().keySet()) {
                    String url = skipModel.getReplaceList().get(string);
                    int endIndex = startIndex + string.length();
                    if (endIndex > text.length()) {
                        continue;
                    }
                    String myString = text.substring(startIndex, endIndex).toLowerCase();
                    if (myString.equals(string)) {
                        skipIndexes.put(startIndex, endIndex);
                        LinkingTag tag = new LinkingTag(
                                startIndex,
                                url,
                                1.0,
                                myString,
                                string.length(),
                                LABEL
                        );
                        ret.add(tag);
                    }
                }
            }

            Map<String, Object> userData;
            String output = request(pars);

            ObjectMapper mapper = new ObjectMapper();
            userData = mapper.readValue(output, Map.class);

            LinkedHashMap annotation = (LinkedHashMap) userData.get(new String("annotation"));
            if (annotation != null) {
                String lang = annotation.get("lang").toString();
                String language = (lang == null || lang.equals("en")) ? "" : lang + ".";
                ArrayList<LinkedHashMap> keywords = (ArrayList<LinkedHashMap>) annotation.get(new String("keyword"));
                if (keywords != null) {
                    for (LinkedHashMap keyword : keywords) {
                        LinkedHashMap sense = (LinkedHashMap) keyword.get("sense");
                        ArrayList dbpClass = (ArrayList) keyword.get("class");
                        ArrayList<LinkedHashMap> images = (ArrayList<LinkedHashMap>) keyword.get("image");
                        ArrayList<LinkedHashMap> ngrams = (ArrayList<LinkedHashMap>) keyword.get("ngram");

                        ngramsLoop:
                        for (LinkedHashMap ngram : ngrams) {
                            String originalText = (String) ngram.get("form");
                            LinkedHashMap span = (LinkedHashMap) ngram.get("span");

                            Integer start = (Integer) span.get("start");
                            Integer end = (Integer) span.get("end");

                            for (Integer skipStart : skipIndexes.keySet()) {
                                Integer skipEnd = skipIndexes.get(skipStart);
                                if (end >= skipStart && start <= skipEnd) {
                                    continue ngramsLoop;
                                }
                            }

                            LinkingTag tag = new LinkingTag(
                                    start,
                                    String.format("https://" + language + "wikipedia.org/wiki/%s",
                                            (String) sense.get("page")),
                                    Double.parseDouble(keyword.get("rel").toString()),
                                    originalText,
                                    end - start,
                                    LABEL
                            );

                            //todo: add to conf
                            if (images != null && images.size() > 0) {
                                try {
                                    tag.setImage(images.get(0).get("image").toString());
                                } catch (Exception e) {
                                    // ignored
                                }
                            }

                            if (extractTypes) {
                                tag.addTypesFromML(dbpClass);
                            }
                            ret.add(tag);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ret;
    }

    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.setProperty("address", "http://ml.apnetwork.it/annotate");
        properties.setProperty("min_confidence", "0.5");
        properties.setProperty("timeout", "2000");

        String fileName = args[0];

        CustomMachineLinking s = new CustomMachineLinking(properties);
        try {
            String text = Files.toString(new File(fileName), Charsets.UTF_8);
            List<LinkingTag> tags = s.tag(text);
            for (LinkingTag tag : tags) {
                System.out.println(tag);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage());
        }
    }
}
