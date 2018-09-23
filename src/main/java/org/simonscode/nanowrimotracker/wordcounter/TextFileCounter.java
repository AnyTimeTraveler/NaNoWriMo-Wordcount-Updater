package org.simonscode.nanowrimotracker.wordcounter;

import java.io.File;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TextFileCounter implements IWordcounter {
    @Override
    public String getName() {
        return "Text Document";
    }

    @Override
    public List<String> getFileExtensions() {
        return Collections.singletonList("txt");
    }

    @Override
    public boolean matches(File path) {
        return path.getName().endsWith(".txt");
    }

    @Override
    public int getWordcount(File file) throws Exception {
        return String.join("\n", Files.readAllLines(file.toPath())).trim().split("\\s+|/").length;
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
