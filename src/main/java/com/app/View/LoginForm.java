package com.app.View;

import com.app.Service.AuthService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LoginForm extends JFrame {

    private JTextField     txtEmail;
    private JPasswordField txtPassword;
    private JButton        btnLogin;
    private JLabel         lblError;
    private JLabel         lblLoading;

    private final AuthService authService = new AuthService();

    public LoginForm() {
        initComponents();
        setupListeners();
    }

    private void initComponents() {
        setTitle("CompraVenta — Iniciar sesión");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setSize(420, 500);
        setLocationRelativeTo(null);

        // Panel principal
        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBackground(Color.WHITE);
        main.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));

        // Título
        JLabel lblTitle = new JLabel("CompraVenta");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(new Color(30, 136, 229));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSubtitle = new JLabel("Ingresa tus credenciales");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubtitle.setForeground(new Color(120, 120, 120));
        lblSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Campo email
        JLabel lblEmail = new JLabel("Correo electrónico");
        lblEmail.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblEmail.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtEmail = new JTextField();
        txtEmail.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtEmail.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        txtEmail.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));

        // Campo contraseña
        JLabel lblPass = new JLabel("Contraseña");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPass.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtPassword = new JPasswordField();
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));

        // Botón login
        btnLogin = new JButton("Iniciar sesión");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setBackground(new Color(30, 136, 229));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnLogin.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Mensaje de error
        lblError = new JLabel(" ");
        lblError.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblError.setForeground(new Color(200, 50, 50));
        lblError.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Indicador de carga
        lblLoading = new JLabel("Verificando...");
        lblLoading.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblLoading.setForeground(new Color(100, 100, 100));
        lblLoading.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblLoading.setVisible(false);

        // Ensamblar
        main.add(lblTitle);
        main.add(Box.createVerticalStrut(6));
        main.add(lblSubtitle);
        main.add(Box.createVerticalStrut(36));
        main.add(lblEmail);
        main.add(Box.createVerticalStrut(6));
        main.add(txtEmail);
        main.add(Box.createVerticalStrut(18));
        main.add(lblPass);
        main.add(Box.createVerticalStrut(6));
        main.add(txtPassword);
        main.add(Box.createVerticalStrut(24));
        main.add(btnLogin);
        main.add(Box.createVerticalStrut(12));
        main.add(lblError);
        main.add(lblLoading);

        setContentPane(main);
    }

    private void setupListeners() {
        btnLogin.addActionListener(e -> doLogin());

        // Enter en cualquier campo dispara login
        txtEmail.addActionListener(e -> txtPassword.requestFocus());
        txtPassword.addActionListener(e -> doLogin());
    }

    private void doLogin() {
        String email    = txtEmail.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            showError("Completa todos los campos.");
            return;
        }

        setLoading(true);

        // Ejecutar en hilo separado para no bloquear la UI
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                authService.Login(email, password);
                return null;
            }

            @Override
            protected void done() {
                setLoading(false);
                try {
                    get();  // lanza la excepción si doInBackground falló
                    onLoginSuccess();
                } catch (Exception ex) {
                    String msg = ex.getCause() != null
                            ? ex.getCause().getMessage()
                            : ex.getMessage();
                    showError(msg);
                    txtPassword.setText("");
                }
            }
        };

        worker.execute();
    }

    private void onLoginSuccess() {
        dispose();
        new MainFrame().setVisible(true);
    }

    private void showError(String msg) {
        lblError.setText(msg);
    }

    private void setLoading(boolean loading) {
        btnLogin.setEnabled(!loading);
        lblLoading.setVisible(loading);
        lblError.setText(" ");
    }
}