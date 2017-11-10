package org.simonscode.nanoupdater

import java.awt.BorderLayout
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*
import javax.swing.text.DefaultCaret


object LogWindow : JFrame("NaNoWriMo Updater") {
    private val textArea: JTextArea

    init {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        } catch (ignored: Exception) {

        }

        setSize(600, 400)
        isResizable = true
        isVisible = false
        defaultCloseOperation = WindowConstants.DO_NOTHING_ON_CLOSE
        addWindowListener(CloseListener())

        textArea = JTextArea()
        textArea.isEditable = false
        textArea.wrapStyleWord = true
        textArea.lineWrap = true
        val caret = textArea.caret as DefaultCaret
        caret.updatePolicy = DefaultCaret.ALWAYS_UPDATE

        val scrollPane = JScrollPane(textArea)
        scrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        scrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS


        val refreshButton = JButton("Refresh now")
        refreshButton.addActionListener { Thread(NanoUpdater.Checker).start() }

        contentPane.layout = BorderLayout()
        contentPane.add(scrollPane, BorderLayout.CENTER)
        contentPane.add(refreshButton, BorderLayout.SOUTH)

        textArea.text = "Welcome to Simon's NaNoWriMo Updater!\n" +
                "I am going to watch your novel and update your wordcount whenever you make progress!\n" +
                "To change parameters after the initial setup, move the document or edit the file config.json by hand.\n" +
                "Have fun writing!\n\n" +
                "Author: Simon Struck (https://github.com/Simon70/NaNoWriMo-Wordcount-Updater)\n\n\n"
    }

    fun log(message: String) {
        textArea.append(message)
    }

    private class CloseListener : WindowAdapter() {
        override fun windowClosing(p0: WindowEvent?) {
            NanoUpdater.shutdown()
        }
    }
}
