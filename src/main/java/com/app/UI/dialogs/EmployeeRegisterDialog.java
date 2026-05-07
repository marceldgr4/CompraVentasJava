package com.app.UI.dialogs;

import com.app.Controllers.AuthController;
import com.app.Model.Enum.RolUser;
import com.app.UI.Components.ButtonFactory;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Diálogo para el registro de nuevos empleados en Supabase Auth.
 * Solo debe ser utilizado por Administradores.
 */
public class EmployeeRegisterDialog extends JDialog {
    private static final Color HEADER_BG   = new Color(18, 28, 58);
    private static final Color TEXT_DARK   = new Color(15, 25, 50);
    private static final Color FIELD_BG    = new Color(245, 247, 252);
    private static final Color BORDER_CLR  = new Color(210, 220, 235);
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
        setUndecorated(true);
        setSize(420, 480);
        setLocationRelativeTo(parent);
        setContentPane(buildRoot());
    }

    private JPanel buildRoot() {
        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(BORDER_CLR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
            }
        };
        root.setOpaque(false);
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildBody(), BorderLayout.CENTER);
        return root;
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(HEADER_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight() + 14, 14, 14);
                g2.fillRect(0, getHeight() / 2, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setPreferredSize(new Dimension(0, 58));
        header.setBorder(new EmptyBorder(0, 20, 0, 20));

        JLabel lbl = new JLabel("👔  Registrar Empleado");
        lbl.setFont(new Font("Segoe UI Emoji", Font.BOLD, 15));
        lbl.setForeground(Color.WHITE);

        JButton btnClose = new JButton("✕");
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnClose.setForeground(new Color(180, 200, 230));
        btnClose.setContentAreaFilled(false);
        btnClose.setBorderPainted(false);
        btnClose.setFocusPainted(false);
        btnClose.addActionListener(e -> dispose());

        header.add(lbl, BorderLayout.WEST);
        header.add(btnClose, BorderLayout.EAST);
        return header;
    }

    private JPanel buildBody() {
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
        gc.gridy = 5; gc.gridwidth = 2; gc.insets = new Insets(20, 4, 8, 4);
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);
        btnCancel = ButtonFactory.createNeutralButton("Cancelar");
        btnCancel.addActionListener(e -> dispose());
        btnRegister = ButtonFactory.createPrimaryButton("Registrar");
        btnRegister.addActionListener(e -> doRegister());
        btnPanel.add(btnCancel);
        btnPanel.add(btnRegister);
        form.add(btnPanel, gc);

        return form;
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
