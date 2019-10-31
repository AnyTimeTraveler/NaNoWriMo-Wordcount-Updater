package org.simonscode.nanowrimotracker.wordcounter;

import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class WordFileCounter implements IWordcounter {
    @Override
    public String getName() {
        return "Word Document";
    }

    @Override
    public List<String> getFileExtensions() {
        return Arrays.asList("doc", "docx", "ods");
    }

    @Override
    public boolean matches(File path) {
        return path.getName().endsWith(".doc")
                || path.getName().endsWith(".docx")
                || path.getName().endsWith(".ods");
    }

    @Override
    public int getWordcount(File file) throws Exception {
        BodyContentHandler handler = new BodyContentHandler(-1);
        new AutoDetectParser().parse(new FileInputStream(file), handler, new org.apache.tika.metadata.Metadata(), new ParseContext());
        final String trimmedText = handler.toString().trim();
        if (trimmedText.isEmpty()) {
            return 0;
        }
        return trimmedText.split("\\s+|/").length;
    }

    @Override
    public int getWordcount(File path, String secondarySelection) {
        throw new RuntimeException("Not implemented!");
    }

    @Override
    public boolean needsSecondarySelection() {
        return false;
    }

    @Override
    public Map<String, String> getSecondarySelectionOptions(File path) {
        return null;
    }
}
