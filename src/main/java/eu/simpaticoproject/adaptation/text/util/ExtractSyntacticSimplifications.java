package eu.simpaticoproject.adaptation.text.util;

import eu.fbk.dh.tint.runner.TintPipeline;
import eu.fbk.utils.core.CommandLine;
import eu.fbk.utils.core.XMLHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by alessio on 03/10/16.
 */

public class ExtractSyntacticSimplifications {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtractSyntacticSimplifications.class);
    private static Pattern delPattern = Pattern.compile("<del>([^<]*)</del>");
    private static Set<Integer> allowedTypes = new HashSet<>();
    static {
        allowedTypes.add(1);
        allowedTypes.add(2);
        allowedTypes.add(3);
        allowedTypes.add(11);
        allowedTypes.add(12);
        allowedTypes.add(13);
        allowedTypes.add(21);
        allowedTypes.add(22);
        allowedTypes.add(23);
        allowedTypes.add(33);
        allowedTypes.add(36);
        allowedTypes.add(37);
    }

    public static void main(String[] args) {
        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("command")
                    .withHeader("Description of the command")
                    .withOption("i", "input-path", "Simpitiki XML file", "FILE",
                            CommandLine.Type.DIRECTORY_EXISTING, true, false, true)
                    .withOption("o", "output-path", "output file", "FILE",
                            CommandLine.Type.FILE, true, false, true)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            final File inputPath = cmd.getOptionValue("i", File.class);
            final File outputPath = cmd.getOptionValue("o", File.class);

            BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath));

            TintPipeline pipeline = new TintPipeline();
            pipeline.loadDefaultProperties();
            pipeline.setProperty("annotators", "ita_toksent");
            pipeline.load();

            Document document = XMLHelper.getDocument(inputPath);
            XPath xPath = XMLHelper.getXPath();
            NodeList nodeList = XMLHelper.getNodeList(document, xPath, "resource/simplifications/simplification");
            for (int nodeIndex = 0; nodeIndex < nodeList.getLength(); nodeIndex++) {
                Node simplification = nodeList.item(nodeIndex);
                String type = XMLHelper.getNodeAttr("type", simplification);
                String origin = XMLHelper.getNodeAttr("origin", simplification);
                if (!origin.equals("tn")) {
                    continue;
                }
                if (!allowedTypes.contains(Integer.parseInt(type))) {
                    continue;
                }
                Node before = XMLHelper.getNode("before", simplification.getChildNodes());
                Node after = XMLHelper.getNode("after", simplification.getChildNodes());

                String beforeText = before.getTextContent();
                String afterText = after.getTextContent();

                writer.append(beforeText);
                writer.append("\t");
                writer.append(afterText);
                writer.append("\n");

//                System.out.println(beforeText);
//                System.out.println(afterText);
//                System.out.println();
//
//                Matcher matcher = delPattern.matcher(beforeText);
//                int lastIndex = 0;
//                int offset = 0;
//                StringBuilder builder = new StringBuilder();
//                HashSet<Integer> finalOffsets = new HashSet<>();
//
//                while (matcher.find()) {
//                    int start = matcher.start();
//                    builder.append(beforeText.substring(lastIndex, start));
//                    String inside = matcher.group(1);
//                    builder.append(inside);
//                    lastIndex = matcher.end();
//                    String all = matcher.group(0);
//                    finalOffsets.add(start - offset);
//                    offset += all.length() - inside.length();
//                }
//                builder.append(beforeText.substring(lastIndex));
//                String text = builder.toString();
//
//                if (finalOffsets.size() == 0) {
//                    continue;
//                }
//
//                String simplifiedToken = null;
//                int simplifiedTokenIndex = 0;
//                StringBuffer tokenizedString = new StringBuffer();
//
//                Annotation annotation = pipeline.runRaw(text);
//                List<CoreLabel> tokens = annotation.get(CoreAnnotations.TokensAnnotation.class);
//                for (int i = 0; i < tokens.size(); i++) {
//                    CoreLabel token = tokens.get(i);
//                    tokenizedString.append(token.word()).append(" ");
//                    int start = token.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
//                    int end = token.get(CoreAnnotations.CharacterOffsetEndAnnotation.class);
//
//                    boolean inside = true;
//                    for (Integer finalOffset : finalOffsets) {
//                        if (finalOffset < start || finalOffset >= end) {
//                            inside = false;
//                        }
//                    }
//
//                    if (inside) {
//                        simplifiedToken = token.word();
//                        simplifiedTokenIndex = i;
//                    }
//
//                }

//                if (simplifiedToken != null) {
//                    writer.append(tokenizedString.toString().trim());
//                    writer.append("\t");
//                    writer.append(simplifiedToken);
//                    writer.append("\t");
//                    writer.append(Integer.toString(simplifiedTokenIndex));
//                    writer.append("\n");
//                }

            }

            writer.close();
        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }
}
