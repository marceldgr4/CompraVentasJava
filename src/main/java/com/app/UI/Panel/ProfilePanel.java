package com.app.UI.Panel;

import Infrastructure.security.SessionManager;
import com.app.Controllers.ProfileController;
import com.app.Model.domain.Profile;
import com.app.Service.ProfileService;
import com.app.UI.Components.ButtonFactory;
import com.app.UI.dialogs.EmployeeRegisterDialog;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;

/**
 * Panel para la gestión de Perfiles de Usuario (Empleados).
 * Solo accesible por Administradores.
 */
public class ProfilePanel extends JPanel {

    private DefaultTableModel tableModel;
    private JTable table;
    private JLabel lblStatus;
    private final ProfileController profileController = new ProfileController();
    private List<Profile> currentProfiles = new ArrayList<>();

    public ProfilePanel() {
        if (!SessionManager.isAdmin()) {
            setLayout(new BorderLayout());
            add(new JLabel("Acceso Denegado: Solo administradores pueden ver este panel.", SwingConstants.CENTER));
            return;
        }
        initComponents();
        loadProfiles();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        setBackground(new Color(245, 247, 250));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel lblTitle = new JLabel("Gestión de Empleados");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(30, 42, 74));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);
        
        JButton btnView = ButtonFactory.createNeutralButton("Ver Detalle");
        JButton btnEdit = ButtonFactory.createPrimaryButton("Editar");
        JButton btnDelete = ButtonFactory.createDangerButton("Eliminar");
        JButton btnAdd = ButtonFactory.createSuccessButton("Registrar Nuevo Empleado");
        
        btnView.addActionListener(e -> viewProfile());
        btnEdit.addActionListener(e -> editProfile());
        btnDelete.addActionListener(e -> deleteProfile());
        btnAdd.addActionListener(e -> showCreateProfileDialog());

        btnPanel.add(btnView);
        btnPanel.add(btnEdit);
        btnPanel.add(btnDelete);
        btnPanel.add(btnAdd);

        header.add(lblTitle, BorderLayout.WEST);
        header.add(btnPanel, BorderLayout.EAST);

        // Table
        String[] columns = {"ID", "Nombre Completo", "Rol", "Estado", "Última Conexión"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(40);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));

        // Footer
        lblStatus = new JLabel("Gestiona los permisos y accesos del personal.");
        lblStatus.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblStatus.setForeground(Color.GRAY);

        add(header, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(lblStatus, BorderLayout.SOUTH);
    }

    private void loadProfiles() {
        tableModel.setRowCount(0);
        lblStatus.setText("Cargando perfiles...");
        profileController.loadAll(this,
            profiles -> {
                currentProfiles = profiles;
                for (Profile p : profiles) {
                    tableModel.addRow(new Object[]{
                       p.getId().substring(0, 8) + "...", 
                       p.getFullName(), 
                       p.getRol() != null ? p.getRol().name() : "N/A", 
                       p.isActive() ? "ACTIVO" : "INACTIVO", 
                       p.getEmail()
                    });
               }
               lblStatus.setText("Total: " + profiles.size() + " empleados encontrados.");
            },
            (msg, ex) -> lblStatus.setText("Error al cargar empleados: " + msg)
        );
    }

    private void showCreateProfileDialog() {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        EmployeeRegisterDialog dialog = new EmployeeRegisterDialog(parentWindow);
        dialog.setVisible(true);
        
        if (dialog.isSuccessful()) {
            loadProfiles();
        }
    }

    private Profile getSelectedProfile() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Por favor seleccione un empleado de la lista.", "Selección requerida", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return currentProfiles.get(row);
    }

    private void viewProfile() {
        Profile p = getSelectedProfile();
        if (p == null) return;
        String details = "ID: " + p.getId() + "\n"
                       + "Nombre: " + p.getFullName() + "\n"
                       + "Email: " + p.getEmail() + "\n"
                       + "Rol: " + (p.getRol() != null ? p.getRol().name() : "N/A") + "\n"
                       + "Estado: " + (p.isActive() ? "Activo" : "Inactivo");
        JOptionPane.showMessageDialog(this, details, "Detalle del Empleado", JOptionPane.INFORMATION_MESSAGE);
    }

    private void editProfile() {
        Profile p = getSelectedProfile();
        if (p == null) return;
        
        JTextField txtName = new JTextField(p.getFullName());
        JComboBox<com.app.Model.Enum.RolUser> cmbRole = new JComboBox<>(com.app.Model.Enum.RolUser.values());
        cmbRole.setSelectedItem(p.getRol());

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Nombre Completo:"));
        panel.add(txtName);
        panel.add(new JLabel("Rol:"));
        panel.add(cmbRole);

        int result = JOptionPane.showConfirmDialog(this, panel, "Editar Empleado", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            p.setFullName(txtName.getText().trim());
            p.setRol((com.app.Model.Enum.RolUser) cmbRole.getSelectedItem());
            profileController.update(p, this, this::loadProfiles, (msg, ex) -> {});
        }
    }

    private void deleteProfile() {
        Profile p = getSelectedProfile();
        if (p == null) return;
        profileController.delete(p.getId(), this, this::loadProfiles, (msg, ex) -> {});
    }
}
