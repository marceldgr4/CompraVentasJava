package com.app.View;

import com.app.Model.SesionUser;
import com.app.Service.AuthService;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private JTabbedPane tabs;
    private JLabel      lblUser;
    private JButton     btnLogout;

    private final AuthService authService = new AuthService();

    public MainFrame() {
        initComponents();
    }

    private void initComponents() {
        setTitle("CompraVenta");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 650);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(900, 550));

        // ---- Barra superior ----
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(25, 118, 210));
        topBar.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        JLabel lblApp = new JLabel("CompraVenta");
        lblApp.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblApp.setForeground(Color.WHITE);

        SesionUser sesion = SesionUser.getInstance();
        String rolText = sesion.isAdmin() ? "Admin" : "Empleado";
        lblUser = new JLabel(sesion.getFullName() + "  |  " + rolText);
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblUser.setForeground(new Color(200, 230, 255));

        btnLogout = new JButton("Cerrar sesión");
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLogout.setBackground(new Color(239, 83, 80));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setBorderPainted(false);
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> doLogout());

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(lblUser);
        rightPanel.add(btnLogout);

        topBar.add(lblApp,    BorderLayout.WEST);
        topBar.add(rightPanel, BorderLayout.EAST);

        // ---- Pestañas ----
        tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        tabs.addTab("Artículos",  new ArticlePanel());
        tabs.addTab("Ventas",     buildPlaceholder("Ventas — próximamente"));
        tabs.addTab("Empeños",    buildPlaceholder("Empeños — próximamente"));

        // Pestaña Perfiles solo para Admin
        if (sesion.isAdmin()) {
            tabs.addTab("Perfiles", buildPlaceholder("Gestión de perfiles — próximamente"));
        }

        // ---- Ensamblar ----
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(topBar, BorderLayout.NORTH);
        getContentPane().add(tabs,   BorderLayout.CENTER);
    }

    private JPanel buildPlaceholder(String text) {
        JPanel p = new JPanel(new BorderLayout());
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        l.setForeground(new Color(150, 150, 150));
        p.add(l, BorderLayout.CENTER);
        return p;
    }

    private void doLogout() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "¿Seguro que deseas cerrar sesión?",
                "Cerrar sesión",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        authService.logout();
        dispose();
       // new LoginForm().setVisible(true);
    }
}