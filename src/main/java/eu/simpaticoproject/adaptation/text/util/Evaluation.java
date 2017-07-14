package eu.simpaticoproject.adaptation.text.util;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.dh.tint.runner.TintPipeline;
import eu.fbk.utils.core.IO;
import eu.fbk.utils.core.PropertiesUtils;
import eu.simpaticoproject.adaptation.text.tae.SimpaticoAnnotations;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by alessio on 03/02/17.
 */

public class Evaluation {

    private static final Logger LOGGER = LoggerFactory.getLogger(Evaluation.class);
    private static Pattern delPattern = Pattern.compile("([^<>]*)<del>(.*)</del>([^<>]*)");
    private static Pattern insPattern = Pattern.compile("([^<>]*)<ins>(.*)</ins>([^<>]*)");
    private static Pattern gustavoPattern = Pattern.compile("\\['(.*?)', '(.*?)', '(.*?)', '(.*?)', '(.*?)'\\]");

    public static void main(String[] args) {
        String inputFile = "/Users/alessio/Documents/scripts/simpitiki/simpitiki.xml";
        String outGustavo = "/Users/alessio/Documents/scripts/simpitiki/gustavo.txt";
        String resGustavo = null;
//        resGustavo = "/Users/alessio/Documents/scripts/simpatico-adaptation-engines/src/main/resources/gustavo-results-ranked-tn.txt";
        resGustavo = "/Users/alessio/Documents/scripts/simpatico-adaptation-engines/src/main/resources/gustavo-results-ranked-all.txt";

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(outGustavo));
            List<String> resGustavoLines = new ArrayList<>();
            if (resGustavo != null) {
                resGustavoLines = Files.readAllLines((new File(resGustavo)).toPath());
            }

            int gustavo = 0;
            int rules = 0;

            int total = 0;
            int doneGustavo = 0;
            int doneRules = 0;

            Properties itProps = new Properties();
            itProps.load(Evaluation.class.getClassLoader().getResourceAsStream("simpatico-default.props"));
            itProps = PropertiesUtils.dotConvertedProperties(itProps, "it");
            itProps.setProperty("annotators", "ita_toksent, pos, ita_morpho, ita_lemma, readability, fakesyn");
            TintPipeline itPipeline = new TintPipeline();
            itPipeline.loadDefaultProperties();
            itPipeline.addProperties(itProps);
            itPipeline.load();

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            XPathExpression expr = xpath.compile("/resource/simplifications/simplification");

            File f = new File(inputFile);
            InputStream read = IO.read(f.getAbsolutePath());
            Document doc = dBuilder.parse(read);
            doc.getDocumentElement().normalize();

            NodeList nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
            int ok = -1;
            for (int i = 0; i < nl.getLength(); i++) {
                Node item = nl.item(i);
                Element element = (Element) item;
                String type = element.getAttribute("type");
                String origin = element.getAttribute("origin");
                if (!type.equals("31")) {
                    continue;
                }
//                if (!origin.equals("itwiki")) {
//                    continue;
//                }

                Node before = (Node) xpath.evaluate("before", item, XPathConstants.NODE);
                Node after = (Node) xpath.evaluate("after", item, XPathConstants.NODE);

                String beforeText = before.getTextContent();
                Matcher delMatcher = delPattern.matcher(beforeText);
                Matcher insMatcher = insPattern.matcher(after.getTextContent());
                if (!delMatcher.find()) {
                    LOGGER.warn("No <del> found!");
                    continue;
                }
                if (!insMatcher.find()) {
                    LOGGER.warn("No <ins> found!");
                    continue;
                }

//                String rawBefore = delMatcher.group(2);
                List<String> contexts = extendToContext(delMatcher, insMatcher, beforeText);
                String beforeContext = contexts.get(0);
                String original = contexts.get(1);
                String afterContext = contexts.get(2);
                String modified = contexts.get(3);

                if (!StringUtils.isAlpha(original) || !StringUtils.isAlpha(modified)) {
                    continue;
                }

                ok++;
                total++;
                List<String> rulesGuessings = new ArrayList<>();

                int offset = beforeContext.length();
                String allText = beforeContext + original + afterContext;
                String goldText = beforeContext + modified + afterContext;
                String goldLemma = null;
                String origLemma = null;
                Annotation annotation = itPipeline.runRaw(allText);
                Annotation goldAnnotation = itPipeline.runRaw(goldText);

                for (CoreLabel token : goldAnnotation.get(CoreAnnotations.TokensAnnotation.class)) {
                    int begin = token.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
                    if (begin == offset) {
                        goldLemma = token.lemma();
                    }
                }

                List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);

                int sentenceID = -1;
                for (CoreMap sentence : sentences) {
                    int start = sentence.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
                    if (start > offset) {
                        break;
                    }
                    sentenceID++;
                }

                StringBuffer sentenceBuffer = new StringBuffer();
                int tokenID = -1;
                int k = 0;
                CoreMap mySentence = sentences.get(sentenceID);
                for (CoreLabel token : mySentence.get(CoreAnnotations.TokensAnnotation.class)) {
                    sentenceBuffer.append(token.originalText().replaceAll("\\s+", "")).append(" ");
                    int begin = token.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
                    if (begin == offset) {
                        tokenID = k;
                        String rulesString = token.get(SimpaticoAnnotations.SimplifiedAnnotation.class);
                        origLemma = token.lemma();
                        if (origLemma != null && origLemma.equals("risultare")) {
                            rulesGuessings.add("essere");
                        }
                        if (rulesString != null) {
                            String[] rulesArray = rulesString.split(",");
                            for (String s : rulesArray) {
                                rulesGuessings.add(s.trim());
                                if (rulesGuessings.size() >= 5) {
                                    break;
                                }
                            }
                        }
                    }
                    k++;
                }

                writer.append(sentenceBuffer.toString().trim()).append("\t").append(original)
                        .append("\t").append(Integer.toString(tokenID)).append("\n");

                List<String> gustavoGuessings = new ArrayList<>();
                try {
                    String resGustavoLine = resGustavoLines.get(ok);

                    Matcher matcher = gustavoPattern.matcher(resGustavoLine);
                    if (matcher.find()) {
                        gustavoGuessings.add(matcher.group(1));
                        gustavoGuessings.add(matcher.group(2));
                        gustavoGuessings.add(matcher.group(3));
                        gustavoGuessings.add(matcher.group(4));
                        gustavoGuessings.add(matcher.group(5));
                    }

                    if (rulesGuessings.size() != 0) {
                        if (gustavoGuessings.contains(modified)) {
                            gustavo++;
                        }
                        if (rulesGuessings.contains(goldLemma)) {
                            rules++;
                        }
                    }
                } catch (Exception e) {
                    // continue
                }

                if (gustavoGuessings.size() > 0) {
                    doneGustavo++;
                }
                if (rulesGuessings.size() > 0) {
                    doneRules++;
                }

//                System.out.println(beforeContext);
//                System.out.println(offset);
//                System.out.println(mySentence);
//                System.out.println(allText);
//                System.out.println(ok);
//                System.out.println(original);
//                System.out.println(origLemma);
//                System.out.println(modified);
//                System.out.println(goldLemma);
//                System.out.println(gustavoGuessings);
//                System.out.println(rulesGuessings);
//                System.out.println(resGustavoLine);
//                System.out.println();
//                System.out.println();

//                String rawAfter = insMatcher.group(1);
//
//                // Controllare che non ci siano spazi e "allungare" la parola prima e dopo
//
//                System.out.println(before.getTextContent());
//                System.out.println(delMatcher.group(1));
//                System.out.println(insMatcher.group(1));
//                System.out.println();

//                int start = delMatcher.start(1);
//                int end = delMatcher.start(1);
//
//                String context = before.getTextContent()
//                System.out.println(start);
//                System.out.println(end);
//                System.out.println(delMatcher.find());
//                System.out.println("Before: " + before.getTextContent());
//                System.out.println("After: " + after.getTextContent());
//                System.out.println();
            }

            writer.close();

            System.out.println("Total: " + total);
            System.out.println("LEXenstein: " + gustavo + "/" + doneGustavo);
            System.out.println("Rules: " + rules + "/" + doneRules);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static List<String> extendToContext(Matcher delMatcher, Matcher insMatcher, String beforeText) {
        int originalStart = delMatcher.start(2) - 5;
        int originalEnd = delMatcher.end(2) + 6;

        int off1 = 0, off2 = 0;
        int start = originalStart;
        int end = originalEnd;

        for (int i = 1; i < 10; i++) {
            char c;
            try {
                c = beforeText.charAt(start - 1);
            } catch (Exception e) {
                break;
            }
            if (!Character.isAlphabetic(c)) {
                break;
            }
            off1++;
            start--;
        }
        for (int i = 1; i < 10; i++) {
            char c;
            try {
                c = beforeText.charAt(end);
            } catch (Exception e) {
                break;
            }
            if (!Character.isAlphabetic(c)) {
                break;
            }
            off2++;
            end++;
        }

        String part1 = beforeText.substring(originalStart - off1, originalStart);
        String part2 = insMatcher.group(2).replaceAll("</?[a-zA-Z]{3}>", "");
        String part3 = beforeText.substring(originalEnd, originalEnd + off2);

        List<String> ret = new ArrayList<>();
        ret.add(beforeText.substring(0, start));
        ret.add(beforeText.substring(start, end).replaceAll("</?[a-zA-Z]{3}>", ""));
        ret.add(beforeText.substring(end));
        ret.add(part1 + part2 + part3);
        return ret;
    }

}
