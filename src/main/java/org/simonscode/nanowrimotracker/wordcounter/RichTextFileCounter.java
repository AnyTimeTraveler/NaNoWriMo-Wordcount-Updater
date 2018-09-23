package org.simonscode.nanowrimotracker.wordcounter;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.File;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class RichTextFileCounter implements IWordcounter {
    @Override
    public String getName() {
        return "Rich Text File";
    }

    @Override
    public List<String> getFileExtensions() {
        return Collections.singletonList("rtf");
    }

    @Override
    public boolean matches(File path) {
        return path.getName().endsWith(".rtf");
    }

    @Override
    public int getWordcount(File file) throws Exception {
        return String.join("\n", Files.readAllLines(file.toPath())).trim().split("\\s+|/").length;
    }

    @Override
    public int getWordcount(File path, String secondarySelection) {
        throw new NotImplementedException();
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
