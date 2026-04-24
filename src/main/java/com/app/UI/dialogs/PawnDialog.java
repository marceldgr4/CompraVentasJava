package com.app.UI.dialogs;

import com.app.Model.domain.Article;
import com.app.Model.domain.Cliente;
import com.app.Model.domain.Pawn;
import com.app.Service.ArticleService;
import com.app.Service.ClienteService;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
public class PawnDialog extends JDialog {

    private JComboBox<Article> cmbArticle;
    private JComboBox<Cliente> cmbCliente;
    private JSpinner spnAmount;
    private JSpinner spnPrice;
    private JSpinner spnReturnDays;
    private JButton btnSave;
    private JButton btnCancel;

    private boolean confirmed = false;
    private Pawn pawn;

    public PawnDialog(JFrame parent, Pawn pawn) {
        super(parent, pawn == null ? "Nuevo Registro de Empeño" : "Editar Registro de Empeño", true);
        this.pawn = pawn;
        initComponents();
        if (pawn != null) fillFields(pawn);
        setSize(500, 350);
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

        Font labelFont = new Font("Segoe UI", Font.BOLD, 13);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 13);

        // ---- Cliente ----
        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = 0;
        form.add(new JLabel("Cliente:"), gc);
        gc.gridx = 1;
        gc.weightx = 1;
        cmbCliente = new JComboBox<>();
        cmbCliente.setFont(fieldFont);
        loadClientes();
        form.add(cmbCliente, gc);

        // ---- Article ----
        gc.gridx = 0;
        gc.gridy = 1;
        gc.weightx = 0;
        form.add(new JLabel("Artículo:"), gc);
        gc.gridx = 1;
        gc.weightx = 1;
        cmbArticle = new JComboBox<>();
        cmbArticle.setFont(fieldFont);
        loadArticles();
        form.add(cmbArticle, gc);

        // ---- Amount ----
        gc.gridx = 0;
        gc.gridy = 2;
        gc.weightx = 0;
        form.add(new JLabel("Cantidad:"), gc);
        gc.gridx = 1;
        gc.weightx = 1;
        spnAmount = new JSpinner(new SpinnerNumberModel(1, 0, 999, 1));
        form.add(spnAmount, gc);

        // ---- Price ----
        gc.gridx = 0;
        gc.gridy = 3;
        gc.weightx = 0;
        form.add(new JLabel("Precio Unitario:"), gc);
        gc.gridx = 1;
        gc.weightx = 1;
        spnPrice = new JSpinner(new SpinnerNumberModel(0.00, 0, 999999, 1000));
        form.add(spnPrice, gc);

        // ---- Return Days ----
        gc.gridx = 0;
        gc.gridy = 4;
        gc.weightx = 0;
        form.add(new JLabel("Días para devolver:"), gc);
        gc.gridx = 1;
        gc.weightx = 1;
        spnReturnDays = new JSpinner(new SpinnerNumberModel(30, 1, 365, 1));
        form.add(spnReturnDays, gc);

        // ---- Buttons ----
        gc.gridy = 5;
        gc.gridwidth = 2;
        gc.weightx = 1;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));

        btnCancel = new JButton("Cancelar");
        btnCancel.setFont(fieldFont);
        btnCancel.addActionListener(e -> dispose());

        btnSave = new JButton("Guardar");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 13));
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

    private void loadClientes() {
        try {
            ClienteService service = new ClienteService();
            List<Cliente> clientes = service.getAll();
            cmbCliente.removeAllItems();
            for (Cliente c : clientes) {
                cmbCliente.addItem(c);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al cargar clientes: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadArticles() {
        try {
            ArticleService service = new ArticleService();
            List<Article> articles = service.getAvailableForPawn();
            cmbArticle.removeAllItems();
            for (Article a : articles) {
                cmbArticle.addItem(a);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al cargar artículos: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void fillFields(Pawn p) {
        if (p != null) {
            // Load cliente
            try {
                ClienteService service = new ClienteService();
                Cliente c = service.findById(p.getCliente_id());
                cmbCliente.setSelectedItem(c);
            } catch (Exception ignored) {}

            spnAmount.setValue(p.getAmount());
            spnPrice.setValue(p.getPrice().doubleValue());

            // Calculate return days
            if (p.getPawn_date() != null && p.getReturn_date() != null) {
                long days = java.time.temporal.ChronoUnit.DAYS.between(
                        p.getPawn_date(),
                        p.getReturn_date()
                );
                spnReturnDays.setValue((int) days);
            }
        }
    }

    private void doSave() {
        if (cmbCliente.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Selecciona un cliente");
            return;
        }
        if (cmbArticle.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Selecciona un artículo");
            return;
        }

        int amount = (int) spnAmount.getValue();
        if (amount <= 0) {
            JOptionPane.showMessageDialog(this, "La cantidad debe ser mayor a 0");
            return;
        }

        confirmed = true;
        dispose();
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public Pawn getPawn() {
        Article article = (Article) cmbArticle.getSelectedItem();
        Cliente cliente = (Cliente) cmbCliente.getSelectedItem();
        int amount = (int) spnAmount.getValue();
        BigDecimal price = new BigDecimal(spnPrice.getValue().toString());
        int days = (int) spnReturnDays.getValue();

        LocalDate pawnDate = LocalDate.now();
        LocalDate returnDate = pawnDate.plusDays(days);

        if (pawn != null) {
            // Edit existing
            pawn.setArticle_id(article.getId());
            pawn.setCliente_id(cliente.getId());
            pawn.setAmount(amount);
            pawn.setPrice(price);
            pawn.setReturn_date(returnDate);
            return pawn;
        } else {
            // Create new
            return new Pawn(
                    Infrastructure.security.SessionManager.isAdmin(),
                    article.getId(),
                    cliente.getId(),
                    amount,
                    price,
                    pawnDate,
                    returnDate,
                    false,
                    false
            );
        }
    }
}
