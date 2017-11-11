package org.simonscode.nanoupdater

import org.apache.tika.parser.AutoDetectParser
import org.apache.tika.parser.ParseContext
import org.apache.tika.sax.BodyContentHandler
import org.simonscode.nanoupdater.NanoAPI.updateCount
import java.io.File
import java.util.*
import javax.swing.JFileChooser
import javax.swing.JOptionPane

private val config = Config.get()
private val timer = Timer()

private val latestVersion = "1.2.2"

fun main(args: Array<String>) {
    println("Program Log:\nPlease copy this when reporting a bug.")
    if (config.doumentPath.isEmpty() || config.minutesBetweenUpdates == 0)
        config.currentVersion = "0.0"

    val username: String
    val secretKey: String
    if (config.currentVersion != latestVersion) {
        NanoUpdater.setup()
    }
    if (config.username.isEmpty() || config.secretKey.isEmpty()) {
        try {
            username = JOptionPane.showInputDialog(null, "NaNoWriMo Username:", "Login", JOptionPane.QUESTION_MESSAGE)
            secretKey = JOptionPane.showInputDialog(null, "NaNoWriMo Secret key (from nanowrimo.org/api/currentWordcount)", "Key", JOptionPane.QUESTION_MESSAGE)
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            return
        }

        if (config.storeCredentials) {
            config.username = username
            config.secretKey = secretKey
            config.save()
        }
    }
    NanoUpdater.startWatching()
}

object NanoUpdater {

    fun setup() {
        val fileChoser = JFileChooser()
        fileChoser.currentDirectory = if (config.doumentPath.isNotEmpty()) File(config.doumentPath) else File(System.getProperty("user.home"))

        val option = fileChoser.showOpenDialog(null)

        if (option != JFileChooser.APPROVE_OPTION) {
            JOptionPane.showMessageDialog(null, "I need you to select your working file.\nTry again.", "Error", JOptionPane.ERROR_MESSAGE)
            System.exit(-1)
        }

        config.doumentPath = fileChoser.selectedFile.absolutePath

        if (config.doumentPath.substring(config.doumentPath.lastIndexOf(".") + 1) == "scrivx") {
            val scrivenerFolders = ScrivenerSupport.getScrivenerFolders(fileChoser.selectedFile)
            val selectedFolder = JOptionPane.showInputDialog(null, "Which Scrivener Folder should I watch?", "Scrivener Setup", JOptionPane.QUESTION_MESSAGE, null, scrivenerFolders.keys.toTypedArray(), scrivenerFolders.keys.first())
            if (!scrivenerFolders.containsKey(selectedFolder)) {
                JOptionPane.showMessageDialog(null, "I need you to select an option.\nTry again.", "Error", JOptionPane.ERROR_MESSAGE)
                System.exit(-1)
            }
            config.scrivenerFolder = scrivenerFolders[selectedFolder]!!
        }

        val timeInput = JOptionPane.showInputDialog(null, "How many Minutes minumum between updating your currentWordcount?", "Step 2/4", JOptionPane.QUESTION_MESSAGE)
        var time = 0
        try {
            time = timeInput.toInt()
        } catch (e: NumberFormatException) {
            JOptionPane.showMessageDialog(null, "I need you to enter a valid Integer.\nTry again.", "Error", JOptionPane.ERROR_MESSAGE)
            System.exit(-1)
        }

        config.minutesBetweenUpdates = time

        val offestInput = JOptionPane.showInputDialog(null, "Do you want to offset your currentWordcount?", 0)
        var offset = 0
        try {
            offset = offestInput.toInt()
        } catch (e: NumberFormatException) {
            JOptionPane.showMessageDialog(null, "I need you to enter a valid Integer.\nTry again.", "Error", JOptionPane.ERROR_MESSAGE)
            System.exit(-1)
        }

        config.offset = offset

        val credentialsInput = JOptionPane.showConfirmDialog(null, "Do you want me to store the Credentials to the Website?", "Setup 4/4", JOptionPane.YES_NO_OPTION)
        config.storeCredentials = when (credentialsInput) {
            JOptionPane.YES_OPTION -> true
            JOptionPane.NO_OPTION -> false
            else -> {
                System.exit(-1)
                false
            }
        }

        config.dateAtStartOfDay = getTodaysDate()
        config.currentVersion = latestVersion
        JOptionPane.showMessageDialog(null, "Setup complete!")
        config.save()
    }

    fun getWordcount(file: File): Int {
        return if (file.name.endsWith(".scrivx")) {
            ScrivenerSupport.getWordcount()
        } else {
            val handler = BodyContentHandler(-1)
            AutoDetectParser().parse(file.inputStream(), handler, org.apache.tika.metadata.Metadata(), ParseContext())
            handler.toString().trim().split("\\s+|/".toRegex()).size - config.offset
        }
    }

    fun startWatching() {
        if (getTodaysDate() != config.dateAtStartOfDay) {
            config.wordcountAtStartOfDay = config.currentWordcount
            config.dateAtStartOfDay = getTodaysDate()
            config.save()
        }
        LogWindow.isVisible = true
        val file = File(config.doumentPath)
        if (!file.exists()) {
            config.currentVersion = "0.0"
            config.save()
        }
        LogWindow.log("Starting to watch: " + file.name)
        LogWindow.log("I will check the wordcount every " + config.minutesBetweenUpdates + " minutes from now on.\n")
        val wordcount = getWordcount(file)
        LogWindow.updateWordcount(config.currentWordcount, wordcount)
        if (config.currentWordcount != wordcount) {
            updateCount(config.username, config.secretKey, wordcount)
            LogWindow.log("Detected changed wordcount from ${config.currentWordcount} to $wordcount. Updated website!")
            config.currentWordcount = wordcount
            config.save()
        }
        val interval = (config.minutesBetweenUpdates * 60 * 1000).toLong()
        timer.scheduleAtFixedRate(Checker, interval, interval)
    }

    object Checker : TimerTask() {
        override fun run() {
            val newWordcount = getWordcount(File(config.doumentPath))
            LogWindow.updateWordcount(config.currentWordcount, newWordcount)
            if (newWordcount != Config.get().currentWordcount) {
                Config.get().currentWordcount = newWordcount
                updateCount(config.username, config.secretKey, newWordcount)
                Config.get().save()
            } else {
            }
        }
    }

    @Suppress("UsePropertyAccessSyntax")
    private fun getTodaysDate(): Int {
        val c = Calendar.getInstance()
        c.setTime(Date())
        return c.get(Calendar.DAY_OF_MONTH)
    }

    fun shutdown() {
        Thread {
            LogWindow.log("Shutting down...\n")
            config.save()
            timer.cancel()
            LogWindow.log("Good bye!\n\n\n")
            Thread.sleep(500)
            System.exit(0)
        }.start()
    }
}
