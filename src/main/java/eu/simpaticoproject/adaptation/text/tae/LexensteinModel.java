package eu.simpaticoproject.adaptation.text.tae;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by alessio on 25/05/15.
 */

@Deprecated
public class LexensteinModel {

    private static LexensteinModel instance;
    private Map<String, String> lemmaList;
    private static final Logger LOGGER = LoggerFactory.getLogger(LexensteinModel.class);

    private LexensteinModel(String listFile) {
        LOGGER.trace("Loading model for Lexenstein");
        lemmaList = new HashMap<>();

        if (listFile != null) {
            try {
                List<String> lines = Files.readLines(new File(listFile), Charsets.UTF_8);
                for (String line : lines) {
                    line = line.trim();
                    if (line.startsWith("#")) {
                        continue;
                    }
                    if (line.length() == 0) {
                        continue;
                    }

                    String[] parts = line.split("\t+");

                    switch (parts.length) {
                    case 1:
                        lemmaList.put(parts[0], null);
                        break;
                    case 2:
                        lemmaList.put(parts[0], parts[1]);
                        break;
                    default:
                        LOGGER.warn("The line '{}' has too many tabs", line);
                    }

//                    System.out.println(line);
//                    System.out.println(parts.length);
//                    System.out.println();

//                    lemmaList.add(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static LexensteinModel getInstance(String listFile) {
        if (instance == null) {
            instance = new LexensteinModel(listFile);
        }

        return instance;
    }

    public Map<String, String> getLemmaList() {
        return lemmaList;
    }
}
