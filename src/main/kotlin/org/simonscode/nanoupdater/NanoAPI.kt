package org.simonscode.nanoupdater

import org.jsoup.Connection
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import java.security.MessageDigest

object NanoAPI {
    fun updateCount(username: String, key: String, wordcount: Int) {
        if (wordcount < 0) {
            println("Negative wordcount: $wordcount. Not updating.")
            return
        }
        val wc = wordcount.toString()
        val hash = hashString("SHA-1", key + username + wc).toLowerCase()
        try {
            Jsoup.connect("https://nanowrimo.org/api/wordcount")
                    .method(Connection.Method.PUT)
                    .data("hash", hash)
                    .data("name", username)
                    .data("wordcount", wc)
                    .execute()
        } catch (e: HttpStatusException) {
            LogWindow.log("\nERROR while updating. Please check your username and secret key in the config file.\n")
            e.printStackTrace()
        }
    }

    private fun hashString(type: String, input: String): String {
        val HEX_CHARS = "0123456789ABCDEF"
        val bytes = MessageDigest
                .getInstance(type)
                .digest(input.toByteArray())
        val result = StringBuilder(bytes.size * 2)

        bytes.forEach {
            val i = it.toInt()
            result.append(HEX_CHARS[i shr 4 and 0x0f])
            result.append(HEX_CHARS[i and 0x0f])
        }

        return result.toString()
    }
}
