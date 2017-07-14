package eu.simpaticoproject.adaptation.text.util;

import eu.fbk.utils.core.CommandLine;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by alessio on 09/05/17.
 */

public class ExtractSyntacticSimplifiedSentences {

    private static Set<String> syntTypes = new HashSet<>();

    static {
        syntTypes.add("1");
        syntTypes.add("2");
        syntTypes.add("3");
        syntTypes.add("11");
        syntTypes.add("12");
        syntTypes.add("13");
        syntTypes.add("21");
        syntTypes.add("22");
        syntTypes.add("23");
        syntTypes.add("33");
        syntTypes.add("36");
        syntTypes.add("37");
    }

    public static void main(String[] args) {

        try {

            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("command")
                    .withHeader("Description of the command")
                    .withOption("i", "input-path", "Excel CSV file", "FILE",
                            CommandLine.Type.DIRECTORY_EXISTING, true, false, true)
                    .withOption("o", "output-path", "output file", "FILE",
                            CommandLine.Type.FILE, true, false, true)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            final File inputPath = cmd.getOptionValue("i", File.class);
            final File outputPath = cmd.getOptionValue("o", File.class);

            BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath));
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(inputPath), "ISO-8859-1"));
            Iterable<CSVRecord> records = CSVFormat.newFormat(';').parse(in);
            for (CSVRecord record : records) {
                String text = record.get(0);
                boolean isSyntactic = false;
                for (int i = 2; i < record.size(); i++) {
                    String id = record.get(i);
                    id = id.trim();
                    if (syntTypes.contains(id)) {
                        isSyntactic = true;
                    }
                }
                if (isSyntactic) {
                    writer.append("1");
                }
                else {
                    writer.append("0");
                }
                writer.append("\t").append(text).append("\n");
//                System.out.println(text);
            }

            in.close();
            writer.close();
        } catch (Exception e) {
            CommandLine.fail(e);
        }

    }
}
