package org.simonscode.nanowrimotracker;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

class SettingsFrame extends JFrame {

    private final SettingsPanel contentPane = new SettingsPanel();

    SettingsFrame() {
        super("Settings");

        setSize(550, 450);
        setContentPane(contentPane.$$$getRootComponent$$$());

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent event) {
                SettingsFrame.this.setVisible(false);
                if (NaNoWriMoTracker.configIsSufficient()) {
                    Storage.get().firstRun = false;
                    Storage.get().save();
                    NaNoWriMoTracker.switchFromSettingsToLogWindow();
                } else {
                    JOptionPane.showMessageDialog(null, String.format("Configuration incomplete.%nValues have not been saved."), "Configuration incomplete", JOptionPane.WARNING_MESSAGE);
                    NaNoWriMoTracker.shutdown();
                }
            }
        });
        pack();
    }

    void hideWelcomeTab() {
        contentPane.hideWelcomeTab();
    }
}
