package org.simonscode.nanowrimotracker;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URL;

public class SystemTrayManager {

    private final boolean supported;
    private final TrayIcon icon;
    private final SystemTray tray;
    private static final String iconImageLoc = "file:///mnt/data/projects/NaNoWriMoTracker/src/main/resources/book-solid.png";

    SystemTrayManager() {
        TrayIcon tempIcon = null;

        // ensure awt toolkit is initialized.
        java.awt.Toolkit.getDefaultToolkit();

        supported = SystemTray.isSupported();
        tray = supported ? SystemTray.getSystemTray() : null;
        try {
            if (supported) {
                // set up a system tray icon.
                java.awt.SystemTray tray = java.awt.SystemTray.getSystemTray();
                URL imageLoc = new URL(iconImageLoc);
                java.awt.Image image = ImageIO.read(imageLoc);
//                tempIcon = new java.awt.TrayIcon(image);
//                Image image = Toolkit.getDefaultToolkit().getImage("book-solid.png");
                tempIcon = new TrayIcon(image, "NaNoWriMo Tracker");
                tempIcon.setImageAutoSize(true);
                tray.add(tempIcon);
            }
        } catch (java.awt.AWTException | IOException e) {
            System.err.println("Error initializing tray icon.");
            e.printStackTrace();
        }
        icon = tempIcon;
    }

    public void pushNotification(String title, String body) {
        if (supported) {
            icon.displayMessage(title, body, TrayIcon.MessageType.INFO);
        }
    }

    void close() {
        if (supported) {
            tray.remove(icon);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        SystemTrayManager systemTrayManager = new SystemTrayManager();
        Thread.sleep(5_000);
        systemTrayManager.close();
    }
}
