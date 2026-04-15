package com.app.View;

import com.app.Componets.ModernButton;
import com.app.Componets.ModernPasswordField;
import com.app.Componets.ModernTextField;
import com.app.Service.AuthService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;


public class LoginForm extends JFrame {

    private ModernTextField txtEmail;
    private ModernPasswordField txtPassword;
    private ModernButton btnLogin;
    private JLabel lblError;
    private final AuthService authService = new AuthService();

    public LoginForm() {
        initComponents();
        setupListeners();
    }

    private void initComponents() {
        setTitle("CompraVenta — Iniciar sesión");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(450, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        // Panel Principal con Gradiente Moderno
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(245, 247, 250), 0, getHeight(), new Color(210, 220, 235));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(50, 60, 50, 60));

        // Título
        JLabel lblTitle = new JLabel("COMPRA VENTA");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 40));
        lblTitle.setForeground(new Color(44, 62, 80));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSubtitle = new JLabel("Gestiona tus ventas fácilmente");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubtitle.setForeground(new Color(127, 140, 141));
        lblSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Inputs
        txtEmail = new ModernTextField("Correo electrónico");
        txtPassword = new ModernPasswordField("Contraseña");

        // Botón
        btnLogin = new ModernButton("Entrar");

        lblError = new JLabel(" ");
        lblError.setForeground(new Color(231, 76, 60));
        lblError.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Agregar al panel con espaciado
        mainPanel.add(lblTitle);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(lblSubtitle);
        mainPanel.add(Box.createVerticalStrut(50));
        mainPanel.add(txtEmail);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(txtPassword);
        mainPanel.add(Box.createVerticalStrut(40));
        mainPanel.add(btnLogin);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(lblError);

        setContentPane(mainPanel);
    }

    private void setupListeners() {
        btnLogin.addActionListener(e -> doLogin());
        txtPassword.addActionListener(e -> doLogin());
    }

    private void doLogin() {
        String email = txtEmail.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            lblError.setText("Por favor, completa los campos.");
            return;
        }

        // Aquí llamarías a tu authService (simulado por ahora)
        System.out.println("Login intent con: " + email);
    }

}