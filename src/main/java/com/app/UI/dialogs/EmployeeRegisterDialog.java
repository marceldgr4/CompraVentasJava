package com.app.UI.dialogs;

import com.app.Controllers.AuthController;
import com.app.Model.Enum.RolUser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Diálogo para el registro de nuevos empleados en Supabase Auth.
 */
public class EmployeeRegisterDialog extends BaseDialog {

    private StyledField txtFullName;
    private StyledField txtEmail;
    private JPasswordField txtPassword;
    private StyledCombo<RolUser> cmbRole;
    private JButton btnRegister;

    private final AuthController authController = new AuthController();

    public EmployeeRegisterDialog(Window parent) {
        super(parent, "Registrar Nuevo Empleado", "👔");
        setSize(460, 480);
        setLocationRelativeTo(parent);
        
        setContentBody(buildBody());
        setFooter(buildFooter());
    }

    private JPanel buildBody() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(24, 28, 16, 28));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        int row = 0;

        // Nombre completo
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 2; gc.insets = ins(0, 0, 4, 0);
        form.add(fieldLabel("Nombre Completo *"), gc); row++;
        txtFullName = styledField("Nombre y apellido");
        gc.gridy = row; gc.insets = ins(0, 0, 14, 0);
        form.add(txtFullName, gc); row++;

        // Email
        gc.gridy = row; gc.insets = ins(0, 0, 4, 0);
        form.add(fieldLabel("Correo Electrónico *"), gc); row++;
        txtEmail = styledField("correo@ejemplo.com");
        gc.gridy = row; gc.insets = ins(0, 0, 14, 0);
        form.add(txtEmail, gc); row++;

        // Contraseña
        gc.gridy = row; gc.insets = ins(0, 0, 4, 0);
        form.add(fieldLabel("Contraseña * (mín. 6 caracteres)"), gc); row++;
        txtPassword = new JPasswordField();
        txtPassword.setFont(FONT_FIELD);
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(8, 10, 8, 10)));
        txtPassword.setBackground(FIELD_BG);
        gc.gridy = row; gc.insets = ins(0, 0, 14, 0);
        form.add(txtPassword, gc); row++;

        // Rol
        gc.gridy = row; gc.insets = ins(0, 0, 4, 0);
        form.add(fieldLabel("Rol del empleado *"), gc); row++;
        cmbRole = styledCombo();
        for (RolUser role : RolUser.values()) cmbRole.addItem(role);
        gc.gridy = row; gc.insets = ins(0, 0, 0, 0);
        form.add(cmbRole, gc);

        return form;
    }

    private JPanel buildFooter() {
        JButton btnCancel = buildCancelButton();
        btnRegister = buildSuccessButton("Registrar");
        btnCancel  .addActionListener(e -> onCancel());
        btnRegister.addActionListener(e -> doRegister());
        return buildStandardFooter(btnCancel, btnRegister);
    }

    private void doRegister() {
        String name     = txtFullName.getText().trim();
        String email    = txtEmail.getText().trim();
        String password = new String(txtPassword.getPassword());
        RolUser role    = (RolUser) cmbRole.getSelectedItem();

        if (name.isBlank())     { showValidationError("El nombre completo es obligatorio."); return; }
        if (email.isBlank())    { showValidationError("El correo electrónico es obligatorio."); return; }
        if (password.length() < 6) { showValidationError("La contraseña debe tener al menos 6 caracteres."); return; }

        btnRegister.setEnabled(false);
        btnRegister.setText("Registrando...");

        authController.registerEmployee(email, password, name, role, this,
            () -> {
                confirmed = true;
                dispose();
            },
            (msg, ex) -> {
                showValidationError("Error al registrar: " + msg);
                btnRegister.setEnabled(true);
                btnRegister.setText("Registrar");
            }
        );
    }

}
