package org.simonscode.nanowrimotracker;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.simonscode.nanowrimotracker.wordcounter.IWordcounter;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SettingsFrame extends JFrame {
    private JSpinner timeBetweenUpdatesSpinner;
    private JTextField officialUsernameField;
    private JTextField officialSecretKeyField;
    private JSpinner wordcountOffsetSpinner;
    private JRadioButton officialServerRadioButton;
    private JRadioButton offlineRadioButton;
    private JRadioButton privateServerRadioButton;
    private JPanel privateSettingsPanel;
    private JPanel officialSettingsPanel;
    private JTextField privateServerAddressField;
    private JButton privateServerTestButton;
    private JComboBox timeBetweenUpdatesUnitComboBox;
    private JTextField projectLocationField;
    private JButton selectProjectLocationButton;
    private JComboBox projectTypeComboBox;
    private JTextField wordcountPreviewField;
    private JTabbedPane mainTabbedPane;
    private JPanel welcomeTab;
    private JComboBox scrivenerFolderComboBox;
    private JPanel contentPane;
    private JPanel secondarySelectionPanel;
    private JPanel wordgoalsPanel;
    private JButton addWordgoalButton;
    private JButton testConnectionButton;
    private JTextField connectionTestResultField;
    private JButton closeButton;


    SettingsFrame() {
        super("Settings");
        $$$setupUI$$$();

        setSize(550, 450);

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent event) {
                SettingsFrame.this.setVisible(false);
                if (NaNoWriMoTracker.configIsSufficient()) {
                    Storage.get().firstRun = false;
                    Storage.get().save();
                    NaNoWriMoTracker.reload();
                } else {
                    JOptionPane.showMessageDialog(NaNoWriMoTracker.getLogWindow(),
                            String.format("Configuration incomplete.%nValues have not been saved."),
                            "Configuration incomplete",
                            JOptionPane.WARNING_MESSAGE);
                    NaNoWriMoTracker.shutdown();
                }
            }
        });

        closeButton.addActionListener(e -> dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)));
        IWordcounter[] supportedFileTypes = NaNoWriMoTracker.getWordcounters();
        List<String> extensionList = Arrays.stream(supportedFileTypes).map(IWordcounter::getFileExtensions).flatMap(List::stream).collect(Collectors.toList());
        String[] extensions = new String[extensionList.size()];
        extensionList.toArray(extensions);

        // Project Location Tab
        secondarySelectionPanel.setVisible(false);
        Vector<String> options = new Vector<>();
        updateProjectLocationSelection(supportedFileTypes, options, new File(Storage.get().projectLocation));
        selectProjectLocationButton.addActionListener(e -> {
            JFileChooser jfc = new JFileChooser();
            jfc.setAcceptAllFileFilterUsed(false);
            jfc.setMultiSelectionEnabled(false);
            jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (!Storage.get().projectLocation.isEmpty()) {
                File file = new File(Storage.get().projectLocation);
                if (file.exists()) {
                    jfc.setSelectedFile(file);
                }
                if (file.getParentFile().exists()) {
                    jfc.setCurrentDirectory(file.getParentFile());
                }
            }
            jfc.setFileFilter(new FileNameExtensionFilter("Compatible Files", extensions));
            jfc.addActionListener(ev -> {
                if (jfc.getSelectedFile() != null) {
                    updateProjectLocationSelection(supportedFileTypes, options, jfc.getSelectedFile());
                }
            });
            jfc.showDialog(contentPane, "Select");
        });
        projectTypeComboBox.addActionListener(e -> {
            File projectLocation = new File(Storage.get().projectLocation);
            for (IWordcounter wc : supportedFileTypes) {
                if (options.get(projectTypeComboBox.getSelectedIndex()).startsWith(wc.getName())) {
                    secondarySelectionPanel.setVisible(wc.needsSecondarySelection());
                    Storage.get().projectType = wc.getName();
                    generateWordcountPreview(wc, projectLocation);
                    break;
                }
            }
        });
        scrivenerFolderComboBox.setSelectedItem(Storage.get().secondarySelection);
        scrivenerFolderComboBox.addActionListener(e -> {
            File projectLocation = new File(Storage.get().projectLocation);
            Storage.get().secondarySelection = (String) scrivenerFolderComboBox.getSelectedItem();
            updateWordcountPreviewForScrivenerFolder(supportedFileTypes, options, projectLocation);
        });
        if (!Storage.get().secondarySelection.isEmpty()) {
            updateWordcountPreviewForScrivenerFolder(supportedFileTypes, options, new File(Storage.get().projectLocation));
        }

        // Wordcount Tab
        timeBetweenUpdatesSpinner.setValue(Storage.get().timeBetweenUpdates);
        timeBetweenUpdatesSpinner.addChangeListener(e -> Storage.get().timeBetweenUpdates = (int) timeBetweenUpdatesSpinner.getValue());

        timeBetweenUpdatesUnitComboBox.setSelectedIndex(Arrays.asList(TimeUnit.SECONDS, TimeUnit.MINUTES, TimeUnit.HOURS, TimeUnit.DAYS).indexOf(Storage.get().timeUnitBetweenUpdates));
        timeBetweenUpdatesUnitComboBox.addActionListener(evt -> Storage.get().timeUnitBetweenUpdates = new TimeUnit[]{TimeUnit.SECONDS, TimeUnit.MINUTES, TimeUnit.HOURS, TimeUnit.DAYS}[timeBetweenUpdatesUnitComboBox.getSelectedIndex()]);

        wordcountOffsetSpinner.setValue(Storage.get().wordcountOffset);
        wordcountOffsetSpinner.addChangeListener(e -> Storage.get().wordcountOffset = (int) wordcountOffsetSpinner.getValue());

        // Server Tab
        switch (Storage.get().serverSelection) {
            case OFFICIAL:
                officialServerRadioButton.setSelected(true);
                officialSettingsPanel.setVisible(true);
                privateSettingsPanel.setVisible(false);
                break;
            case PRIVATE:
                privateServerRadioButton.setSelected(true);
                officialSettingsPanel.setVisible(false);
                privateSettingsPanel.setVisible(true);
                break;
            case OFFLINE:
                offlineRadioButton.setSelected(true);
                officialSettingsPanel.setVisible(false);
                privateSettingsPanel.setVisible(false);
                break;
        }
        ActionListener serverSelectionListener = e -> {
            if (e.getSource() == officialServerRadioButton) {
                officialSettingsPanel.setVisible(true);
                privateSettingsPanel.setVisible(false);
                Storage.get().serverSelection = ServerSelection.OFFICIAL;
            } else if (e.getSource() == privateServerRadioButton) {
                officialSettingsPanel.setVisible(false);
                privateSettingsPanel.setVisible(true);
                Storage.get().serverSelection = ServerSelection.PRIVATE;
            } else if (e.getSource() == offlineRadioButton) {
                officialSettingsPanel.setVisible(false);
                privateSettingsPanel.setVisible(false);
                Storage.get().serverSelection = ServerSelection.OFFLINE;
            }
        };
        officialServerRadioButton.addActionListener(serverSelectionListener);
        offlineRadioButton.addActionListener(serverSelectionListener);
        privateServerRadioButton.addActionListener(serverSelectionListener);

        officialUsernameField.setText(Storage.get().officialUsername);
        officialSecretKeyField.setText(Storage.get().officialSecretKey);
        privateServerAddressField.setText(Storage.get().privateServerAddress);

        // Wordgoals Tab
        wordgoalsPanel.setLayout(new BoxLayout(wordgoalsPanel, BoxLayout.PAGE_AXIS));
        addWordgoalButton.addActionListener(e -> {
            wordgoalsPanel.remove(addWordgoalButton);
            WordGoal wordGoal = new WordGoal();
            Storage.get().customWordGoals.add(wordGoal);
            wordgoalsPanel.add(generateWordgoalLine(wordGoal));
            wordgoalsPanel.add(addWordgoalButton);
            addWordgoalButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            invalidate();
            repaint();
        });
        wordgoalsPanel.remove(addWordgoalButton);
        for (WordGoal wg : Storage.get().customWordGoals) {
            wordgoalsPanel.add(generateWordgoalLine(wg));
        }
        wordgoalsPanel.add(addWordgoalButton);
        addWordgoalButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        setContentPane($$$getRootComponent$$$());
        pack();
    }

    private void updateProjectLocationSelection(IWordcounter[] supportedFileTypes, Vector<String> options, File selectedFile) {
        projectLocationField.setText(selectedFile.getAbsolutePath());
        options.clear();
        if (!selectedFile.exists() || selectedFile.isDirectory()) {
            wordcountPreviewField.setText("Not found!");
            projectTypeComboBox.removeAllItems();
            return;
        }
        Storage.get().projectLocation = selectedFile.getAbsolutePath();
        for (IWordcounter wc : supportedFileTypes) {
            if (wc.matches(selectedFile)) {
                options.add(wc.getName() + " (Detected)");
                generateWordcountPreview(wc, selectedFile);
            }
        }
        for (IWordcounter wc : supportedFileTypes) {
            if (!wc.matches(selectedFile)) {
                options.add(wc.getName());
            }
        }
        //noinspection unchecked
        projectTypeComboBox.setModel(new DefaultComboBoxModel<>(options));
        projectTypeComboBox.setSelectedIndex(0);
    }

    private void generateWordcountPreview(IWordcounter wc, File file) {
        try {
            int wordcount = 0;
            if (wc.needsSecondarySelection()) {
                Map<String, String> map = wc.getSecondarySelectionOptions(file);
                secondarySelectionPanel.setVisible(true);
                //noinspection unchecked
                scrivenerFolderComboBox.setModel(new DefaultComboBoxModel<>(new Vector<>(map.keySet())));
                if (!map.values().isEmpty()) {
                    wordcount = wc.getWordcount(file, map.values().stream().findFirst().get());
                }
            } else {
                wordcount = wc.getWordcount(file);
            }
            wordcountPreviewField.setText(String.valueOf(wordcount));
        } catch (Exception e) {
            e.printStackTrace();
            wordcountPreviewField.setText("Error!");
        }
    }

    private void updateWordcountPreviewForScrivenerFolder(IWordcounter[] supportedFileTypes, Vector<String> options, File projectLocation) {
        for (IWordcounter wc : supportedFileTypes) {
            if (options.get(projectTypeComboBox.getSelectedIndex()).startsWith(wc.getName())) {
                try {
                    //noinspection SuspiciousMethodCalls
                    wordcountPreviewField.setText(
                            String.valueOf(wc.getWordcount(projectLocation,
                                    wc.getSecondarySelectionOptions(projectLocation)
                                            .get(scrivenerFolderComboBox.getSelectedItem()))));
                } catch (Exception e1) {
                    e1.printStackTrace();
                    wordcountPreviewField.setText("Error!");
                }
                break;
            }
        }
    }

    void hideWelcomeTab() {
        mainTabbedPane.remove(welcomeTab);
    }

    private JPanel generateWordgoalLine(WordGoal wg) {
        JPanel panel = new JPanel(new FlowLayout());

        JTextField nameField = new JTextField(wg.getName());
        nameField.setPreferredSize(new Dimension(100, 30));
        nameField.setMaximumSize(new Dimension(500, 30));
        nameField.addCaretListener(e -> wg.setName(nameField.getText()));

        JComboBox<String> typeBox = new JComboBox<>(new String[]{"Fixed goal", "Repeating Goal"});
        typeBox.addActionListener(e -> wg.setType(typeBox.getSelectedIndex() == 0 ? WordGoal.Type.FIXED : WordGoal.Type.REPEATING));

        JSpinner goalField = new JSpinner(new SpinnerNumberModel(wg.getTargetWordcount(), 0, 2_000_000, 1));
        goalField.addChangeListener(e -> wg.setTargetWordcount((int) goalField.getValue()));

        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(e -> {
            Storage.get().customWordGoals.remove(wg);
            wordgoalsPanel.remove(panel);
            wordgoalsPanel.revalidate();
        });

        panel.add(nameField);
        panel.add(typeBox);
        panel.add(new JLabel("Words:"));
        panel.add(goalField);
        panel.add(deleteButton);
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        return panel;
    }

    // GUI initializer generated by IntelliJ IDEA GUI Designer
    // >>> IMPORTANT!! <<<
    // DO NOT EDIT OR ADD ANY CODE HERE!

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        mainTabbedPane = new JTabbedPane();
        contentPane.add(mainTabbedPane, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 411), null, 0, false));
        welcomeTab = new JPanel();
        welcomeTab.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        mainTabbedPane.addTab("Welcome", welcomeTab);
        final JTextArea textArea1 = new JTextArea();
        textArea1.setEditable(false);
        textArea1.setLineWrap(false);
        textArea1.setText("Thank you for giving my NaNoWriMoTracker a shot!\n\nPlease take a minute to configure the software properly.\n\nEnjoy the month!");
        welcomeTab.add(textArea1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, -1));
        mainTabbedPane.addTab("Project Location", panel1);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Project Type");
        panel2.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 19), null, 0, false));
        projectTypeComboBox = new JComboBox();
        panel2.add(projectTypeComboBox, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 30), null, 0, false));
        wordcountPreviewField = new JTextField();
        wordcountPreviewField.setEditable(false);
        panel2.add(wordcountPreviewField, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, 1, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(120, 30), new Dimension(120, -1), 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Project Location");
        panel3.add(label2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 19), null, 0, false));
        projectLocationField = new JTextField();
        projectLocationField.setEditable(false);
        panel3.add(projectLocationField, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 30), null, 0, false));
        selectProjectLocationButton = new JButton();
        selectProjectLocationButton.setText("...");
        panel3.add(selectProjectLocationButton, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, 1, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(80, 30), new Dimension(50, -1), 0, false));
        secondarySelectionPanel = new JPanel();
        secondarySelectionPanel.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(secondarySelectionPanel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Scrivener Folder");
        secondarySelectionPanel.add(label3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        scrivenerFolderComboBox = new JComboBox();
        secondarySelectionPanel.add(scrivenerFolderComboBox, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(3, 3, new Insets(0, 0, 0, 0), -1, -1));
        mainTabbedPane.addTab("Wordcount", panel4);
        final JLabel label4 = new JLabel();
        label4.setText("Update my wordcount every");
        panel4.add(label4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(265, 19), null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel4.add(spacer2, new GridConstraints(2, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        timeBetweenUpdatesUnitComboBox = new JComboBox();
        timeBetweenUpdatesUnitComboBox.setEditable(false);
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        defaultComboBoxModel1.addElement("Seconds");
        defaultComboBoxModel1.addElement("Minutes");
        defaultComboBoxModel1.addElement("Hours");
        defaultComboBoxModel1.addElement("Days");
        timeBetweenUpdatesUnitComboBox.setModel(defaultComboBoxModel1);
        panel4.add(timeBetweenUpdatesUnitComboBox, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(58, 30), null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Subtract this many words before submitting");
        panel4.add(label5, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(265, 19), null, 0, false));
        wordcountOffsetSpinner = new JSpinner();
        panel4.add(wordcountOffsetSpinner, new GridConstraints(1, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 4, false));
        timeBetweenUpdatesSpinner = new JSpinner();
        panel4.add(timeBetweenUpdatesSpinner, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(100, -1), new Dimension(159, 30), new Dimension(100, -1), 4, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, -1));
        mainTabbedPane.addTab("Server", panel5);
        privateSettingsPanel = new JPanel();
        privateSettingsPanel.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel5.add(privateSettingsPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        privateServerAddressField = new JTextField();
        privateServerAddressField.setEnabled(false);
        privateServerAddressField.setText("");
        privateSettingsPanel.add(privateServerAddressField, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        privateServerTestButton = new JButton();
        privateServerTestButton.setEnabled(true);
        privateServerTestButton.setText("Test Connection");
        privateSettingsPanel.add(privateServerTestButton, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setEnabled(true);
        label6.setText("Private Server Address");
        label6.setVerticalAlignment(3);
        label6.setVerticalTextPosition(3);
        privateSettingsPanel.add(label6, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
        panel5.add(panel6, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        offlineRadioButton = new JRadioButton();
        offlineRadioButton.setText("Offline");
        panel6.add(offlineRadioButton, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Mode:");
        panel6.add(label7, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        officialServerRadioButton = new JRadioButton();
        officialServerRadioButton.setSelected(true);
        officialServerRadioButton.setText("Official Server");
        panel6.add(officialServerRadioButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        privateServerRadioButton = new JRadioButton();
        privateServerRadioButton.setEnabled(true);
        privateServerRadioButton.setText("Private Server");
        panel6.add(privateServerRadioButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel5.add(spacer3, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        officialSettingsPanel = new JPanel();
        officialSettingsPanel.setLayout(new GridLayoutManager(5, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel5.add(officialSettingsPanel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        officialUsernameField = new JTextField();
        officialSettingsPanel.add(officialUsernameField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        officialSecretKeyField = new JTextField();
        officialSettingsPanel.add(officialSecretKeyField, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setText("Username");
        officialSettingsPanel.add(label8, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label9 = new JLabel();
        label9.setText("Secret Key");
        officialSettingsPanel.add(label9, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label10 = new JLabel();
        label10.setText("To aquire the Secret Key, visit to http://nanowrimo.org/api/currentWordcount .");
        officialSettingsPanel.add(label10, new GridConstraints(3, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new BorderLayout(0, 0));
        officialSettingsPanel.add(panel7, new GridConstraints(4, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        testConnectionButton = new JButton();
        testConnectionButton.setText("Test Connection");
        panel7.add(testConnectionButton, BorderLayout.WEST);
        connectionTestResultField = new JTextField();
        connectionTestResultField.setEditable(false);
        panel7.add(connectionTestResultField, BorderLayout.CENTER);
        final JLabel label11 = new JLabel();
        label11.setText("The Secret Key is NOT your password");
        officialSettingsPanel.add(label11, new GridConstraints(2, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel8 = new JPanel();
        panel8.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        mainTabbedPane.addTab("Wordgoals", panel8);
        wordgoalsPanel = new JPanel();
        wordgoalsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        panel8.add(wordgoalsPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        addWordgoalButton = new JButton();
        addWordgoalButton.setText("Add Wordgoal");
        wordgoalsPanel.add(addWordgoalButton);
        final Spacer spacer4 = new Spacer();
        panel8.add(spacer4, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel9 = new JPanel();
        panel9.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel9, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        closeButton = new JButton();
        closeButton.setText("Close");
        panel9.add(closeButton, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer5 = new Spacer();
        panel9.add(spacer5, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JLabel label12 = new JLabel();
        label12.setText("Your settings are automatically saved.");
        panel9.add(label12, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer6 = new Spacer();
        panel9.add(spacer6, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        ButtonGroup buttonGroup;
        buttonGroup = new ButtonGroup();
        buttonGroup.add(offlineRadioButton);
        buttonGroup.add(officialServerRadioButton);
        buttonGroup.add(privateServerRadioButton);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

}
