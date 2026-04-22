package com.app.UI.dialogs;

import com.app.Model.domain.Article;
import com.app.Model.domain.Cliente;
import com.app.Service.ClienteService;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;



public class ArticleDialog extends JDialog {

    private JTextField txtName;
    private JTextField txtDescription;
    private JTextField txtAmount;
    private JTextField txtPrice;
    private JCheckBox chkSold;
    private JComboBox<Cliente> cmbCliente;
    private JButton btnSave;
    private JButton btnCancel;

    private boolean confirmed = false;

    public ArticleDialog(JFrame parent, Article article) {
        super(parent, article == null ? "New article" : "Edit article", true);
        initComponents();
        if (article != null) fillFields(article);
        setSize(400, 350);
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

        // ---- Name ----
        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = 0;
        form.add(new JLabel("Name:"), gc);
        gc.gridx = 1;
        gc.weightx = 1;
        txtName = new JTextField();
        txtName.setFont(fieldFont);
        form.add(txtName, gc);

        // ---- Description ----
        gc.gridx = 0;
        gc.gridy = 1;
        gc.weightx = 0;
        form.add(new JLabel("Description:"), gc);
        gc.gridx = 1;
        gc.weightx = 1;
        txtDescription = new JTextField();  // ✅ INICIALIZADO
        txtDescription.setFont(fieldFont);
        form.add(txtDescription, gc);

        // ---- Amount ----
        gc.gridx = 0;
        gc.gridy = 2;
        gc.weightx = 0;
        form.add(new JLabel("Amount:"), gc);
        gc.gridx = 1;
        gc.weightx = 1;
        txtAmount = new JTextField("0");
        txtAmount.setFont(fieldFont);
        form.add(txtAmount, gc);

        // ---- Price ----
        gc.gridx = 0;
        gc.gridy = 3;
        gc.weightx = 0;
        form.add(new JLabel("Price:"), gc);
        gc.gridx = 1;
        gc.weightx = 1;
        txtPrice = new JTextField();
        txtPrice.setFont(fieldFont);
        form.add(txtPrice, gc);

        // ---- Sold Checkbox ----
        gc.gridx = 0;
        gc.gridy = 4;
        gc.gridwidth = 2;
        chkSold = new JCheckBox("Is sellable?");
        chkSold.setFont(fieldFont);
        form.add(chkSold, gc);

        // ---- Buttons ----
        gc.gridy = 5;
        gc.gridwidth = 2;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));

        btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(e -> dispose());

        btnSave = new JButton("Save");
        btnSave.setBackground(new Color(30, 136, 229));
        btnSave.setForeground(Color.WHITE);
        btnSave.setBorderPainted(false);
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSave.addActionListener(e -> doSave());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        form.add(btnPanel, gc);

        setContentPane(form);
    }

    private void loadClientes(){
        try{
            ClienteService clienteService = new ClienteService();
            List<Cliente> list = clienteService.getAll();
            cmbCliente.removeAllItems();
            for(Cliente c : list){
                cmbCliente.addItem(c);
            }

        }catch (Exception ex){
            JOptionPane.showMessageDialog(this,
                    "No se pudo cargar los clientes:\n" + ex.getMessage(),
                    "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }
    private void fillFields(Article a) {
        if (a != null) {
            txtName.setText(a.getNameArticle());
            txtDescription.setText(a.getDescription() != null ? a.getDescription() : "");
            txtAmount.setText(String.valueOf(a.getAmount()));
            txtPrice.setText(a.getPrice().toPlainString());
            chkSold.setSelected(a.isSold());
        }
    }

    private void doSave() {
        // Validations
        if (txtName.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Name is required.");
            return;
        }

        try {
            Integer.parseInt(txtAmount.getText().trim());
            new BigDecimal(txtPrice.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Amount and price must be numeric.");
            return;
        }

        confirmed = true;
        dispose();
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public Article getArticle() {
        Cliente cliente = (Cliente) cmbCliente.getSelectedItem();
        int clienteId = (cliente != null) ? cliente.getId() : 0;
        Article article = new Article(
                txtName.getText().trim(),
                txtDescription.getText().trim(),
                Integer.parseInt(txtAmount.getText().trim()),
                new BigDecimal(txtPrice.getText().trim()),
                chkSold.isSelected()
        );
        article.setClienteId(clienteId);
        return article;
    }
}