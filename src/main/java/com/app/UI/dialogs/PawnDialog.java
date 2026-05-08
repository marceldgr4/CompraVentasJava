package com.app.UI.dialogs;

import com.app.Model.domain.Article;
import com.app.Model.domain.Cliente;
import com.app.Model.domain.Pawn;
import com.app.Service.ArticleService;
import com.app.Service.ClienteService;
import Infrastructure.security.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Diálogo para crear o editar un empeño.
 */
public class PawnDialog extends BaseDialog {

    private StyledCombo<Article> cmbArticle;
    private StyledCombo<Cliente> cmbCliente;
    private JSpinner spnAmount;
    private JSpinner spnPrice;
    private JSpinner spnReturnDays;
    private JSpinner spnInstallments;
    private JSpinner spnWeightGrams;
    private JLabel   lblWeight;
    private JPanel   weightRow;

    private boolean confirmed = false;
    private Pawn existingPawn;

    public PawnDialog(JFrame parent, Pawn pawn) {
        super(parent, pawn == null ? "Nuevo Registro de Empeño" : "Editar Registro de Empeño", "🤝");
        this.existingPawn = pawn;
        setSize(520, 550);
        setLocationRelativeTo(parent);
        
        setContentBody(buildBody());
        setFooter(buildFooter());
        
        if (pawn != null) fillFields(pawn);
    }

    private JScrollPane buildBody() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(20, 24, 12, 24));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        int row = 0;

        // Cliente
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 2; gc.insets = ins(0, 0, 4, 0);
        form.add(fieldLabel("Cliente *"), gc); row++;
        cmbCliente = styledCombo();
        loadClientes();
        gc.gridy = row; gc.insets = ins(0, 0, 12, 0);
        form.add(cmbCliente, gc); row++;

        // Artículo
        gc.gridy = row; gc.insets = ins(0, 0, 4, 0);
        form.add(fieldLabel("Artículo *"), gc); row++;
        cmbArticle = styledCombo();
        loadArticles();
        cmbArticle.addActionListener(e -> updateWeightVisibility());
        gc.gridy = row; gc.insets = ins(0, 0, 12, 0);
        form.add(cmbArticle, gc); row++;

        // Cantidad + Precio
        gc.gridwidth = 1;
        gc.gridx = 0; gc.gridy = row; gc.insets = ins(0, 0, 4, 6);
        form.add(fieldLabel("Cantidad *"), gc);
        gc.gridx = 1; gc.insets = ins(0, 6, 4, 0);
        form.add(fieldLabel("Precio unitario ($) *"), gc);
        row++;
        
        spnAmount = new JSpinner(new SpinnerNumberModel(1, 0, 999, 1));
        spnPrice  = new JSpinner(new SpinnerNumberModel(0.00, 0.0, 9_999_999.0, 1000.0));
        gc.gridx = 0; gc.gridy = row; gc.insets = ins(0, 0, 12, 6);
        form.add(spnAmount, gc);
        gc.gridx = 1; gc.insets = ins(0, 6, 12, 0);
        form.add(spnPrice, gc);
        row++;

        // Cuotas + Días
        gc.gridx = 0; gc.gridy = row; gc.insets = ins(0, 0, 4, 6);
        form.add(fieldLabel("Número de cuotas"), gc);
        gc.gridx = 1; gc.insets = ins(0, 6, 4, 0);
        form.add(fieldLabel("Días para devolver"), gc);
        row++;
        
        spnInstallments = new JSpinner(new SpinnerNumberModel(1, 0, 36, 1));
        spnReturnDays   = new JSpinner(new SpinnerNumberModel(30, 1, 365, 1));
        gc.gridx = 0; gc.gridy = row; gc.insets = ins(0, 0, 12, 6);
        form.add(spnInstallments, gc);
        gc.gridx = 1; gc.insets = ins(0, 6, 12, 0);
        form.add(spnReturnDays, gc);
        row++;

        // Peso en gramos
        lblWeight    = fieldLabel("Peso (gramos) *:");
        spnWeightGrams = new JSpinner(new SpinnerNumberModel(1.0, 0.1, 9999.0, 1.0));

        weightRow = new JPanel(new GridLayout(1, 2, 8, 0));
        weightRow.setOpaque(false);
        weightRow.add(lblWeight);
        weightRow.add(spnWeightGrams);
        weightRow.setVisible(false);

        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 2; gc.insets = ins(0, 0, 0, 0);
        form.add(weightRow, gc);

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Color.WHITE);
        return scroll;
    }

    private JPanel buildFooter() {
        JButton btnCancel = buildCancelButton();
        JButton btnSave   = buildPrimaryButton("Guardar");
        btnCancel.addActionListener(e -> onCancel());
        btnSave  .addActionListener(e -> doSave());
        return buildStandardFooter(btnCancel, btnSave);
    }

    private void loadClientes() {
        new SwingWorker<List<Cliente>, Void>() {
            @Override protected List<Cliente> doInBackground() throws Exception {
                return new ClienteService().getAll();
            }
            @Override protected void done() {
                try {
                    List<Cliente> list = get();
                    cmbCliente.removeAllItems();
                    list.forEach(cmbCliente::addItem);
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void loadArticles() {
        new SwingWorker<List<Article>, Void>() {
            @Override protected List<Article> doInBackground() throws Exception {
                return new ArticleService().getAvailableForSaleOrPawn();
            }
            @Override protected void done() {
                try {
                    List<Article> list = get();
                    cmbArticle.removeAllItems();
                    list.forEach(cmbArticle::addItem);
                    updateWeightVisibility();
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void updateWeightVisibility() {
        Article sel = (Article) cmbArticle.getSelectedItem();
        weightRow.setVisible(sel != null && sel.requireWeigthForPawn());
        revalidate();
        repaint();
    }

    private void fillFields(Pawn p) {
        spnAmount.setValue(p.getAmount());
        spnPrice.setValue(p.getPrice() != null ? p.getPrice().doubleValue() : 0.0);
        spnInstallments.setValue(Math.max(1, p.getInstallmentCount()));
        if (p.getPawnDate() != null && p.getReturnDate() != null) {
            long days = ChronoUnit.DAYS.between(LocalDate.now(), p.getReturnDate());
            spnReturnDays.setValue((int) Math.max(1, days));
        }
        if (p.getWeightGrams() != null) {
            spnWeightGrams.setValue(p.getWeightGrams().doubleValue());
        }
    }

    private void doSave() {
        if (cmbCliente.getSelectedItem() == null) {
            showValidationError("Selecciona un cliente."); return;
        }
        if (cmbArticle.getSelectedItem() == null) {
            showValidationError("Selecciona un artículo."); return;
        }
        int amount = (int) spnAmount.getValue();
        if (amount <= 0) { showValidationError("La cantidad debe ser mayor a 0."); return; }

        Article article = (Article) cmbArticle.getSelectedItem();
        if (article != null && article.requireWeigthForPawn()) {
            double weight = (double) spnWeightGrams.getValue();
            if (weight <= 0) { showValidationError("El peso debe ser mayor a 0."); return; }
        }
        confirmed = true;
        dispose();
    }

    public boolean isConfirmed() { return confirmed; }

    public Pawn getExistingPawn() {
        Article article  = (Article) cmbArticle.getSelectedItem();
        Cliente cliente  = (Cliente) cmbCliente.getSelectedItem();
        int     amount   = (int)    spnAmount.getValue();
        BigDecimal price = new BigDecimal(spnPrice.getValue().toString());
        int  installments= (int)    spnInstallments.getValue();
        int  days        = (int)    spnReturnDays.getValue();
        double weightVal = (double) spnWeightGrams.getValue();

        LocalDate pawnDate   = LocalDate.now();
        LocalDate returnDate = pawnDate.plusDays(days);

        BigDecimal weight = (article != null && article.requireWeigthForPawn() && weightVal > 0)
                ? BigDecimal.valueOf(weightVal) : null;

        if (existingPawn != null) {
            existingPawn.setArticleId(article != null ? article.getId() : 0);
            existingPawn.setClientId(cliente != null ? cliente.getId() : 0);
            existingPawn.setAmount(amount);
            existingPawn.setPrice(price);
            existingPawn.setReturnDate(returnDate);
            existingPawn.setWeightGrams(weight);
            return existingPawn;
        }
        return new Pawn(
                SessionManager.getEmployeeId(),
                article != null ? article.getId() : 0,
                cliente != null ? cliente.getId() : 0,

                amount, price, weight, installments,
                pawnDate, returnDate, null
        );
    }
}
