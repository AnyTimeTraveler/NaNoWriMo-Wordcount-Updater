package org.simonscode.nanowrimotracker;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Config {
    // Configfile name;
    private static String CONFIGFILE = "config.json";
    private static Config instance = null;

    boolean firstRun = true;

    String projectLocation = "";
    String projectType = "";
    String secondarySelection = "";

    long timeBetweenUpdates = 0L;
    TimeUnit timeUnitBetweenUpdates = TimeUnit.SECONDS;

    int serverSelection = 0; // 0: Official; 1: Private; 2: Offline
    String officialUsername = "";
    String officialSecretKey = "";
    String privateServerAddress = "";

    int currentWordcount = 0;
    int wordcountOffset = 0;
    int wordcountAtStartOfDay = 0;

    List<WordGoal> customWordGoals = new ArrayList<>();

    Map<Date, Integer> wordcounts = new HashMap<>();

    private Config() {
        customWordGoals.add(new WordGoal("First 100 words!", 100, WordGoal.Type.FIXED));
        customWordGoals.add(new WordGoal("First 1.000 words!", 1_000, WordGoal.Type.FIXED));
        customWordGoals.add(new WordGoal("First 10.000 words!", 10_000, WordGoal.Type.FIXED));
        customWordGoals.add(new WordGoal("Daily goal!", 1667, WordGoal.Type.REPEATING));
    }

    private static void load() {
        load(new File(CONFIGFILE));
    }

    private static void load(File file) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            instance = new GsonBuilder().create().fromJson(reader, Config.class);
        } catch (FileNotFoundException e) {
            if (instance == null) {
                instance = new Config();
                instance.save();
            }
        } catch (JsonIOException | JsonSyntaxException e) {
            System.err.println("Config file improperly formatted!");
            e.printStackTrace();
        }
    }

    static Config get() {
        if (instance == null) {
            load();
        }
        return instance;
    }

    void save() {
        save(new File(CONFIGFILE));
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
