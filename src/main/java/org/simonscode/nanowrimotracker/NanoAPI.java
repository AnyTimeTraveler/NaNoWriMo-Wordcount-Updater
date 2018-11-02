package org.simonscode.nanowrimotracker;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class NanoAPI {
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
            NaNoWriMoTracker.getLogWindow().log("\nERROR while updating. Please check if your username and secret key in the settings are correct.\n");
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
