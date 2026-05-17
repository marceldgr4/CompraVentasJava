package com.app.UI.Panel;

import Infrastructure.security.SessionManager;
import com.app.Controllers.EmployeeController;
import com.app.Model.domain.Employee;
import com.app.UI.Components.ButtonFactory;
import com.app.UI.dialogs.EmployeeEditDialog;
import com.app.UI.dialogs.EmployeeRegisterDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeePanel extends BasePanel {

    private static final String[] COLUMNS = {"ID", "Email", "Nombre Completo", "Rol", "Estado"};
    
    private DefaultTableModel tableModel;
    private JTable table;
    private JLabel lblStatus;

    private final EmployeeController employeeController = new EmployeeController();
    private List<Employee> currentEmployees = new ArrayList<>();

    public EmployeePanel() {
        super();
        if (SessionManager.isAdmin()) {
            refresh();
        }
    }

    @Override
    protected void initComponents() {
        if (!SessionManager.isAdmin()) {
            setLayout(new BorderLayout());
            JLabel lbl = new JLabel(" Acceso Denegado: Solo administradores pueden ver este panel.", SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
            lbl.setForeground(new Color(180, 40, 40));
            add(lbl, BorderLayout.CENTER);
            return;
        }

        // Header
        JPanel topPanel = new JPanel(new BorderLayout(0, 12));
        topPanel.setOpaque(false);
        topPanel.add(buildHeader(" Gestión de Empleados", "Administre los accesos y roles del personal del sistema"), BorderLayout.NORTH);

        com.app.UI.Components.ResponsivePanel actionsBar = new com.app.UI.Components.ResponsivePanel();
        
        JButton btnRefresh = ButtonFactory.createNeutralButton("Actualizar", "refresh");
        JButton btnEdit    = ButtonFactory.createPrimaryButton("Editar", "edit");
        JButton btnToggle  = ButtonFactory.createNeutralButton("Activar/Desactivar", "toggle");
        JButton btnDelete  = ButtonFactory.createDangerButton("Eliminar", "delete");
        JButton btnAdd     = ButtonFactory.createSuccessButton("Nuevo Empleado", "add");

        btnRefresh.addActionListener(e -> refresh());
        btnEdit   .addActionListener(e -> editEmployee());
        btnToggle .addActionListener(e -> toggleActive());
        btnDelete .addActionListener(e -> deleteEmployee());
        btnAdd    .addActionListener(e -> showRegisterDialog());

        actionsBar.addActionComponent(btnRefresh);
        actionsBar.addActionComponent(btnEdit);
        actionsBar.addActionComponent(btnToggle);
        actionsBar.addActionComponent(btnDelete);
        actionsBar.addActionComponent(btnAdd);
        
        topPanel.add(actionsBar, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        styleTable(table);
        add(createTableScroll(table), BorderLayout.CENTER);

        // Status
        lblStatus = new JLabel("Listo");
        lblStatus.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblStatus.setForeground(SUBTITLE_FG);
        add(lblStatus, BorderLayout.SOUTH);
    }

    @Override
    public void refresh() {
        lblStatus.setText("Cargando empleados...");
        employeeController.loadAll(this, 
            list -> {
                currentEmployees = list;
                tableModel.setRowCount(0);
                for (Employee e : list) {
                    tableModel.addRow(new Object[]{
                        e.getId(), e.getEmail(), e.getFullName(), e.getRol(),
                        e.isActive() ? "✅ Activo" : "❌ Inactivo"
                    });
                }
                lblStatus.setText(list.size() + " empleados registrados");
            },
            (msg, ex) -> showError(msg)
        );
    }

    private void showRegisterDialog() {
        EmployeeRegisterDialog dlg = new EmployeeRegisterDialog((JFrame) SwingUtilities.getWindowAncestor(this));
        dlg.setVisible(true);
        if (dlg.isConfirmed()) refresh();
    }

    private void editEmployee() {
        Employee selected = getSelectedEmployee();
        if (selected == null) return;

        EmployeeEditDialog dlg = new EmployeeEditDialog((JFrame) SwingUtilities.getWindowAncestor(this), selected);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) refresh();
    }

    private void toggleActive() {
        Employee selected = getSelectedEmployee();
        if (selected == null) return;

        boolean nextState = !selected.isActive();
        employeeController.setActive(selected.getId(), nextState, selected.getFullName(), this, this::refresh, (m, e) -> showError(m));
    }

    private void deleteEmployee() {
        Employee selected = getSelectedEmployee();
        if (selected == null) return;

        employeeController.delete(selected.getId(), this, this::refresh, (m, e) -> showError(m));
    }

    private Employee getSelectedEmployee() {
        int row = table.getSelectedRow();
        if (row < 0) {
            showWarning("Seleccione un empleado de la lista.");
            return null;
        }
        String id = (String) tableModel.getValueAt(row, 0);
        return currentEmployees.stream()
                .filter(e -> e.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
}
