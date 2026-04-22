package com.app.UI.Panel;

import Infrastructure.security.SessionManager;
import com.app.UI.Components.KpiCard;
import com.app.Service.ArticleService;
import com.app.Service.ClienteService;
import com.app.Service.PawnService;
import com.app.Utils.CurrencyUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

public class DashboardPanel extends JPanel {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    // KPI cards
    private KpiCard cardActivePawn;
    private KpiCard cardOverdue;
    private KpiCard cardArticle;
    private KpiCard cardClientes;
    private KpiCard cardTotalValue;

    private DefaultTableModel recentTableModel;
    private JLabel lblLastUpdate;

    private final PawnService pawnService = new PawnService();
    private final ArticleService articleService = new ArticleService();
    private final ClienteService clienteService = new ClienteService();

    public DashboardPanel() {
        initComponents();
        loadStats();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        setBackground(new Color(245, 247, 250));

        add(buildWelcomeHeader(), BorderLayout.NORTH);
        add(buildCenterContent(), BorderLayout.CENTER);
    }

    private JPanel buildWelcomeHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        String greeting = greetingByHour();
        String fullName = SessionManager.getInstance().getFullName();
        JLabel lblGreeting = new JLabel(greeting + ", " + (fullName != null ? fullName : "Usuario"));
        lblGreeting.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblGreeting.setForeground(new Color(30, 42, 74));

        lblLastUpdate = new JLabel("Actualizado: -");
        lblLastUpdate.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblLastUpdate.setForeground(Color.GRAY);

        JButton btnRefresh = new JButton("Actualizar");
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRefresh.setBackground(new Color(25, 118, 210));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> loadStats());

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(lblLastUpdate);
        rightPanel.add(btnRefresh);

        panel.add(lblGreeting, BorderLayout.WEST);
        panel.add(rightPanel, BorderLayout.EAST);
        return panel;
    }

    private JScrollPane buildCenterContent() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(false);

        // KPI Panel
        JPanel kpiPanel = new JPanel(new GridLayout(1, 5, 16, 0));
        kpiPanel.setOpaque(false);
        kpiPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        cardActivePawn = new KpiCard("Empeños Activos", "0", new Color(33, 150, 243));
        cardOverdue = new KpiCard("Vencidos", "0", new Color(244, 67, 54));
        cardArticle = new KpiCard("Artículos", "0", new Color(76, 175, 80));
        cardClientes = new KpiCard("Clientes", "0", new Color(255, 152, 0));
        cardTotalValue = new KpiCard("Valor Total", "$0", new Color(156, 39, 176));

        kpiPanel.add(cardActivePawn);
        kpiPanel.add(cardOverdue);
        kpiPanel.add(cardArticle);
        kpiPanel.add(cardClientes);
        kpiPanel.add(cardTotalValue);

        container.add(kpiPanel);
        container.add(Box.createVerticalStrut(24));

        // Info Panel (Quick Actions or Recent Activity could go here)
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));
        
        JLabel lblInfoTitle = new JLabel("Resumen del Sistema");
        lblInfoTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        infoPanel.add(lblInfoTitle, BorderLayout.NORTH);
        
        recentTableModel = new DefaultTableModel(new Object[]{"ID", "Cliente", "Fecha", "Monto", "Estado"}, 0);
        JTable recentTable = new JTable(recentTableModel);
        recentTable.setRowHeight(30);
        infoPanel.add(new JScrollPane(recentTable), BorderLayout.CENTER);

        container.add(infoPanel);

        JScrollPane scroll = new JScrollPane(container);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        return scroll;
    }

    private String greetingByHour() {
        int hour = LocalTime.now().getHour();
        if (hour >= 5 && hour < 12) return "Buenos días";
        if (hour >= 12 && hour < 18) return "Buenas tardes";
        return "Buenas noches";
    }

    private void loadStatus() {
        loadStats();
    }

    private void loadStats() {
        CompletableFuture.runAsync(() -> {
            try {
                // Fetch stats from services
                int activePawns = pawnService.getActivePawns().size();
                int overduePawns = pawnService.getOverduePawns().size();
                int articles = articleService.getAll().size();
                int clientes = clienteService.getAll().size();
                String totalValue = CurrencyUtils.format(pawnService.getTotalActiveValues());

                // Update UI on EDT
                SwingUtilities.invokeLater(() -> {
                    cardActivePawn.setValue(String.valueOf(activePawns));
                    cardOverdue.setValue(String.valueOf(overduePawns));
                    cardArticle.setValue(String.valueOf(articles));
                    cardClientes.setValue(String.valueOf(clientes));
                    cardTotalValue.setValue(totalValue);
                    
                    lblLastUpdate.setText("Actualizado: " + LocalDateTime.now().format(DATE_FORMAT));
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
