package org.simonscode.nanowrimotracker;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Storage {
    // filename;
    private static String CONFIG_FILENAME = "nanotracker-config.json";
    private static String DATA_FILENAME = "nanotracker-wordcounts.csv";
    private static SimpleDateFormat CSV_TIME = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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

    transient List<Date> wordCountTimes = new ArrayList<>();
    transient List<Integer> wordCountAmounts = new ArrayList<>();

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
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(CONFIG_FILENAME)));
            instance = new GsonBuilder().create().fromJson(reader, Storage.class);
        } catch (FileNotFoundException e) {
            if (instance == null) {
                instance = new Storage();
                instance.save();
            }
            return;
        } catch (JsonIOException | JsonSyntaxException e) {
            System.err.println("Storage file improperly formatted!");
            e.printStackTrace();
        }
        try {
            final File file = new File(DATA_FILENAME);
            if (!file.exists()) {
                return;
            }
            instance.wordCountTimes.clear();
            instance.wordCountAmounts.clear();
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            while (reader.ready()) {
                String line = reader.readLine();
                String[] parts = line.split(",");
                if (parts.length != 2) {
                    throw new RuntimeException("Improperly formatted CSV file: " + line);
                }
                instance.wordCountTimes.add(CSV_TIME.parse(parts[0]));
                instance.wordCountAmounts.add(Integer.parseInt(parts[1]));
            }
        } catch (FileNotFoundException ignored) {
            System.out.println("No previous CSV datafile found.");
        } catch (IOException e) {
            e.printStackTrace();
            if (instance.wordCountTimes.isEmpty()) {
                instance.wordCountTimes.add(new Date());
            }
            if (instance.wordCountAmounts.isEmpty()) {
                instance.wordCountAmounts.add(0);
            }
        } catch (ParseException e) {
            throw new RuntimeException("Improperly formatted date in CSV file!", e);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Improperly formatted wordcount in CSV file!", e);
        }
    }

    static Storage get() {
        if (instance == null) {
            load();
        }
        return instance;
    }

    private void save() {
        try {
            PrintWriter pw = new PrintWriter(DATA_FILENAME);
            final Iterator<Date> timesIterator = wordCountTimes.iterator();
            final Iterator<Integer> amountsIterator = wordCountAmounts.iterator();
            while (timesIterator.hasNext() && amountsIterator.hasNext()) {
                pw.print(CSV_TIME.format(timesIterator.next()));
                pw.print(',');
                pw.println(amountsIterator.next());
            }
            pw.flush();
            pw.close();
        } catch (IOException e) {
            throw new RuntimeException("Could not save CSV!", e);
        }

        String jsonConfig = new GsonBuilder().setPrettyPrinting().create().toJson(this);
        FileWriter writer;
        try {
            writer = new FileWriter(CONFIG_FILENAME);
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
