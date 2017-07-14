package eu.simpaticoproject.adaptation.text.tae;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import eu.fbk.dkm.pikes.twm.LinkingTag;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.languagetool.rules.DemoRule;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

import java.util.*;

/**
 * Created by alessio on 10/01/17.
 */

public class SimpaticoOutput {

    SimpaticoReadability readability;
    String docDate;
    String timings;
    List<Linking> linkings;
    List<Simplification> simplifications;
    List<String> sentenceTexts;
    List<String> sentences;

    String simplifiedText;

    public SimpaticoReadability getReadability() {
        return readability;
    }

    public void setReadability(SimpaticoReadability readability) {
        this.readability = readability;
    }

    public String getDocDate() {
        return docDate;
    }

    public void setDocDate(String docDate) {
        this.docDate = docDate;
    }

    public String getTimings() {
        return timings;
    }

    public void setTimings(String timings) {
        this.timings = timings;
    }

    public List<Linking> getLinkings() {
        return linkings;
    }

    public void setLinkings(List<Linking> linkings) {
        this.linkings = linkings;
    }

    public List<Simplification> getSimplifications() {
        return simplifications;
    }

    public void setSimplifications(List<Simplification> simplifications) {
        this.simplifications = simplifications;
    }

    public List<String> getSentenceTexts() {
        return sentenceTexts;
    }

    public void setSentenceTexts(List<String> sentenceTexts) {
        this.sentenceTexts = sentenceTexts;
    }

    public List<String> getSentences() {
        return sentences;
    }

    public void setSentences(List<String> sentences) {
        this.sentences = sentences;
    }

    public String getSimplifiedText() {
        return simplifiedText;
    }

    public void setSimplifiedText(String simplifiedText) {
        this.simplifiedText = simplifiedText;
    }

    public static class Simplification extends RawSimplification {

        public Simplification() {
            super(0, 0, null);
        }

        public Simplification(int start, int end, String simplification) {
            super(start, end, simplification);
        }

    }

    public static class SimpaticoReadability {

        private String language = null;
        private int contentWordSize = 0, contentEasyWordSize = 0, wordCount = 0;
        private int docLenWithSpaces = 0, docLenWithoutSpaces = 0, docLenLettersOnly = 0;
        private int sentenceCount = 0, tokenCount = 0;
        private int hyphenCount = 0;
        private int hyphenWordCount = 0;

        protected Map<String, Double> measures = new HashMap<>();
        protected Map<String, String> labels = new HashMap<>();
        protected Map<String, Object> forms = new HashMap<>();

        protected HashMap<String, String> genericPosDescription = new HashMap<>();
        protected HashMap<String, String> posDescription = new HashMap<>();

        Set<Integer> tooLongSentences = new HashSet<>();
        Stats posStats = new Stats();
        Stats genericPosStats = new Stats();

        public Map<String, Object> getForms() {
            return forms;
        }

        public void setForms(Map<String, Object> forms) {
            this.forms = forms;
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        public int getContentWordSize() {
            return contentWordSize;
        }

        public void setContentWordSize(int contentWordSize) {
            this.contentWordSize = contentWordSize;
        }

        public int getContentEasyWordSize() {
            return contentEasyWordSize;
        }

        public void setContentEasyWordSize(int contentEasyWordSize) {
            this.contentEasyWordSize = contentEasyWordSize;
        }

        public int getWordCount() {
            return wordCount;
        }

        public void setWordCount(int wordCount) {
            this.wordCount = wordCount;
        }

        public int getDocLenWithSpaces() {
            return docLenWithSpaces;
        }

        public void setDocLenWithSpaces(int docLenWithSpaces) {
            this.docLenWithSpaces = docLenWithSpaces;
        }

        public int getDocLenWithoutSpaces() {
            return docLenWithoutSpaces;
        }

        public void setDocLenWithoutSpaces(int docLenWithoutSpaces) {
            this.docLenWithoutSpaces = docLenWithoutSpaces;
        }

        public int getDocLenLettersOnly() {
            return docLenLettersOnly;
        }

        public void setDocLenLettersOnly(int docLenLettersOnly) {
            this.docLenLettersOnly = docLenLettersOnly;
        }

        public int getSentenceCount() {
            return sentenceCount;
        }

        public void setSentenceCount(int sentenceCount) {
            this.sentenceCount = sentenceCount;
        }

        public int getTokenCount() {
            return tokenCount;
        }

        public void setTokenCount(int tokenCount) {
            this.tokenCount = tokenCount;
        }

        public int getHyphenCount() {
            return hyphenCount;
        }

        public void setHyphenCount(int hyphenCount) {
            this.hyphenCount = hyphenCount;
        }

        public int getHyphenWordCount() {
            return hyphenWordCount;
        }

        public void setHyphenWordCount(int hyphenWordCount) {
            this.hyphenWordCount = hyphenWordCount;
        }

        public Map<String, Double> getMeasures() {
            return measures;
        }

        public void setMeasures(Map<String, Double> measures) {
            this.measures = measures;
        }

        public HashMap<String, String> getGenericPosDescription() {
            return genericPosDescription;
        }

        public void setGenericPosDescription(HashMap<String, String> genericPosDescription) {
            this.genericPosDescription = genericPosDescription;
        }

        public HashMap<String, String> getPosDescription() {
            return posDescription;
        }

        public void setPosDescription(HashMap<String, String> posDescription) {
            this.posDescription = posDescription;
        }

        public Set<Integer> getTooLongSentences() {
            return tooLongSentences;
        }

        public void setTooLongSentences(Set<Integer> tooLongSentences) {
            this.tooLongSentences = tooLongSentences;
        }

        public Stats getPosStats() {
            return posStats;
        }

        public void setPosStats(Stats posStats) {
            this.posStats = posStats;
        }

        public Stats getGenericPosStats() {
            return genericPosStats;
        }

        public void setGenericPosStats(Stats genericPosStats) {
            this.genericPosStats = genericPosStats;
        }

    }

    public static class Stats {

        private HashMap<String, Integer> support = new HashMap<String, Integer>();

        public HashMap<String, Integer> getSupport() {
            return support;
        }

        public void setSupport(HashMap<String, Integer> support) {
            this.support = support;
        }
    }

    public static class Linking extends LinkingTag {

        private static final long serialVersionUID = 5329649530750133527L;

        public Linking() {
            super(0, null, 0, null, 0, null);
        }

        public Linking(int offset, String page, double score, String originalText, int length, String source) {
            super(offset, page, score, originalText, length, source);
        }

    }

    @JsonIgnoreProperties({ "rule", "context" })
    public static class SimpRuleMatch extends RuleMatch {

        public SimpRuleMatch() {
            super(new DemoRule(), 0, 1, "");
        }

        public SimpRuleMatch(Rule rule, int fromPos, int toPos, String message, String shortMessage,
                boolean startWithUppercase, String suggestionsOutMsg) {
            super(rule, fromPos, toPos, message, shortMessage, startWithUppercase, suggestionsOutMsg);
        }

        public SimpRuleMatch(Rule rule, int fromPos, int toPos, String message, String shortMessage) {
            super(rule, fromPos, toPos, message, shortMessage);
        }

        public SimpRuleMatch(Rule rule, int fromPos, int toPos, String message) {
            super(rule, fromPos, toPos, message);
        }

        private int offset, length;

        public int getOffset() {
            return offset;
        }

        public void setOffset(int offset) {
            this.offset = offset;
        }

        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            this.length = length;
        }

        public void setRule(Rule rule) {

        }
    }

    public static void main(String[] args) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        // configure mapper, if necessary, then create schema generator
        JsonSchemaGenerator schemaGen = new JsonSchemaGenerator(mapper);
        JsonSchema schema = schemaGen.generateSchema(SimpaticoOutput.class);
        System.err.println(mapper.writeValueAsString(schema));
    }
}
