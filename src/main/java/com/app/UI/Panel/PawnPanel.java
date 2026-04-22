package com.app.UI.Panel;

import Infrastructure.security.SessionManager;
import com.app.Model.domain.Pawn;
import com.app.Service.ArticleService;
import com.app.Service.ClienteService;
import com.app.Service.PawnService;
import com.app.UI.dialogs.PawnDialog;
import java.util.List;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

import static com.app.UI.Components.ButtonFactory.createButton;

/**
 * PawnPanel - COMPLETAMENTE CORREGIDO
 * Gestión de empeños con CRUD completo
 */
public class PawnPanel extends JPanel {

    private static final String[] COLUMNS = {
            "ID", "CLIENTE", "ARTICULO", "CANTIDAD", "PRECIO UNIT", "TOTAL",
            "FECHA INGRESO", "FECHA LIMITE", "ESTADO", "EMPLEADO"
    };

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<String> cmbFilter;
    private JButton btnNew;
    private JButton btnEdit;
    private JButton btnDelete;
    private JButton btnRefresh;
    private JButton btnProcessOverdue;
    private JButton btnMarkReturned;

    private JLabel lblStatus;
    private JLabel lblTotal;

    private final PawnService pawnService = new PawnService();
    private final ArticleService articleService = new ArticleService();
    private final ClienteService clienteService = new ClienteService();

    public PawnPanel() {
        initComponents();
        configurePermissions();
        loadData();
    }

    private void configurePermissions() {
        boolean isAdmin = SessionManager.isAdmin();
        btnEdit.setVisible(isAdmin);
        btnDelete.setVisible(isAdmin);
        btnProcessOverdue.setVisible(isAdmin);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        setBackground(new Color(245, 247, 250));

        JPanel topBar = createTopBar();
        JScrollPane scrollPane = createTable();
        JPanel bottomBar = createBottomBar();

        add(topBar, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomBar, BorderLayout.SOUTH);
    }

    /**
     * Crea la tabla con modelo.
     */
    private JScrollPane createTable() {
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(26);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.setGridColor(new Color(230, 230, 230));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        return scrollPane;
    }

    /**
     * Crea barra inferior con estado y total.
     */
    private JPanel createBottomBar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        lblStatus = new JLabel("Loading...");
        lblStatus.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblStatus.setForeground(Color.DARK_GRAY);

        //  INICIALIZAR lblTotal
        lblTotal = new JLabel("Total Active Value: $0.00");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTotal.setForeground(new Color(56, 142, 60));

        panel.add(lblStatus, BorderLayout.WEST);
        panel.add(lblTotal, BorderLayout.EAST);
        return panel;
    }

    /**
     * Crea barra superior con filtros y botones.
     */
    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout(10, 0));
        topBar.setOpaque(false);
        topBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        JPanel filterPanel = createFilterPanel();
        JPanel btnPanel = createButtonPanel();
        topBar.add(filterPanel, BorderLayout.WEST);
        topBar.add(btnPanel, BorderLayout.EAST);
        return topBar;
    }

    /**
     * Panel de filtros.
     */
    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panel.setOpaque(false);

        JLabel lblFilter = new JLabel("Filter:");
        lblFilter.setFont(new Font("Segoe UI", Font.BOLD, 12));

        cmbFilter = new JComboBox<>(new String[]{
                "All", "Active", "Overdue", "Returned", "Expired"
        });
        cmbFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cmbFilter.addActionListener(e -> loadTable((String) cmbFilter.getSelectedItem()));

        panel.add(lblFilter);
        panel.add(cmbFilter);
        return panel;
    }

    /**
     * ✅ IMPLEMENTADO: Cargar tabla según filtro
     */
    private void loadTable(String selectedItem) {
        if (selectedItem == null || selectedItem.equals("All")) {
            loadData();
        } else {
            applyFilter();
        }
    }

    /**
     * Panel de botones.
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        panel.setOpaque(false);

        btnNew = createButton("+ New Pawn", new Color(30, 136, 229));
        btnNew.addActionListener(e -> openNewDialog());

        btnEdit = createButton("Edit", new Color(255, 152, 0));
        btnEdit.addActionListener(e -> openEditDialog());

        btnMarkReturned = createButton("Mark Returned", new Color(56, 142, 60));
        btnMarkReturned.addActionListener(e -> doMarkReturned());

        btnDelete = createButton("Delete", new Color(239, 83, 80));
        btnDelete.addActionListener(e -> doDelete());

        btnProcessOverdue = createButton("Process Overdue", new Color(255, 152, 0));
        btnProcessOverdue.addActionListener(e -> doProcessOverdue());

        btnRefresh = createButton("Refresh", new Color(100, 100, 100));
        btnRefresh.addActionListener(e -> loadData());

        panel.add(btnNew);
        panel.add(btnEdit);
        panel.add(btnMarkReturned);
        panel.add(btnDelete);
        panel.add(btnProcessOverdue);
        panel.add(btnRefresh);

        return panel;
    }

    // ==================== OPERACIONES CRUD ====================

    /**
     * Crear nuevo empeño.
     */
    private void openNewDialog() {
        PawnDialog dialog = new PawnDialog((JFrame) SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            new CreatePawnTask(dialog.getPawn()).execute();
        }
    }

    /**
     * Editar empeño existente.
     */
    private void openEditDialog() {
        Pawn selected = getSelectedPawn();
        if (selected == null) {
            showWarning("Select a pawn to edit");
            return;
        }
        PawnDialog dialog = new PawnDialog((JFrame) SwingUtilities.getWindowAncestor(this), selected);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            new UpdatePawnTask(dialog.getPawn()).execute();
        }
    }

    /**
     * Marcar como devuelto.
     */
    private void doMarkReturned() {
        Pawn selected = getSelectedPawn();
        if (selected == null) {
            showWarning("Select a pawn to mark as returned");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Mark pawn as returned?",
                "Confirm",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm == JOptionPane.YES_OPTION) {
            new MarkReturnedTask(selected.getId()).execute();
        }
    }

    /**
     * ✅ COMPLETADO: Eliminar empeño
     */
    private void doDelete() {
        Pawn selected = getSelectedPawn();
        if (selected == null) {
            showWarning("Select a pawn to delete");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete pawn #" + selected.getId() + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            new DeleteTask(selected.getId()).execute();
        }
    }

    /**
     * Procesar empeños vencidos.
     */
    private void doProcessOverdue() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Process overdue pawns? This will mark all expired items.",
                "Confirm",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            new ProcessOverdueTask().execute();
        }
    }

    // ==================== HELPER METHODS ====================

    private Pawn getSelectedPawn() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return null;
        }

        int id = (int) tableModel.getValueAt(row, 0);  // ✅ Usar tableModel
        try {
            return pawnService.getById(id).orElse(null);
        } catch (SQLException e) {
            showError("Error loading pawn: " + e.getMessage());
            return null;
        }
    }

    private void clearTable() {
        tableModel.setRowCount(0);
    }

    private void loadData() {
        lblStatus.setText("Loading...");
        clearTable();
        new LoadPawnsTask().execute();
    }

    /**
     * ✅ NORMALIZADO: Parámetro camelCase
     */
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * ✅ NORMALIZADO: Parámetro camelCase
     */
    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    // ==================== SwingWorker Tasks ====================

    private class LoadPawnsTask extends SwingWorker<List<Pawn>, Void> {
        @Override
        protected List<Pawn> doInBackground() throws Exception {
            return pawnService.getAll();
        }

        @Override
        protected void done() {
            try {
                List<Pawn> pawns = get();
                for (Pawn pawn : pawns) {
                    tableModel.addRow(new Object[]{
                            pawn.getId(),
                            pawn.getCliente_name() != null ? pawn.getCliente_name() : "N/A",
                            pawn.getArticle_name() != null ? pawn.getArticle_name() : "N/A",
                            pawn.getAmount(),
                            "$" + pawn.getPrice(),
                            "$" + pawn.getTotal(),
                            pawn.getPawn_date() != null ? pawn.getPawn_date().format(FORMATTER) : "N/A",
                            pawn.getReturn_date() != null ? pawn.getReturn_date().format(FORMATTER) : "N/A",
                            pawn.getStatus(),
                            pawn.getProfile_name() != null ? pawn.getProfile_name() : "N/A"
                    });
                }
                lblStatus.setText(pawns.size() + " pawns loaded");
                updateTotalValue();
            } catch (Exception ex) {
                showError("Load failed: " + ex.getMessage());
            }
        }
    }

    private class FilterPawnsTask extends SwingWorker<List<Pawn>, Void> {
        private final String filter;

        FilterPawnsTask(String filter) {
            this.filter = filter;
        }

        @Override
        protected List<Pawn> doInBackground() throws Exception {
            return switch (filter) {
                case "Active" -> pawnService.getActivePawns();
                case "Overdue" -> pawnService.getOverduePawns();
                default -> pawnService.getAll();
            };
        }

        @Override
        protected void done() {
            try {
                List<Pawn> pawns = get();
                for (Pawn pawn : pawns) {
                    tableModel.addRow(new Object[]{
                            pawn.getId(),
                            pawn.getCliente_name(),
                            pawn.getArticle_name(),
                            pawn.getAmount(),
                            "$" + pawn.getPrice(),
                            "$" + pawn.getTotal(),
                            pawn.getPawn_date().format(FORMATTER),
                            pawn.getReturn_date().format(FORMATTER),
                            pawn.getStatus(),
                            pawn.getProfile_name()
                    });
                }
                lblStatus.setText(pawns.size() + " pawns found");
                updateTotalValue();
            } catch (Exception ex) {
                showError("Filter failed: " + ex.getMessage());
            }
        }
    }

    private class CreatePawnTask extends SwingWorker<Void, Void> {
        private final Pawn pawn;

        CreatePawnTask(Pawn pawn) {
            this.pawn = pawn;
        }

        @Override
        protected Void doInBackground() throws Exception {
            pawnService.create(pawn);
            return null;
        }

        @Override
        protected void done() {
            try {
                get();
                loadData();
                showSuccess("Pawn created");
            } catch (Exception ex) {
                showError("Create failed: " + ex.getMessage());
            }
        }
    }

    private class UpdatePawnTask extends SwingWorker<Void, Void> {
        private final Pawn pawn;

        UpdatePawnTask(Pawn pawn) {
            this.pawn = pawn;
        }

        @Override
        protected Void doInBackground() throws Exception {
            pawnService.update(pawn);
            return null;
        }

        @Override
        protected void done() {
            try {
                get();
                loadData();
                showSuccess("Pawn updated");
            } catch (Exception ex) {
                showError("Update failed: " + ex.getMessage());
            }
        }
    }

    /**
     * ✅ COMPLETADO: DeleteTask
     */
    private class DeleteTask extends SwingWorker<Void, Void> {
        private final int pawnId;

        DeleteTask(int pawnId) {
            this.pawnId = pawnId;
        }

        @Override
        protected Void doInBackground() throws Exception {
            pawnService.delete(pawnId);
            return null;
        }

        @Override
        protected void done() {
            try {
                get();
                loadData();
                showSuccess("Pawn deleted");
            } catch (Exception ex) {
                showError("Delete failed: " + ex.getMessage());
            }
        }
    }

    private class MarkReturnedTask extends SwingWorker<Void, Void> {
        private final int pawnId;

        MarkReturnedTask(int pawnId) {
            this.pawnId = pawnId;
        }

        @Override
        protected Void doInBackground() throws Exception {
            pawnService.markAsReturned(pawnId);
            return null;
        }

        @Override
        protected void done() {
            try {
                get();
                loadData();
                showSuccess("Pawn marked as returned");
            } catch (Exception ex) {
                showError("Mark failed: " + ex.getMessage());
            }
        }
    }

    private class ProcessOverdueTask extends SwingWorker<Void, Void> {
        @Override
        protected Void doInBackground() throws Exception {
            pawnService.processOverduePawns();
            return null;
        }

        @Override
        protected void done() {
            try {
                get();
                loadData();
                showSuccess("Overdue pawns processed");
            } catch (Exception ex) {
                showError("Process failed: " + ex.getMessage());
            }
        }
    }

    /**
     * Actualizar label de total.
     */
    private void updateTotalValue() {
        try {
            var totalValue = pawnService.getTotalActiveValues();
            lblTotal.setText("Total Active Value: $" + totalValue);
        } catch (SQLException e) {
            lblTotal.setText("Total: Error");
        }
    }

    private void applyFilter() {
        String filter = (String) cmbFilter.getSelectedItem();
        lblStatus.setText("Filtering: " + filter);
        clearTable();
        new FilterPawnsTask(filter).execute();
    }
}