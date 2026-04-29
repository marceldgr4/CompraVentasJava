package com.app.UI.Panel;

import Infrastructure.security.SessionManager;
import com.app.Model.domain.Cliente;
import com.app.Service.ClienteService;
import com.app.Service.exceptions.ServiceException;
import com.app.UI.Components.ButtonFactory;
import com.app.UI.Components.ModernTextField;
import com.app.UI.dialogs.ClienteDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Panel de gestión de clientes.
 *
 * <p><strong>Corrección de botones:</strong> usa {@link ButtonFactory} para que
 * los colores se rendericen correctamente en el L&F nativo de Windows.
 */
public class ClientePanel extends JPanel {

    private final ClienteService clienteService = new ClienteService();

    private DefaultTableModel tableModel;
    private JTable            table;
    private ModernTextField   txtSearch;
    private JLabel            lblStatus;
    private JButton           btnEdit;
    private JButton           btnDelete;

    public ClientePanel() {
        initComponents();
        loadData();
    }

    // ── UI ────────────────────────────────────────────────────────────────────

    private void initComponents() {
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        setBackground(new Color(245, 247, 250));

        add(buildHeader(),   BorderLayout.NORTH);
        add(buildTable(),    BorderLayout.CENTER);
        add(buildFooter(),   BorderLayout.SOUTH);
    }

    private JPanel buildHeader() {
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

        // Botones con ButtonFactory — color correcto en Windows
        JButton btnSearch = ButtonFactory.createPrimaryButton("Buscar");
        btnSearch.addActionListener(e -> searchClientes());

        JButton btnAdd = ButtonFactory.createSuccessButton("Nuevo Cliente");
        btnAdd.addActionListener(e -> showAddDialog());

        btnEdit   = ButtonFactory.createWarningButton("Editar");
        btnDelete = ButtonFactory.createDangerButton("Eliminar");
        btnEdit  .addActionListener(e -> showEditDialog());
        btnDelete.addActionListener(e -> doDelete());

        // Control de permisos
        boolean isAdmin = SessionManager.isAdmin();
        btnEdit  .setVisible(isAdmin);
        btnDelete.setVisible(isAdmin);

        actions.add(txtSearch);
        actions.add(btnSearch);
        actions.add(btnAdd);
        actions.add(btnEdit);
        actions.add(btnDelete);

        header.add(lblTitle, BorderLayout.WEST);
        header.add(actions,  BorderLayout.EAST);
        return header;
    }

    private JScrollPane buildTable() {
        String[] cols = {"ID", "Nombre", "Apellido", "Correo", "Teléfono"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(35);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        return sp;
    }

    private JLabel buildFooter() {
        lblStatus = new JLabel("Cargando clientes...");
        lblStatus.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        return lblStatus;
    }

    // ── Operaciones ───────────────────────────────────────────────────────────

    private void loadData() {
        lblStatus.setText("Cargando...");
        new LoadTask().execute();
    }

    private void searchClientes() {
        String term = txtSearch.getText().trim();
        if (term.isEmpty()) { loadData(); return; }
        lblStatus.setText("Buscando '" + term + "'...");
        new SearchTask(term).execute();
    }

    private void showAddDialog() {
        ClienteDialog dlg = new ClienteDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this), null);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) new CreateTask(dlg.getCliente()).execute();
    }

    private void showEditDialog() {
        Cliente selected = getSelectedCliente();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Selecciona un cliente para editar.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        ClienteDialog dlg = new ClienteDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this), selected);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            Cliente updated = dlg.getCliente();
            updated.setId(selected.getId());
            new UpdateTask(updated).execute();
        }
    }

    private void doDelete() {
        Cliente selected = getSelectedCliente();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Selecciona un cliente para eliminar.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this,
                "¿Eliminar a " + selected.getFullName() + "?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) new DeleteTask(selected.getId()).execute();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Cliente getSelectedCliente() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        return new Cliente(
                (int)    tableModel.getValueAt(row, 0),  // id
                (String) tableModel.getValueAt(row, 1),  // firstName
                (String) tableModel.getValueAt(row, 2),  // lastName
                (String) tableModel.getValueAt(row, 3),  // email
                (String) tableModel.getValueAt(row, 4),  // phone
                null,   // status
                null,   // createdAt
                null    // updatedAt
        );
    }

    private void updateTable(List<Cliente> clientes) {
        tableModel.setRowCount(0);
        for (Cliente c : clientes) {
            tableModel.addRow(new Object[]{
                    c.getId(), c.getFirstName(), c.getLastName(),
                    c.getEmail(), c.getPhone()
            });
        }
        lblStatus.setText("Total: " + clientes.size() + " clientes encontrados.");
    }

    // ── SwingWorker tasks ─────────────────────────────────────────────────────

    private class LoadTask extends SwingWorker<List<Cliente>, Void> {
        @Override protected List<Cliente> doInBackground() throws Exception { return clienteService.getAll(); }
        @Override protected void done() {
            try { updateTable(get()); }
            catch (ExecutionException ex) { lblStatus.setText("Error al cargar datos: " + ex.getCause().getMessage()); }
            catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
        }
    }

    private class SearchTask extends SwingWorker<List<Cliente>, Void> {
        private final String term;
        SearchTask(String t) { this.term = t; }
        @Override protected List<Cliente> doInBackground() throws Exception { return clienteService.search(term); }
        @Override protected void done() {
            try { updateTable(get()); }
            catch (ExecutionException ex) { lblStatus.setText("Error en búsqueda."); }
            catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
        }
    }

    private class CreateTask extends SwingWorker<Void, Void> {
        private final Cliente cliente;
        CreateTask(Cliente c) { this.cliente = c; }
        @Override protected Void doInBackground() throws Exception { clienteService.create(cliente); return null; }
        @Override protected void done() {
            try { get(); loadData(); JOptionPane.showMessageDialog(ClientePanel.this, "Cliente creado.", "OK", JOptionPane.INFORMATION_MESSAGE); }
            catch (ExecutionException ex) { JOptionPane.showMessageDialog(ClientePanel.this, "Error: " + ex.getCause().getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
            catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
        }
    }

    private class UpdateTask extends SwingWorker<Void, Void> {
        private final Cliente cliente;
        UpdateTask(Cliente c) { this.cliente = c; }
        @Override protected Void doInBackground() throws Exception { clienteService.update(cliente); return null; }
        @Override protected void done() {
            try { get(); loadData(); JOptionPane.showMessageDialog(ClientePanel.this, "Cliente actualizado.", "OK", JOptionPane.INFORMATION_MESSAGE); }
            catch (ExecutionException ex) { JOptionPane.showMessageDialog(ClientePanel.this, "Error: " + ex.getCause().getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
            catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
        }
    }

    private class DeleteTask extends SwingWorker<Void, Void> {
        private final int id;
        DeleteTask(int id) { this.id = id; }
        @Override protected Void doInBackground() throws Exception { clienteService.delete(id); return null; }
        @Override protected void done() {
            try { get(); loadData(); JOptionPane.showMessageDialog(ClientePanel.this, "Cliente eliminado.", "OK", JOptionPane.INFORMATION_MESSAGE); }
            catch (ExecutionException ex) { JOptionPane.showMessageDialog(ClientePanel.this, "Error: " + ex.getCause().getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
            catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
        }
    }
}