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

                // Run wordcount
                int wordcount;
                if (wordcounter.needsSecondarySelection()) {
                    wordcount = wordcounter.getWordcount(new File(Storage.get().projectLocation), Storage.get().secondarySelection);
                } else {
                    wordcount = wordcounter.getWordcount(new File(Storage.get().projectLocation));
                }

                // Get last wordcount from storage
                int lastWordcount = 0;
                if (!Storage.get().wordCountAmounts.isEmpty()) {
                    lastWordcount = Storage.get().wordCountAmounts.get(Storage.get().wordCountAmounts.size() - 1);
                }

                // If wordcount has changed, record and show it
                if (lastWordcount != wordcount || !previousUnchanged) {
                    previousUnchanged = lastWordcount == wordcount;

                    Storage.get().wordCountTimes.add(System.currentTimeMillis());
                    Storage.get().wordCountAmounts.add(wordcount);

                    // Explicitly recheck if the wordcounts are different, so as to not needlessly spam online services
                    if (lastWordcount != wordcount) {
                        switch (Storage.get().serverSelection) {
                            case OFFICIAL:
                                NanoAPI.updateCount(Storage.get().officialUsername, Storage.get().officialSecretKey, wordcount);
                                break;
                            case PRIVATE:
                                // TODO: Implement private servers
                                break;
                            case OFFLINE:
                            default:
                                // Do nothing
                                break;
                        }
                    }

                    checkAndNotifyWordgoals(wordcount);
                    Storage.get().save();
                    NaNoWriMoTracker.getLogWindow().showUpdatedWordcount(lastWordcount, wordcount);
                }
            } catch (Exception e) {
                e.printStackTrace();
                //TODO: Better error handling
                NaNoWriMoTracker.getLogWindow().log("Error: ", e.getMessage());
            }
            try {
                sleep(Storage.get().timeUnitBetweenUpdates.toMillis(Storage.get().timeBetweenUpdates));
            } catch (InterruptedException ignored) {
            }
        }
    }

    private void checkAndNotifyWordgoals(int wordcount) {
        for (WordGoal wg : Storage.get().customWordGoals) {
            if (wg.hasBeenReached(wordcount)) {
                NaNoWriMoTracker.getTrayManager().pushNotification("Goal reached!", "You have reached your goal:\n" + wg.getCompletionMessage());
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
