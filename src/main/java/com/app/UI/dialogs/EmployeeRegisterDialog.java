package com.app.UI.dialogs;

import com.app.Service.AuthService;
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
    private JButton btnRegister;
    private JButton btnCancel;
    private boolean successful = false;

    public EmployeeRegisterDialog(Window parent) {
        super(parent, "Registrar Nuevo Empleado", ModalityType.APPLICATION_MODAL);
        initComponents();
        setSize(400, 320);
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

        // Help label
        gc.gridx = 1; gc.gridy = 3;
        JLabel lblHelp = new JLabel("Mínimo 6 caracteres");
        lblHelp.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblHelp.setForeground(Color.GRAY);
        form.add(lblHelp, gc);

        // Buttons
        gc.gridy = 4; gc.gridwidth = 2;
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

        // Validaciones básicas
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            JOptionPane.showMessageDialog(this, "Correo electrónico inválido", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (password.length() < 6) {
            JOptionPane.showMessageDialog(this, "La contraseña debe tener al menos 6 caracteres", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Ejecutar registro en hilo secundario para no congelar la UI
        btnRegister.setEnabled(false);
        btnRegister.setText("Registrando...");

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                new AuthService().registerEmployee(email, password, name);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    successful = true;
                    JOptionPane.showMessageDialog(EmployeeRegisterDialog.this, 
                        "Empleado registrado exitosamente.", 
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } catch (Exception e) {
                    btnRegister.setEnabled(true);
                    btnRegister.setText("Registrar");
                    String msg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                    JOptionPane.showMessageDialog(EmployeeRegisterDialog.this, 
                        "Error al registrar: " + msg, "Error de Registro", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    public boolean isSuccessful() {
        return successful;
    }
}
