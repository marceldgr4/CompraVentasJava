package com.app.UI.Panel;

import com.app.Infrastructure.logging.LoggerFactory;
import com.app.Infrastructure.security.SessionManager;
import com.app.Model.Dao.DashBoardDao;
import com.app.Model.domain.DashBoardDto;
import com.app.UI.Components.KpiCard;
import com.app.Utils.CurrencyUtils;
import org.slf4j.Logger;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

public class DashboardPanel extends JPanel {
    private static final Logger log = LoggerFactory.getLogger(DashboardPanel.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    // KPI cards
    private KpiCard cardActivePawn;
    private KpiCard cardOverdue;
    private KpiCard cardArticle;
    private KpiCard cardClientes;
    private KpiCard cardTotalValue;

    private DefaultTableModel recentTableModel;
    private JLabel lblLastUpdate;

    private final DashBoardDao dashBoardDao = new DashBoardDao();

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

        JButton btnRefresh = com.app.UI.Components.ButtonFactory.createPrimaryButton("Actualizar", "refresh");
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
                // Fetch stats efficiently from single database view
                DashBoardDto metrics = dashBoardDao.getDashboardMetric();
                java.util.List<com.app.Model.domain.Pawn> recentPawns = new com.app.Model.Dao.PawnDao().findAll();

                // Update UI on EDT
                SwingUtilities.invokeLater(() -> {
                    cardActivePawn.setValue(String.valueOf(metrics.getActivePawns()));
                    cardOverdue.setValue(String.valueOf(metrics.getOverduePawns()));
                    cardArticle.setValue(String.valueOf(metrics.getTotalArticle()));
                    cardClientes.setValue(String.valueOf(metrics.getTotalClientes()));
                    cardTotalValue.setValue(CurrencyUtils.format(metrics.getTotalActiveValue()));
                    
                    recentTableModel.setRowCount(0);
                    int count = 0;
                    for (com.app.Model.domain.Pawn p : recentPawns) {
                        if (count++ >= 15) break;
                        String client = p.getClientName() != null ? p.getClientName() : "Cliente #" + p.getClientId();
                        String date = p.getPawnDate() != null ? p.getPawnDate().toString() : "";
                        String amount = CurrencyUtils.format(p.getTotal());
                        recentTableModel.addRow(new Object[]{p.getId(), client, date, amount, p.getStatusLabel()});
                    }

                    lblLastUpdate.setText("Actualizado: " + LocalDateTime.now().format(DATE_FORMAT));
                });
            } catch (Exception e) {
                log.error("Error al cargar métricas del dashboard", e);
                SwingUtilities.invokeLater(() -> {
                    lblLastUpdate.setText("Error al actualizar");
                    lblLastUpdate.setForeground(Color.RED);
                });
            }
        });
    }

    public void refresh() {
        loadStats();
    }
}
