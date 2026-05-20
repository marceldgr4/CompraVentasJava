package com.app.UI.Panel;

import com.app.Infrastructure.security.SessionManager;
import com.app.Controllers.PawnController;
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

    private final PawnController pawnController = new PawnController();

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

    private com.app.UI.Components.ResponsivePanel buildTopBar() {
        com.app.UI.Components.ResponsivePanel toolbar = new com.app.UI.Components.ResponsivePanel();
        toolbar.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        buildFilterPanel(toolbar);
        buildButtonPanel(toolbar);
        return toolbar;
    }

    private void buildFilterPanel(com.app.UI.Components.ResponsivePanel toolbar) {
        JLabel lbl = new JLabel("Filtro Estado:");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));

        cmbFilter = new JComboBox<>(
                new String[]{"Todos", "Activos", "Vencidos", "Devueltos", "Expirados"});
        cmbFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbFilter.setPreferredSize(new Dimension(130, 36));
        cmbFilter.addActionListener(e -> applyFilter());

        JPanel pFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pFilter.setOpaque(false);
        pFilter.add(lbl);
        pFilter.add(cmbFilter);

        toolbar.addFilterComponent(pFilter);
    }

    private void buildButtonPanel(com.app.UI.Components.ResponsivePanel toolbar) {
        btnNew            = ButtonFactory.createPrimaryButton("Nuevo Empeño", "add");
        JButton btnPrint  = ButtonFactory.createPrimaryButton("Imprimir Boleta", "print");
        btnEdit           = ButtonFactory.createWarningButton("Editar", "edit");
        btnMarkReturned   = ButtonFactory.createSuccessButton("Marcar Devuelto", "toggle");
        btnDelete         = ButtonFactory.createDangerButton("Eliminar", "delete");
        btnProcessOverdue = ButtonFactory.createAmberButton("Procesar Vencidos", "warning");
        btnRefresh        = ButtonFactory.createNeutralButton("Actualizar", "refresh");

        btnNew           .addActionListener(e -> openNewDialog());
        btnPrint         .addActionListener(e -> doPrint());
        btnEdit          .addActionListener(e -> openEditDialog());
        btnMarkReturned  .addActionListener(e -> doMarkReturned());
        btnDelete        .addActionListener(e -> doDelete());
        btnProcessOverdue.addActionListener(e -> doProcessOverdue());
        btnRefresh       .addActionListener(e -> loadData());

        toolbar.addActionComponent(btnNew);
        toolbar.addActionComponent(btnPrint);
        toolbar.addActionComponent(btnEdit);
        toolbar.addActionComponent(btnMarkReturned);
        toolbar.addActionComponent(btnDelete);
        toolbar.addActionComponent(btnProcessOverdue);
        toolbar.addActionComponent(btnRefresh);
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
        pawnController.loadAll(
            list -> {
                populateTable(list);
                lblStatus.setText(list.size() + " empeño(s) cargado(s)");
                refreshTotalValue();
            },
            (msg, ex) -> showError("Error al cargar: " + msg)
        );
    }

    private void applyFilter() {
        String filter = (String) cmbFilter.getSelectedItem();
        lblStatus.setText("Filtrando: " + filter);
        pawnController.filter(filter,
            list -> {
                populateTable(list);
                lblStatus.setText(list.size() + " empeño(s)");
                refreshTotalValue();
            },
            (msg, ex) -> showError("Error de filtrado: " + msg)
        );
    }

    private void openNewDialog() {
        PawnDialog dlg = new PawnDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this), null);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            if (dlg.isAgile()) {
                pawnController.createAgile(dlg.getExistingPawn(), dlg.getNewArticle(), dlg.getNewCliente(), this,
                    () -> {
                        loadData();
                        com.app.Utils.pdf.PdfInvoiceGenerator.generatePawnInvoice(dlg.getExistingPawn());
                    },
                    (msg, ex) -> {}
                );
            } else {
                pawnController.create(dlg.getExistingPawn(), this,
                    () -> {
                        loadData();
                        com.app.Utils.pdf.PdfInvoiceGenerator.generatePawnInvoice(dlg.getExistingPawn());
                    },
                    (msg, ex) -> {}
                );
            }
        }
    }

    private void doPrint() {
        int row = table.getSelectedRow();
        if (row < 0) { showWarning("Selecciona un empeño de la tabla para imprimir su boleta."); return; }
        int id = (int) tableModel.getValueAt(row, 0);
        
        pawnController.getById(id,
            opt -> {
                if (opt.isPresent()) {
                    com.app.Utils.pdf.PdfInvoiceGenerator.generatePawnInvoice(opt.get());
                } else {
                    showError("No se pudo cargar la información del empeño.");
                }
            },
            (msg, ex) -> showError("Error al cargar empeño: " + msg)
        );
    }

    private void openEditDialog() {
        Pawn selected = getSelectedPawn();
        if (selected == null) { showWarning("Seleccione un empeño para editar."); return; }
        PawnDialog dlg = new PawnDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this), selected);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            pawnController.update(dlg.getExistingPawn(), this,
                this::loadData,
                (msg, ex) -> {}
            );
        }
    }

    private void doMarkReturned() {
        Pawn selected = getSelectedPawn();
        if (selected == null) { showWarning("Seleccione un empeño para marcar como devuelto."); return; }
        
        pawnController.markAsReturned(selected.getId(), this,
            this::loadData,
            (msg, ex) -> {}
        );
    }

    private void doDelete() {
        Pawn selected = getSelectedPawn();
        if (selected == null) { showWarning("Seleccione un empeño para eliminar."); return; }
        
        pawnController.delete(selected.getId(), this,
            this::loadData,
            (msg, ex) -> {}
        );
    }

    private void doProcessOverdue() {
        pawnController.processOverdue(this,
            n -> {
                loadData();
                showSuccess(n + " empeño(s) vencido(s) procesado(s).");
            },
            (msg, ex) -> showError("Error al procesar: " + msg)
        );
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Pawn getSelectedPawn() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        int id = (int) tableModel.getValueAt(row, 0);
        
        // Using service synchronously for UI selection context - ideally should be async or from controller cache
        try {
            return new PawnService().getById(id).orElse(null);
        } catch (ServiceException e) {
            showError("Error al cargar empeño: " + e.getMessage());
            return null;
        }
    }

    private void populateTable(List<Pawn> pawns) {
        tableModel.setRowCount(0);
        for (Pawn p : pawns) {
            tableModel.addRow(new Object[]{
                    p.getId(),
                    p.getClientName() != null ? p.getClientName() : "N/A",
                    p.getArticleName() != null ? p.getArticleName() : "N/A",
                    p.getAmount(),
                    "$" + p.getPrice(),
                    "$" + p.getTotal(),
                    p.getPawnDate()   != null ? p.getPawnDate()  .format(FMT) : "N/A",
                    p.getReturnDate() != null ? p.getReturnDate().format(FMT) : "N/A",
                    p.getStatus(),
                    p.getEmployeeName() != null ? p.getEmployeeName() : "N/A"

            });
        }
    }

    private void refreshTotalValue() {
        pawnController.getTotalActiveValue(
            total -> lblTotal.setText("Valor total activo: $" + total),
            (msg, ex) -> lblTotal.setText("Total: Error")
        );
    }

    private void showError  (String msg) { JOptionPane.showMessageDialog(this, msg, "Error",   JOptionPane.ERROR_MESSAGE); }
    private void showWarning(String msg) { JOptionPane.showMessageDialog(this, msg, "Advertencia", JOptionPane.WARNING_MESSAGE); }
    private void showSuccess(String msg) { JOptionPane.showMessageDialog(this, msg, "Éxito", JOptionPane.INFORMATION_MESSAGE); }

    public void refresh() {
        loadData();
    }
}