package com.app;

import com.app.Config.ConexionVerificador;
import com.app.View.LoginForm;

import javax.swing.*;

public class App {
    public static void main(String[] args) {


        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new LoginForm().setVisible(true);
        });
    }
}