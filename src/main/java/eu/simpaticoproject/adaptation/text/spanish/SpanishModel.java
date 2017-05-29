package eu.simpaticoproject.adaptation.text.spanish;

import eus.ixa.ixa.pipe.pos.Annotate;

import java.util.Properties;

/**
 * Created by alessio on 28/09/16.
 */

public class SpanishModel {

    private static SpanishModel ourInstance = null;

    private Annotate posAnnotator;

    public SpanishModel(Annotate posAnnotator) {
        this.posAnnotator = posAnnotator;
    }

    public Annotate getPosAnnotator() {
        return posAnnotator;
    }

    public void setPosAnnotator(Annotate posAnnotator) {
        this.posAnnotator = posAnnotator;
    }

    public static SpanishModel getInstance(Properties properties) {
        if (ourInstance == null) {

            final Properties posProperties = new Properties();
            posProperties.setProperty("model", properties.getProperty("posModel"));
            posProperties.setProperty("lemmatizerModel", properties.getProperty("lemmaModel"));
            posProperties.setProperty("language", "es");
            posProperties.setProperty("multiwords", "no");
            posProperties.setProperty("dictag", "false");

            try {
                ourInstance = new SpanishModel(new Annotate(posProperties));
            } catch (Exception e) {
                e.printStackTrace();
            }

//            Annotate annotator = new Annotate(br, tokProperties);

        }
        return ourInstance;
    }
}
