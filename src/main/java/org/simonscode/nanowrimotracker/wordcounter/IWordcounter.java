package org.simonscode.nanowrimotracker.wordcounter;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface IWordcounter {

    String getName();

    List<String> getFileExtensions();

    boolean matches(File path);

    int getWordcount(File path) throws Exception;

    int getWordcount(File path, String secondarySelection) throws Exception;

    boolean needsSecondarySelection();

    Map<String, String> getSecondarySelectionOptions(File path) throws Exception;
}
