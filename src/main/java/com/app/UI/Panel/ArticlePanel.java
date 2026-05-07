package com.app.UI.Panel;

import Infrastructure.security.SessionManager;
import com.app.Controllers.ArticleController;
import com.app.Model.domain.Article;
import com.app.Service.ArticleService;
import com.app.Service.exceptions.ServiceException;
import com.app.UI.Components.ButtonFactory;
import com.app.UI.dialogs.ArticleDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class ArticlePanel extends JPanel {

    private static final String[] COLUMNS = {
            "Id", "Nombre", "Descripción", "Cantidad", "Precio", "Estado"
    };

    private JTable             table;
    private DefaultTableModel  tableModel;
    private JScrollPane        scrollPane;
    private JTextField         txtSearch;
    private JButton            btnNew;
    private JButton            btnEdit;
    private JButton            btnDelete;
    private JButton            btnRefresh;
    private JLabel             lblStatus;

    private final ArticleController articleController = new ArticleController();

    public ArticlePanel() {
        initComponents();
        configurePermissions();
        loadTable();
    }

    // ── UI ────────────────────────────────────────────────────────────────────

    private void initComponents() {
        setLayout(new BorderLayout(0, 0));
        setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        setBackground(new Color(245, 247, 250));

        add(buildTopBar(),   BorderLayout.NORTH);
        add(buildTable(),    BorderLayout.CENTER);
        add(buildStatusBar(),BorderLayout.SOUTH);
    }

    /** Barra superior: búsqueda a la izquierda, botones a la derecha. */
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        bar.add(buildSearchPanel(), BorderLayout.WEST);
        bar.add(buildButtonPanel(), BorderLayout.EAST);
        return bar;
    }

    private JPanel buildSearchPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        panel.setOpaque(false);

        txtSearch = new JTextField(20);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.putClientProperty("JTextField.placeholderText", "Buscar artículo...");
        txtSearch.addActionListener(e -> doSearch());

        // Botón Search — usa ButtonFactory (color azul correcto en Windows)
        JButton btnSearch = ButtonFactory.createPrimaryButton("Buscar");
        btnSearch.addActionListener(e -> doSearch());

        panel.add(new JLabel("Buscar: "));
        panel.add(txtSearch);
        panel.add(btnSearch);
        return panel;
    }

    /** Botones de acción — TODOS creados con ButtonFactory para que el color se vea. */
    private JPanel buildButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        panel.setOpaque(false);

        btnNew     = ButtonFactory.createPrimaryButton("+ Nuevo");
        btnEdit    = ButtonFactory.createWarningButton("Editar");
        btnDelete  = ButtonFactory.createDangerButton("Eliminar");
        btnRefresh = ButtonFactory.createNeutralButton("Actualizar");

        btnNew    .addActionListener(e -> openNewDialog());
        btnEdit   .addActionListener(e -> openEditDialog());
        btnDelete .addActionListener(e -> doDelete());
        btnRefresh.addActionListener(e -> loadTable());

        panel.add(btnNew);
        panel.add(btnEdit);
        panel.add(btnDelete);
        panel.add(btnRefresh);
        return panel;
    }

    private JScrollPane buildTable() {
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(28);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.setGridColor(new Color(230, 230, 230));
        table.setShowGrid(true);
        table.setFillsViewportHeight(true);

        // Anchos de columna
        table.getColumnModel().getColumn(0).setMaxWidth(60);
        table.getColumnModel().getColumn(1).setPreferredWidth(130);
        table.getColumnModel().getColumn(2).setPreferredWidth(200);
        table.getColumnModel().getColumn(3).setMaxWidth(80);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setMaxWidth(70);

        scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(153, 153, 153)));
        return scrollPane;
    }

    private JLabel buildStatusBar() {
        lblStatus = new JLabel("Cargando...");
        lblStatus.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblStatus.setForeground(Color.DARK_GRAY);
        lblStatus.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
        return lblStatus;
    }

    // ── Permisos ──────────────────────────────────────────────────────────────

    private void configurePermissions() {
        boolean isAdmin = SessionManager.isAdmin();
        btnEdit  .setVisible(isAdmin);
        btnDelete.setVisible(isAdmin);
    }

    // ── Operaciones ───────────────────────────────────────────────────────────

    private void loadTable() {
        lblStatus.setText("Cargando...");
        articleController.loadAll(this, 
            list -> {
                populateTable(list);
                lblStatus.setText(list.size() + " artículo(s) cargado(s)");
            },
            (msg, ex) -> {
                lblStatus.setText("Error al cargar datos");
                showError("Error al cargar: " + msg);
            }
        );
    }

    private void doSearch() {
        String term = txtSearch.getText().trim();
        lblStatus.setText("Buscando...");
        articleController.searchArticles(term, this,
            list -> {
                populateTable(list);
                lblStatus.setText(list.size() + " resultado(s)");
            },
            (msg, ex) -> showError("Error en la búsqueda: " + msg)
        );
    }

    private void openNewDialog() {
        ArticleDialog dlg = new ArticleDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this), null);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            articleController.createArticle(dlg.getArticle(), this,
                result -> loadTable(),
                (msg, ex) -> showError("Error al crear: " + msg)
            );
        }
    }

    private void openEditDialog() {
        Article selected = getSelectedArticle();
        if (selected == null) { showWarning("Seleccione un artículo para editar."); return; }

        ArticleDialog dlg = new ArticleDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this), selected);
        dlg.setVisible(true);

        if (dlg.isConfirmed()) {
            Article updated = dlg.getArticle();
            updated.setId(selected.getId());
            articleController.editArticle(updated, this,
                this::loadTable,
                (msg, ex) -> showError("Error al actualizar: " + msg)
            );
        }
    }

    private void doDelete() {
        Article selected = getSelectedArticle();
        if (selected == null) { showWarning("Seleccione un artículo para eliminar."); return; }

        articleController.delete(selected.getId(), selected.getNameArticle(), this,
            this::loadTable,
            (msg, ex) -> showError("Error al eliminar: " + msg)
        );
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Article getSelectedArticle() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        int id = (int) tableModel.getValueAt(row, 0);
        
        // Use service as a last resort for synchronous retrieval in selection 
        // until we implement a better state management in controllers.
        try {
            return new ArticleService().getById(id);
        } catch (ServiceException e) {
            showError("Error: " + e.getMessage());
            return null;
        }
    }

    private void populateTable(List<Article> articles) {
        tableModel.setRowCount(0);
        for (Article a : articles) {
            tableModel.addRow(new Object[]{
                    a.getId(),
                    a.getNameArticle(),
                    a.getDescription(),
                    a.getAmount(),
                    "$" + a.getPrice(),
                    a.getStockStatus()

            });
        }
    }

    private void showWarning(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Advertencia", JOptionPane.WARNING_MESSAGE);
    }
    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
    private void showSuccess(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }
}