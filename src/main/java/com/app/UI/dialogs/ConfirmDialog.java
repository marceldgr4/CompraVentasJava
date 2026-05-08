package com.app.UI.dialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Diálogo de confirmación genérico y estilizado.
 * Reemplaza los JOptionPane de confirmación tradicionales.
 */
public class ConfirmDialog extends BaseDialog {

    private boolean confirmed = false;

    public ConfirmDialog(Window parent, String title, String message, String icon) {
        super(parent, title, icon);
        setSize(400, 220);
        setLocationRelativeTo(parent);

        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel lblMsg = new JLabel("<html><div style='text-align: center;'>" + message + "</div></html>");
        lblMsg.setFont(FONT_FIELD);
        lblMsg.setForeground(TEXT_DARK);
        lblMsg.setHorizontalAlignment(SwingConstants.CENTER);
        body.add(lblMsg, BorderLayout.CENTER);

        setContentBody(body);

        JButton btnCancel = buildCancelButton();
        JButton btnConfirm = buildPrimaryButton("Confirmar");
        
        btnCancel.addActionListener(e -> onCancel());
        btnConfirm.addActionListener(e -> {
            confirmed = true;
            dispose();
        });

        setFooter(buildStandardFooter(btnCancel, btnConfirm));
    }

    public static boolean show(Window parent, String title, String message, String icon) {
        ConfirmDialog cd = new ConfirmDialog(parent, title, message, icon);
        cd.setVisible(true);
        return cd.isConfirmed();
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}
