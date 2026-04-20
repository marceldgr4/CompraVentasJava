package com.app;

import com.app.UI.Frame.LoginFrame;

import javax.swing.*;

public class App {
    public static void main(String[] args) {


        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new LoginFrame().setVisible(true);
        });
    }
}