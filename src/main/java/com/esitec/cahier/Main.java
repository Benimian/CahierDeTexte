package com.esitec.cahier;

import com.esitec.cahier.ui.LoginFrame;
import com.esitec.cahier.ui.SplashScreen;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            SplashScreen splash = new SplashScreen();
            splash.demarrer(() -> new LoginFrame().setVisible(true));
        });
    }
}
