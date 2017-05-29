package eu.simpaticoproject.adaptation.text.spanish;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.util.ArrayCoreMap;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.utils.core.PropertiesUtils;
import eus.ixa.ixa.pipe.tok.Annotate;
import ixa.kaflib.KAFDocument;
import ixa.kaflib.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by alessio on 28/09/16.
 */

public class BasicAnnotator implements Annotator {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicAnnotator.class);

    Properties tokProperties = new Properties();
    SpanishModel model;
    CoreLabelTokenFactory factory = new CoreLabelTokenFactory();

    public BasicAnnotator(String annotatorName, Properties props) {
        tokProperties.setProperty("hardParagraph", "yes");
        tokProperties.setProperty("language", "es");
        tokProperties.setProperty("untokenizable", "yes");
        model = SpanishModel.getInstance(PropertiesUtils.dotConvertedProperties(props, annotatorName));
    }

    @Override public void annotate(Annotation annotation) {
        String text = annotation.get(CoreAnnotations.TextAnnotation.class);
        if (text != null) {
            InputStream is = new ByteArrayInputStream(text.getBytes());
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            Annotate annotator = new Annotate(br, tokProperties);
            KAFDocument document = new KAFDocument("es", "1");
            try {
                annotator.tokenizeToKAF(document);
            } catch (Exception e) {
                e.printStackTrace();
            }

            model.getPosAnnotator().annotatePOSToKAF(document);
            int sentNo = 0;
            for (Term term : document.getTerms()) {
                sentNo = term.getSent();
            }

//            System.out.println(document);
//            System.out.println(document.getNumSentences());

            List<CoreMap> sentences = new ArrayList<>();
            ArrayList<CoreLabel> tokens = new ArrayList<>();
            int tokenIndex = 0;

            for (int i = 1; i <= sentNo; i++) {
                int index = 0;

                CoreMap sent = new ArrayCoreMap(1);
                List<CoreLabel> sentTokens = new ArrayList<>();
                int sIndex = i - 1;

                List<Term> terms = document.getTermsBySent(i);
                for (Term term : terms) {
                    int begin = term.getWFs().get(0).getOffset();
                    int length = term.getWFs().get(term.getWFs().size() - 1).getLength();
                    CoreLabel token = factory.makeToken(term.getForm(), term.getForm(), begin, length);
                    token.setIndex(++index);
                    token.setSentIndex(sIndex);
                    token.setLemma(term.getLemma());
                    token.setTag(term.getMorphofeat());
                    sentTokens.add(token);
                }

                int begin = sentTokens.get(0).beginPosition();
                int end = sentTokens.get(sentTokens.size() - 1).endPosition();

                sent.set(CoreAnnotations.TokensAnnotation.class, sentTokens);

                sent.set(CoreAnnotations.SentenceIndexAnnotation.class, sIndex++);
                sent.set(CoreAnnotations.CharacterOffsetBeginAnnotation.class, begin);
                sent.set(CoreAnnotations.CharacterOffsetEndAnnotation.class, end);

                sent.set(CoreAnnotations.TokenBeginAnnotation.class, tokenIndex);
                tokenIndex += sentTokens.size();
                sent.set(CoreAnnotations.TokenEndAnnotation.class, tokenIndex);
                sent.set(CoreAnnotations.TextAnnotation.class, text.substring(begin, end));

                sentences.add(sent);
                tokens.addAll(sentTokens);
            }

            annotation.set(CoreAnnotations.TokensAnnotation.class, tokens);
            annotation.set(CoreAnnotations.SentencesAnnotation.class, sentences);

        }
    }

    /**
     * Returns a set of requirements for which tasks this annotator can
     * provide.  For example, the POS annotator will return "pos".
     */
    @Override public Set<Class<? extends CoreAnnotation>> requirementsSatisfied() {
        return new HashSet<>(Arrays.asList(
                CoreAnnotations.TextAnnotation.class,
                CoreAnnotations.SentencesAnnotation.class,
                CoreAnnotations.TokensAnnotation.class,
                CoreAnnotations.CharacterOffsetBeginAnnotation.class,
                CoreAnnotations.CharacterOffsetEndAnnotation.class,
                CoreAnnotations.BeforeAnnotation.class,
                CoreAnnotations.AfterAnnotation.class,
                CoreAnnotations.TokenBeginAnnotation.class,
                CoreAnnotations.TokenEndAnnotation.class,
                CoreAnnotations.PositionAnnotation.class,
                CoreAnnotations.IndexAnnotation.class,
                CoreAnnotations.OriginalTextAnnotation.class,
                CoreAnnotations.ValueAnnotation.class,
                CoreAnnotations.PartOfSpeechAnnotation.class,
                CoreAnnotations.LemmaAnnotation.class
        ));
    }

    /**
     * Returns the set of tasks which this annotator requires in order
     * to perform.  For example, the POS annotator will return
     * "tokenize", "ssplit".
     */
    @Override public Set<Class<? extends CoreAnnotation>> requires() {
        return Collections.emptySet();
    }

}
