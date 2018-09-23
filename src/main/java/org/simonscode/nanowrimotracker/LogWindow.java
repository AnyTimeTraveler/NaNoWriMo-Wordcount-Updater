package org.simonscode.nanowrimotracker;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


class LogWindow extends JFrame {
    private JTextArea textArea;
    private SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
    private int wordsAtStartOfSession = Config.get().currentWordcount;
    private JLabel wordsSinceStartOfSession = new JLabel("0 Session");
    private JLabel wordsSinceStartOfDay = new JLabel("0 Today");

    LogWindow() {
        super("NaNoWriMo Updater");
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {

        }

        //TODO: Make prettier
        //TODO: Add live chart

        setSize(600, 400);
        setResizable(true);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent event) {
                NaNoWriMoTracker.shutdown();
            }
        });

        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        DefaultCaret caret = (DefaultCaret) textArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        JButton refreshButton = new JButton("Refresh now");
        refreshButton.addActionListener((ignored) -> NaNoWriMoTracker.reload());

        JButton settingsButton = new JButton("Settings");
        settingsButton.addActionListener((ignored) -> NaNoWriMoTracker.switchFromLogWindowToSettings());

        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.add(wordsSinceStartOfDay);
        bottomPanel.add(refreshButton);
        bottomPanel.add(settingsButton);
        bottomPanel.add(wordsSinceStartOfSession);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        getContentPane().add(bottomPanel, BorderLayout.SOUTH);

        // TODO: Fix message (url)
        textArea.setText("Welcome to Simon's NaNoWriMo Updater!\n" + "I am going to watch your novel and update your currentWordcount whenever you make progress!\n" + "To change parameters after the initial setup, move the document or edit the file config.json by hand.\n" + "Have fun writing!\n\n" + "Simon Author Struck (https://github.com/Simon70/NaNoWriMo-Wordcount-Updater)\n\n\n");
    }

    void updateWordcount(int oldCount, int newCount) {
        NumberFormat nf = DecimalFormat.getInstance(new Locale("de", "DE"));
        wordsSinceStartOfSession.setText("Session: " + nf.format(newCount - wordsAtStartOfSession));
        wordsSinceStartOfDay.setText("Today: " + nf.format(newCount - Config.get().wordcountAtStartOfDay));
        textArea.append(String.format("[%s] %s => %s\n", formatter.format(new Date()), format(newCount - oldCount), nf.format(newCount)));
        revalidate();
    }

    void log(String message) {
        textArea.append(String.format("[%s] %s", formatter.format(new Date()), message));
        revalidate();
    }

    private String format(int number) {
        StringBuilder sb = new StringBuilder();
        for (int i = 2; i < 6 - String.valueOf(number).length(); i++) {
            sb.append(' ');
        }

        if (number > 0)
            sb.append('+');
        else
            sb.append(' ');
        sb.append(number);
        return sb.toString();
    }
}
