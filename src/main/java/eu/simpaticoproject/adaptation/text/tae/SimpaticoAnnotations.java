package eu.simpaticoproject.adaptation.text.tae;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.util.ErasureUtils;
import eu.fbk.utils.gson.JSONLabel;

import java.util.List;

/**
 * Created by giovannimoretti on 19/05/16.
 */
public class SimpaticoAnnotations {

    @JSONLabel("simplifications")
    public static class SimplificationsAnnotation implements CoreAnnotation<List<RawSimplification>> {

        public Class<List<RawSimplification>> getType() {
            return ErasureUtils.<Class<List<RawSimplification>>>uncheckedCast(List.class);
        }
    }

    @JSONLabel("simplifiedVersion")
    public static class SimplifiedAnnotation implements CoreAnnotation<String> {

        public Class<String> getType() {
            return String.class;
        }
    }

}
