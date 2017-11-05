package org.simonscode.nanoupdater

import org.jsoup.Connection
import org.jsoup.Jsoup

object NanoAPI {
    private var loggedIn = false
    private var userCredentialsCookie = ""
    private var nanoSessionCookie = ""
    private var authenticityToken = ""
    private var noveId = ""
    fun login(username: String, password: String): Boolean {
        if (loggedIn)
            return true
        LogWindow.log("Logging into NaNoWriMo...")
        val jsoup = Jsoup.connect("https://nanowrimo.org/sign_in")
                .method(Connection.Method.POST)
                .followRedirects(true)
                .data("user_session[name]", username)
                .data("user_session[password]", password)
                .execute()
        if (jsoup.statusCode() != 200 || !jsoup.hasCookie("user_credentials"))
            return false

        userCredentialsCookie = jsoup.cookie("user_credentials")
        nanoSessionCookie = jsoup.cookie("_nanowrimo_session")
        val doc = jsoup.parse()
        val updateForm = doc.getElementById("menu_novel_word_count_form")
        authenticityToken = updateForm.getElementsByAttributeValueMatching("name", "authenticity_token").first().attr("value")
        noveId = updateForm.attr("action")

        loggedIn = true
        LogWindow.log("Success!\n")
        return true
    }

    fun updateCount(wordcount: Int) {
        Jsoup.connect("https://nanowrimo.org" + noveId)
                .method(Connection.Method.POST)
                .followRedirects(true)
                .header("Content-Type", "multipart/form-data")
                .data("utf8", "✓")
                .data("_method", "put")
                .data("authenticity_token", authenticityToken)
                .data("novel[session_counting]", "false")
                .data("novel[word_count]", wordcount.toString())
                .data("novel[session_word_count]", "0")
                .data("commit", "Update")
                .cookie("_nanowrimo_session", nanoSessionCookie)
                .cookie("user_credentials", userCredentialsCookie)
                .execute()
    }

    fun signOut() {
        Jsoup.connect("https://nanowrimo.org/sign_out")
                .method(Connection.Method.GET)
                .cookie("_nanowrimo_session", nanoSessionCookie)
                .cookie("user_credentials", userCredentialsCookie)
                .execute()
    }
}