package eu.simpaticoproject.adaptation.text.tae;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.util.ArraySet;
import eu.fbk.dh.tint.readability.DescriptionForm;
import eu.fbk.dh.tint.readability.GlossarioEntry;
import eu.fbk.dh.tint.readability.it.ItalianReadability;
import eu.fbk.utils.core.PropertiesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by alessio on 19/12/16.
 */

public class LexSimpAnnotator implements Annotator {

    private static final Logger LOGGER = LoggerFactory.getLogger(LexSimpAnnotator.class);

    public LexSimpAnnotator(String annotatorName, Properties props) {
//        Properties globalProperties = props;
//        Properties localProperties = PropertiesUtils.dotConvertedProperties(props, annotatorName);
//        this.onlyOne = PropertiesUtils.getBoolean(localProperties.getProperty("only_one", "false"), false);
//        this.skipModel = SkipModel.getInstance(localProperties.getProperty("skipLemmaFile"));
//        this.model = FakeSynModel.getInstance(globalProperties, localProperties, this.skipModel);
    }

    @Override public void annotate(Annotation annotation) {

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
                CoreAnnotations.LemmaAnnotation.class,
                CoreAnnotations.SentencesAnnotation.class
        )));

    }
}
