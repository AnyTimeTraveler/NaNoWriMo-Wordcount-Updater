package org.simonscode.nanowrimotracker;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Optional;

class NanoAPI {
    static String login(String username, String password) {
        return "";
    }

    static Optional<HashMap<String, String>> listProjects(String userid, String token) {
        try {
            URL url = new URL("https://api.nanowrimo.org/projects?filter[user_id]=" + userid);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(false);
            conn.setDoOutput(true);
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10_000);
            conn.addRequestProperty("Host", "api.nanowrimo.org");
            conn.addRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:71.0) Gecko/20100101 Firefox/71.0");
            conn.addRequestProperty("Accept", "application/vnd.api+json");
            conn.addRequestProperty("Accept-Language", "en-US,en;q=0.5");
            conn.addRequestProperty("Accept-Encoding", "gzip, deflate, br");
            conn.addRequestProperty("Origin", "https://nanowrimo.org");
            conn.addRequestProperty("DNT", "1");
            conn.addRequestProperty("Connection", "keep-alive");
            conn.addRequestProperty("Pragma", "no-cache");
            conn.addRequestProperty("Cache-Control", "no-cache");
            conn.addRequestProperty("Authorization", token);
            final JsonElement response = new JsonParser().parse(new JsonReader(new InputStreamReader(conn.getInputStream())));
            final JsonObject responseObject = response.getAsJsonObject();
            if (responseObject.has("error")) {
                return Optional.empty();
            }
            HashMap<String, String> results = new HashMap<>();
            final JsonArray projects = responseObject.getAsJsonArray("data");
            for (JsonElement project : projects) {
                String id = project.getAsJsonObject().get("id").getAsString();
                String name = project.getAsJsonObject().getAsJsonObject("attributes").get("title").getAsString();
                results.put(name, id);
            }
            return Optional.of(results);

        } catch (IOException e) {
            NaNoWriMoTracker.getLogWindow().log("\nERROR while updating. Please check if your username and secret key in the settings are correct.");
            e.printStackTrace();
        }
        return Optional.empty();
    }

    static void submit(String token, String projectid, int wordcount) {

    }

    static void updateCount(String username, String key, int wordcount) {
        if (wordcount < 0) {
            System.out.printf("Negative wordcount: %s. Not updating.\n", wordcount);
            return;
        }
        String wc = String.valueOf(wordcount);
        try {
            String hash = hashString(key + username + wc).toLowerCase();

            URL url = new URL("https://nanowrimo.org/api/wordcount");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("PUT");
            connection.setConnectTimeout(10_000);
            connection.addRequestProperty("hash", hash);
            connection.addRequestProperty("name", username);
            connection.addRequestProperty("wordcount", wc);
        } catch (IOException | NoSuchAlgorithmException e) {
            NaNoWriMoTracker.getLogWindow().log("\nERROR while updating. Please check if your username and secret key in the settings are correct.");
            e.printStackTrace();
        }
    }

    private static String hashString(String input) throws NoSuchAlgorithmException {
        char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();
        byte[] bytes = MessageDigest
                .getInstance("SHA-1")
                .digest(input.getBytes());
        StringBuilder result = new StringBuilder(bytes.length * 2);

        for (byte aByte : bytes) {
            int it = Byte.toUnsignedInt(aByte);
            result.append(HEX_CHARS[it >> 4 & 0x0f]);
            result.append(HEX_CHARS[it & 0x0f]);
        }

        return result.toString();
    }
}
