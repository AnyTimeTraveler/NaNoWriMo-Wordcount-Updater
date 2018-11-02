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
import java.util.Date;
import java.util.List;
import java.util.Locale;


class LogWindow extends JFrame {
    private JTextArea textArea;
    private SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
    private int wordsAtStartOfSession = Storage.get().wordCountAmounts.isEmpty() ? 0 : Storage.get().wordCountAmounts.get(Storage.get().wordCountAmounts.size() - 1);
    private JLabel wordsSinceStartOfSession = new JLabel("0 Session");
    private JLabel wordsSinceStartOfDay = new JLabel("0 Today");
    private final XYChart liveChart;
    private final XChartPanel<XYChart> liveChartPanel;
    private final JCheckBox showOnlyCurrentSessionCheckBox;

    private boolean firstUpdate = true;

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
        bottomPanel.add(wordsSinceStartOfDay);
        bottomPanel.add(refreshButton);
        bottomPanel.add(settingsButton);
        bottomPanel.add(wordsSinceStartOfSession);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(liveChartWrapperPanel, BorderLayout.NORTH);
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        getContentPane().add(bottomPanel, BorderLayout.SOUTH);

        textArea.setText("NaNoWriMo Tracker running!\n" +
                "Have a good writing session!\n\n");
    }

    void showUpdatedWordcount(int oldCount, int newCount) {
        if (firstUpdate) {
            firstUpdate = false;
            showOnlyCurrentSessionCheckBox.setSelected(true);
        }
        NumberFormat nf = DecimalFormat.getInstance(new Locale("de", "DE"));
        wordsSinceStartOfSession.setText("Session: " + nf.format(newCount - wordsAtStartOfSession));
        wordsSinceStartOfDay.setText("Today: " + nf.format(newCount - Storage.get().wordcountAtStartOfDay));
        textArea.append(String.format("[%s] %s => %s\n", formatter.format(new Date()), format(newCount - oldCount), nf.format(newCount)));
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
        textArea.append(String.format("[%s] %s", formatter.format(new Date()), message));
        revalidate();
    }


    void log(String message, Object... args) {
        log(String.format(message, args));
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
