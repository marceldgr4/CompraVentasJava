package com.app.UI.dialogs;

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
        super(parent, cliente == null ? "New Client" : "Edit Client", true);
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
        form.add(new JLabel("First Name:"), gc);
        gc.gridx = 1; gc.weightx = 1;
        txtFirstName = new JTextField();
        txtFirstName.setFont(fieldFont);
        form.add(txtFirstName, gc);

        // Last Name
        gc.gridx = 0; gc.gridy = 1; gc.weightx = 0;
        form.add(new JLabel("Last Name:"), gc);
        gc.gridx = 1; gc.weightx = 1;
        txtLastName = new JTextField();
        txtLastName.setFont(fieldFont);
        form.add(txtLastName, gc);

        // Email
        gc.gridx = 0; gc.gridy = 2; gc.weightx = 0;
        form.add(new JLabel("Email:"), gc);
        gc.gridx = 1; gc.weightx = 1;
        txtEmail = new JTextField();
        txtEmail.setFont(fieldFont);
        form.add(txtEmail, gc);

        // Phone
        gc.gridx = 0; gc.gridy = 3; gc.weightx = 0;
        form.add(new JLabel("Phone:"), gc);
        gc.gridx = 1; gc.weightx = 1;
        txtPhone = new JTextField();
        txtPhone.setFont(fieldFont);
        form.add(txtPhone, gc);

        // Buttons
        gc.gridy = 4; gc.gridwidth = 2;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(e -> dispose());
        btnSave = new JButton("Save");
        btnSave.setBackground(new Color(30, 136, 229));
        btnSave.setForeground(Color.WHITE);
        btnSave.setBorderPainted(false);
        btnSave.setFocusPainted(false);
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
            JOptionPane.showMessageDialog(this, "Name is required");
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
                0,
                txtFirstName.getText().trim(),
                txtLastName.getText().trim(),
                txtEmail.getText().trim(),
                txtPhone.getText().trim(),
                null
        );
    }
}

