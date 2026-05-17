package com.app.UI.dialogs;

import com.app.Model.domain.Cliente;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Diálogo para crear o editar un cliente.
 */
public class ClienteDialog extends BaseDialog {

    private StyledField txtCedula;
    private StyledField txtFirstName;
    private StyledField txtLastName;
    private StyledField txtEmail;
    private StyledField txtPhone;
    private StyledField txtAddress;
    private StyledField txtCity;

    private boolean confirmed = false;

    public ClienteDialog(Window parent, Cliente cliente) {
        super(parent, cliente == null ? "Nuevo Cliente" : "Editar Cliente", "👤");
        setSize(500, 560);
        setLocationRelativeTo(parent);
        
        setContentBody(buildBody());
        setFooter(buildFooter());
        
        if (cliente != null) fillFields(cliente);
    }

    public ClienteDialog(JFrame parent, Cliente cliente) {
        this((Window) parent, cliente);
    }

    private JPanel buildBody() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(20, 24, 12, 24));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        int row = 0;

        // Cedula
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 2; gc.insets = ins(0, 0, 4, 0);
        form.add(fieldLabel("Identificación *"), gc); row++;
        txtCedula = styledField("Número de cédula o NIT");
        gc.gridy = row; gc.insets = ins(0, 0, 12, 0);
        form.add(txtCedula, gc); row++;

        // Nombre + Apellido
        gc.gridwidth = 1; gc.weightx = 1;
        gc.gridx = 0; gc.gridy = row; gc.insets = ins(0, 0, 4, 6);
        form.add(fieldLabel("Nombre *"), gc);
        gc.gridx = 1; gc.insets = ins(0, 6, 4, 0);
        form.add(fieldLabel("Apellido *"), gc);
        row++;
        txtFirstName = styledField("Nombre");
        txtLastName  = styledField("Apellido");
        gc.gridx = 0; gc.gridy = row; gc.insets = ins(0, 0, 12, 6);
        form.add(txtFirstName, gc);
        gc.gridx = 1; gc.insets = ins(0, 6, 12, 0);
        form.add(txtLastName, gc);
        row++;

        // Email
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 2; gc.insets = ins(0, 0, 4, 0);
        form.add(fieldLabel("Correo electrónico"), gc); row++;
        txtEmail = styledField("ejemplo@correo.com");
        gc.gridy = row; gc.insets = ins(0, 0, 12, 0);
        form.add(txtEmail, gc); row++;

        // Teléfono
        gc.gridy = row; gc.insets = ins(0, 0, 4, 0);
        form.add(fieldLabel("Teléfono"), gc); row++;
        txtPhone = styledField("Número de teléfono");
        gc.gridy = row; gc.insets = ins(0, 0, 12, 0);
        form.add(txtPhone, gc); row++;

        // Dirección + Ciudad
        gc.gridwidth = 1;
        gc.gridx = 0; gc.gridy = row; gc.insets = ins(0, 0, 4, 6);
        form.add(fieldLabel("Dirección"), gc);
        gc.gridx = 1; gc.insets = ins(0, 6, 4, 0);
        form.add(fieldLabel("Ciudad"), gc);
        row++;
        txtAddress = styledField("Dirección");
        txtCity    = styledField("Ciudad");
        gc.gridx = 0; gc.gridy = row; gc.insets = ins(0, 0, 6, 6);
        form.add(txtAddress, gc);
        gc.gridx = 1; gc.insets = ins(0, 6, 6, 0);
        form.add(txtCity, gc);

        return form;
    }

    private JPanel buildFooter() {
        JButton btnCancel = buildCancelButton();
        JButton btnSave   = buildPrimaryButton("Guardar");
        btnCancel.addActionListener(e -> onCancel());
        btnSave  .addActionListener(e -> doSave());
        return buildStandardFooter(btnCancel, btnSave);
    }

    private void fillFields(Cliente c) {
        txtCedula   .setText(c.getCedula());
        txtFirstName.setText(c.getFirstName());
        txtLastName .setText(c.getLastName());
        txtEmail    .setText(c.getEmail());
        txtPhone    .setText(c.getPhone());
        txtAddress  .setText(c.getAddress());
        txtCity     .setText(c.getCity());
    }

    private void doSave() {
        if (txtFirstName.getText().isBlank()) {
            showValidationError("El nombre es obligatorio.");
            return;
        }
        String cedula = txtCedula.getText().trim();
        if (!cedula.isEmpty() && !cedula.matches("^[0-9]+$")) {
            showValidationError("La cédula solo permite números.");
            return;
        }
        String email = txtEmail.getText().trim();
        if (!email.isEmpty() && (!email.contains("@") || !email.contains("."))) {
            showValidationError("Ingresa un correo electrónico válido.");
            return;
        }
        confirmed = true;
        dispose();
    }

    public boolean isConfirmed() { return confirmed; }

    public Cliente getCliente() {
        String cedula = txtCedula.getText().trim();
        return new Cliente(
                cedula.isEmpty() ? null : cedula,
                txtFirstName.getText().trim(),
                txtLastName .getText().trim(),
                txtEmail    .getText().trim(),
                txtPhone    .getText().trim(),
                txtAddress  .getText().trim(),
                txtCity     .getText().trim()
        );
    }
}
