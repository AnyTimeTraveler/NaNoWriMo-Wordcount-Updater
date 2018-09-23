package org.simonscode.nanowrimotracker;

import org.simonscode.nanowrimotracker.wordcounter.IWordcounter;

import java.io.File;

public class WordcountChecker extends Thread {

    private boolean running = true;
    private final IWordcounter wordcounter;
    private boolean previousUnchanged = false;

    WordcountChecker(IWordcounter wordcounter) {
        this.wordcounter = wordcounter;
    }

    public void run() {
        while (running) {
            try {
                int wordcount;
                if (wordcounter.needsSecondarySelection()) {
                    wordcount = wordcounter.getWordcount(new File(Config.get().projectLocation), Config.get().secondarySelection);
                } else {
                    wordcount = wordcounter.getWordcount(new File(Config.get().projectLocation));
                }
                if (Config.get().currentWordcount != wordcount || !previousUnchanged) {
                    previousUnchanged = Config.get().currentWordcount == wordcount;
                    NaNoWriMoTracker.updateWordcount(Config.get().currentWordcount, wordcount);

                    if (Config.get().serverSelection != 2 && Config.get().currentWordcount != wordcount) {
                        NanoAPI.updateCount(Config.get().officialUsername, Config.get().officialSecretKey, wordcount);
                    }
                    Config.get().currentWordcount = wordcount;
                    Config.get().save();
                }
            } catch (Exception e) {
                e.printStackTrace();
                //TODO
                NaNoWriMoTracker.log("ERRor");
            }
            try {
                sleep(Config.get().timeUnitBetweenUpdates.toMillis(Config.get().timeBetweenUpdates));
            } catch (InterruptedException ignored) {
            }
        }
    }

    /**
     * Stops the thread gracefully.
     */
    void stopRunning() {
        running = false;
        interrupt();
        try {
            while (this.isAlive()) {
                sleep(10);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
