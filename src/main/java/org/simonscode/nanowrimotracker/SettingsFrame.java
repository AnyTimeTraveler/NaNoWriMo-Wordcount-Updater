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
                if (NaNoWriMoTracker.configIsSane()) {
                    Config.get().firstRun = false;
                    Config.get().save();
                    NaNoWriMoTracker.switchFromSettingsToLogWindow();
                } else {
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
