package com.app.UI.Panel;

import Infrastructure.security.SessionManager;
import com.app.Model.domain.Pawn;
import com.app.Service.PawnService;
import com.app.Service.exceptions.ServiceException;
import com.app.UI.Components.ButtonFactory;
import com.app.UI.dialogs.PawnDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Panel de gestión de empeños (Pawns).
 *
 * <p><strong>Corrección de color en botones:</strong>
 * El import estático de {@code ButtonFactory.createButton()} anterior usaba
 * {@code setBackground()}, que el L&F nativo de Windows ignora.
 * Ahora todos los botones se crean con los métodos tipados de {@link ButtonFactory},
 * que aplican el color mediante {@code paintComponent}.
 */
public class PawnPanel extends JPanel {

    private static final String[] COLUMNS = {
            "ID", "CLIENTE", "ARTICULO", "CANTIDAD",
            "PRECIO UNIT", "TOTAL", "FECHA INGRESO",
            "FECHA LIMITE", "ESTADO", "EMPLEADO"
    };

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ── Componentes ───────────────────────────────────────────────────────────
    private JTable            table;
    private DefaultTableModel tableModel;
    private JComboBox<String> cmbFilter;
    private JButton           btnNew;
    private JButton           btnEdit;
    private JButton           btnMarkReturned;
    private JButton           btnDelete;
    private JButton           btnProcessOverdue;
    private JButton           btnRefresh;
    private JLabel            lblStatus;
    private JLabel            lblTotal;

    private final PawnService pawnService = new PawnService();

    public PawnPanel() {
        initComponents();
        configurePermissions();
        loadData();
    }

    // ── UI ────────────────────────────────────────────────────────────────────

    private void initComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        setBackground(new Color(245, 247, 250));

        add(buildTopBar(),   BorderLayout.NORTH);
        add(buildTable(),    BorderLayout.CENTER);
        add(buildBottomBar(),BorderLayout.SOUTH);
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        bar.add(buildFilterPanel(), BorderLayout.WEST);
        bar.add(buildButtonPanel(), BorderLayout.EAST);
        return bar;
    }

    private JPanel buildFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panel.setOpaque(false);

        JLabel lbl = new JLabel("Filter:");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));

        cmbFilter = new JComboBox<>(
                new String[]{"All", "Active", "Overdue", "Returned", "Expired"});
        cmbFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbFilter.addActionListener(e -> applyFilter());

        panel.add(lbl);
        panel.add(cmbFilter);
        return panel;
    }

    /**
     * Todos los botones creados con {@link ButtonFactory} para garantizar
     * que el color se vea en el Look&Feel nativo de Windows.
     */
    private JPanel buildButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        panel.setOpaque(false);

        btnNew            = ButtonFactory.createPrimaryButton("+ New Pawn");
        btnEdit           = ButtonFactory.createWarningButton("Edit");
        btnMarkReturned   = ButtonFactory.createSuccessButton("Mark Returned");
        btnDelete         = ButtonFactory.createDangerButton("Delete");
        btnProcessOverdue = ButtonFactory.createAmberButton("Process Overdue");
        btnRefresh        = ButtonFactory.createNeutralButton("Refresh");

        btnNew           .addActionListener(e -> openNewDialog());
        btnEdit          .addActionListener(e -> openEditDialog());
        btnMarkReturned  .addActionListener(e -> doMarkReturned());
        btnDelete        .addActionListener(e -> doDelete());
        btnProcessOverdue.addActionListener(e -> doProcessOverdue());
        btnRefresh       .addActionListener(e -> loadData());

        panel.add(btnNew);
        panel.add(btnEdit);
        panel.add(btnMarkReturned);
        panel.add(btnDelete);
        panel.add(btnProcessOverdue);
        panel.add(btnRefresh);
        return panel;
    }

    private JScrollPane buildTable() {
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(26);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.setGridColor(new Color(230, 230, 230));
        table.setFillsViewportHeight(true);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        return sp;
    }

    private JPanel buildBottomBar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        lblStatus = new JLabel("Loading...");
        lblStatus.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblStatus.setForeground(Color.DARK_GRAY);

        lblTotal = new JLabel("Total Active Value: $0.00");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTotal.setForeground(new Color(56, 142, 60));

        panel.add(lblStatus, BorderLayout.WEST);
        panel.add(lblTotal,  BorderLayout.EAST);
        return panel;
    }

    // ── Permisos ──────────────────────────────────────────────────────────────

    private void configurePermissions() {
        boolean isAdmin = SessionManager.isAdmin();
        btnEdit          .setVisible(isAdmin);
        btnDelete        .setVisible(isAdmin);
        btnProcessOverdue.setVisible(isAdmin);
    }

    // ── Operaciones ───────────────────────────────────────────────────────────

    private void loadData() {
        lblStatus.setText("Loading...");
        tableModel.setRowCount(0);
        new LoadPawnsTask().execute();
    }

    private void applyFilter() {
        String filter = (String) cmbFilter.getSelectedItem();
        if (filter == null || "All".equals(filter)) {
            loadData();
            return;
        }
        lblStatus.setText("Filtering: " + filter);
        tableModel.setRowCount(0);
        new FilterPawnsTask(filter).execute();
    }

    private void openNewDialog() {
        PawnDialog dlg = new PawnDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this), null);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) new CreatePawnTask(dlg.getPawn()).execute();
    }

    private void openEditDialog() {
        Pawn selected = getSelectedPawn();
        if (selected == null) { showWarning("Select a pawn to edit."); return; }
        PawnDialog dlg = new PawnDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this), selected);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) new UpdatePawnTask(dlg.getPawn()).execute();
    }

    private void doMarkReturned() {
        Pawn selected = getSelectedPawn();
        if (selected == null) { showWarning("Select a pawn to mark as returned."); return; }
        int ok = JOptionPane.showConfirmDialog(this,
                "Mark pawn #" + selected.getId() + " as returned?",
                "Confirm", JOptionPane.YES_NO_OPTION);
        if (ok == JOptionPane.YES_OPTION) new MarkReturnedTask(selected.getId()).execute();
    }

    private void doDelete() {
        Pawn selected = getSelectedPawn();
        if (selected == null) { showWarning("Select a pawn to delete."); return; }
        int ok = JOptionPane.showConfirmDialog(this,
                "Delete pawn #" + selected.getId() + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) new DeleteTask(selected.getId()).execute();
    }

    private void doProcessOverdue() {
        int ok = JOptionPane.showConfirmDialog(this,
                "Process all overdue pawns? This marks them as expired.",
                "Confirm", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) new ProcessOverdueTask().execute();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Pawn getSelectedPawn() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        int id = (int) tableModel.getValueAt(row, 0);
        try { return pawnService.getById(id).orElse(null); }
        catch (ServiceException e) { showError("Error loading pawn: " + e.getMessage()); return null; }
    }

    private void populateTable(List<Pawn> pawns) {
        tableModel.setRowCount(0);
        for (Pawn p : pawns) {
            tableModel.addRow(new Object[]{
                    p.getId(),
                    p.getCliente_name() != null ? p.getCliente_name() : "N/A",
                    p.getArticle_name() != null ? p.getArticle_name() : "N/A",
                    p.getAmount(),
                    "$" + p.getPrice(),
                    "$" + p.getTotal(),
                    p.getPawn_date()   != null ? p.getPawn_date()  .format(FMT) : "N/A",
                    p.getReturn_date() != null ? p.getReturn_date().format(FMT) : "N/A",
                    p.getStatus(),
                    p.getProfile_name() != null ? p.getProfile_name() : "N/A"
            });
        }
    }

    private void refreshTotalValue() {
        try {
            var total = pawnService.getTotalActiveValues();
            lblTotal.setText("Total Active Value: $" + total);
        } catch (ServiceException e) {
            lblTotal.setText("Total: Error");
        }
    }

    private void showError  (String msg) { JOptionPane.showMessageDialog(this, msg, "Error",   JOptionPane.ERROR_MESSAGE); }
    private void showWarning(String msg) { JOptionPane.showMessageDialog(this, msg, "Warning", JOptionPane.WARNING_MESSAGE); }
    private void showSuccess(String msg) { JOptionPane.showMessageDialog(this, msg, "Success", JOptionPane.INFORMATION_MESSAGE); }

    // ── SwingWorker tasks ─────────────────────────────────────────────────────

    private class LoadPawnsTask extends SwingWorker<List<Pawn>, Void> {
        @Override protected List<Pawn> doInBackground() throws Exception { return pawnService.getAll(); }
        @Override protected void done() {
            try { List<Pawn> list = get(); populateTable(list); lblStatus.setText(list.size() + " pawns loaded"); refreshTotalValue(); }
            catch (ExecutionException ex) { showError("Load failed: " + ex.getCause().getMessage()); }
            catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
        }
    }

    private class FilterPawnsTask extends SwingWorker<List<Pawn>, Void> {
        private final String filter;
        FilterPawnsTask(String f) { this.filter = f; }
        @Override protected List<Pawn> doInBackground() throws Exception {
            return switch (filter) {
                case "Active"  -> pawnService.getActivePawns();
                case "Overdue" -> pawnService.getOverduePawns();
                default        -> pawnService.getAll();
            };
        }
        @Override protected void done() {
            try { List<Pawn> list = get(); populateTable(list); lblStatus.setText(list.size() + " pawns"); refreshTotalValue(); }
            catch (ExecutionException ex) { showError("Filter failed: " + ex.getCause().getMessage()); }
            catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
        }
    }

    private class CreatePawnTask extends SwingWorker<Void, Void> {
        private final Pawn pawn;
        CreatePawnTask(Pawn p) { this.pawn = p; }
        @Override protected Void doInBackground() throws Exception { pawnService.create(pawn); return null; }
        @Override protected void done() {
            try { get(); loadData(); showSuccess("Pawn created."); }
            catch (ExecutionException ex) { showError("Create failed: " + ex.getCause().getMessage()); }
            catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
        }
    }

    private class UpdatePawnTask extends SwingWorker<Void, Void> {
        private final Pawn pawn;
        UpdatePawnTask(Pawn p) { this.pawn = p; }
        @Override protected Void doInBackground() throws Exception { pawnService.update(pawn); return null; }
        @Override protected void done() {
            try { get(); loadData(); showSuccess("Pawn updated."); }
            catch (ExecutionException ex) { showError("Update failed: " + ex.getCause().getMessage()); }
            catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
        }
    }

    private class DeleteTask extends SwingWorker<Void, Void> {
        private final int id;
        DeleteTask(int id) { this.id = id; }
        @Override protected Void doInBackground() throws Exception { pawnService.delete(id); return null; }
        @Override protected void done() {
            try { get(); loadData(); showSuccess("Pawn deleted."); }
            catch (ExecutionException ex) { showError("Delete failed: " + ex.getCause().getMessage()); }
            catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
        }
    }

    private class MarkReturnedTask extends SwingWorker<Void, Void> {
        private final int id;
        MarkReturnedTask(int id) { this.id = id; }
        @Override protected Void doInBackground() throws Exception { pawnService.markAsReturned(id); return null; }
        @Override protected void done() {
            try { get(); loadData(); showSuccess("Pawn marked as returned."); }
            catch (ExecutionException ex) { showError("Mark failed: " + ex.getCause().getMessage()); }
            catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
        }
    }

    private class ProcessOverdueTask extends SwingWorker<Integer, Void> {
        @Override protected Integer doInBackground() throws Exception { return pawnService.processOverduePawns(); }
        @Override protected void done() {
            try { int n = get(); loadData(); showSuccess(n + " overdue pawn(s) processed."); }
            catch (ExecutionException ex) { showError("Process failed: " + ex.getCause().getMessage()); }
            catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
        }
    }
}