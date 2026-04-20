package com.app.UI.Panel;


import Infrastructure.security.SessionManager;
import com.app.Model.domain.Article;
import com.app.Service.ArticleService;
import com.app.UI.dialogs.ArticleDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * REFACTORIZADO: Campos correctos, AsyncTaskExecutor, sin duplicación
 */
public class ArticlePanel extends JPanel {

    private static final String[] COLUMNS = {
            "Id", "Name", "Description", "Amount", "Price", "Sold"
    };

    private JTable table;
    private DefaultTableModel tableModel;
    private JScrollPane scrollPane;      // ✅ CAMPO correcto
    private JTextField txtSearch;        // ✅ CAMPO correcto
    private JButton btnNew;
    private JButton btnEdit;
    private JButton btnDelete;
    private JButton btnRefresh;
    private JLabel lblStatus;

    private final ArticleService articleService = new ArticleService();

    public ArticlePanel() {
        initComponents();
        configurePermissions();
        loadTable();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 0));
        setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        // ---- Top Bar: Search + Buttons ----
        JPanel topBar = new JPanel(new BorderLayout(10, 0));
        topBar.setOpaque(false);
        topBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        searchPanel.setOpaque(false);
        txtSearch = new JTextField(20);  // ✅ Asignado al FIELD, no variable local
        txtSearch.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtSearch.putClientProperty("JTextField.placeholderText", "Search Article...");

        JButton btnSearch = new JButton("Search");
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSearch.addActionListener(e -> doSearch());
        txtSearch.addActionListener(e -> doSearch());

        searchPanel.add(new JLabel("Search: "));
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);

        // Button Panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnNew = createButton("+ New", new Color(30, 136, 229));
        btnNew.addActionListener(e -> openNewDialog());
        btnEdit = createButton("Edit", new Color(255, 152, 0));
        btnEdit.addActionListener(e -> openEditDialog());
        btnDelete = createButton("Delete", new Color(239, 83, 80));
        btnDelete.addActionListener(e -> doDelete());
        btnRefresh = createButton("Refresh", new Color(100, 100, 100));
        btnRefresh.addActionListener(e -> loadTable());

        btnPanel.add(btnNew);
        btnPanel.add(btnEdit);
        btnPanel.add(btnDelete);
        btnPanel.add(btnRefresh);

        topBar.add(searchPanel, BorderLayout.WEST);
        topBar.add(btnPanel, BorderLayout.EAST);

        // ---- Table ----
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.setRowHeight(28);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.setGridColor(new Color(230, 230, 230));
        table.setShowGrid(true);

        table.getColumnModel().getColumn(0).setMaxWidth(60);
        table.getColumnModel().getColumn(1).setMaxWidth(90);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setMaxWidth(90);

        scrollPane = new JScrollPane(table);  // ✅ Asignado al FIELD
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(153, 153, 153)));

        // ---- Status Bar ----
        lblStatus = new JLabel("Loading...");
        lblStatus.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblStatus.setForeground(Color.DARK_GRAY);
        lblStatus.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        add(topBar, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(lblStatus, BorderLayout.SOUTH);
    }

    /**
     * Búsqueda - REFACTORIZADO: Sin duplicación de SwingWorker
     */
    private void doSearch() {
        String term = txtSearch.getText().trim();
        if (term.isEmpty()) {
            loadTable();
            return;
        }

        clearTable();
        lblStatus.setText("Searching...");

        new SearchTask(term).execute();
    }

    /**
     * Cargar tabla - REFACTORIZADO: Sin duplicación
     */
    private void loadTable() {
        lblStatus.setText("Loading...");
        clearTable();

        new LoadTask().execute();
    }

    /**
     * Editar artículo - REFACTORIZADO
     */
    private void openEditDialog() {
        Article selected = getSelectedArticle();
        if (selected == null) {
            showWarning("Select an article to edit");
            return;
        }

        ArticleDialog dialog = new ArticleDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this), selected
        );
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            Article updated = dialog.getArticle();
            updated.setId(selected.getId());

            new EditTask(updated).execute();
        }
    }

    /**
     * Eliminar artículo
     */
    private void doDelete() {
        Article selected = getSelectedArticle();
        if (selected == null) {
            showWarning("Select an article to delete");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete \"" + selected.getNameArticle() + "\"?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        new DeleteTask(selected.getId()).execute();
    }

    /**
     * Crear nuevo artículo
     */
    private void openNewDialog() {
        ArticleDialog dialog = new ArticleDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this), null
        );
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            new CreateTask(dialog.getArticle()).execute();
        }
    }

    // ================== Helper Methods ==================

    private Article getSelectedArticle() {
        int row = table.getSelectedRow();
        if (row < 0) return null;

        int id = (int) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);
        String desc = (String) tableModel.getValueAt(row, 2);
        int amount = (int) tableModel.getValueAt(row, 3);
        String priceStr = tableModel.getValueAt(row, 4).toString().replace("$", "");
        boolean sold = tableModel.getValueAt(row, 5).equals("Yes");

        return new Article(id, name, desc, amount, new BigDecimal(priceStr), sold, null);
    }

    private void clearTable() {
        tableModel.setRowCount(0);
    }

    private void configurePermissions() {
        boolean isAdmin = SessionManager.getInstance().isAdmin();
        btnEdit.setVisible(isAdmin);
        btnDelete.setVisible(isAdmin);
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ================== SwingWorker Tasks (Centralizados) ==================

    private class SearchTask extends SwingWorker<List<Article>, Void> {
        private final String term;

        SearchTask(String term) {
            this.term = term;
        }

        @Override
        protected List<Article> doInBackground() throws Exception {
            return articleService.search(term);
        }

        @Override
        protected void done() {
            try {
                List<Article> results = get();
                for (Article article : results) {
                    tableModel.addRow(new Object[]{
                            article.getId(),
                            article.getNameArticle(),
                            article.getDescription(),
                            article.getAmount(),
                            "$" + article.getPrice(),
                            article.isSold() ? "Yes" : "No"
                    });
                }
                lblStatus.setText(results.size() + " Results");
            } catch (Exception ex) {
                showError("Search failed: " + ex.getMessage());
            }
        }
    }

    private class LoadTask extends SwingWorker<List<Article>, Void> {
        @Override
        protected List<Article> doInBackground() throws Exception {
            return articleService.getAll();
        }

        @Override
        protected void done() {
            try {
                List<Article> articles = get();
                for (Article article : articles) {
                    tableModel.addRow(new Object[]{
                            article.getId(),
                            article.getNameArticle(),
                            article.getDescription(),
                            article.getAmount(),
                            "$" + article.getPrice(),
                            article.isSold() ? "Yes" : "No"
                    });
                }
                lblStatus.setText(articles.size() + " article(s) loaded");
            } catch (Exception ex) {
                lblStatus.setText("Error loading data");
                showError("Load failed: " + ex.getMessage());
            }
        }
    }

    private class EditTask extends SwingWorker<Void, Void> {
        private final Article article;

        EditTask(Article article) {
            this.article = article;
        }

        @Override
        protected Void doInBackground() throws Exception {
            articleService.edit(article);
            return null;
        }

        @Override
        protected void done() {
            try {
                get();
                loadTable();
                showSuccess("Article updated");
            } catch (Exception ex) {
                showError("Update failed: " + ex.getMessage());
            }
        }
    }

    private class CreateTask extends SwingWorker<Void, Void> {
        private final Article article;

        CreateTask(Article article) {
            this.article = article;
        }

        @Override
        protected Void doInBackground() throws Exception {
            articleService.create(article);
            return null;
        }

        @Override
        protected void done() {
            try {
                get();
                loadTable();
                showSuccess("Article created");
            } catch (Exception ex) {
                showError("Create failed: " + ex.getMessage());
            }
        }
    }

    private class DeleteTask extends SwingWorker<Void, Void> {
        private final int id;

        DeleteTask(int id) {
            this.id = id;
        }

        @Override
        protected Void doInBackground() throws Exception {
            articleService.remove(id);
            return null;
        }

        @Override
        protected void done() {
            try {
                get();
                loadTable();
                showSuccess("Article deleted");
            } catch (Exception ex) {
                showError("Delete failed: " + ex.getMessage());
            }
        }
    }
}