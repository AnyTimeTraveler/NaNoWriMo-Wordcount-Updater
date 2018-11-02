package org.simonscode.nanowrimotracker;

import org.simonscode.nanowrimotracker.wordcounter.*;

public class NaNoWriMoTracker {
    private static Storage storage = Storage.get();
    private static WordcountChecker wordcountChecker;
    private static final LogWindow logWindow = new LogWindow();
    private static final SettingsFrame settingsFrame = new SettingsFrame();
    private static final SystemTrayManager trayManager = new SystemTrayManager();

    public static void main(String[] args) {
        // check if storage is sane
        if (!storage.firstRun && configIsSufficient()) {
            // find correct wordcounter
            IWordcounter currentWC = getSelectedWordcounter();
            // if wordcounter is found, everything is in order, otherwise treat as firstrun
            if (currentWC != null) {
                // set the index of the start of the new session
                storage.wordCountIndexAtSessionStart = storage.wordCountAmounts.size() < 2 ? 0 : storage.wordCountAmounts.size() - 2;
                logWindow.setVisible(true);
                settingsFrame.setVisible(false);
                settingsFrame.hideWelcomeTab();
                wordcountChecker = new WordcountChecker(currentWC);
                wordcountChecker.start();
            } else {
                storage.firstRun = true;
                storage.save();
            }
        }
        // if firstrun then show settings first
        if (storage.firstRun) {
            logWindow.setVisible(false);
            settingsFrame.setVisible(true);
        }
    }

    static boolean configIsSufficient() {
        return !storage.projectLocation.isEmpty()
                && !storage.projectType.isEmpty()
                && storage.timeUnitBetweenUpdates != null
                && storage.timeBetweenUpdates != 0;
    }

    /**
     * @return the selected wordcounter or null
     */
    private static IWordcounter getSelectedWordcounter() {
        IWordcounter selectedWC = null;
        for (IWordcounter wc : getWordcounters()) {
            if (wc.getName().equals(Storage.get().projectType)) {
                selectedWC = wc;
                break;
            }
        }
        return selectedWC;
    }


    public static LogWindow getLogWindow() {
        return logWindow;
    }

    public static SettingsFrame getSettingsFrame() {
        return settingsFrame;
    }

    public static SystemTrayManager getTrayManager() {
        return trayManager;
    }

    /**
     * Shut the entire application down gracefully.
     */
    static void shutdown() {
        (new Thread(() -> {
            logWindow.log("Shutting down...\n");
            storage.save();
            trayManager.close();
            logWindow.log("Good bye!\n\n\n");
            System.exit(0);
        })).start();
    }

    /**
     * Reload the storage values into the wordcounter and restart it.
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
            logWindow.log("Something went wrong, please check the project location in the settings.\n");
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
