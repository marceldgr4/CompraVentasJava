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

        JLabel lbl = new JLabel("Filtro:");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));

        cmbFilter = new JComboBox<>(
                new String[]{"Todos", "Activos", "Vencidos", "Devueltos", "Expirados"});
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

        btnNew            = ButtonFactory.createPrimaryButton("+ Nuevo empeño");
        btnEdit           = ButtonFactory.createWarningButton("Editar");
        btnMarkReturned   = ButtonFactory.createSuccessButton("Marcar devuelto");
        btnDelete         = ButtonFactory.createDangerButton("Eliminar");
        btnProcessOverdue = ButtonFactory.createAmberButton("Procesar vencidos");
        btnRefresh        = ButtonFactory.createNeutralButton("Actualizar");

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

        lblStatus = new JLabel("Cargando...");
        lblStatus.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblStatus.setForeground(Color.DARK_GRAY);

        lblTotal = new JLabel("Valor total activo: $0.00");
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
        lblStatus.setText("Cargando...");
        tableModel.setRowCount(0);
        new LoadPawnsTask().execute();
    }

    private void applyFilter() {
        String filter = (String) cmbFilter.getSelectedItem();
        if (filter == null || "Todos".equals(filter)) {
            loadData();
            return;
        }
        lblStatus.setText("Filtrando: " + filter);
        tableModel.setRowCount(0);
        new FilterPawnsTask(filter).execute();
    }

    private void openNewDialog() {
        PawnDialog dlg = new PawnDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this), null);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) new CreatePawnTask(dlg.getExistingPawn()).execute();
    }

    private void openEditDialog() {
        Pawn selected = getSelectedPawn();
        if (selected == null) { showWarning("Seleccione un empeño para editar."); return; }
        PawnDialog dlg = new PawnDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this), selected);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) new UpdatePawnTask(dlg.getExistingPawn()).execute();
    }

    private void doMarkReturned() {
        Pawn selected = getSelectedPawn();
        if (selected == null) { showWarning("Seleccione un empeño para marcar como devuelto."); return; }
        int ok = JOptionPane.showConfirmDialog(this,
                "¿Marcar empeño #" + selected.getId() + " como devuelto?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (ok == JOptionPane.YES_OPTION) new MarkReturnedTask(selected.getId()).execute();
    }

    private void doDelete() {
        Pawn selected = getSelectedPawn();
        if (selected == null) { showWarning("Seleccione un empeño para eliminar."); return; }
        int ok = JOptionPane.showConfirmDialog(this,
                "¿Eliminar empeño #" + selected.getId() + "?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) new DeleteTask(selected.getId()).execute();
    }

    private void doProcessOverdue() {
        int ok = JOptionPane.showConfirmDialog(this,
                "¿Procesar todos los empeños vencidos? Esto los marcará como expirados.",
                "Confirmar", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) new ProcessOverdueTask().execute();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Pawn getSelectedPawn() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        int id = (int) tableModel.getValueAt(row, 0);
        try { return pawnService.getById(id).orElse(null); }
        catch (ServiceException e) { showError("Error al cargar empeño: " + e.getMessage()); return null; }
    }

    private void populateTable(List<Pawn> pawns) {
        tableModel.setRowCount(0);
        for (Pawn p : pawns) {
            tableModel.addRow(new Object[]{
                    p.getId(),
                    p.getClienteName() != null ? p.getClienteName() : "N/A",
                    p.getArticleName() != null ? p.getArticleName() : "N/A",
                    p.getAmount(),
                    "$" + p.getPrice(),
                    "$" + p.getTotal(),
                    p.getPawnDate()   != null ? p.getPawnDate()  .format(FMT) : "N/A",
                    p.getReturnDate() != null ? p.getReturnDate().format(FMT) : "N/A",
                    p.getStatus(),
                    p.getProfileName() != null ? p.getProfileName() : "N/A"
            });
        }
    }

    private void refreshTotalValue() {
        try {
            var total = pawnService.getTotalActiveValues();
            lblTotal.setText("Valor total activo: $" + total);
        } catch (ServiceException e) {
            lblTotal.setText("Total: Error");
        }
    }

    private void showError  (String msg) { JOptionPane.showMessageDialog(this, msg, "Error",   JOptionPane.ERROR_MESSAGE); }
    private void showWarning(String msg) { JOptionPane.showMessageDialog(this, msg, "Advertencia", JOptionPane.WARNING_MESSAGE); }
    private void showSuccess(String msg) { JOptionPane.showMessageDialog(this, msg, "Éxito", JOptionPane.INFORMATION_MESSAGE); }

    // ── SwingWorker tasks ─────────────────────────────────────────────────────

    private class LoadPawnsTask extends SwingWorker<List<Pawn>, Void> {
        @Override protected List<Pawn> doInBackground() throws Exception { return pawnService.getAll(); }
        @Override protected void done() {
            try { List<Pawn> list = get(); populateTable(list); lblStatus.setText(list.size() + " empeño(s) cargado(s)"); refreshTotalValue(); }
            catch (ExecutionException ex) { showError("Error al cargar: " + ex.getCause().getMessage()); }
            catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
        }
    }

    private class FilterPawnsTask extends SwingWorker<List<Pawn>, Void> {
        private final String filter;
        FilterPawnsTask(String f) { this.filter = f; }
        @Override protected List<Pawn> doInBackground() throws Exception {
            return switch (filter) {
                case "Activos"  -> pawnService.getActivePawns();
                case "Vencidos" -> pawnService.getOverduePawns();
                default        -> pawnService.getAll();
            };
        }
        @Override protected void done() {
            try { List<Pawn> list = get(); populateTable(list); lblStatus.setText(list.size() + " empeño(s)"); refreshTotalValue(); }
            catch (ExecutionException ex) { showError("Error de filtrado: " + ex.getCause().getMessage()); }
            catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
        }
    }

    private class CreatePawnTask extends SwingWorker<Void, Void> {
        private final Pawn pawn;
        CreatePawnTask(Pawn p) { this.pawn = p; }
        @Override protected Void doInBackground() throws Exception { pawnService.create(pawn); return null; }
        @Override protected void done() {
            try { get(); loadData(); showSuccess("Empeño creado."); }
            catch (ExecutionException ex) { showError("Error al crear: " + ex.getCause().getMessage()); }
            catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
        }
    }

    private class UpdatePawnTask extends SwingWorker<Void, Void> {
        private final Pawn pawn;
        UpdatePawnTask(Pawn p) { this.pawn = p; }
        @Override protected Void doInBackground() throws Exception { pawnService.update(pawn); return null; }
        @Override protected void done() {
            try { get(); loadData(); showSuccess("Empeño actualizado."); }
            catch (ExecutionException ex) { showError("Error al actualizar: " + ex.getCause().getMessage()); }
            catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
        }
    }

    private class DeleteTask extends SwingWorker<Void, Void> {
        private final int id;
        DeleteTask(int id) { this.id = id; }
        @Override protected Void doInBackground() throws Exception { pawnService.delete(id); return null; }
        @Override protected void done() {
            try { get(); loadData(); showSuccess("Empeño eliminado."); }
            catch (ExecutionException ex) { showError("Error al eliminar: " + ex.getCause().getMessage()); }
            catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
        }
    }

    private class MarkReturnedTask extends SwingWorker<Void, Void> {
        private final int id;
        MarkReturnedTask(int id) { this.id = id; }
        @Override protected Void doInBackground() throws Exception { pawnService.markAsReturned(id); return null; }
        @Override protected void done() {
            try { get(); loadData(); showSuccess("Empeño marcado como devuelto."); }
            catch (ExecutionException ex) { showError("Error al marcar: " + ex.getCause().getMessage()); }
            catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
        }
    }

    private class ProcessOverdueTask extends SwingWorker<Integer, Void> {
        @Override protected Integer doInBackground() throws Exception { return pawnService.processOverduePawns(); }
        @Override protected void done() {
            try { int n = get(); loadData(); showSuccess(n + " empeño(s) vencido(s) procesado(s)."); }
            catch (ExecutionException ex) { showError("Error al procesar: " + ex.getCause().getMessage()); }
            catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
        }
    }
}