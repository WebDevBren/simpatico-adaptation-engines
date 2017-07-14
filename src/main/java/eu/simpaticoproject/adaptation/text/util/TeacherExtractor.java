package eu.simpaticoproject.adaptation.text.util;

import eu.fbk.utils.core.CommandLine;
import eu.fbk.utils.core.diff_match_patch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by alessio on 07/05/17.
 */

public class TeacherExtractor {

    private static final Logger LOGGER = LoggerFactory.getLogger(TeacherExtractor.class);
    private static Pattern frasePattern = Pattern.compile("<frase\\s+id\\s*=\\s*\"([0-9]+)\"\\s*frase_all\\s*=\\s*\"([0-9;]+)\"\\s*>([^<]+)</frase>");
    private static Set<String> typesToRemove = new HashSet<>();

    static {
        typesToRemove.add("SOST_SYN");
        typesToRemove.add("SOST_PER");
        typesToRemove.add("SOST_PER-");
        typesToRemove.add("SOST_CONNECTIVE_DISCOURSE");
        typesToRemove.add("SOST_CLAUSE");
        typesToRemove.add("SOST_CLAUSE-");
        typesToRemove.add("SOST_MULTIWORD");
        typesToRemove.add("SOST_MULTIWORD-");
        typesToRemove.add("DELETE_X");
    }

    private static diff_match_patch diffMatchPatch = new diff_match_patch();

    public static void main(String[] args) {
        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("command")
                    .withHeader("Description of the command")
                    .withOption("i", "input-path", "input Teacher folder", "FILE",
                            CommandLine.Type.DIRECTORY_EXISTING, true, false, true)
                    .withOption("o", "output-path", "output file", "FILE",
                            CommandLine.Type.FILE, true, false, true)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            final File inputPath = cmd.getOptionValue("i", File.class);
            final File outputPath = cmd.getOptionValue("o", File.class);

            BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath));

            for (File file : inputPath.listFiles()) {
                if (!file.getAbsolutePath().endsWith(".ann")) {
                    continue;
                }
//                if (!file.getAbsolutePath().equals("/Users/alessio/Desktop/CORPORA-PISA/Teacher/1_anna_frank_last_senza_ann.ann")) {
//                    continue;
//                }

                File txtFile = new File(file.getAbsolutePath().substring(0, file.getAbsolutePath().length() - 4) + ".txt");
                List<String> lines;

                HashMap<Integer, Integer> okOffsets = new HashMap<>();

                lines = Files.readAllLines(file.toPath());
                for (String line : lines) {
                    line = line.trim();
                    if (!line.startsWith("T")) {
                        continue;
                    }

                    String[] parts = line.split("\t");
                    if (parts.length < 3) {
                        continue;
                    }

                    String[] partsType = parts[1].split("\\s+");
                    if (partsType.length < 3) {
                        continue;
                    }

                    String type = partsType[0];
                    if (typesToRemove.contains(type)) {
                        continue;
                    }

                    okOffsets.put(Integer.parseInt(partsType[1]), Integer.parseInt(partsType[2]));
//                    System.out.println(type);
                }

                HashMap<String, String> before = new HashMap<>();
                HashMap<String, String> after = new HashMap<>();
                HashMap<String, String[]> beforeAll = new HashMap<>();
                HashMap<String, String[]> afterAll = new HashMap<>();
                HashMap<String, Integer> offsets = new HashMap<>();

                boolean useAfter = false;

                int offset = 0;
                lines = Files.readAllLines(txtFile.toPath());
                for (String line : lines) {
                    Matcher matcher = frasePattern.matcher(line);
                    if (matcher.find()) {
                        String idFrase = matcher.group(1);
                        String[] idFraseAll = matcher.group(2).split(";");
                        String text = matcher.group(3);
                        int thisOffset = matcher.start(3) + offset;

                        if (idFrase.equals("1") && before.size() > 0) {
                            useAfter = true;
                        }

                        if (useAfter) {
                            after.put(idFrase, text);
                            afterAll.put(idFrase, idFraseAll);
                        } else {
                            before.put(idFrase, text);
                            beforeAll.put(idFrase, idFraseAll);
                            offsets.put(idFrase, thisOffset);
                        }

//                        System.out.println(idFrase);
//                        System.out.println(Arrays.toString(idFraseAll));
//                        System.out.println(text);
//                        System.out.println(thisOffset);
//                        System.out.println();
                    }
                    offset += line.length() + 1;
                }

                Set<String> beforeAlreadyDone = new HashSet<>();
                for (String s : before.keySet()) {

                    if (beforeAlreadyDone.contains(s)) {
                        continue;
                    }

                    String b = before.get(s);
                    String[] afters = beforeAll.get(s);

                    String a = null;
                    if (afters.length > 1) {
                        StringBuffer aTmp = new StringBuffer();
                        for (String af : afters) {
                            aTmp.append(after.get(af)).append(" ");
                        }
                        a = aTmp.toString().trim();
                    } else {
                        String[] befores = afterAll.get(afters[0]);
                        if (befores == null) {
                            System.out.println(file.getAbsolutePath());
                            System.out.println(s);
                            System.out.println(afterAll);
                            System.out.println();
                        }
                        if (befores.length > 1) {
                            StringBuffer bTmp = new StringBuffer();
                            for (String bf : befores) {
                                bTmp.append(before.get(bf)).append(" ");
                                beforeAlreadyDone.add(bf);
                            }
                            b = bTmp.toString().trim();
                        }
                        a = after.get(afters[0]);
                    }

                    a = a.trim();
                    b = b.trim();

                    StringBuilder bb = new StringBuilder();
                    StringBuilder ab = new StringBuilder();

                    LinkedList<diff_match_patch.Diff> diffs = diffMatchPatch.diff_main(b, a);
                    diffMatchPatch.diff_cleanupSemantic(diffs);
                    for (diff_match_patch.Diff diff : diffs) {
                        switch (diff.operation) {
                        case EQUAL:
                            bb.append(diff.text);
                            ab.append(diff.text);
                            break;
                        case INSERT:
                            ab.append("<ins>");
                            ab.append(diff.text);
                            ab.append("</ins>");
                            break;
                        case DELETE:
                            bb.append("<del>");
                            bb.append(diff.text);
                            bb.append("</del>");
                            break;
                        }
                    }

                    writer.append(bb.toString());
                    writer.append("\t");
                    writer.append(ab.toString());
                    writer.append("\n");
                }

//                System.out.println(before);
//                System.out.println(after);
//                System.out.println(beforeAll);
//                System.out.println(afterAll);
//                System.out.println(offsets);

//                String xmlText = new String(Files.readAllBytes(txtFile.toPath()));
//                System.out.println(xmlText);
//                System.out.println(file.getAbsolutePath());
//                System.out.println(txtFile.getAbsolutePath());
            }

            writer.close();

        } catch (Exception e) {
            CommandLine.fail(e);
        }

    }
}
