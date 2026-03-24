package com.app.View;

import com.app.Model.Article;
import com.app.Model.SesionUser;
import com.app.Service.ArticleService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;



public class ArticlePanel extends JPanel {

    private static final String[] COLUMNS ={
            "Id","Name","Description","Amount","Price","Sold"
    };

    private JTable table;
    private DefaultTableModel tableModel;
    private JScrollPane scrollPane;
    private JTextField txtSearch;
    private JButton btnNew;
    private JButton btnEdit;
    private JButton btnDelete;
    private JButton btnRefresh;
    private JLabel lblStatus;
    private JButton btnCancel;

    private final ArticleService articleService = new ArticleService();
    public ArticlePanel() {
        initComponents();
        configurePermissions();
        loadTable();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0,0));
        setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        //-- Bar top up :Search + button
        JPanel topBar = new JPanel(new BorderLayout(10,0));
        topBar.setBorder(BorderFactory.createEmptyBorder(0,0,12,0));

        //--Search
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,6,0));
        JTextField txtSearch = new JTextField(20);
        txtSearch.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtSearch.putClientProperty("JTextField.placeholderText","Search Article...");

        JButton btnSearch = new JButton("Search");
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSearch.addActionListener(e->doSearch());
        txtSearch.addActionListener(e -> doSearch());

        searchPanel.add(new JLabel("Search: "));
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);

        //--Button CRUD
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT,6,0));

        btnNew = createButton("+ New", new Color(30,136,229));
        btnEdit = createButton("Edit", new Color(255,152,0));
        btnDelete = createButton("Deleted", new Color(239, 83, 80));
        btnRefresh = createButton("Updated", new Color(100, 100, 100));

        btnPanel.add(btnNew);
        btnPanel.add(btnEdit);
        btnPanel.add(btnDelete);
        btnPanel.add(btnRefresh);

        topBar.add(searchPanel, BorderLayout.WEST);
        topBar.add(btnPanel, BorderLayout.EAST);

        //--TABLE--
        tableModel = new DefaultTableModel(COLUMNS,0) {
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
        table.setGridColor(new Color(230,230,230));
        table.setShowGrid(true);

        table.getColumnModel().getColumn(0).setMaxWidth(60);
        table.getColumnModel().getColumn(1).setMaxWidth(90);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setMaxWidth(90);

        JScrollPane scroll = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(153,153,153)));
        //--Bar Status

        lblStatus = new JLabel("Loading...");
        lblStatus.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblStatus.setForeground(Color.DARK_GRAY);
        lblStatus.setBorder(BorderFactory.createEmptyBorder(8,0,0,0));

        add(topBar,BorderLayout.NORTH);
        add(scroll,BorderLayout.CENTER);
        add(lblStatus,BorderLayout.SOUTH);

    }

    private void doSearch() {
        String term = txtSearch.getText().trim();
        if (term.isEmpty()) {
            loadTable();
            return;
        }
        clearTable();
        lblStatus.setText("Searching...");

        SwingWorker<List<Article>, Void> worker = new SwingWorker<>() {
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
                    showError(ex.getMessage());
                }
            }
        };
        worker.execute();
    }


    private void configurePermissions() {
        boolean isAdmin = SesionUser.getInstance().isAdmin();
        btnEdit.setVisible(isAdmin);
        btnDelete.setVisible(isAdmin);

    }
    private void loadTable() {
        lblStatus.setText("Loading...");
        clearTable();

        SwingWorker<List<Article>,Void> worker = new SwingWorker<>() {
            @Override
            protected List<Article> doInBackground() throws Exception {
                return articleService.getAll();
            }
            @Override
            protected void done() {
                try{
                    List<Article> articles = get();
                    for (Article article : articles) {
                        tableModel.addRow(new Object[]{
                                article.getId(),
                                article.getNameArticle(),
                                article.getDescription(),
                                article.getAmount(),
                                "$" +article.getPrice(),
                                article.isSold()? "Yes": "No",
                        });
                    }
                    lblStatus.setText(articles.size() + " article(s) loaded");
                }catch (Exception ex){
                    lblStatus.setText("Error loading data");
                    showError(ex.getMessage());
                }
            }
        };
        worker.execute();

    }
//---Edit Dialog article
    private void openEditDialog() {
        Article selected = getSelectedArticle();
        if (selected == null) {
            showWarning("Selected article for edit");
            return;
        }
        ArticleDialog dialog = new ArticleDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this), selected
        );
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            Article updated = dialog.getArticle();
            updated.setId(selected.getId());
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    articleService.edit(updated);
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        loadTable();
                        showSuccess("Updated Article");

                    } catch (Exception ex) {
                        showError(ex.getMessage());
                    }
                }

            };
            worker.execute();
        }
    }
    //--Delete
    private void doDelete(){
        Article Selected = getSelectedArticle();
        if (Selected == null) {
            showWarning("Selected article for delete");
            return ;
        }
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete \"" + Selected.getNameArticle() + "\"?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if(confirm == JOptionPane.YES_OPTION) return;

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                articleService.remove(Selected.getId());
                return null;
            }
            @Override
            protected void done() {
                try{
                    get();
                    loadTable();
                    showSuccess("Delete Article");

                }catch (Exception ex){
                    showError(ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private Article getSelectedArticle() {
        int row = table.getSelectedRow();
        if (row < 0) { return null; }
        int id = (int) tableModel.getValueAt(row, 0);
        String nameArticle = (String) tableModel.getValueAt(row, 1);
        String description = (String) tableModel.getValueAt(row, 2);
        int amount = (int) tableModel.getValueAt(row, 3);
        String priceStr = tableModel.getValueAt(row, 4).toString().replace("$","");
        boolean sold = tableModel.getValueAt(row, 5).equals("Yes");
        return  new Article(id,nameArticle,description,amount, new BigDecimal(priceStr),sold,null);

    }
    private void clearTable() {
        tableModel.setRowCount(0);
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

    private JButton createButton(String text,Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(Color.white);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

}
