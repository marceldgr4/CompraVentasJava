package com.app.UI.dialogs;

import com.app.Model.Enum.ArticleCategory;
import com.app.Model.Enum.ItemState;
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
 * Diálogo para crear o editar un empeño con soporte de Empeño Ágil (HU-26).
 */
public class PawnDialog extends BaseDialog {

    private JRadioButton rbClienteExistente;
    private JRadioButton rbClienteNuevo;
    private StyledCombo<Cliente> cmbCliente;
    private JPanel pnlClienteRapido;
    private StyledField txtNombreRapido;
    private StyledField txtTelefonoRapido;

    private JRadioButton rbArticuloExistente;
    private JRadioButton rbArticuloNuevo;
    private StyledCombo<Article> cmbArticle;
    private JPanel pnlArticuloNuevo;
    private StyledField txtNombreArticulo;
    private StyledCombo<ArticleCategory> cmbCategoria;
    private StyledCombo<ItemState> cmbEstado;
    private JTextArea txtDescription;

    private JSpinner spnAmount;
    private JSpinner spnPrice;
    private JSpinner spnReturnDays;
    private JSpinner spnInstallments;
    private JSpinner spnWeightGrams;
    private JLabel   lblWeight;
    private JPanel   weightRow;

    private boolean confirmed = false;
    private boolean agile = false;
    private Pawn existingPawn;

    public PawnDialog(JFrame parent, Pawn pawn) {
        super(parent, pawn == null ? "Nuevo Registro de Empeño (Ágil)" : "Editar Registro de Empeño", "🤝");
        this.existingPawn = pawn;
        setSize(650, 720);
        setLocationRelativeTo(parent);
        
        setContentBody(buildBody());
        setFooter(buildFooter());
        
        if (pawn != null) {
            rbClienteNuevo.setEnabled(false);
            rbArticuloNuevo.setEnabled(false);
            fillFields(pawn);
        }
    }

    private JScrollPane buildBody() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(16, 24, 12, 24));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        int row = 0;

        // ── Sección Cliente ───────────────────────────────────────────────────
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 2; gc.insets = ins(0, 0, 4, 0);
        form.add(sectionLabel("👤  Cliente Vendedor"), gc); row++;

        rbClienteExistente = new JRadioButton("Cliente registrado");
        rbClienteNuevo     = new JRadioButton("Cliente nuevo rápido");
        rbClienteExistente.setSelected(true);
        rbClienteExistente.setOpaque(false); rbClienteNuevo.setOpaque(false);
        rbClienteExistente.setFont(FONT_FIELD); rbClienteNuevo.setFont(FONT_FIELD);

        ButtonGroup grpCli = new ButtonGroup();
        grpCli.add(rbClienteExistente); grpCli.add(rbClienteNuevo);

        JPanel pnlRadioCli = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlRadioCli.setOpaque(false);
        pnlRadioCli.add(rbClienteExistente); pnlRadioCli.add(rbClienteNuevo);
        gc.gridy = row; gc.insets = ins(0, 0, 6, 0); form.add(pnlRadioCli, gc); row++;

        cmbCliente = styledCombo();
        loadClientes();
        gc.gridy = row; gc.insets = ins(0, 0, 8, 0); form.add(cmbCliente, gc); row++;

        pnlClienteRapido = new JPanel(new GridLayout(1, 2, 8, 0));
        pnlClienteRapido.setOpaque(false);
        txtNombreRapido   = styledField("Nombre completo *");
        txtTelefonoRapido = styledField("Teléfono (opcional)");
        pnlClienteRapido.add(txtNombreRapido); pnlClienteRapido.add(txtTelefonoRapido);
        pnlClienteRapido.setVisible(false);
        gc.gridy = row; gc.insets = ins(0, 0, 12, 0); form.add(pnlClienteRapido, gc); row++;

        rbClienteExistente.addActionListener(e -> { cmbCliente.setVisible(true); pnlClienteRapido.setVisible(false); });
        rbClienteNuevo.addActionListener(e -> { cmbCliente.setVisible(false); pnlClienteRapido.setVisible(true); });

        // ── Sección Artículo ──────────────────────────────────────────────────
        gc.gridy = row; gc.insets = ins(4, 0, 4, 0);
        form.add(sectionLabel("📦  Datos del Artículo"), gc); row++;

        rbArticuloExistente = new JRadioButton("Artículo en inventario");
        rbArticuloNuevo     = new JRadioButton("Artículo nuevo");
        rbArticuloExistente.setSelected(true);
        rbArticuloExistente.setOpaque(false); rbArticuloNuevo.setOpaque(false);
        rbArticuloExistente.setFont(FONT_FIELD); rbArticuloNuevo.setFont(FONT_FIELD);

        ButtonGroup grpArt = new ButtonGroup();
        grpArt.add(rbArticuloExistente); grpArt.add(rbArticuloNuevo);

        JPanel pnlRadioArt = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlRadioArt.setOpaque(false);
        pnlRadioArt.add(rbArticuloExistente); pnlRadioArt.add(rbArticuloNuevo);
        gc.gridy = row; gc.insets = ins(0, 0, 6, 0); form.add(pnlRadioArt, gc); row++;

        cmbArticle = styledCombo();
        loadArticles();
        cmbArticle.addActionListener(e -> updateWeightVisibility());
        gc.gridy = row; gc.insets = ins(0, 0, 8, 0); form.add(cmbArticle, gc); row++;

        pnlArticuloNuevo = new JPanel(new GridBagLayout());
        pnlArticuloNuevo.setOpaque(false);
        GridBagConstraints gca = new GridBagConstraints();
        gca.fill = GridBagConstraints.HORIZONTAL; gca.weightx = 1;

        gca.gridx = 0; gca.gridy = 0; gca.gridwidth = 2; gca.insets = ins(0, 0, 6, 0);
        txtNombreArticulo = styledField("Nombre del producto *");
        pnlArticuloNuevo.add(txtNombreArticulo, gca);

        gca.gridwidth = 1; gca.gridy = 1; gca.insets = ins(0, 0, 6, 6);
        cmbCategoria = styledCombo();
        for (ArticleCategory cat : ArticleCategory.values()) cmbCategoria.addItem(cat);
        cmbCategoria.addActionListener(e -> updateWeightVisibility());
        pnlArticuloNuevo.add(cmbCategoria, gca);

        gca.gridx = 1; gca.insets = ins(0, 6, 6, 0);
        cmbEstado = styledCombo();
        for (ItemState st : ItemState.values()) cmbEstado.addItem(st);
        pnlArticuloNuevo.add(cmbEstado, gca);

        gca.gridx = 0; gca.gridy = 2; gca.gridwidth = 2; gca.insets = ins(0, 0, 0, 0);
        txtDescription = styledTextArea(2);
        pnlArticuloNuevo.add(new JScrollPane(txtDescription), gca);

        pnlArticuloNuevo.setVisible(false);
        gc.gridy = row; gc.insets = ins(0, 0, 12, 0); form.add(pnlArticuloNuevo, gc); row++;

        rbArticuloExistente.addActionListener(e -> { cmbArticle.setVisible(true); pnlArticuloNuevo.setVisible(false); updateWeightVisibility(); });
        rbArticuloNuevo.addActionListener(e -> { cmbArticle.setVisible(false); pnlArticuloNuevo.setVisible(true); updateWeightVisibility(); });

        // ── Cantidad + Precio ─────────────────────────────────────────────────
        gc.gridwidth = 1;
        gc.gridx = 0; gc.gridy = row; gc.insets = ins(0, 0, 4, 6); form.add(fieldLabel("Cantidad *"), gc);
        gc.gridx = 1; gc.insets = ins(0, 6, 4, 0); form.add(fieldLabel("Precio de préstamo ($) *"), gc);
        row++;
        
        spnAmount = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        spnPrice  = new JSpinner(new SpinnerNumberModel(0.00, 0.0, 9_999_999.0, 1000.0));
        gc.gridx = 0; gc.gridy = row; gc.insets = ins(0, 0, 12, 6); form.add(spnAmount, gc);
        gc.gridx = 1; gc.insets = ins(0, 6, 12, 0); form.add(spnPrice, gc);
        row++;

        // ── Cuotas + Días ─────────────────────────────────────────────────────
        gc.gridx = 0; gc.gridy = row; gc.insets = ins(0, 0, 4, 6); form.add(fieldLabel("Número de cuotas"), gc);
        gc.gridx = 1; gc.insets = ins(0, 6, 4, 0); form.add(fieldLabel("Días para devolver"), gc);
        row++;
        
        spnInstallments = new JSpinner(new SpinnerNumberModel(1, 1, 36, 1));
        spnReturnDays   = new JSpinner(new SpinnerNumberModel(30, 1, 365, 1));
        gc.gridx = 0; gc.gridy = row; gc.insets = ins(0, 0, 12, 6); form.add(spnInstallments, gc);
        gc.gridx = 1; gc.insets = ins(0, 6, 12, 0); form.add(spnReturnDays, gc);
        row++;

        // ── Peso en gramos ────────────────────────────────────────────────────
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
        JButton btnSave   = buildPrimaryButton("Guardar Empeño");
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
        boolean req = false;
        if (rbArticuloExistente.isSelected()) {
            Article sel = (Article) cmbArticle.getSelectedItem();
            if (sel != null && sel.requireWeigthForPawn()) req = true;
        } else {
            ArticleCategory cat = (ArticleCategory) cmbCategoria.getSelectedItem();
            if (cat == ArticleCategory.Joyeria) req = true;
        }
        weightRow.setVisible(req);
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
        if (rbClienteExistente.isSelected() && cmbCliente.getSelectedItem() == null) {
            showValidationError("Selecciona un cliente registrado."); return;
        }
        if (rbClienteNuevo.isSelected() && txtNombreRapido.getText().isBlank()) {
            showValidationError("El nombre del cliente es obligatorio."); return;
        }

        if (rbArticuloExistente.isSelected() && cmbArticle.getSelectedItem() == null) {
            showValidationError("Selecciona un artículo en inventario."); return;
        }
        if (rbArticuloNuevo.isSelected() && txtNombreArticulo.getText().isBlank()) {
            showValidationError("El nombre del artículo es obligatorio."); return;
        }

        int amount = (int) spnAmount.getValue();
        if (amount <= 0) { showValidationError("La cantidad debe ser mayor a 0."); return; }

        double price = (double) spnPrice.getValue();
        if (price <= 0) { showValidationError("El precio de préstamo debe ser mayor a $0."); return; }

        boolean reqWeight = false;
        if (rbArticuloExistente.isSelected()) {
            Article sel = (Article) cmbArticle.getSelectedItem();
            if (sel != null && sel.requireWeigthForPawn()) reqWeight = true;
        } else {
            if (cmbCategoria.getSelectedItem() == ArticleCategory.Joyeria) reqWeight = true;
        }

        if (reqWeight) {
            double weight = (double) spnWeightGrams.getValue();
            if (weight <= 0) { showValidationError("El peso debe ser mayor a 0."); return; }
        }

        if (rbClienteNuevo.isSelected() || rbArticuloNuevo.isSelected()) {
            agile = true;
        }

        confirmed = true;
        dispose();
    }

    public boolean isConfirmed() { return confirmed; }
    public boolean isAgile()     { return agile; }

    public Cliente getNewCliente() {
        if (!rbClienteNuevo.isSelected()) return null;
        return Cliente.createRapido(null, txtNombreRapido.getText().trim(), null, txtTelefonoRapido.getText().trim());
    }

    public Article getNewArticle() {
        if (!rbArticuloNuevo.isSelected()) return null;
        double price = (double) spnPrice.getValue();
        return new Article(
                0,
                txtNombreArticulo.getText().trim(),
                txtDescription.getText().trim(),
                (int) spnAmount.getValue(),
                BigDecimal.valueOf(price), // sugerido igual al préstamo
                (ArticleCategory) cmbCategoria.getSelectedItem(),
                com.app.Model.Enum.SourceType.EMPENO,
                (ItemState) cmbEstado.getSelectedItem(),
                BigDecimal.valueOf(price)  // costo igual al préstamo
        );
    }

    public Pawn getExistingPawn() {
        Article article  = rbArticuloExistente.isSelected() ? (Article) cmbArticle.getSelectedItem() : null;
        Cliente cliente  = rbClienteExistente.isSelected()  ? (Cliente) cmbCliente.getSelectedItem() : null;
        int     amount   = (int)    spnAmount.getValue();
        BigDecimal price = new BigDecimal(spnPrice.getValue().toString());
        int  installments= (int)    spnInstallments.getValue();
        int  days        = (int)    spnReturnDays.getValue();
        double weightVal = (double) spnWeightGrams.getValue();

        LocalDate pawnDate   = LocalDate.now();
        LocalDate returnDate = pawnDate.plusDays(days);

        boolean reqWeight = (article != null && article.requireWeigthForPawn()) || 
                            (rbArticuloNuevo.isSelected() && cmbCategoria.getSelectedItem() == ArticleCategory.Joyeria);

        BigDecimal weight = (reqWeight && weightVal > 0) ? BigDecimal.valueOf(weightVal) : null;

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
