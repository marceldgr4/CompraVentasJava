package com.app.UI.Panel;

import com.app.Infrastructure.security.SessionManager;
import com.app.Controllers.ArticleController;
import com.app.Model.domain.Article;
import com.app.Service.ArticleService;
import com.app.Service.exceptions.ServiceException;
import com.app.UI.Components.ButtonFactory;
import com.app.UI.dialogs.ArticleDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ArticlePanel extends BasePanel {

    private static final String[] COLUMNS = {
            "Id", "Nombre", "Descripción", "Cantidad", "Precio", "Estado"
    };

    private JTable             table;
    private DefaultTableModel  tableModel;
    private JTextField         txtSearch;
    private JButton            btnNew;
    private JButton            btnEdit;
    private JButton            btnDelete;
    private JLabel             lblStatus;

    private final ArticleController articleController = new ArticleController();

    public ArticlePanel() {
        super();
        configurePermissions();
        refresh();
    }

    @Override
    protected void initComponents() {
        // Top Bar: Header + Search/Actions
        JPanel topPanel = new JPanel(new BorderLayout(0, 12));
        topPanel.setOpaque(false);
        topPanel.add(buildHeader("Inventario de Artículos", "Gestione el stock, precios y categorías de sus productos"), BorderLayout.NORTH);
        
        com.app.UI.Components.ResponsivePanel actionsBar = new com.app.UI.Components.ResponsivePanel();
        
        txtSearch = new JTextField();
        actionsBar.addFilterComponent(buildSearchPanel("Buscar artículo...", txtSearch, e -> doSearch()));
        buildButtonPanel(actionsBar);
        
        topPanel.add(actionsBar, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        styleTable(table);
        
        // Column widths
        table.getColumnModel().getColumn(0).setMaxWidth(60);
        table.getColumnModel().getColumn(3).setMaxWidth(80);
        table.getColumnModel().getColumn(5).setMaxWidth(100);

        add(createTableScroll(table), BorderLayout.CENTER);

        // Status
        lblStatus = new JLabel("Listo");
        lblStatus.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblStatus.setForeground(SUBTITLE_FG);
        add(lblStatus, BorderLayout.SOUTH);
    }

    private void buildButtonPanel(com.app.UI.Components.ResponsivePanel actionsBar) {
        btnNew     = ButtonFactory.createPrimaryButton("Nuevo", "add");
        btnEdit    = ButtonFactory.createWarningButton("Editar", "edit");
        btnDelete  = ButtonFactory.createDangerButton("Eliminar", "delete");
        JButton btnRefresh = ButtonFactory.createNeutralButton("Actualizar", "refresh");

        btnNew    .addActionListener(e -> openNewDialog());
        btnEdit   .addActionListener(e -> openEditDialog());
        btnDelete .addActionListener(e -> doDelete());
        btnRefresh.addActionListener(e -> refresh());

        actionsBar.addActionComponent(btnNew);
        actionsBar.addActionComponent(btnEdit);
        actionsBar.addActionComponent(btnDelete);
        actionsBar.addActionComponent(btnRefresh);
    }

    private void configurePermissions() {
        boolean isAdmin = SessionManager.isAdmin();
        btnEdit.setVisible(isAdmin);
        btnDelete.setVisible(isAdmin);
    }

    @Override
    public void refresh() {
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
        ArticleDialog dlg = new ArticleDialog((JFrame) SwingUtilities.getWindowAncestor(this), null);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            articleController.createArticle(dlg.getArticle(), this, r -> refresh(), (m, e) -> showError(m));
        }
    }

    private void openEditDialog() {
        Article selected = getSelectedArticle();
        if (selected == null) { showWarning("Seleccione un artículo para editar."); return; }

        ArticleDialog dlg = new ArticleDialog((JFrame) SwingUtilities.getWindowAncestor(this), selected);
        dlg.setVisible(true);

        if (dlg.isConfirmed()) {
            Article updated = dlg.getArticle();
            updated.setId(selected.getId());
            articleController.editArticle(updated, this, this::refresh, (m, e) -> showError(m));
        }
    }

    private void doDelete() {
        Article selected = getSelectedArticle();
        if (selected == null) { showWarning("Seleccione un artículo para eliminar."); return; }

        if (showConfirmation("¿Está seguro de eliminar el artículo '" + selected.getNameArticle() + "'?", "Confirmar Eliminación")) {
            articleController.delete(selected.getId(), selected.getNameArticle(), this, this::refresh, (m, e) -> showError(m));
        }
    }

    private Article getSelectedArticle() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        int id = (int) tableModel.getValueAt(row, 0);
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
                    a.getId(), a.getNameArticle(), a.getDescription(),
                    a.getAmount(), "$" + a.getPrice(), a.getStockStatus()
            });
        }
    }
}