package org.simonscode.nanowrimotracker;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;

public class SystemTrayManager {

    private final boolean supported;
    private final TrayIcon icon;
    private final SystemTray tray;

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
                java.awt.Image image = ImageIO.read(SystemTrayManager.class.getResourceAsStream("/book-solid.gif"));
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

//    public static void main(String[] args) throws InterruptedException {
//        SystemTrayManager systemTrayManager = new SystemTrayManager();
//        systemTrayManager.pushNotification("Title", "Body");
//        Thread.sleep(5_000);
//        systemTrayManager.close();
//    }
}
