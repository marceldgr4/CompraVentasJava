package com.app.UI.Frame;

import Infrastructure.security.SessionManager;
import com.app.UI.Components.NavButton;
import com.app.Service.AuthService;
import com.app.UI.Panel.ArticlePanel;
import com.app.UI.Panel.DashboardPanel;
import com.app.UI.Panel.PawnPanel;
import com.app.UI.Panel.ClientePanel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {
    private static final int SIDEBAR_EXPANDED = 220;
    private static final int SIDEBAR_COLLAPSED = 64;

    private static final String PANEL_DASHBOARD = "Dashboard";
    private static final String PANEL_ARTICLES = "Article";
    private static final String PANEL_PAWNS = "Pawns";
    private static final String PANEL_CLIENTS = "Client";
    private static final String PANEL_PROFILES = "Empleados";
    private static final String PANEL_SALES = "Sales";

    private static final Color SIDEBAR_BG = new Color(20, 30, 60);
    private static final Color TOPBAR_BG = new Color(25, 118, 210);

    private JPanel sidebar;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private boolean sidebarExpanded = true;

    private final List<NavButton> navButtons = new ArrayList<>();
    private final ButtonGroup navGroup = new ButtonGroup();
    private final AuthService authService = new AuthService();

    public MainFrame() {
        initComponents();
        selectPanel(PANEL_DASHBOARD);
    }

    private void initComponents() {
        setTitle("CompraVenta — Sistema de Gestión");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 750);
        setMinimumSize(new Dimension(1000, 600));
        setLocationRelativeTo(null);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(buildTopBar(), BorderLayout.NORTH);
        getContentPane().add(buildSidebar(), BorderLayout.WEST);
        getContentPane().add(buildContent(), BorderLayout.CENTER);
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(TOPBAR_BG);
        bar.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        // Left: hamburguesa + app name
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);
        
        JButton btnHamburger = new JButton("☰");
        btnHamburger.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnHamburger.setForeground(Color.WHITE);
        btnHamburger.setBackground(TOPBAR_BG);
        btnHamburger.setBorderPainted(false);
        btnHamburger.setFocusPainted(false);
        btnHamburger.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnHamburger.addActionListener(e -> toggleSidebar());

        JLabel lblApp = new JLabel("CompraVenta");
        lblApp.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblApp.setForeground(Color.WHITE);

        left.add(btnHamburger);
        left.add(lblApp);

        // Right: info de usuario + logout
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        right.setOpaque(false);

        String userName = SessionManager.getInstance().getFullName();
        String role = SessionManager.isAdmin() ? "Administrador" : "Empleado";
        JLabel lblUser = new JLabel(userName + " | " + role);
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblUser.setForeground(new Color(230, 242, 255));

        JButton btnLogout = new JButton("Cerrar Sesión");
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLogout.setBackground(new Color(239, 83, 80));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> doLogout());

        right.add(lblUser);
        right.add(btnLogout);

        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);

        return bar;
    }

    private JPanel buildSidebar() {
        sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(SIDEBAR_EXPANDED, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));

        addNavItem("🏠", "Dashboard", PANEL_DASHBOARD);
        addNavItem("📦", "Artículos", PANEL_ARTICLES);
        addNavItem("🤝", "Empeños", PANEL_PAWNS);
        addNavItem("💰", "Ventas", PANEL_SALES);
        addNavItem("👤", "Clientes", PANEL_CLIENTS);
        
        if (SessionManager.isAdmin()) {
            addNavItem("👥", "Perfiles", PANEL_PROFILES);
        }
        
        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    private void addNavItem(String icon, String label, String panelId) {
        NavButton btn = new NavButton(icon, label);
        navGroup.add(btn);
        navButtons.add(btn);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        btn.addActionListener(e -> selectPanel(panelId));
        sidebar.add(btn);
    }

    private JPanel buildContent() {
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(new Color(245, 247, 250));

        contentPanel.add(new DashboardPanel(), PANEL_DASHBOARD);
        contentPanel.add(new ArticlePanel(), PANEL_ARTICLES);
        contentPanel.add(new PawnPanel(), PANEL_PAWNS);
        contentPanel.add(new ClientePanel(), PANEL_CLIENTS);
        contentPanel.add(buildPlaceholder("Módulo de Ventas — Próximamente"), PANEL_SALES);

        if (SessionManager.isAdmin()) {
            // Placeholder hasta que ProfilePanel esté listo
            contentPanel.add(buildPlaceholder("Gestión de Perfiles"), PANEL_PROFILES);
        }
        
        return contentPanel;
    }

    private JPanel buildPlaceholder(String text) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 247, 250));
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.ITALIC, 18));
        lbl.setForeground(new Color(180, 180, 180));
        panel.add(lbl, BorderLayout.CENTER);
        return panel;
    }

    private void selectPanel(String panelId) {
        cardLayout.show(contentPanel, panelId);
        // Actualizar estado visual de los botones si es necesario
    }

    private void toggleSidebar() {
        sidebarExpanded = !sidebarExpanded;
        int width = sidebarExpanded ? SIDEBAR_EXPANDED : SIDEBAR_COLLAPSED;
        sidebar.setPreferredSize(new Dimension(width, 0));
        navButtons.forEach(b -> b.setExpanded(sidebarExpanded));
        sidebar.revalidate();
        sidebar.repaint();
    }

    private void doLogout() {
        int confirm = JOptionPane.showConfirmDialog(
                this, 
                "¿Seguro que desea cerrar sesión?", 
                "Confirmar Salida", 
                JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            authService.logout();
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        }
    }
}