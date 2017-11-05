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

fun main(args: Array<String>) {
    println("Program Log:\nPlease copy this when reporting a bug.")
    if (config.doumentPath.isEmpty() || config.minutesBetweenUpdates == 0)
        config.firstRun = true

    val username: String
    val secretKey: String
    if (config.firstRun) {
        NanoUpdater.setup()
    }
    if (config.username.isEmpty() || config.secretKey.isEmpty()) {
        try {
            username = JOptionPane.showInputDialog(null, "NaNoWriMo Username:", "Login", JOptionPane.QUESTION_MESSAGE)
            secretKey = JOptionPane.showInputDialog(null, "NaNoWriMo Secret key (from nanowrimo.org/api/wordcount)", "Key", JOptionPane.QUESTION_MESSAGE)
        } catch (e: IllegalStateException) {
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


    fun getWordcount(file: File): Int {
        val handler = BodyContentHandler()
        val metadata = org.apache.tika.metadata.Metadata()
        val pcontext = ParseContext()

        AutoDetectParser().parse(file.inputStream(), handler, metadata, pcontext)
        val regex = Regex("\\S+")
        return regex.findAll(handler.toString()).count()
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
            LogWindow.log("Detected changed wordcount since last time this program ran. Updating...\n")
            updateCount(config.username, config.secretKey, wordcount)
            LogWindow.log("Done!\n")
            Config.get().wordcount = wordcount
            Config.get().save()
        }
        LogWindow.log("I will check the wordcount every " + config.minutesBetweenUpdates + " minutes from now on.\n")
        val interval = (config.minutesBetweenUpdates * 60 * 1000).toLong()
        timer.scheduleAtFixedRate(Checker(file), interval, interval)
    }

    private class Checker(val file: File) : TimerTask() {
        override fun run() {
            LogWindow.log("Rechecking Wordcount...")
            val newWordcount = getWordcount(file)
            LogWindow.log("Done!\n$newWordcount words read: ")
            if (newWordcount != Config.get().wordcount) {
                Config.get().wordcount = newWordcount
                updateCount(config.username, config.secretKey, newWordcount)
                Config.get().save()
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
            LogWindow.log("Done!\n\n")
            LogWindow.log("Good bye!")
            Thread.sleep(500)
            System.exit(0)
        }
        t.start()
    }
}
