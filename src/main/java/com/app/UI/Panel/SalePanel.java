package com.app.UI.Panel;

import Infrastructure.security.SessionManager;
import com.app.Controllers.SaleController;
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

    private final SaleController saleController = new SaleController();

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
        JLabel lblTitle = new JLabel("Gestión de Ventas");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(30, 42, 74));
        wrapper.add(lblTitle, BorderLayout.NORTH);

        com.app.UI.Components.ResponsivePanel toolbar = new com.app.UI.Components.ResponsivePanel();
        buildFilterPanel(toolbar);
        buildButtonPanel(toolbar);
        
        wrapper.add(toolbar, BorderLayout.CENTER);
        return wrapper;
    }

    private void buildFilterPanel(com.app.UI.Components.ResponsivePanel toolbar) {
        SpinnerDateModel fromModel = new SpinnerDateModel();
        SpinnerDateModel toModel = new SpinnerDateModel();
        spnFrom = new JSpinner(fromModel);
        spnTo = new JSpinner(toModel);

        JSpinner.DateEditor fromEd = new JSpinner.DateEditor(spnFrom, "dd/MM/yyyy");
        JSpinner.DateEditor toEd = new JSpinner.DateEditor(spnTo, "dd/MM/yyyy");
        spnFrom.setEditor(fromEd);
        spnTo.setEditor(toEd);
        spnFrom.setPreferredSize(new Dimension(115, 36));
        spnTo.setPreferredSize(new Dimension(115, 36));
        
        txtClienteId = new JTextField();
        txtClienteId.setPreferredSize(new Dimension(140, 36));
        txtClienteId.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtClienteId.putClientProperty("JTextField.placeholderText", "ID Cliente");

        btnFilter = ButtonFactory.createPrimaryButton("Filtrar", "search");
        btnFilter.addActionListener(e -> applyFilter());

        JButton btnClear = ButtonFactory.createNeutralButton("Limpiar", "refresh");
        btnClear.addActionListener(e -> { txtClienteId.setText(""); loadAll(); });

        toolbar.addFilterComponent(createFilterPair("Desde:", spnFrom));
        toolbar.addFilterComponent(createFilterPair("Hasta:", spnTo));
        toolbar.addFilterComponent(createFilterPair("Cliente ID:", txtClienteId));
        
        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnlBtns.setOpaque(false);
        pnlBtns.add(btnFilter);
        pnlBtns.add(btnClear);
        toolbar.addFilterComponent(pnlBtns);
    }

    private JPanel createFilterPair(String label, Component comp) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        p.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(30, 42, 74));
        p.add(lbl);
        p.add(comp);
        return p;
    }

    private void buildButtonPanel(com.app.UI.Components.ResponsivePanel toolbar) {
        btnNew = ButtonFactory.createSuccessButton("Nueva Venta", "add");
        btnNew.addActionListener(e -> openNewSaleDialog());

        JButton btnPrint = ButtonFactory.createPrimaryButton("Imprimir Factura", "print");
        btnPrint.addActionListener(e -> doPrint());

        btnDelete = ButtonFactory.createDangerButton("Eliminar", "delete");
        btnDelete.addActionListener(e -> doDelete());

        JButton btnRefresh = ButtonFactory.createNeutralButton("Actualizar", "refresh");
        btnRefresh.addActionListener(e -> loadAll());

        toolbar.addActionComponent(btnNew);
        toolbar.addActionComponent(btnPrint);
        toolbar.addActionComponent(btnDelete);
        toolbar.addActionComponent(btnRefresh);
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
        saleController.loadAll(
            this::populateTable,
            (msg, ex) -> {
                lblStatus.setText("Error al cargar.");
                showError("Error: " + msg);
            }
        );
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

        lblStatus.setText("Filtrando...");
        saleController.filter(from, to, clienteId,
            this::populateTable,
            (msg, ex) -> {
                lblStatus.setText("Error al filtrar.");
                showError("Error: " + msg);
            }
        );
    }

    private void openNewSaleDialog() {
        SaleDialog dlg = new SaleDialog((JFrame) SwingUtilities.getWindowAncestor(this));
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            com.app.Utils.pdf.PdfInvoiceGenerator.generateSaleInvoice(dlg.getConfirmedSale());
            showSuccess("Venta registrada correctamente.");
            loadAll();
        }
    }

    private void doDelete() {
        int row = table.getSelectedRow();
        if (row < 0) { showWarning("Selecciona una venta para eliminar."); return; }
        int id = (int) tableModel.getValueAt(row, 0);
        
        saleController.delete(id, this,
            this::loadAll,
            (msg, ex) -> {} // Error already shown by controller
        );
    }

    private void doPrint() {
        int row = table.getSelectedRow();
        if (row < 0) { showWarning("Selecciona una venta de la tabla para imprimir su factura."); return; }
        int id = (int) tableModel.getValueAt(row, 0);
        
        new SwingWorker<Sale, Void>() {
            @Override protected Sale doInBackground() throws Exception {
                return new com.app.Service.SaleService().findById(id);
            }
            @Override protected void done() {
                try {
                    Sale sale = get();
                    if (sale != null) {
                        com.app.Utils.pdf.PdfInvoiceGenerator.generateSaleInvoice(sale);
                    } else {
                        showError("No se pudo cargar la información de la venta.");
                    }
                } catch (Exception ex) {
                    showError("Error al generar PDF: " + ex.getMessage());
                }
            }
        }.execute();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void populateTable(List <Sale> sales) {
        tableModel.setRowCount(0);
        for (Sale s : sales) {
            tableModel.addRow(new Object[]{
                    s.getId(),
                    s.getEmployeeId() != null ? s.getEmployeeId().substring(0, 8) + "..." : "N/A",
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

    public void refresh() { loadAll(); }
}
