package org.simonscode.nanowrimotracker;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Storage {
    // filename;
    private static String FILENAME = "nanotracker-data.json";
    private static Storage instance = null;

    boolean firstRun = true;

    String projectLocation = "";
    String projectType = "";
    String secondarySelection = "";

    long timeBetweenUpdates = 0L;
    TimeUnit timeUnitBetweenUpdates = TimeUnit.SECONDS;

    ServerSelection serverSelection = ServerSelection.OFFICIAL;
    String officialUsername = "";
    String officialSecretKey = "";
    String privateServerAddress = "";

    int wordcountOffset = 0;
    int wordcountAtStartOfDay = 0;

    List<WordGoal> customWordGoals = new ArrayList<>();

    List<Date> wordCountTimes = new ArrayList<>();
    List<Integer> wordCountAmounts = new ArrayList<>();

    transient int wordCountIndexAtSessionStart = 0;

    private Storage() {
        // Add sample goals
        customWordGoals.add(new WordGoal("First 100 words!", 100, WordGoal.Type.FIXED));
        customWordGoals.add(new WordGoal("First 1.000 words!", 1_000, WordGoal.Type.FIXED));
        customWordGoals.add(new WordGoal("First 10.000 words!", 10_000, WordGoal.Type.FIXED));
        customWordGoals.add(new WordGoal("Daily goal!", 1667, WordGoal.Type.REPEATING));

        // Add initial values, so XChart doesn't crash when rendering the liveChart
        // Yes, this is actual intended behaviour, somehow
        wordCountTimes.add(new Date());
        wordCountAmounts.add(0);
    }

    private static void load() {
        load(new File(FILENAME));
    }

    private static void load(File file) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            instance = new GsonBuilder().create().fromJson(reader, Storage.class);
        } catch (FileNotFoundException e) {
            if (instance == null) {
                instance = new Storage();
                instance.save();
            }
        } catch (JsonIOException | JsonSyntaxException e) {
            System.err.println("Storage file improperly formatted!");
            e.printStackTrace();
        }
    }

    static Storage get() {
        if (instance == null) {
            load();
        }
        return instance;
    }

    void save() {
        save(new File(FILENAME));
    }

    private void save(File file) {
        String jsonConfig = new GsonBuilder().setPrettyPrinting().create().toJson(this);
        FileWriter writer;
        try {
            writer = new FileWriter(file);
            writer.write(jsonConfig);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String toString() {
        return new GsonBuilder().setPrettyPrinting().create().toJson(this);
    }
}
