package com.app.UI.dialogs;

import com.app.Model.Enum.RolUser;
import com.app.Model.domain.Employee;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Diálogo para editar los datos de un empleado existente.
 */
public class EmployeeEditDialog extends BaseDialog {

    private StyledField txtFullName;
    private StyledCombo<RolUser> cmbRole;
    private JCheckBox chkActive;

    private boolean confirmed = false;
    private final Employee employee;

    public EmployeeEditDialog(Window parent, Employee employee) {
        super(parent, "Editar Empleado", "✏️");
        this.employee = employee;
        setSize(440, 400);
        setLocationRelativeTo(parent);
        
        setContentBody(buildBody());
        setFooter(buildFooter());
        
        fillFields();
    }

    private JPanel buildBody() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(24, 28, 16, 28));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        int row = 0;

        // Info de solo lectura: email
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 2; gc.insets = ins(0, 0, 4, 0);
        form.add(fieldLabel("Correo electrónico (solo lectura)"), gc); row++;
        
        JTextField txtEmail = new JTextField(employee.getEmail() != null ? employee.getEmail() : "N/A");
        txtEmail.setEditable(false);
        txtEmail.setFont(FONT_FIELD);
        txtEmail.setBackground(new Color(235, 238, 245));
        txtEmail.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(8, 10, 8, 10)));
        gc.gridy = row; gc.insets = ins(0, 0, 16, 0);
        form.add(txtEmail, gc); row++;

        // Nombre completo
        gc.gridy = row; gc.insets = ins(0, 0, 4, 0);
        form.add(fieldLabel("Nombre Completo *"), gc); row++;
        txtFullName = styledField("Nombre completo");
        gc.gridy = row; gc.insets = ins(0, 0, 16, 0);
        form.add(txtFullName, gc); row++;

        // Rol
        gc.gridy = row; gc.insets = ins(0, 0, 4, 0);
        form.add(fieldLabel("Rol *"), gc); row++;
        cmbRole = styledCombo();
        for (RolUser rol : RolUser.values()) cmbRole.addItem(rol);
        gc.gridy = row; gc.insets = ins(0, 0, 16, 0);
        form.add(cmbRole, gc); row++;

        // Estado activo/inactivo
        chkActive = new JCheckBox("Cuenta activa (el empleado puede iniciar sesión)");
        chkActive.setFont(FONT_FIELD);
        chkActive.setOpaque(false);
        chkActive.setForeground(TEXT_DARK);
        gc.gridy = row; gc.insets = ins(0, 0, 0, 0);
        form.add(chkActive, gc);

        return form;
    }

    private JPanel buildFooter() {
        JButton btnCancel = buildCancelButton();
        JButton btnSave   = buildPrimaryButton("Guardar");
        btnCancel.addActionListener(e -> onCancel());
        btnSave  .addActionListener(e -> doSave());
        return buildStandardFooter(btnCancel, btnSave);
    }

    private void fillFields() {
        txtFullName.setText(employee.getFullName() != null ? employee.getFullName() : "");
        if (employee.getRol() != null) cmbRole.setSelectedItem(employee.getRol());
        chkActive.setSelected(employee.isActive());
    }

    private void doSave() {
        String name = txtFullName.getText().trim();
        if (name.isBlank()) {
            showValidationError("El nombre completo es obligatorio."); return;
        }
        employee.setFullName(name);
        employee.setRol((RolUser) cmbRole.getSelectedItem());
        employee.setActive(chkActive.isSelected());
        confirmed = true;
        dispose();
    }

    public boolean isConfirmed()  { return confirmed; }
    public Employee getEmployee()   { return employee; }
}

