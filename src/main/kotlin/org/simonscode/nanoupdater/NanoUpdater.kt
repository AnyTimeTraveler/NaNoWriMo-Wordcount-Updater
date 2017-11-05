package org.simonscode.nanoupdater

import org.apache.tika.parser.ParseContext
import org.apache.tika.parser.microsoft.OfficeParser
import org.apache.tika.parser.microsoft.ooxml.OOXMLParser
import org.apache.tika.parser.odf.OpenDocumentParser
import org.apache.tika.parser.rtf.RTFParser
import org.apache.tika.sax.BodyContentHandler
import org.jsoup.Connection
import org.jsoup.Jsoup
import java.io.File
import java.util.*
import javax.swing.JFileChooser
import javax.swing.JOptionPane

private val config = Config.get()
private val timer = Timer()

fun main(args: Array<String>) {
    println("If you are reading this, you can safely ignore the following warnings.")
    if (config.doumentPath.isEmpty() || config.minutesBetweenUpdates == 0)
        config.firstRun = true

    var username = ""
    var password: String
    if (config.firstRun) {
        NanoUpdater.setup()
    }
    if (config.username.isEmpty() || config.password.isEmpty()) {
        do {
            try {
                username = if (username.isEmpty())
                    JOptionPane.showInputDialog(null, "NaNoWriMo Username:", "Login", JOptionPane.QUESTION_MESSAGE)
                else
                    JOptionPane.showInputDialog(null, "Login failed, please try again.\nNaNoWriMo Username:", "Error", JOptionPane.ERROR_MESSAGE)
                password = JOptionPane.showInputDialog(null, "NaNoWriMo Password:", "Login", JOptionPane.QUESTION_MESSAGE)
            } catch (e: IllegalStateException) {
                return
            }
        } while (!NanoUpdater.login(username, password))

        if (config.storeCredentials) {
            config.username = username
            config.password = password
            config.save()
        }
    } else {
        username = config.username
        password = config.password
    }
    NanoUpdater.login(username, password)
    NanoUpdater.startWatching()
}

object NanoUpdater {
    private var loggedIn = false
    private var userCredentialsCookie = ""
    private var nanoSessionCookie = ""
    private var authenticityToken = ""
    private var noveId = ""

    fun setup() {
        val fileChoser = JFileChooser()
        fileChoser.currentDirectory = File(System.getProperty("user.home"))

        val option = fileChoser.showOpenDialog(null)

        if (option != JFileChooser.APPROVE_OPTION) {
            JOptionPane.showMessageDialog(null, "I need you to select your working file.\nTry again.", "Error", JOptionPane.ERROR_MESSAGE)
            System.exit(-1)
        }

        config.doumentPath = fileChoser.selectedFile.absolutePath

        val timeInput = JOptionPane.showInputDialog(null, "How many Minutes minumum between updating your wordcount?", "Step 2/3", JOptionPane.QUESTION_MESSAGE)
        var time = 0
        try {
            time = timeInput.toInt()
        } catch (e: NumberFormatException) {
            JOptionPane.showMessageDialog(null, "I need you to enter a valid Integer.\nTry again.", "Error", JOptionPane.ERROR_MESSAGE)
            System.exit(-1)
        }

        config.minutesBetweenUpdates = time

        val credentialsInput = JOptionPane.showConfirmDialog(null, "Do you want me to store the Credentials to the Website?", "Setup 3/3", JOptionPane.YES_NO_OPTION)
        config.storeCredentials = when (credentialsInput) {
            JOptionPane.YES_OPTION -> true
            JOptionPane.NO_OPTION -> false
            else -> {
                System.exit(-1)
                false
            }
        }

        config.firstRun = false
        JOptionPane.showMessageDialog(null, "Setup complete!")
        config.save()
    }

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

    fun getWordcount(file: File): Int {
        val handler = BodyContentHandler()
        val metadata = org.apache.tika.metadata.Metadata()
        val pcontext = ParseContext()

        val filetype = file.name.substring(file.name.indexOf(".") + 1)
        val text = when (filetype) {
            "rtf" -> {
                RTFParser().parse(file.inputStream(), handler, metadata, pcontext)
                handler.toString()
            }
            "docx" -> {
                OOXMLParser().parse(file.inputStream(), handler, metadata, pcontext)
                handler.toString()
            }
            "doc" -> {
                OfficeParser().parse(file.inputStream(), handler, metadata, pcontext)
                handler.toString()
            }
            "odt" -> {
                OpenDocumentParser().parse(file.inputStream(), handler, metadata, pcontext)
                handler.toString()
            }
            "txt" -> {
                file.readText()
            }
            else -> {
                JOptionPane.showMessageDialog(null, "I can't handle $filetype, yet.\nAsk Simon to implement it.", "Error", JOptionPane.ERROR_MESSAGE)
                System.exit(-1)
                "ERROR"
            }
        }
        val regex = Regex("\\S+")
        return regex.findAll(text).count()
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

    private fun signOut() {
        Jsoup.connect("https://nanowrimo.org/sign_out")
                .method(Connection.Method.GET)
                .cookie("_nanowrimo_session", nanoSessionCookie)
                .cookie("user_credentials", userCredentialsCookie)
                .execute()
    }

    fun startWatching() {
        LogWindow.isVisible = true
        val file = File(config.doumentPath)
        if (!file.exists()) {
            config.firstRun = true
            config.save()
        }
        LogWindow.log("Starting to watch: " + file.name + "\n")
        val wordcount = getWordcount(file)
        LogWindow.log("Found $wordcount words.\n")
        if (config.wordcount != wordcount) {
            LogWindow.log("Detected changed wordcount since last time this program ran. Updating...")
            updateCount(wordcount)
            LogWindow.log("Done!\n")

        }
        LogWindow.log("I will check the wordcount every " + config.minutesBetweenUpdates + " minutes from now on.\n")
        val timer = timer
        val interval = (config.minutesBetweenUpdates * 60 * 1000).toLong()
        timer.scheduleAtFixedRate(Checker(file, wordcount), interval, interval)
    }

    private class Checker(val file: File, var wordcount: Int) : TimerTask() {
        override fun run() {
            LogWindow.log("Rechecking Wordcount...")
            val newWordcount = getWordcount(file)
            LogWindow.log("Done!\n$newWordcount words read: ")
            if (newWordcount != wordcount) {
                wordcount = newWordcount
                updateCount(newWordcount)
                LogWindow.log("Updating Website!\n")
            } else {
                LogWindow.log("No Update required!\n")
            }
        }

    }

    fun shutdown() {
        val t = Thread {
            LogWindow.log("Shutting down...\n")
            config.save()
            timer.cancel()
            LogWindow.log("Logging out from NaNoWriMo-Website...")
            NanoUpdater.signOut()
            LogWindow.log("Done!\n\n")
            LogWindow.log("Good bye!")
            Thread.sleep(500)
            System.exit(0)
        }
        t.start()
    }
}
