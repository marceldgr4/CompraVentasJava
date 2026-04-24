package com.app.UI.Panel;

import Infrastructure.security.SessionManager;
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

/**
 * Panel de gestión del inventario de artículos.
 *
 * <p><strong>Corrección de color en botones:</strong>
 * El método local {@code createButton()} con {@code setBackground()} no funcionaba
 * en Windows porque el L&F nativo ignora ese método. Ahora todos los botones
 * se crean con {@link ButtonFactory}, que usa {@code paintComponent} personalizado.
 */
public class ArticlePanel extends JPanel {

    private static final String[] COLUMNS = {
            "Id", "Nombre", "Descripción", "Cantidad", "Precio", "Vendido"
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

    private final ArticleService articleService = new ArticleService();

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
        tableModel.setRowCount(0);
        new LoadTask().execute();
    }

    private void doSearch() {
        String term = txtSearch.getText().trim();
        if (term.isEmpty()) { loadTable(); return; }
        tableModel.setRowCount(0);
        lblStatus.setText("Buscando...");
        new SearchTask(term).execute();
    }

    private void openNewDialog() {
        ArticleDialog dlg = new ArticleDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this), null);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            new CreateTask(dlg.getArticle()).execute();
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
            new EditTask(updated).execute();
        }
    }

    private void doDelete() {
        Article selected = getSelectedArticle();
        if (selected == null) { showWarning("Seleccione un artículo para eliminar."); return; }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "¿Eliminar \"" + selected.getNameArticle() + "\"?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            new DeleteTask(selected.getId()).execute();
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Article getSelectedArticle() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        int    id     = (int)    tableModel.getValueAt(row, 0);
        String name   = (String) tableModel.getValueAt(row, 1);
        String desc   = (String) tableModel.getValueAt(row, 2);
        int    amount = (int)    tableModel.getValueAt(row, 3);
        String price  = tableModel.getValueAt(row, 4).toString().replace("$", "");
        boolean sold  = "Sí".equals(tableModel.getValueAt(row, 5));
        return new Article(id, name, desc, amount, new BigDecimal(price), sold, null);
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
                    a.isSold() ? "Sí" : "No"
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

    // ── SwingWorker tasks ─────────────────────────────────────────────────────

    private class LoadTask extends SwingWorker<List<Article>, Void> {
        @Override protected List<Article> doInBackground() throws Exception {
            return articleService.getAll();
        }
        @Override protected void done() {
            try {
                List<Article> list = get();
                populateTable(list);
                lblStatus.setText(list.size() + " artículo(s) cargado(s)");
            } catch (ExecutionException ex) {
                lblStatus.setText("Error al cargar datos");
                showError("Error al cargar: " + ex.getCause().getMessage());
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private class SearchTask extends SwingWorker<List<Article>, Void> {
        private final String term;
        SearchTask(String term) { this.term = term; }

        @Override protected List<Article> doInBackground() throws Exception {
            return articleService.search(term);
        }
        @Override protected void done() {
            try {
                List<Article> list = get();
                populateTable(list);
                lblStatus.setText(list.size() + " resultado(s)");
            } catch (ExecutionException ex) {
                showError("Error en la búsqueda: " + ex.getCause().getMessage());
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private class CreateTask extends SwingWorker<Void, Void> {
        private final Article article;
        CreateTask(Article a) { this.article = a; }

        @Override protected Void doInBackground() throws Exception {
            articleService.create(article); return null;
        }
        @Override protected void done() {
            try { get(); loadTable(); showSuccess("Artículo creado."); }
            catch (ExecutionException ex) { showError("Error al crear: " + ex.getCause().getMessage()); }
            catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
        }
    }

    private class EditTask extends SwingWorker<Void, Void> {
        private final Article article;
        EditTask(Article a) { this.article = a; }

        @Override protected Void doInBackground() throws Exception {
            articleService.edit(article); return null;
        }
        @Override protected void done() {
            try { get(); loadTable(); showSuccess("Artículo actualizado."); }
            catch (ExecutionException ex) { showError("Error al actualizar: " + ex.getCause().getMessage()); }
            catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
        }
    }

    private class DeleteTask extends SwingWorker<Void, Void> {
        private final int id;
        DeleteTask(int id) { this.id = id; }

        @Override protected Void doInBackground() throws Exception {
            articleService.remove(id); return null;
        }
        @Override protected void done() {
            try { get(); loadTable(); showSuccess("Artículo eliminado."); }
            catch (ExecutionException ex) { showError("Error al eliminar: " + ex.getCause().getMessage()); }
            catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
        }
    }
}