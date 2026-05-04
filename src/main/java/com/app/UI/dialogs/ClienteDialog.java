package com.app.UI.dialogs;

import com.app.Model.domain.Cliente;
import com.app.Model.Enum.ClienteStatus;
import com.app.Model.Enum.RegistrationType;

import javax.swing.*;
import java.awt.*;

public class ClienteDialog extends JDialog {
    private JTextField txtCedula;
    private JTextField txtFirstName;
    private JTextField txtLastName;
    private JTextField txtEmail;
    private JTextField txtPhone;
    private JTextField txtAddress;
    private JTextField txtCity;
    private JButton btnSave;
    private JButton btnCancel;
    private boolean confirmed = false;

    public ClienteDialog(JFrame parent, Cliente cliente) {
        super(parent, cliente == null ? "Nuevo Cliente" : "Editar Cliente", true);
        initComponents();
        if (cliente != null) fillFields(cliente);
        setSize(450, 400);
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

        int row = 0;

        // Cedula
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0;
        form.add(new JLabel("Identificación:"), gc);
        gc.gridx = 1; gc.weightx = 1;
        txtCedula = new JTextField();
        txtCedula.setFont(fieldFont);
        form.add(txtCedula, gc);
        row++;

        // First Name
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0;
        form.add(new JLabel("Nombre:"), gc);
        gc.gridx = 1; gc.weightx = 1;
        txtFirstName = new JTextField();
        txtFirstName.setFont(fieldFont);
        form.add(txtFirstName, gc);
        row++;

        // Last Name
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0;
        form.add(new JLabel("Apellido:"), gc);
        gc.gridx = 1; gc.weightx = 1;
        txtLastName = new JTextField();
        txtLastName.setFont(fieldFont);
        form.add(txtLastName, gc);
        row++;

        // Email
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0;
        form.add(new JLabel("Correo:"), gc);
        gc.gridx = 1; gc.weightx = 1;
        txtEmail = new JTextField();
        txtEmail.setFont(fieldFont);
        form.add(txtEmail, gc);
        row++;

        // Phone
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0;
        form.add(new JLabel("Teléfono:"), gc);
        gc.gridx = 1; gc.weightx = 1;
        txtPhone = new JTextField();
        txtPhone.setFont(fieldFont);
        form.add(txtPhone, gc);
        row++;

        // Address
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0;
        form.add(new JLabel("Dirección:"), gc);
        gc.gridx = 1; gc.weightx = 1;
        txtAddress = new JTextField();
        txtAddress.setFont(fieldFont);
        form.add(txtAddress, gc);
        row++;

        // City
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0;
        form.add(new JLabel("Ciudad:"), gc);
        gc.gridx = 1; gc.weightx = 1;
        txtCity = new JTextField();
        txtCity.setFont(fieldFont);
        form.add(txtCity, gc);
        row++;

        // Buttons
        gc.gridy = row; gc.gridwidth = 2; gc.insets = new Insets(20, 4, 6, 4);
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
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
            txtCedula.setText(c.getCedula());
            txtFirstName.setText(c.getFirstName());
            txtLastName.setText(c.getLastName());
            txtEmail.setText(c.getEmail());
            txtPhone.setText(c.getPhone());
            txtAddress.setText(c.getAddress());
            txtCity.setText(c.getCity());
        }
    }

    private void doSave() {
        if (txtCedula.getText().isBlank() || txtFirstName.getText().isBlank() || txtLastName.getText().isBlank()) {
            JOptionPane.showMessageDialog(this,
                    "Cédula, nombre y apellido son obligatorios",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validar cédula numérica
        if (!txtCedula.getText().trim().matches("^[0-9]+$")) {
            JOptionPane.showMessageDialog(this,
                    "La cédula solo permite números",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validar email básico
        String email = txtEmail.getText().trim();
        if (!email.isEmpty() && (!email.contains("@") || !email.contains("."))) {
            JOptionPane.showMessageDialog(this,
                    "Ingresa un correo electrónico válido.",
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
                txtCedula.getText().trim(),
                txtFirstName.getText().trim(),
                txtLastName.getText().trim(),
                txtEmail.getText().trim(),
                txtPhone.getText().trim(),
                txtAddress.getText().trim(),
                txtCity.getText().trim()
        );
    }
}
