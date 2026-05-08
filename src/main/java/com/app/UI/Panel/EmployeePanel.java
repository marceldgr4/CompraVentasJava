package com.app.UI.Panel;

import Infrastructure.security.SessionManager;
import com.app.Controllers.EmployeeController;
import com.app.Model.domain.Employee;
import com.app.UI.Components.ButtonFactory;
import com.app.UI.dialogs.EmployeeEditDialog;
import com.app.UI.dialogs.EmployeeRegisterDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel CRUD para la gestión de Empleados.
 * Solo accesible por Administradores. (HU-Admin)
 */
public class EmployeePanel extends JPanel {

    private static final Color PANEL_BG  = new Color(245, 247, 250);
    private static final Color HEADER_FG = new Color(30, 42, 74);

    private DefaultTableModel tableModel;
    private JTable table;
    private JLabel lblStatus;

    private final EmployeeController employeeController = new EmployeeController();
    private List<Employee> currentEmployees = new ArrayList<>();

    public EmployeePanel() {
        if (!SessionManager.isAdmin()) {
            setLayout(new BorderLayout());
            JLabel lbl = new JLabel("⛔ Acceso Denegado: Solo administradores pueden ver este panel.",
                    SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
            lbl.setForeground(new Color(180, 40, 40));
            add(lbl, BorderLayout.CENTER);
            return;
        }
        initComponents();
        loadEmployees();
    }

    // ── UI ────────────────────────────────────────────────────────────────────

    private void initComponents() {
        setLayout(new BorderLayout(0, 16));
        setBorder(new EmptyBorder(20, 24, 20, 24));
        setBackground(PANEL_BG);

        add(buildHeader(),  BorderLayout.NORTH);
        add(buildTable(),   BorderLayout.CENTER);
        add(buildStatus(),  BorderLayout.SOUTH);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);

        // Título
        JPanel titleArea = new JPanel();
        titleArea.setOpaque(false);
        titleArea.setLayout(new BoxLayout(titleArea, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel("👔  Gestión de Empleados");
        lblTitle.setFont(new Font("Segoe UI Emoji", Font.BOLD, 22));
        lblTitle.setForeground(HEADER_FG);

        JLabel lblSub = new JLabel("Administre los accesos y roles del personal del sistema");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(120, 130, 155));

        titleArea.add(lblTitle);
        titleArea.add(Box.createVerticalStrut(3));
        titleArea.add(lblSub);

        // Botones
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);

        JButton btnRefresh = ButtonFactory.createNeutralButton("↻ Actualizar");
        JButton btnView    = ButtonFactory.createNeutralButton("Ver Detalle");
        JButton btnEdit    = ButtonFactory.createPrimaryButton("✏ Editar");
        JButton btnToggle  = ButtonFactory.createNeutralButton("⏯ Activar/Desactivar");
        JButton btnDelete  = ButtonFactory.createDangerButton("🗑 Eliminar");
        JButton btnAdd     = ButtonFactory.createSuccessButton("+ Nuevo Empleado");

        btnRefresh.addActionListener(e -> loadEmployees());
        btnView   .addActionListener(e -> viewEmployee());
        btnEdit   .addActionListener(e -> editEmployee());
        btnToggle .addActionListener(e -> toggleActive());
        btnDelete .addActionListener(e -> deleteEmployee());
        btnAdd    .addActionListener(e -> showRegisterDialog());

        btnPanel.add(btnRefresh);
        btnPanel.add(btnView);
        btnPanel.add(btnEdit);
        btnPanel.add(btnToggle);
        btnPanel.add(btnDelete);
        btnPanel.add(btnAdd);

        header.add(titleArea, BorderLayout.WEST);
        header.add(btnPanel,  BorderLayout.EAST);
        return header;
    }

    private JScrollPane buildTable() {
        String[] columns = {"ID (8 car.)", "Nombre Completo", "Email", "Rol", "Estado"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(36);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(235, 240, 250));
        table.setGridColor(new Color(230, 235, 245));
        table.setShowVerticalLines(false);
        table.setFillsViewportHeight(true);
        table.getColumnModel().getColumn(0).setMaxWidth(100);
        table.getColumnModel().getColumn(3).setMaxWidth(140);
        table.getColumnModel().getColumn(4).setMaxWidth(100);

        // Renderer para colorear estado
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                boolean active = "ACTIVO".equals(val);
                setForeground(sel ? Color.WHITE : (active ? new Color(30, 130, 50) : new Color(180, 40, 40)));
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                setHorizontalAlignment(CENTER);
                return this;
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(210, 220, 235)));
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }

    private JLabel buildStatus() {
        lblStatus = new JLabel("Cargando empleados...");
        lblStatus.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblStatus.setForeground(Color.GRAY);
        lblStatus.setBorder(new EmptyBorder(4, 0, 0, 0));
        return lblStatus;
    }

    // ── Datos ─────────────────────────────────────────────────────────────────

    private void loadEmployees() {
        tableModel.setRowCount(0);
        lblStatus.setText("Cargando empleados...");
        employeeController.loadAll(this,
            employees -> {
                currentEmployees = employees;
                for (Employee e : employees) {
                    tableModel.addRow(new Object[]{
                        e.getId() != null && e.getId().length() >= 8
                                ? e.getId().substring(0, 8) + "…" : e.getId(),
                        e.getFullName(),
                        e.getEmail(),
                        e.getRol() != null ? e.getRol().name() : "N/A",
                        e.isActive() ? "ACTIVO" : "INACTIVO"
                    });
                }
                lblStatus.setText("Total: " + employees.size() + " empleado(s) registrado(s).");
            },
            (msg, ex) -> lblStatus.setText("Error al cargar: " + msg)
        );
    }

    // ── Acciones ──────────────────────────────────────────────────────────────

    private Employee getSelectedEmployee() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Por favor seleccione un empleado de la lista.",
                    "Selección requerida", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return currentEmployees.get(row);
    }

    private void showRegisterDialog() {
        Window parent = SwingUtilities.getWindowAncestor(this);
        EmployeeRegisterDialog dlg = new EmployeeRegisterDialog(parent);
        dlg.setVisible(true);
        if (dlg.isSuccessful()) {
            loadEmployees();
        }
    }

    private void viewEmployee() {
        Employee e = getSelectedEmployee();
        if (e == null) return;
        String info = String.format(
                "<html><body style='font-family:Segoe UI;padding:8px'>" +
                "<b>ID:</b> %s<br><br>" +
                "<b>Nombre:</b> %s<br>" +
                "<b>Email:</b> %s<br>" +
                "<b>Rol:</b> %s<br>" +
                "<b>Estado:</b> %s" +
                "</body></html>",
                e.getId(),
                e.getFullName(),
                e.getEmail(),
                e.getRol() != null ? e.getRol().name() : "N/A",
                e.isActive() ? "✅ Activo" : "❌ Inactivo"
        );
        JOptionPane.showMessageDialog(this, info,
                "Detalle del Empleado", JOptionPane.INFORMATION_MESSAGE);
    }

    private void editEmployee() {
        Employee e = getSelectedEmployee();
        if (e == null) return;

        Window parent = SwingUtilities.getWindowAncestor(this);
        EmployeeEditDialog dlg = new EmployeeEditDialog(parent, e);
        dlg.setVisible(true);

        if (dlg.isConfirmed()) {
            employeeController.update(dlg.getEmployee(), this, this::loadEmployees,
                    (msg, ex) -> JOptionPane.showMessageDialog(this,
                            "Error al actualizar: " + msg, "Error", JOptionPane.ERROR_MESSAGE));
        }
    }

    private void toggleActive() {
        Employee e = getSelectedEmployee();
        if (e == null) return;
        employeeController.setActive(e.getId(), !e.isActive(), e.getFullName(), this,
                this::loadEmployees,
                (msg, ex) -> JOptionPane.showMessageDialog(this,
                        "Error: " + msg, "Error", JOptionPane.ERROR_MESSAGE));
    }

    private void deleteEmployee() {
        Employee e = getSelectedEmployee();
        if (e == null) return;
        employeeController.delete(e.getId(), this, this::loadEmployees,
                (msg, ex) -> JOptionPane.showMessageDialog(this,
                        "Error: " + msg, "Error", JOptionPane.ERROR_MESSAGE));
    }

    /** Recarga la tabla cuando el panel vuelve a ser visible. */
    public void refresh() { loadEmployees(); }
}
