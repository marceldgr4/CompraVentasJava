package com.app.View;



import com.app.Model.Article;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

public class ArticleDialog extends JDialog {

    private JTextField  txtName;
    private JTextField  txtDescription;
    private JTextField  txtAmount;
    private JTextField  txtPrice;
    private JCheckBox   chkSold;
    private JButton     btnSave;
    private JButton     btnCancel;

    private boolean confirmed = false;

    public ArticleDialog(JFrame parent, Article article) {
        super(parent, article == null ? "Nuevo artículo" : "Editar artículo", true);
        initComponents();
        if (article != null) fillFields(article);
        setSize(360, 300);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initComponents() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 12, 24));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 4, 6, 4);
        gc.fill = GridBagConstraints.HORIZONTAL;

        Font labelFont = new Font("Segoe UI", Font.BOLD, 13);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 13);

        // Nombre
        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0;
        form.add(new JLabel("Nombre:"), gc);
        gc.gridx = 1; gc.weightx = 1;
        txtName = new JTextField(); txtName.setFont(fieldFont);
        form.add(txtName, gc);

        // Cantidad
        gc.gridx = 0; gc.gridy = 1; gc.weightx = 0;
        form.add(new JLabel("Cantidad:"), gc);
        gc.gridx = 1; gc.weightx = 1;
        txtAmount = new JTextField("0"); txtAmount.setFont(fieldFont);
        form.add(txtAmount, gc);

        // Precio
        gc.gridx = 0; gc.gridy = 2; gc.weightx = 0;
        form.add(new JLabel("Precio:"), gc);
        gc.gridx = 1; gc.weightx = 1;
        txtPrice = new JTextField(); txtPrice.setFont(fieldFont);
        form.add(txtPrice, gc);

        // Vendible
        gc.gridx = 0; gc.gridy = 3; gc.gridwidth = 2;
        chkSold = new JCheckBox("¿Es vendible?");
        chkSold.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        form.add(chkSold, gc);

        // Botones
        gc.gridy = 4; gc.gridwidth = 2;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnCancel = new JButton("Cancelar");
        btnSave   = new JButton("Guardar");
        btnSave.setBackground(new Color(30, 136, 229));
        btnSave.setForeground(Color.WHITE);
        btnSave.setBorderPainted(false);
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e   -> doSave());
        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        form.add(btnPanel, gc);

        setContentPane(form);
    }

    private void fillFields(Article a) {
        txtName.setText(a.getNameArticle());
       // txtDescription.setText(a.getDescription());
        txtAmount.setText(String.valueOf(a.getAmount()));
        txtPrice.setText(a.getPrice().toPlainString());
        chkSold.setSelected(a.isSold());
    }

    private void doSave() {
        if (txtName.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "El nombre es obligatorio.");
            return;
        }
        try {
            Integer.parseInt(txtAmount.getText().trim());
            new BigDecimal(txtPrice.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Cantidad y precio deben ser numéricos.");
            return;
        }
        confirmed = true;
        dispose();
    }

    public boolean isConfirmed() { return confirmed; }

    public Article getArticle() {
        return new Article(
                txtName.getText().trim(),
                txtDescription.getText().trim(),
                Integer.parseInt(txtAmount.getText().trim()),
                new BigDecimal(txtPrice.getText().trim()),
                chkSold.isSelected()
        );
    }
}
