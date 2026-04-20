package com.app.UI.Panel;

import Infrastructure.security.SessionManager;
import com.app.UI.Components.KpiCard;
import com.app.Service.ArticleService;
import com.app.Service.ClienteService;
import com.app.Service.PawnService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;

public class DashboardPanel extends JPanel {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    //KPI card
    private KpiCard cardActivePawn;
    private KpiCard cardOverdue;
    private KpiCard carArticle;
    private KpiCard cardClientes;
    private KpiCard cardTotalValue;
    
    private DefaultTableModel recentTableModel;
    private JLabel lblLastUpdate;
    
    private final PawnService pawnService = new PawnService();
    private final ArticleService articleService = new ArticleService();
    private final ClienteService clienteService = new ClienteService();
    
    public DashboardPanel() {
        initComponents();
        loadStatus();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0,16));
        setBorder(BorderFactory.createEmptyBorder(20,24,20,24));
        setBackground(new Color(245,247,250));

        add(buildWelconHeader(), BorderLayout.NORTH);
        add(buildCenterContent(), BorderLayout.CENTER);
    }
    private JPanel buildWelconHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0,0,16,0));
        String greeting = greetingByHour();
        JLabel lblGreeting = new JLabel(STR."\{greeting}, \{SessionManager.getInstance().getFullName()} ");
        lblGreeting.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblGreeting.setForeground(new Color(30,42,74));

        lblLastUpdate = new JLabel("Actulizar: -");
        lblLastUpdate.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblLastUpdate.setForeground(Color.GRAY);

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRefresh.setBackground(new Color(25,118,210));
        btnRefresh.setForeground(Color.GRAY);
        btnRefresh.setBorderPainted(false);
        btnRefresh.setFocusPainted(false);
        btnRefresh.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> loadStats());

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));
        rightPanel.setOpaque(false);
        rightPanel.add(lblLastUpdate);
        rightPanel.add(btnRefresh);

        panel.add(lblGreeting, BorderLayout.WEST);
        panel.add(rightPanel, BorderLayout.EAST);
        return panel;
    }


}
