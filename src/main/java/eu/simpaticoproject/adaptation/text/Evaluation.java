package eu.simpaticoproject.adaptation.text;

import eu.fbk.utils.core.IO;
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
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isAlpha;

/**
 * Created by alessio on 03/02/17.
 */

public class Evaluation {

    private static final Logger LOGGER = LoggerFactory.getLogger(Evaluation.class);
    private static Pattern delPattern = Pattern.compile("([^<>]*)<del>(.*)</del>([^<>]*)");
    private static Pattern insPattern = Pattern.compile("([^<>]*)<ins>(.*)</ins>([^<>]*)");

    public static void main(String[] args) {
        String inputFile = "/Users/alessio/Documents/scripts/simpitiki/simpitiki.xml";

        try {
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
            for (int i = 0; i < nl.getLength(); i++) {
                Node item = nl.item(i);
                Element element = (Element) item;
                String type = element.getAttribute("type");
                if (!type.equals("31")) {
                    continue;
                }

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
                String original = contexts.get(1);
                String modified = contexts.get(3);

                if (!isAlpha(original) || !isAlpha(modified)) {
                    continue;
                }

                System.out.println(original);
                System.out.println(modified);
                System.out.println();

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
