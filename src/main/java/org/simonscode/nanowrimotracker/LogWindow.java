package org.simonscode.nanowrimotracker;

import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.style.Styler;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


class LogWindow extends JFrame {
    private JTextArea textArea;
    private SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
    private int wordsAtStartOfSession = Storage.get().wordCountAmounts.isEmpty() ? 0 : Storage.get().wordCountAmounts.get(Storage.get().wordCountAmounts.size() - 1);
    private JLabel wordsSinceStartOfTime = new JLabel("Total: 0");
    private JLabel wordsSinceStartOfSession = new JLabel("Session: 0");
    private JLabel wordsSinceStartOfDay = new JLabel("Today: 0");
    private final XYChart liveChart;
    private final XChartPanel<XYChart> liveChartPanel;
    private final JCheckBox showOnlyCurrentSessionCheckBox;

    private boolean cliMode;

    LogWindow() {
        super("NaNoWriMo Wordcount Tracker and Updater");
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {

        }

        setSize(800, 600);
        setResizable(true);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent event) {
                NaNoWriMoTracker.shutdown();
            }
        });

        showOnlyCurrentSessionCheckBox = new JCheckBox("Show only this session", false);
        showOnlyCurrentSessionCheckBox.addActionListener(e -> updateGraph());
        JPanel liveChartWrapperPanel = new JPanel(new BorderLayout());

        liveChart = new XYChart(800, 400, Styler.ChartTheme.XChart);
        liveChart.addSeries("Wordcount", Storage.get().wordCountTimes, Storage.get().wordCountAmounts);
        liveChart.setXAxisLabelOverrideMap(ChartTimescaler.generateDateOverrideMap(Storage.get().wordCountTimes));
        liveChartPanel = new XChartPanel<>(liveChart);

        liveChartWrapperPanel.add(liveChartPanel, BorderLayout.CENTER);
        liveChartWrapperPanel.add(showOnlyCurrentSessionCheckBox, BorderLayout.SOUTH);

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
        bottomPanel.add(wordsSinceStartOfTime);
        bottomPanel.add(wordsSinceStartOfDay);
        bottomPanel.add(wordsSinceStartOfSession);
        bottomPanel.add(refreshButton);
        bottomPanel.add(settingsButton);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(liveChartWrapperPanel, BorderLayout.NORTH);
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        getContentPane().add(bottomPanel, BorderLayout.SOUTH);

        final Integer wordcount = Storage.get().wordCountAmounts.get(Storage.get().wordCountAmounts.size() - 1);
        showUpdatedWordcount(wordcount, wordcount);
        textArea.setText("NaNoWriMo Tracker running!\n" +
                "Have a good writing session!\n\n");
    }

    void showUpdatedWordcount(int oldCount, int newCount) {
        if (Storage.get().lastDay != Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) {
            Storage.get().lastDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
            Storage.get().wordcountAtStartOfDay = newCount;
            Storage.get().save();
        }

        NumberFormat nf = DecimalFormat.getInstance(Locale.getDefault(Locale.Category.DISPLAY));
        wordsSinceStartOfTime.setText("Total: " + nf.format(newCount) + '\t');
        wordsSinceStartOfSession.setText("Session: " + nf.format(newCount - wordsAtStartOfSession) + '\t');
        wordsSinceStartOfDay.setText("Today: " + nf.format(newCount - Storage.get().wordcountAtStartOfDay) + '\t');
        log(String.format("%s => %s", formatWordcountDelta(newCount - oldCount), nf.format(newCount)));
        updateGraph();
        revalidate();
    }

    private void updateGraph() {
        Storage storage = Storage.get();
        List<Date> xData = showOnlyCurrentSessionCheckBox.isSelected() ? storage.wordCountTimes.subList(storage.wordCountIndexAtSessionStart, storage.wordCountTimes.size()) : storage.wordCountTimes;
        liveChart.updateXYSeries("Wordcount",
                xData,
                showOnlyCurrentSessionCheckBox.isSelected() ? storage.wordCountAmounts.subList(storage.wordCountIndexAtSessionStart, storage.wordCountAmounts.size()) : storage.wordCountAmounts
                , null);
        liveChart.setXAxisLabelOverrideMap(ChartTimescaler.generateDateOverrideMap(xData));
        liveChartPanel.revalidate();
        liveChartPanel.repaint();
    }

    void log(String message) {
        final String formattedMessage = String.format("[%s] %s\n", formatter.format(new Date()), message);
        if (cliMode) {
            System.out.print(formattedMessage);
        } else {
            textArea.append(formattedMessage);
            revalidate();
        }
    }

    private String formatWordcountDelta(int number) {
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

    void cliMode() {
        this.cliMode = true;
    }

}
