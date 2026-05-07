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

/**
 * Panel para la gestión de Perfiles de Usuario (Empleados).
 * Solo accesible por Administradores.
 */
public class ProfilePanel extends JPanel {

    private DefaultTableModel tableModel;
    private JTable table;
    private JLabel lblStatus;
    private final ProfileController profileController = new ProfileController();

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

        JLabel lblTitle = new JLabel("Configuración de Cuentas / Perfiles");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(30, 42, 74));

        JButton btnAdd = ButtonFactory.createPrimaryButton("Registrar Nuevo Empleado", null);
        btnAdd.addActionListener(e -> showCreateProfileDialog());

        header.add(lblTitle, BorderLayout.WEST);
        header.add(btnAdd, BorderLayout.EAST);

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
                for (Profile p : profiles) {
                    tableModel.addRow(new Object[]{
                       p.getId(), 
                       p.getFullName(), 
                       p.getRol() != null ? p.getRol().name() : "N/A", 
                       p.isActive() ? "ACTIVO" : "INACTIVO", 
                       "N/A"
                    });
               }
               lblStatus.setText("Total: " + profiles.size() + " perfiles encontrados.");
            },
            (msg, ex) -> lblStatus.setText("Error al cargar perfiles: " + msg)
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
}
