package org.simonscode.nanoupdater

/*
Logging In Java with the JDK 1.4 Logging API and Apache log4j
by Samudra Gupta
Apress Copyright 2003
ISBN:1590590996

*/

import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*


object LogWindow : JFrame("NaNoWriMo Updater") {
    private var textArea: JTextArea? = null
    private var pane: JScrollPane? = null

    init {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        } catch (ignored: Exception) {

        }

        setSize(600, 400)
        isResizable = true
        textArea = JTextArea()
        textArea!!.isEditable = false
        textArea!!.wrapStyleWord = true
        textArea!!.lineWrap = true
        pane = JScrollPane(textArea)
        pane!!.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        pane!!.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
        pane!!.autoscrolls = true
        contentPane.add(pane)
        isVisible = false
        defaultCloseOperation = WindowConstants.DO_NOTHING_ON_CLOSE
        addWindowListener(CloseListener())

        log("Welcome to Simon's NaNoWriMo Updater!\n" +
                "I am going to watch your novel and update your wordcount whenever you make progress!\n" +
                "To change parameters after the initial setup, move the document or edit the file config.json by hand.\n" +
                "Have fun writing!\n\n" +
                "Author: Simon Struck (https://github.com/Simon70/NaNoWriMo-Wordcount-Updater)\n\n\n")
    }

    fun log(message: String) {
        textArea!!.append(message)
        this.contentPane.validate()
    }

    private class CloseListener : WindowAdapter() {
        override fun windowClosing(p0: WindowEvent?) {
            NanoUpdater.shutdown()
        }
    }
}
