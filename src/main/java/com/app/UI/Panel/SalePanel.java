package com.app.UI.Panel;

import Infrastructure.security.SessionManager;
import com.app.Model.domain.Sale;
import com.app.Service.SaleService;
import com.app.UI.Components.ButtonFactory;
import com.app.UI.dialogs.SaleDialog;
import com.app.Utils.CurrencyUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;
import java.util.List;


public class SalePanel extends JPanel {
    private static final String [] COLUMNS = {
            "ID", "Articulo Empeñado", "Cliente ID" ," Fecha", "Total", "Articulos"
    };
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private DefaultTableModel tableModel;
    private JTable table;
    private JLabel lblStatus;
    private JButton btnDelete;
    private JButton btnNew;

    private JSpinner spnFrom;
    private JSpinner spnTo;
    private JTextField txtClienteId;
    private JButton btnFilter;

    private final SaleService saleService = new SaleService();

    public SalePanel() {
        initComponents();
            configurePermissions();
            loadAll();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0,10));
        setBorder(BorderFactory.createEmptyBorder(12,16,12,16));
        setBackground(new Color(245, 247, 250));

        add(builHeader(),BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);
        add(buildStatusBar(),BorderLayout.SOUTH);
    }
    private JPanel builHeader() {
        JPanel wrapper = new JPanel(new BorderLayout(0,8));
        wrapper.setOpaque(false);
        JLabel lblTitle = new JLabel("Gestion de Ventas");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(new Color(30, 42, 74));
        wrapper.add(lblTitle,BorderLayout.NORTH);

        JPanel toolbar = new JPanel(new BorderLayout(12,0));
        toolbar.setOpaque(false);
        toolbar.add(buildFilterPanel(), BorderLayout.WEST);
        toolbar.add(buildButtonPanel(), BorderLayout.EAST);
        wrapper.add(toolbar,BorderLayout.CENTER);
        return wrapper;
    }
    private JPanel buildFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        panel.setOpaque(false);

        SpinnerDateModel fromModel = new SpinnerDateModel();
        SpinnerDateModel toModel = new SpinnerDateModel();
        spnFrom = new JSpinner(fromModel);
        spnTo = new JSpinner(toModel);

        JSpinner.DateEditor fromEd = new JSpinner.DateEditor(spnFrom, "dd/MM/yyyy");
        JSpinner.DateEditor toEd =new JSpinner.DateEditor(spnTo, "dd/MM/yyyy");
        spnFrom.setEditor(fromEd);
        spnTo.setEditor(toEd);
        spnFrom.setPreferredSize(new Dimension(110,32));
        spnTo.setPreferredSize(new Dimension(110,32));
        txtClienteId = new JTextField();
        txtClienteId.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtClienteId.putClientProperty("JTextField.placeholderText", "ID Cliente");

        btnFilter = ButtonFactory.createPrimaryButton("Filter");
        btnFilter.addActionListener(e -> applyFilter());

        JButton btnClear = ButtonFactory.createPrimaryButton("Limpiar");
        btnClear.addActionListener(e -> { txtClienteId.setText(""); loadAll(); });

        panel.add(new JLabel("Desde:"));
        panel.add(spnFrom);
        panel.add(new JLabel("Hasta:"));
        panel.add(spnTo);
        panel.add(new JLabel("Cliente ID:"));
        panel.add(txtClienteId);
        panel.add(btnFilter);
        panel.add(btnClear);
        return panel;
    }
    private JPanel buildButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        panel.setOpaque(false);

        btnNew = ButtonFactory.createSuccessButton("+ Nueva Venta");
        btnNew.addActionListener(e -> openNewSaleDialog());

        btnDelete = ButtonFactory.createDangerButton("Eliminar");
        btnDelete.addActionListener(e -> doDelete());

        JButton btnRefresh = ButtonFactory.createNeutralButton("Actualizar");
        btnRefresh.addActionListener(e -> loadAll());

        panel.add(btnNew);
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
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.setGridColor(new Color(230, 230, 230));
        table.setFillsViewportHeight(true);
        table.getColumnModel().getColumn(0).setMaxWidth(60);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));
        return sp;
    }

    private JLabel buildStatusBar() {
        lblStatus = new JLabel("Cargando...");
        lblStatus.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblStatus.setForeground(Color.DARK_GRAY);
        lblStatus.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        return lblStatus;
    }

    // ── Permisos ──────────────────────────────────────────────────────────────

    private void configurePermissions() {
        btnDelete.setVisible(SessionManager.isAdmin());
    }

    // ── Operaciones ───────────────────────────────────────────────────────────

    private void loadAll() {
        lblStatus.setText("Cargando...");
        tableModel.setRowCount(0);
        new LoadTask(null, null, -1).execute();
    }

    private void applyFilter() {
        LocalDate from = null;
        LocalDate to   = null;

        try {
            java.util.Date dFrom = (java.util.Date) spnFrom.getValue();
            java.util.Date dTo   = (java.util.Date) spnTo.getValue();
            from = dFrom.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            to   = dTo  .toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        } catch (Exception ignored) {}

        int clienteId = -1;
        try {
            String txt = txtClienteId.getText().trim();
            if (!txt.isEmpty()) clienteId = Integer.parseInt(txt);
        } catch (NumberFormatException e) {
            showError("El ID de cliente debe ser un número.");
            return;
        }

        tableModel.setRowCount(0);
        lblStatus.setText("Filtrando...");
        new LoadTask(from, to, clienteId).execute();
    }

    private void openNewSaleDialog() {
        SaleDialog dlg = new SaleDialog((JFrame) SwingUtilities.getWindowAncestor(this));
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            showSuccess("Venta registrada correctamente.");
            loadAll();
        }
    }

    private void doDelete() {
        int row = table.getSelectedRow();
        if (row < 0) { showWarning("Selecciona una venta para eliminar."); return; }
        int id = (int) tableModel.getValueAt(row, 0);
        int ok = JOptionPane.showConfirmDialog(this,
                "¿Eliminar la venta #" + id + "? Esta acción no se puede deshacer.",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) new DeleteTask(id).execute();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void populateTable(List <Sale> sales) {
        tableModel.setRowCount(0);
        for (Sale s : sales) {
            tableModel.addRow(new Object[]{
                    s.getId(),
                    s.getProfileId() != null ? s.getProfileId().substring(0, 8) + "..." : "N/A",
                    s.getClienteId(),
                    s.getSaleDate() != null ? s.getSaleDate().format(FORMATTER) : "N/A",
                    CurrencyUtils.format(s.getTotal()),
                    s.getDetails() != null ? s.getDetails().size() + " art." : "0 art."
            });
        }
        lblStatus.setText(sales.size() + " venta(s) encontrada(s).");
    }

    private void showSuccess(String msg) { JOptionPane.showMessageDialog(this, msg, "Éxito",       JOptionPane.INFORMATION_MESSAGE); }
    private void showError  (String msg) { JOptionPane.showMessageDialog(this, msg, "Error",       JOptionPane.ERROR_MESSAGE); }
    private void showWarning(String msg) { JOptionPane.showMessageDialog(this, msg, "Advertencia", JOptionPane.WARNING_MESSAGE); }

    // ── SwingWorker tasks ─────────────────────────────────────────────────────

    private class LoadTask extends SwingWorker<List<Sale>, Void> {
        private final LocalDate from;
        private final LocalDate to;
        private final int       clienteId;

        LoadTask(LocalDate from, LocalDate to, int clienteId) {
            this.from      = from;
            this.to        = to;
            this.clienteId = clienteId;
        }

        @Override protected List<Sale> doInBackground() throws Exception {
            if (clienteId > 0)             return saleService.findByCliente(clienteId);
            if (from != null && to != null) return saleService.findByDateRange(from, to);
            return (List<Sale>) saleService.getAllSales();
        }

        @Override protected void done() {
            try { populateTable(get()); }
            catch (ExecutionException ex) {
                lblStatus.setText("Error al cargar.");
                showError("Error: " + ex.getCause().getMessage());
            }
            catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
        }
    }

    private class DeleteTask extends SwingWorker<Void, Void> {
        private final int id;
        DeleteTask(int id) { this.id = id; }
        @Override protected Void doInBackground() throws Exception { saleService.delete(id); return null; }
        @Override protected void done() {
            try { get(); loadAll(); showSuccess("Venta eliminada."); }
            catch (ExecutionException ex) { showError("Error al eliminar: " + ex.getCause().getMessage()); }
            catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
        }
    }

    public void refresh() { loadAll(); }
}
