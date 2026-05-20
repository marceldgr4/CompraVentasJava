package com.app.UI.Panel;

import com.app.Infrastructure.security.SessionManager;
import com.app.Controllers.ClienteController;
import com.app.Model.domain.Cliente;
import com.app.UI.Components.ButtonFactory;
import com.app.UI.dialogs.ClienteDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ClientePanel extends BasePanel {

    private final ClienteController clienteController = new ClienteController();

    private DefaultTableModel tableModel;
    private JTable            table;
    private JTextField        txtSearch;
    private JLabel            lblStatus;
    private JButton           btnEdit;
    private JButton           btnDelete;
    
    private List<Cliente> currentClientes = new ArrayList<>();

    public ClientePanel() {
        super();
        refresh();
    }

    @Override
    protected void initComponents() {
        // Top Bar
        JPanel topPanel = new JPanel(new BorderLayout(0, 12));
        topPanel.setOpaque(false);
        topPanel.add(buildHeader(" Gestión de Clientes", "Administre la base de datos de clientes y su información de contacto"), BorderLayout.NORTH);

        com.app.UI.Components.ResponsivePanel actionsBar = new com.app.UI.Components.ResponsivePanel();

        txtSearch = new JTextField();
        actionsBar.addFilterComponent(buildSearchPanel("Buscar por nombre, CC o tel...", txtSearch, e -> searchClientes()));
        buildButtonPanel(actionsBar);

        topPanel.add(actionsBar, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "CC", "Nombre", "Apellido", "Correo", "Teléfono", "Dirección", "Ciudad"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        styleTable(table);
        add(createTableScroll(table), BorderLayout.CENTER);

        // Footer
        lblStatus = new JLabel("Cargando clientes...");
        lblStatus.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblStatus.setForeground(SUBTITLE_FG);
        add(lblStatus, BorderLayout.SOUTH);
    }

    private void buildButtonPanel(com.app.UI.Components.ResponsivePanel actionsBar) {
        JButton btnAdd = ButtonFactory.createSuccessButton("Nuevo Cliente", "add");
        btnEdit   = ButtonFactory.createWarningButton("Editar", "edit");
        btnDelete = ButtonFactory.createDangerButton("Eliminar", "delete");
        JButton btnRefresh = ButtonFactory.createNeutralButton("Actualizar", "refresh");

        btnAdd.addActionListener(e -> showAddDialog());
        btnEdit.addActionListener(e -> showEditDialog());
        btnDelete.addActionListener(e -> doDelete());
        btnRefresh.addActionListener(e -> refresh());

        // Permissions
        boolean isAdmin = SessionManager.isAdmin();
        btnEdit.setVisible(isAdmin);
        btnDelete.setVisible(isAdmin);

        actionsBar.addActionComponent(btnAdd);
        actionsBar.addActionComponent(btnEdit);
        actionsBar.addActionComponent(btnDelete);
        actionsBar.addActionComponent(btnRefresh);
    }

    @Override
    public void refresh() {
        lblStatus.setText("Cargando...");
        clienteController.loadAll(this, 
            list -> {
                currentClientes = list;
                updateTable(list);
            }, 
            (msg, ex) -> lblStatus.setText("Error: " + msg)
        );
    }

    private void searchClientes() {
        String term = txtSearch.getText().trim();
        lblStatus.setText("Buscando '" + term + "'...");
        clienteController.search(term, this, this::updateTable, (msg, ex) -> lblStatus.setText("Error: " + msg));
    }

    private void showAddDialog() {
        ClienteDialog dlg = new ClienteDialog((JFrame) SwingUtilities.getWindowAncestor(this), null);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            clienteController.create(dlg.getCliente(), this, this::refresh, (m, e) -> {});
        }
    }

    private void showEditDialog() {
        Cliente selected = getSelectedCliente();
        if (selected == null) return;
        
        ClienteDialog dlg = new ClienteDialog((JFrame) SwingUtilities.getWindowAncestor(this), selected);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            Cliente updated = dlg.getCliente();
            updated.setId(selected.getId());
            clienteController.update(updated, this, this::refresh, (m, e) -> {});
        }
    }

    private void doDelete() {
        Cliente selected = getSelectedCliente();
        if (selected == null) return;
        
        if (showConfirmation("¿Está seguro de eliminar al cliente '" + selected.getFullName() + "'?", "Confirmar")) {
            clienteController.delete(selected.getId(), selected.getFullName(), this, this::refresh, (m, e) -> {});
        }
    }

    private Cliente getSelectedCliente() {
        int row = table.getSelectedRow();
        if (row < 0) {
            showWarning("Seleccione un cliente de la lista.");
            return null;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        return currentClientes.stream()
                .filter(c -> c.getId() == id)
                .findFirst()
                .orElse(null);
    }

    private void updateTable(List<Cliente> clientes) {
        tableModel.setRowCount(0);
        for (Cliente c : clientes) {
            tableModel.addRow(new Object[]{
                    c.getId(), c.getCedula(), c.getFirstName(), c.getLastName(),
                    c.getEmail(), c.getPhone(), c.getAddress(), c.getCity()
            });
        }
        lblStatus.setText("Total: " + clientes.size() + " clientes encontrados.");
    }
}