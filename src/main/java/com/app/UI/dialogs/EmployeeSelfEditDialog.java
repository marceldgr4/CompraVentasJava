package com.app.UI.dialogs;

import com.app.Controllers.EmployeeController;
import com.app.UI.Frame.MainFrame;
import Infrastructure.security.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Diálogo para que el usuario actual (Empleado o Admin) edite su propio perfil.
 * Permite cambiar nombre y contraseña.
 */
public class EmployeeSelfEditDialog extends BaseDialog {

    private StyledField txtFullName;
    private StyledPasswordField txtPassword;
    private StyledPasswordField txtConfirmPassword;
    private final EmployeeController employeeController = new EmployeeController();

    public EmployeeSelfEditDialog(JFrame parent) {
        super(parent, "Mi Perfil", "👤");
        setSize(480, 500);
        setLocationRelativeTo(parent);

        setContentBody(buildBody());
        setFooter(buildFooter());

        loadCurrentData();
    }

    private JPanel buildBody() {
        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(24, 24, 16, 24));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        int row = 0;

        // --- Información ---
        gc.gridy = row++; gc.insets = ins(0,0,12,0);
        JLabel lblInfo = new JLabel("<html>Actualiza tus datos de acceso y nombre público.</html>");
        lblInfo.setFont(FONT_FIELD);
        lblInfo.setForeground(Color.GRAY);
        body.add(lblInfo, gc);

        // --- Nombre Completo ---
        gc.gridy = row++; gc.insets = ins(10, 0, 4, 0);
        body.add(fieldLabel("Nombre Completo *"), gc);
        txtFullName = styledField("Tu nombre...");
        gc.gridy = row++; gc.insets = ins(0, 0, 16, 0);
        body.add(txtFullName, gc);

        // --- Contraseña ---
        gc.gridy = row++; gc.insets = ins(10, 0, 4, 0);
        body.add(fieldLabel("Nueva Contraseña (opcional)"), gc);
        txtPassword = styledPasswordField("Mínimo 6 caracteres");
        gc.gridy = row++; gc.insets = ins(0, 0, 16, 0);
        body.add(txtPassword, gc);

        gc.gridy = row++; gc.insets = ins(0, 0, 4, 0);
        body.add(fieldLabel("Confirmar Nueva Contraseña"), gc);
        txtConfirmPassword = styledPasswordField("Repite la contraseña");
        gc.gridy = row++; gc.insets = ins(0, 0, 12, 0);
        body.add(txtConfirmPassword, gc);

        return body;
    }

    private JPanel buildFooter() {
        JButton btnCancel = buildCancelButton();
        JButton btnSave   = buildPrimaryButton("Guardar Cambios");

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> doSave());

        return buildStandardFooter(btnCancel, btnSave);
    }

    private void loadCurrentData() {
        try {
            txtFullName.setText(SessionManager.getFullName());
        } catch (Exception ignored) {}
    }

    private void doSave() {
        String name = txtFullName.getText().trim();
        String pass = new String(txtPassword.getPassword()).trim();
        String conf = new String(txtConfirmPassword.getPassword()).trim();

        if (name.isEmpty()) {
            showValidationError("El nombre es obligatorio.");
            return;
        }

        if (!pass.isEmpty()) {
            if (pass.length() < 6) {
                showValidationError("La contraseña debe tener al menos 6 caracteres.");
                return;
            }
            if (!pass.equals(conf)) {
                showValidationError("Las contraseñas no coinciden.");
                return;
            }
        }

        employeeController.updateActiveEmployee(name, pass, this,
                () -> {
                    dispose();
                },
                (msg, ex) -> showValidationError("Error: " + msg)
        );
    }
}
