package org.simonscode.nanowrimotracker;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class NanoAPI {;
    static void updateCount(String username, String key, int wordcount) {
        if (wordcount < 0) {
            System.out.printf("Negative wordcount: %s. Not updating.\n", wordcount);
            return;
        }
        String wc = String.valueOf(wordcount);
        try {
            String hash = hashString(key + username + wc).toLowerCase();
            //TODO: Drop Jsoup dependency
            Jsoup.connect("https://nanowrimo.org/api/wordcount")
                    .method(Connection.Method.PUT)
                    .data("hash", hash)
                    .data("name", username)
                    .data("wordcount", wc)
                    .execute();
        } catch (IOException | NoSuchAlgorithmException e) {
            NaNoWriMoTracker.log("\nERROR while updating. Please check if your username and secret key in the settings are correct.\n");
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
