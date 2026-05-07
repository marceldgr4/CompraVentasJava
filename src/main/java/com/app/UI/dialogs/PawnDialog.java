package com.app.UI.dialogs;

import Infrastructure.security.SessionManager;
import com.app.Model.domain.Article;
import com.app.Model.domain.Cliente;
import com.app.Model.domain.Pawn;
import com.app.Service.ArticleService;
import com.app.Service.ClienteService;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/*
 * Diálogo para crear o editar un empeño.
 *  añadido (cuotas pactadas
 *  visible solo cuando el artículo es Joyería
  */

public class PawnDialog extends JDialog {

    private JComboBox<Article> cmbArticle;
    private JComboBox<Cliente> cmbCliente;
    private JSpinner spnAmount;
    private JSpinner spnPrice;
    private JSpinner spnReturnDays;

    private JSpinner spnInstallments;
    private JSpinner spnWeightGrams;
    private JLabel lblWeight;
    private JPanel weingthRow;

    private JButton btnSave;
    private JButton btnCancel;

    private boolean confirmed = false;
    private Pawn existingPawn;

    public PawnDialog(JFrame parent, Pawn pawn) {
        super(parent, pawn == null ? "Nuevo Registro de Empeño" : "Editar Registro de Empeño", true);
        this.existingPawn = pawn;
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
        int row = 0;

        // ---- Cliente ----
        gc.gridx = 0;   gc.gridy = row;    gc.weightx = 0;
        form.add(new JLabel("Cliente:"), gc);
        gc.gridx = 1;   gc.weightx = 1;
        cmbCliente = new JComboBox<>();
        cmbCliente.setFont(fieldFont);
        loadClientes();
        form.add(cmbCliente, gc);
        row++;

        // ---- Article ----
        gc.gridx = 0;   gc.gridy = row;    gc.weightx = 0;
        form.add(new JLabel("Artículo:"), gc);
        gc.gridx = 1; gc.weightx = 1;
        cmbArticle = new JComboBox<>();
        cmbArticle.setFont(fieldFont);
        loadArticles();
        cmbArticle.addActionListener(e-> updateWeightVisibility());
        form.add(cmbArticle, gc);
        row++;

        // ---- Amount ----
        gc.gridx = 0;  gc.gridy = row;   gc.weightx = 0;
        form.add(new JLabel("Cantidad:"), gc);
        gc.gridx = 1;        gc.weightx = 1;
        spnAmount = new JSpinner(new SpinnerNumberModel(1, 0, 999, 1));
        form.add(spnAmount, gc);
        row++;

        // ---- Price ----
        gc.gridx = 0;  gc.gridy = row;    gc.weightx = 0;
        form.add(new JLabel("Precio Unitario($) : "), gc);
        gc.gridx = 1;     gc.weightx = 1;
        spnPrice = new JSpinner(new SpinnerNumberModel(0.00, 0, 999999, 1000));
        form.add(spnPrice, gc);
        row++;

        //----Coutas pactadas-----------
        gc.gridx = 0;  gc.gridy = row;    gc.weightx = 0;
        form.add(new JLabel("Nuemor de cuotas:"), gc);
        gc.gridx = 1;     gc.weightx = 1;
        spnInstallments = new JSpinner(new SpinnerNumberModel(1, 0, 36, 1));
        form.add(spnInstallments, gc);
        row++;

        // ---- Return Days ----
        gc.gridx = 0;  gc.gridy = row;        gc.weightx = 0;
        form.add(new JLabel("Días para devolver:"), gc);
        gc.gridx = 1;        gc.weightx = 1;
        spnReturnDays = new JSpinner(new SpinnerNumberModel(30, 1, 365, 1));
        form.add(spnReturnDays, gc);
        row++;


        //----- Peso en gramos (solo Joyeria)----
        lblWeight = new JLabel("Peso (gramos) *:");
        spnWeightGrams = new  JSpinner(new SpinnerNumberModel(1, 1, 9999.0, 1));

        weingthRow = new JPanel(new GridLayout(1,2,8,0));
        weingthRow.setOpaque(false);
        weingthRow.add(lblWeight);
        weingthRow.add(spnWeightGrams);
        weingthRow.setVisible(false);

        gc.gridx = 0;  gc.gridy = row;    gc.weightx = 0;
        form.add(weingthRow, gc);
        gc.gridwidth = 1;
        row++;

        // ---- Buttons ----
        gc.gridy = row;   gc.gridwidth = 2;
        //gc.weightx = 1;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnCancel = com.app.UI.Components.ButtonFactory.createNeutralButton("Cancelar");
        btnCancel.addActionListener(e -> dispose());

        btnSave = com.app.UI.Components.ButtonFactory.createPrimaryButton("Guardar");
        btnSave.addActionListener(e -> doSave());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        form.add(btnPanel, gc);

        setContentPane(form);
        updateWeightVisibility();
    }
    private void updateWeightVisibility() {
        Article selected =  (Article) cmbArticle.getSelectedItem();
        boolean needsWeight = selected != null && selected.requireWeigthForPawn();
        weingthRow.setVisible(needsWeight);
        pack();
    }

    private void loadClientes() {
        try {

            List<Cliente> list = new  ClienteService().getAll();
            cmbCliente.removeAllItems();
            list.forEach(cmbCliente::addItem);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al cargar clientes: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadArticles() {
        try {

            List<Article> list =new ArticleService().getAvailableForSaleOrPawn();
            cmbArticle.removeAllItems();
            list.forEach(cmbArticle::addItem);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al cargar artículos: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void fillFields(Pawn p) {
        spnAmount.setValue(p.getAmount());
        spnPrice.setValue(p.getPrice() != null ? p.getPrice().doubleValue() : 0.00);
        spnInstallments.setValue(Math.max(1, p.getInstallmentCount()));
        if(p.getPawnDate() !=null && p.getReturnDate() != null){
            long days = ChronoUnit.DAYS.between(LocalDate.now(), p.getReturnDate());
            spnReturnDays.setValue((int) Math.max(1,days));
        }
        if (p.getWeightGrams() != null) {
            spnWeightGrams.setValue(p.getWeightGrams().doubleValue());
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
        Article article = (Article) cmbArticle.getSelectedItem();
        if (article != null && article.requireWeigthForPawn()) {
            double weight = (double) spnWeightGrams.getValue();
        if(weight <= 0){
            JOptionPane.showMessageDialog(this, "La cantidad debe ser mayor a 0");
        return;}
        }

        confirmed = true;
        dispose();
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public Pawn getExistingPawn() {
        Article article = (Article) cmbArticle.getSelectedItem();
        Cliente cliente = (Cliente) cmbCliente.getSelectedItem();
        int amount = (int) spnAmount.getValue();
        BigDecimal price = new BigDecimal(spnPrice.getValue().toString());
        int installments = (int) spnInstallments.getValue();
        int days = (int) spnReturnDays.getValue();
        double weightVal = (double) spnWeightGrams.getValue();

        LocalDate pawnDate = LocalDate.now();
        LocalDate returnDate = pawnDate.plusDays(days);

        BigDecimal weight = (article != null && article.requireWeigthForPawn() && weightVal > 0)
                ? BigDecimal.valueOf(weightVal): null;
        String profileId = Infrastructure.security.SessionManager.getProfileId();

        if (existingPawn != null) {
            // Edit existing
            existingPawn.setArticleId(article !=null ?  article.getId():0);
            existingPawn.setClientId(cliente != null ? cliente.getId() : 0);
            existingPawn.setAmount(amount);
            existingPawn.setPrice(price);
            existingPawn.setReturnDate(returnDate);
            existingPawn.setWeightGrams(weight);
            return existingPawn;
        }
        return  new Pawn(
                SessionManager.getProfileId(),
                article != null ? article.getId(): 0,
                cliente != null ? cliente.getId(): 0,
                amount, price,weight,installments,
                pawnDate,returnDate, null

        );
    }
}
