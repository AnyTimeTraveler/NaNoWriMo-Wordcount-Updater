package org.simonscode.nanoupdater

import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.Font
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.swing.*
import javax.swing.text.DefaultCaret


object LogWindow : JFrame("NaNoWriMo Updater") {
    private val textArea: JTextArea
    private val formatter = SimpleDateFormat("HH:mm")
    private val wordsAtStartOfSession = Config.get().currentWordcount
    private val wordsSinceStartOfSession = JLabel("Session: 0")
    private val wordsSinceStartOfDay = JLabel("Today: 0")

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
        textArea.font = Font(Font.MONOSPACED, Font.PLAIN, 14)
        val caret = textArea.caret as DefaultCaret
        caret.updatePolicy = DefaultCaret.ALWAYS_UPDATE

        val scrollPane = JScrollPane(textArea)
        scrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        scrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS

        val refreshButton = JButton("Refresh now")
        refreshButton.addActionListener { Thread(NanoUpdater.Checker).start() }

        val bottomPanel = JPanel(FlowLayout())
        bottomPanel.add(wordsSinceStartOfDay)
        bottomPanel.add(refreshButton)
        bottomPanel.add(wordsSinceStartOfSession)

        contentPane.layout = BorderLayout()
        contentPane.add(scrollPane, BorderLayout.CENTER)
        contentPane.add(bottomPanel, BorderLayout.SOUTH)

        textArea.text = "Welcome to Simon's NaNoWriMo Updater!\n" +
                "I am going to watch your novel and update your currentWordcount whenever you make progress!\n" +
                "To change parameters after the initial setup, move the document or edit the file config.json by hand.\n" +
                "Have fun writing!\n\n" +
                "Author: Simon Struck (https://github.com/Simon70/NaNoWriMo-Wordcount-Updater)\n\n\n"
    }

    fun updateWordcount(old: Int, new: Int) {
        val nf = DecimalFormat.getInstance(Locale("de", "DE"))
        wordsSinceStartOfSession.text = "Session: " + nf.format(new - wordsAtStartOfSession)
        wordsSinceStartOfDay.text = "Today: " + nf.format(new - Config.get().wordcountAtStartOfDay)
        textArea.append(String.format("[%s] %s => %s\n", formatter.format(Date()), format(new - old, 6), nf.format(new)))
        revalidate()
    }

    fun log(message: String) {
        textArea.append(String.format("[%s] %s\n", formatter.format(Date()), message))
        revalidate()
    }

    private fun format(number: Int, space: Int): String {
        val sb = StringBuilder()
        (2..space - number.toString().length).forEach { sb.append(' ') }
        if (number > 0)
            sb.append('+')
        else
            sb.append(' ')
        sb.append(number)
        return sb.toString()
    }

    private class CloseListener : WindowAdapter() {
        override fun windowClosing(p0: WindowEvent?) {
            NanoUpdater.shutdown()
        }
    }
}
