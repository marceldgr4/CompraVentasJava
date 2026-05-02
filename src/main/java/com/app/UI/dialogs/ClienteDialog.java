package com.app.UI.dialogs;

import com.app.Model.Enum.ClienteStatus;
import com.app.Model.domain.Cliente;

import javax.swing.*;
import java.awt.*;

public class ClienteDialog extends JDialog {
    private JTextField txtFirstName;
    private JTextField txtLastName;
    private JTextField txtEmail;
    private JTextField txtPhone;
    private JButton btnSave;
    private JButton btnCancel;
    private boolean confirmed = false;

    public ClienteDialog(JFrame parent, Cliente cliente) {
        super(parent, cliente == null ? "Nuevo Cliente" : "Editar Cliente", true);
        initComponents();
        if (cliente != null) fillFields(cliente);
        setSize(400, 280);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initComponents() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 12, 24));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 4, 6, 4);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 13);

        // First Name
        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0;
        form.add(new JLabel("Nombre:"), gc);
        gc.gridx = 1; gc.weightx = 1;
        txtFirstName = new JTextField();
        txtFirstName.setFont(fieldFont);
        form.add(txtFirstName, gc);

        // Last Name
        gc.gridx = 0; gc.gridy = 1; gc.weightx = 0;
        form.add(new JLabel("Apellido:"), gc);
        gc.gridx = 1; gc.weightx = 1;
        txtLastName = new JTextField();
        txtLastName.setFont(fieldFont);
        form.add(txtLastName, gc);

        // Email
        gc.gridx = 0; gc.gridy = 2; gc.weightx = 0;
        form.add(new JLabel("Correo electrónico:"), gc);
        gc.gridx = 1; gc.weightx = 1;
        txtEmail = new JTextField();
        txtEmail.setFont(fieldFont);
        form.add(txtEmail, gc);

        // Phone
        gc.gridx = 0; gc.gridy = 3; gc.weightx = 0;
        form.add(new JLabel("Teléfono:"), gc);
        gc.gridx = 1; gc.weightx = 1;
        txtPhone = new JTextField();
        txtPhone.setFont(fieldFont);
        form.add(txtPhone, gc);

        // Buttons
        gc.gridy = 4; gc.gridwidth = 2;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnCancel = com.app.UI.Components.ButtonFactory.createNeutralButton("Cancelar");
        btnCancel.addActionListener(e -> dispose());
        btnSave = com.app.UI.Components.ButtonFactory.createPrimaryButton("Guardar");
        btnSave.addActionListener(e -> doSave());
        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        form.add(btnPanel, gc);

        setContentPane(form);
    }

    private void fillFields(Cliente c) {
        if (c != null) {
            txtFirstName.setText(c.getFirstName());
            txtLastName.setText(c.getLastName());
            txtEmail.setText(c.getEmail());
            txtPhone.setText(c.getPhone());
        }
    }

    private void doSave() {
        if (txtFirstName.getText().isBlank() || txtLastName.getText().isBlank()) {
            JOptionPane.showMessageDialog(this,
                    "El nombre y apellido son obligatorios",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ✅ Validar email básico
        String email = txtEmail.getText().trim();
        if (!email.contains("@") || !email.contains(".")) {
            JOptionPane.showMessageDialog(this,
                    "Ingresa un correo electrónico válido.\nEjemplo: usuario@correo.com",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ✅ Validar teléfono — mínimo 7 dígitos
        String phone = txtPhone.getText().trim();
        if (!phone.matches("^[+]?[0-9\\s\\-]{7,20}$")) {
            JOptionPane.showMessageDialog(this,
                    "Teléfono inválido.\n" +
                            "• Mínimo 7 dígitos\n" +
                            "• Solo números, espacios o guiones\n" +
                            "• Ejemplo: 3001234567 o +57 300 123 4567",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        confirmed = true;
        dispose();
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public Cliente getCliente() {
        return new Cliente(
                txtFirstName.getText().trim(),
                txtLastName.getText().trim(),
                txtEmail.getText().trim(),
                txtPhone.getText().trim()
        );
    }
}

