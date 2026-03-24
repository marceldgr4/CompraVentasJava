package com.app;


import com.app.View.LoginForm;

import javax.swing.*;

public class App
{
    public static void main(String[] args) {

        // Verificar conexiones al iniciar
        //ConexionVerificador.verificarTodo();

        // Lanzar UI en el hilo de Swing
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            new LoginForm().setVisible(true);
        });
    }
}