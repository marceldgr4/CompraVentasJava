package com.app.UI.dialogs;

import com.app.Controllers.AuthController;
import com.app.Model.Enum.RolUser;
import com.app.UI.Components.ButtonFactory;
import javax.swing.*;
import java.awt.*;

/**
 * Diálogo para el registro de nuevos empleados en Supabase Auth.
 * Solo debe ser utilizado por Administradores.
 */
public class EmployeeRegisterDialog extends JDialog {
    private JTextField txtFullName;
    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private JComboBox<RolUser> cmbRole;
    private JButton btnRegister;
    private JButton btnCancel;
    private boolean successful = false;
    private final AuthController authController = new AuthController();

    public EmployeeRegisterDialog(Window parent) {
        super(parent, "Registrar Nuevo Empleado", ModalityType.APPLICATION_MODAL);
        initComponents();
        setSize(400, 380);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initComponents() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 12, 24));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 4, 8, 4);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 13);

        // Full Name
        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0;
        form.add(new JLabel("Nombre Completo:"), gc);
        gc.gridx = 1; gc.weightx = 1;
        txtFullName = new JTextField();
        txtFullName.setFont(fieldFont);
        form.add(txtFullName, gc);

        // Email
        gc.gridx = 0; gc.gridy = 1; gc.weightx = 0;
        form.add(new JLabel("Correo Electrónico:"), gc);
        gc.gridx = 1; gc.weightx = 1;
        txtEmail = new JTextField();
        txtEmail.setFont(fieldFont);
        form.add(txtEmail, gc);

        // Password
        gc.gridx = 0; gc.gridy = 2; gc.weightx = 0;
        form.add(new JLabel("Contraseña:"), gc);
        gc.gridx = 1; gc.weightx = 1;
        txtPassword = new JPasswordField();
        txtPassword.setFont(fieldFont);
        form.add(txtPassword, gc);

        // Role
        gc.gridx = 0; gc.gridy = 3; gc.weightx = 0;
        form.add(new JLabel("Rol:"), gc);
        gc.gridx = 1; gc.weightx = 1;
        cmbRole = new JComboBox<>(RolUser.values());
        cmbRole.setFont(fieldFont);
        form.add(cmbRole, gc);

        // Help label
        gc.gridx = 1; gc.gridy = 4;
        JLabel lblHelp = new JLabel("Mínimo 6 caracteres");
        lblHelp.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblHelp.setForeground(Color.GRAY);
        form.add(lblHelp, gc);

        // Buttons
        gc.gridy = 5; gc.gridwidth = 2;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 10));
        btnCancel = ButtonFactory.createNeutralButton("Cancelar");
        btnCancel.addActionListener(e -> dispose());
        btnRegister = ButtonFactory.createPrimaryButton("Registrar");
        btnRegister.addActionListener(e -> doRegister());
        btnPanel.add(btnCancel);
        btnPanel.add(btnRegister);
        form.add(btnPanel, gc);

        setContentPane(form);
    }

    private void doRegister() {
        String name = txtFullName.getText().trim();
        String email = txtEmail.getText().trim();
        String password = new String(txtPassword.getPassword());
        RolUser role = (RolUser) cmbRole.getSelectedItem();

        btnRegister.setEnabled(false);
        btnRegister.setText("Registrando...");

        authController.registerEmployee(email, password, name, role, this,
            () -> {
                successful = true;
                dispose();
            },
            (msg, ex) -> {
                btnRegister.setEnabled(true);
                btnRegister.setText("Registrar");
            }
        );
    }

    public boolean isSuccessful() {
        return successful;
    }
}
