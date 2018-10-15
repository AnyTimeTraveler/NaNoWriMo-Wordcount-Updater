package org.simonscode.nanowrimotracker;

import org.simonscode.nanowrimotracker.wordcounter.*;

public class NaNoWriMoTracker {
    private static Config config = Config.get();
    private static WordcountChecker wordcountChecker;
    private static final LogWindow logWindow = new LogWindow();
    private static final SettingsFrame settingsFrame = new SettingsFrame();
    private static final SystemTrayManager trayManager = new SystemTrayManager();

    public static void main(String[] args) {
        // check if config is sane
        if (!config.firstRun && configIsSane()) {
            // find correct wordcounter
            IWordcounter currentWC = getSelectedWordcounter();
            // if wordcounter is found, everything is in order, otherwise treat as firstrun
            if (currentWC != null) {
                logWindow.setVisible(true);
                settingsFrame.setVisible(false);
                settingsFrame.hideWelcomeTab();
                wordcountChecker = new WordcountChecker(currentWC);
                wordcountChecker.start();
            } else {
                config.firstRun = true;
                config.save();
            }
        }
        // if firstrun then show settings first
        if (config.firstRun) {
            logWindow.setVisible(false);
            settingsFrame.setVisible(true);
        }
    }

    static boolean configIsSane() {
        return !config.projectLocation.isEmpty()
                && !config.projectType.isEmpty()
                && config.timeUnitBetweenUpdates != null
                && config.timeBetweenUpdates != 0;
    }

    /**
     * @return the selected wordcounter or null
     */
    private static IWordcounter getSelectedWordcounter() {
        IWordcounter selectedWC = null;
        for (IWordcounter wc : getWordcounters()) {
            if (wc.getName().equals(Config.get().projectType)) {
                selectedWC = wc;
                break;
            }
        }
        return selectedWC;
    }

    /**
     * Logs a string to the logFrame
     *
     * @param line String to appear on the LogFrame. Newline needs to be added as well.
     */
    static void log(String line) {
        logWindow.log(line);
    }

    /**
     * Shut the entire application down gracefully.
     */
    static void shutdown() {
        (new Thread(() -> {
            log("Shutting down...\n");
            config.save();
            trayManager.close();
            log("Good bye!\n\n\n");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.exit(0);
        })).start();
    }

    /**
     * Reload the config values into the wordcounter and restart it.
     * Prints to log is something fails.
     */
    static void reload() {
        if (wordcountChecker != null) {
            wordcountChecker.stopRunning();
        }
        IWordcounter selectedWordcounter = getSelectedWordcounter();
        if (selectedWordcounter != null) {
            wordcountChecker = new WordcountChecker(selectedWordcounter);
            wordcountChecker.start();
        } else {
            log("Something went wrong, please check the project location in the settings.\n");
        }
    }

    static IWordcounter[] getWordcounters() {
        return new IWordcounter[]{
                new TextFileCounter(),
                new WordFileCounter(),
                new ScrivenerProjectCounter(),
                new RichTextFileCounter()
        };
    }

    static void updateWordcount(int currentWordcount, int wordcount) {
        logWindow.updateWordcount(currentWordcount, wordcount);
    }

    static void switchFromSettingsToLogWindow() {
        logWindow.setVisible(true);
        reload();
    }
    static void switchFromLogWindowToSettings() {
        wordcountChecker.stopRunning();
        wordcountChecker = null;
        settingsFrame.setVisible(true);
    }
}
