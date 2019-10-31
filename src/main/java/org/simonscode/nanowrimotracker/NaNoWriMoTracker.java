package org.simonscode.nanowrimotracker;

import org.simonscode.nanowrimotracker.wordcounter.*;

import javax.swing.*;

public class NaNoWriMoTracker {
    private static Storage storage = Storage.get();
    private static WordcountChecker wordcountChecker;
    private static final LogWindow logWindow = new LogWindow();
    private static final SettingsFrame settingsFrame = new SettingsFrame();
    private static SystemTrayManager trayManager;

    public static void main(String[] args) {
        if (args.length == 1 && args[0].equals("--nogui")) {
            cliMode();
        } else {
            guiMode();
        }
    }

    private static void guiMode() {
        trayManager = new SystemTrayManager();
        // check if storage is sane
        if (!storage.firstRun && configIsSufficient()) {
            // find correct wordcounter
            IWordcounter currentWC = getSelectedWordcounter();
            // if wordcounter is found, everything is in order, otherwise treat as firstrun
            if (currentWC != null) {
                // set the index of the start of the new session
                storage.wordCountIndexAtSessionStart = storage.wordCountAmounts.size() - 1;
                settingsFrame.hideWelcomeTab();
                wordcountChecker = new WordcountChecker(currentWC);
                wordcountChecker.start();
            } else {
                storage.firstRun = true;
                storage.save();
            }
        }
        SwingUtilities.invokeLater(() -> logWindow.setVisible(true));
        SwingUtilities.invokeLater(() -> settingsFrame.setVisible(storage.firstRun));
    }

    private static void cliMode() {
        if (!configIsSufficient()) {
            System.err.println("Please configure the program first.\nThere should be a config file for you to edit.\nAlternatively, you can configure this software via the GUI.");
            return;
        }
        IWordcounter currentWC = getSelectedWordcounter();
        if (currentWC == null) {
            storage.firstRun = true;
            storage.save();
            System.err.println("Unable to load your project!\nPlease check the configuration of the program.\nThere should be a config file for you to edit.\nAlternatively, you can configure this software via the GUI.");
            return;
        }
        storage.wordCountIndexAtSessionStart = storage.wordCountAmounts.size() < 2 ? 0 : storage.wordCountAmounts.get(storage.wordCountAmounts.size() - 1);
        logWindow.cliMode();
        wordcountChecker = new WordcountChecker(currentWC);
        wordcountChecker.start();
        System.out.println("NaNoWriMo Tracker running!\n" +
                "Have a good writing session!\n\n" +
                "Checking progress every " + Storage.get().timeBetweenUpdates + " " + Storage.get().timeUnitBetweenUpdates.toString().toLowerCase() + ".\n");
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


    static LogWindow getLogWindow() {
        return logWindow;
    }

    public static SettingsFrame getSettingsFrame() {
        return settingsFrame;
    }

    static SystemTrayManager getTrayManager() {
        return trayManager;
    }

    /**
     * Shut the entire application down gracefully.
     */
    static void shutdown() {
        (new Thread(() -> {
            logWindow.log("Shutting down...");
            storage.save();

            trayManager.close();
            logWindow.log("Good bye!\n\n");
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
            logWindow.log("Something went wrong, please check the project location in the settings.");
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

    static void switchFromLogWindowToSettings() {
        if (wordcountChecker != null) {
            wordcountChecker.stopRunning();
        }
        wordcountChecker = null;
        SwingUtilities.invokeLater(() -> settingsFrame.setVisible(true));
    }
}
