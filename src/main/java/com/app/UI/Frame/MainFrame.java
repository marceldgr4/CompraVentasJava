package com.app.UI.Frame;

import Infrastructure.security.SessionManager;
import com.app.UI.Components.NavButton;
import com.app.Service.AuthService;
import com.app.UI.Panel.ArticlePanel;
import com.app.UI.Panel.PawnPanel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class MainFrame extends JFrame {
    private static final int SIDEBAR_EXPANDED = 220;
    private static final int SIDEBAR_COLLAPSED = 64;

    private static final String PANEL_DASHBOARD = "Dashboard";
    private static final String PANEL_ARTICLES = "Article";
    private static final String PANEL_PAWNS = "Pawns";
    private static final String PANEL_CLIENTS = "Client";
    private static final String PANEL_PROFILES = "Empleados";
    private static final String PANEL_SALES = "Sales";

    // Sidebar colors
    private static final Color SIDEBAR_BG = new Color(20, 30, 60);
    private static final Color SIDEBAR_HOVER = new Color(35, 50, 95);
    private static final Color SIDEBAR_SELECTED = new Color(25, 118, 210);
    private static final Color TOPBAR_BG = new Color(25, 118, 210);

    private JPanel sidebar;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private boolean sidebarExpanded = true;

    private final java.util.List<Object> navButtons = new ArrayList<>();
    private final ButtonGroup navGroup = new ButtonGroup();
    private final AuthService authService = new AuthService();

    private JTabbedPane tabs;
    private JLabel lblUser;
    private JButton btnLogout;



    public MainFrame() {
        initComponents();
        selectPanel(PANEL_DASHBOARD);
    }

    private void initComponents() {
        setTitle("CompraVenta");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 650);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(900, 550));
        setLocationRelativeTo(null);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(buidTopBar(), BorderLayout.NORTH);
        getContentPane().add(buildSidebar(), BorderLayout.WEST);
        getContentPane().add(buildContent(), BorderLayout.CENTER);

    }
    private JPanel buidTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(TOPBAR_BG);
        bar.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        // Left: hamburger + app name

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        JButton btnHamburger = new JButton("☰");
        btnHamburger.setFont(new Font("Segeo UI", Font.BOLD, 16));
        btnHamburger.setForeground(Color.WHITE);
        btnHamburger.setBackground(TOPBAR_BG);
        btnHamburger.setBorderPainted(false);
        btnHamburger.setFocusPainted(false);
        btnHamburger.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnHamburger.setToolTipText("Expandir / Collapse menú");
        btnHamburger.addActionListener(e -> toggleSidebar());

        JLabel lblApp = new JLabel("CompraVenta");
        lblApp.setFont(new Font("Segeo UI", Font.BOLD, 18));
        lblApp.setForeground(Color.WHITE);

        left.add(btnHamburger);
        left.add(lblApp);
        // Right: user info + logout
        SessionManager session = SessionManager.getInstance();
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);

        JLabel lblUser = new JLabel(session.getFullName() + " | " + (session.isAdmin() ? "Administrador" : "Empleado"));

        lblUser.setFont(new Font("Segeo UI", Font.PLAIN, 13));
        lblUser.setForeground(new Color(200,230,255));
        JButton btnLogout = new JButton("Logout");
        btnLogout.setFont(new Font("Segeo UI", Font.PLAIN, 13));
        btnLogout.setBackground(new Color(239,83,80));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setBorderPainted(false);
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
        sidebar.setPreferredSize(new Dimension(SIDEBAR_EXPANDED,0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(8,0,8,0));

        addNavItem("🏠", "Dashboard", PANEL_DASHBOARD);
        addNavItem("📦", "Artículos",  PANEL_ARTICLES);
        addNavItem("🤝", "Empeños",    PANEL_PAWNS);
        addNavItem("💰", "Ventas",     PANEL_SALES);
        addNavItem("👤", "Clientes",   PANEL_CLIENTS);
        if (SessionManager.getInstance().isAdmin()) {
            addNavItem("👥", "Perfiles", PANEL_PROFILES);
        }
        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    private void addNavItem(String icon, String label, String panelId) {
        NavButton btn = new NavButton (icon, label);
        navGroup.add(btn);
        navButtons.add(btn);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE,52));
        btn.addActionListener(e-> selectPanel(panelId));
        sidebar.add(btn);
    }
    // ---- Content area ----
    private JPanel buildContent() {
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(new Color(245,247,250));

        contentPanel.add(new DashboardPanel(), PANEL_DASHBOARD);
        contentPanel.add(new ArticlePanel(), PANEL_ARTICLES);
        contentPanel.add(new PawnPanel(), PANEL_PAWNS);
        contentPanel.add(buildPlaceholder("💰 Ventas — próximamente"), PANEL_SALES);
        contentPanel.add(new ClientePanel(),PANEL_CLIENTS);

        if (SessionManager.getInstance().isAdmin()) {
            contentPanel.add(buildPlaceholder("Gestion de perfiles"),PANEL_PROFILES);
        }
        return contentPanel;
    }
    private JPanel buildPlaceholder(String text) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245,247,250));
        JLabel lbl = new JLabel(text,SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        lbl.setForeground(new Color(160,160,160));
        panel.add(lbl,BorderLayout.CENTER);
        return panel;
    }
    private void selectPanel(String panelId) {
        cardLayout.show(contentPanel, panelId);
        int index = List.of(PANEL_DASHBOARD,PANEL_ARTICLES,PANEL_PAWNS,PANEL_SALES, PANEL_CLIENTS,PANEL_PROFILES).indexOf(panelId);
        if(index >= 0 && index <navButtons.size()){
            navButtons.get(index).setSelected(true);
        }
    }

    private void toggleSidebar() {
        sidebarExpanded = !sidebarExpanded;
        int width = sidebarExpanded ? SIDEBAR_EXPANDED : SIDEBAR_COLLAPSED;
        sidebar.setPreferredSize(new Dimension(width,0));
        navButtons.forEach(b-> b.setExpanded(sidebarExpanded));
        sidebar.revalidate();
        sidebar.repaint();
    }

    //---Logout----
    private void doLogout() {
        int confirm = JOptionPane.showConfirmDialog(
                this,"¿Seguro que desea cerrar Sesión?");
        if (confirm == JOptionPane.YES_OPTION) {return;}
        authService.logout();
        dispose();
        SwingUtilities.invokeLater(()->new LoginFrame().setVisible(true));
    }
    // ---- NavButton (inner class) ---




}