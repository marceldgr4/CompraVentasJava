package com.app.UI.Panel;

import com.app.Model.domain.Cliente;
import com.app.Service.ClienteService;
import com.app.UI.Components.ButtonFactory;
import com.app.UI.Components.ModernTextField;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Panel para la gestión de clientes.
 * Permite listar, buscar y gestionar la información de los clientes.
 */
public class ClientePanel extends JPanel {

    private final ClienteService clienteService = new ClienteService();
    private DefaultTableModel tableModel;
    private JTable table;
    private ModernTextField txtSearch;
    private JLabel lblStatus;

    public ClientePanel() {
        initComponents();
        loadData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        setBackground(new Color(245, 247, 250));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel lblTitle = new JLabel("Gestión de Clientes");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(30, 42, 74));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actions.setOpaque(false);

        txtSearch = new ModernTextField("Buscar cliente...");
        txtSearch.setPreferredSize(new Dimension(250, 40));
        txtSearch.addActionListener(e -> searchClientes());

        JButton btnSearch = ButtonFactory.createPrimaryButton("Buscar", null);
        btnSearch.addActionListener(e -> searchClientes());

        JButton btnAdd = ButtonFactory.createPrimaryButton("Nuevo Cliente", null);
        btnAdd.addActionListener(e -> showAddDialog());

        actions.add(txtSearch);
        actions.add(btnSearch);
        actions.add(btnAdd);

        header.add(lblTitle, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);

        // Table
        String[] columns = {"ID", "Nombre", "Apellido", "Correo", "Teléfono"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(35);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));

        // Footer / Status
        lblStatus = new JLabel("Cargando clientes...");
        lblStatus.setFont(new Font("Segoe UI", Font.ITALIC, 12));

        add(header, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(lblStatus, BorderLayout.SOUTH);
    }

    private void loadData() {
        lblStatus.setText("Cargando...");
        CompletableFuture.supplyAsync(() -> {
            try {
                return clienteService.getAll();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).thenAccept(clientes -> SwingUtilities.invokeLater(() -> updateTable(clientes)))
          .exceptionally(ex -> {
              SwingUtilities.invokeLater(() -> lblStatus.setText("Error al cargar datos."));
              return null;
          });
    }

    private void searchClientes() {
        String term = txtSearch.getText().trim();
        if (term.isEmpty()) {
            loadData();
            return;
        }

        lblStatus.setText("Buscando '" + term + "'...");
        CompletableFuture.supplyAsync(() -> {
            try {
                return clienteService.search(term);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).thenAccept(clientes -> SwingUtilities.invokeLater(() -> updateTable(clientes)))
          .exceptionally(ex -> {
              SwingUtilities.invokeLater(() -> lblStatus.setText("Error en la búsqueda."));
              return null;
          });
    }

    private void updateTable(List<Cliente> clientes) {
        tableModel.setRowCount(0);
        for (Cliente c : clientes) {
            tableModel.addRow(new Object[]{
                    c.getId(),
                    c.getFirstName(),
                    c.getLastName(),
                    c.getEmail(),
                    c.getPhone()
            });
        }
        lblStatus.setText("Total: " + clientes.size() + " clientes encontrados.");
    }

    private void showAddDialog() {
        JOptionPane.showMessageDialog(this, 
            "La funcionalidad de agregar/editar clientes se habilitará en la próxima actualización.", 
            "Próximamente", 
            JOptionPane.INFORMATION_MESSAGE);
    }
}
