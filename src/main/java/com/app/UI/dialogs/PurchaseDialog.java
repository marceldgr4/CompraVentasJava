package com.app.UI.dialogs;

import com.app.Model.Enum.ArticleCategory;
import com.app.Model.Enum.ItemState;
import com.app.Model.domain.Article;
import com.app.Model.domain.Cliente;
import com.app.Model.domain.Purchase;
import com.app.Model.domain.Employee;
import com.app.Service.ClienteService;
import com.app.Service.EmployeeService;
import com.app.Service.PurchaseService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Diálogo para registrar la compra de un producto usado al negocio (HU-28).
 */
public class PurchaseDialog extends BaseDialog {

    private JRadioButton rbClienteExistente;
    private JRadioButton rbClienteNuevo;
    private JRadioButton rbEmpleado;
    private JRadioButton rbSinCliente;
    private StyledCombo<Cliente> cmbCliente;
    private StyledCombo<Employee> cmbEmpleado;
    private JPanel pnlClienteRapido;
    private StyledField txtNombreRapido;
    private StyledField txtTelefonoRapido;

    private StyledField txtNombreArticulo;
    private StyledCombo<ArticleCategory> cmbCategoria;
    private StyledCombo<ItemState> cmbEstado;
    private JTextArea txtDescription;

    private JSpinner spnPrecioCompra;
    private JSpinner spnPrecioVenta;
    private JLabel lblMargenWarning;

    private boolean confirmed = false;
    private Purchase result;

    private final PurchaseService purchaseService = new PurchaseService();
    private final ClienteService clienteService = new ClienteService();
    private final EmployeeService employeeService = new EmployeeService();

    public PurchaseDialog(JFrame parent) {
        super(parent, "Registrar Compra de Producto", "🛒");
        setSize(600, 650);
        setLocationRelativeTo(parent);
        
        setContentBody(buildBody());
        setFooter(buildFooter());
        
        loadClientes();
    }

    private JScrollPane buildBody() {
        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(16, 24, 8, 24));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        int row = 0;

        // ── Sección Cliente ───────────────────────────────────────────────────
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 2; gc.insets = ins(0, 0, 6, 0);
        body.add(sectionLabel("👤  Cliente Vendedor"), gc); row++;

        rbClienteExistente = new JRadioButton("Cliente registrado");
        rbClienteNuevo     = new JRadioButton("Cliente nuevo");
        rbEmpleado         = new JRadioButton("Empleado");
        rbSinCliente       = new JRadioButton("Compra general (Sin cliente)");
        rbClienteExistente.setSelected(true);
        rbClienteExistente.setOpaque(false);
        rbClienteNuevo.setOpaque(false);
        rbEmpleado.setOpaque(false);
        rbSinCliente.setOpaque(false);
        rbClienteExistente.setFont(FONT_FIELD);
        rbClienteNuevo.setFont(FONT_FIELD);
        rbEmpleado.setFont(FONT_FIELD);
        rbSinCliente.setFont(FONT_FIELD);

        ButtonGroup grp = new ButtonGroup();
        grp.add(rbClienteExistente);
        grp.add(rbClienteNuevo);
        grp.add(rbEmpleado);
        grp.add(rbSinCliente);

        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        radioPanel.setOpaque(false);
        radioPanel.add(rbClienteExistente);
        radioPanel.add(rbClienteNuevo);
        radioPanel.add(rbEmpleado);
        radioPanel.add(rbSinCliente);
        gc.gridy = row; gc.insets = ins(0, 0, 6, 0);
        body.add(radioPanel, gc); row++;

        JPanel comboPanel = new JPanel(new CardLayout());
        comboPanel.setOpaque(false);
        
        cmbCliente = styledCombo();
        cmbEmpleado = styledCombo();
        
        comboPanel.add(cmbCliente, "CLIENTE");
        comboPanel.add(cmbEmpleado, "EMPLEADO");
        comboPanel.add(new JLabel("Se registrará como compra anónima."), "SIN_CLIENTE");
        
        gc.gridy = row; gc.insets = ins(0, 0, 12, 0);
        body.add(comboPanel, gc); row++;

        pnlClienteRapido = new JPanel(new GridLayout(1, 2, 8, 0));
        pnlClienteRapido.setOpaque(false);
        txtNombreRapido   = styledField("Nombre completo *");
        txtTelefonoRapido = styledField("Teléfono (opcional)");
        pnlClienteRapido.add(txtNombreRapido);
        pnlClienteRapido.add(txtTelefonoRapido);
        pnlClienteRapido.setVisible(false);
        gc.gridy = row; gc.insets = ins(0, 0, 12, 0);
        body.add(pnlClienteRapido, gc); row++;

        CardLayout cl = (CardLayout) comboPanel.getLayout();
        rbClienteExistente.addActionListener(e -> {
            comboPanel.setVisible(true);
            cl.show(comboPanel, "CLIENTE");
            pnlClienteRapido.setVisible(false);
        });
        rbClienteNuevo.addActionListener(e -> {
            comboPanel.setVisible(false);
            pnlClienteRapido.setVisible(true);
        });
        rbEmpleado.addActionListener(e -> {
            comboPanel.setVisible(true);
            cl.show(comboPanel, "EMPLEADO");
            pnlClienteRapido.setVisible(false);
        });
        rbSinCliente.addActionListener(e -> {
            comboPanel.setVisible(true);
            cl.show(comboPanel, "SIN_CLIENTE");
            pnlClienteRapido.setVisible(false);
        });

        // ── Sección Artículo ──────────────────────────────────────────────────
        gc.gridy = row; gc.insets = ins(4, 0, 6, 0);
        body.add(sectionLabel("📦  Datos del Artículo"), gc); row++;

        gc.gridy = row; gc.gridwidth = 2; gc.insets = ins(0, 0, 4, 0);
        body.add(fieldLabel("Nombre del artículo *"), gc); row++;
        txtNombreArticulo = styledField("Nombre del producto");
        gc.gridy = row; gc.insets = ins(0, 0, 10, 0);
        body.add(txtNombreArticulo, gc); row++;

        gc.gridwidth = 1; gc.weightx = 1;
        gc.gridx = 0; gc.gridy = row; gc.insets = ins(0, 0, 4, 6);
        body.add(fieldLabel("Categoría *"), gc);
        gc.gridx = 1; gc.insets = ins(0, 6, 4, 0);
        body.add(fieldLabel("Estado del producto"), gc);
        row++;

        cmbCategoria = styledCombo();
        for (ArticleCategory cat : ArticleCategory.values()) cmbCategoria.addItem(cat);
        cmbEstado = styledCombo();
        for (ItemState st : ItemState.values()) cmbEstado.addItem(st);

        gc.gridx = 0; gc.gridy = row; gc.insets = ins(0, 0, 10, 6);
        body.add(cmbCategoria, gc);
        gc.gridx = 1; gc.insets = ins(0, 6, 10, 0);
        body.add(cmbEstado, gc);
        row++;

        gc.gridx = 0; gc.gridwidth = 2; gc.gridy = row; gc.insets = ins(0, 0, 4, 0);
        body.add(fieldLabel("Descripción / Observaciones"), gc); row++;
        txtDescription = styledTextArea(2);
        gc.gridy = row; gc.insets = ins(0, 0, 12, 0);
        body.add(new JScrollPane(txtDescription), gc); row++;

        // ── Sección Precios ───────────────────────────────────────────────────
        gc.gridy = row; gc.insets = ins(4, 0, 6, 0);
        body.add(sectionLabel("💰  Precios"), gc); row++;

        gc.gridwidth = 1;
        gc.gridx = 0; gc.gridy = row; gc.insets = ins(0, 0, 4, 6);
        body.add(fieldLabel("Precio de compra *"), gc);
        gc.gridx = 1; gc.insets = ins(0, 6, 4, 0);
        body.add(fieldLabel("Precio de venta sugerido *"), gc);
        row++;

        spnPrecioCompra = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 9_999_999.0, 1000.0));
        spnPrecioVenta  = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 9_999_999.0, 1000.0));
        spnPrecioCompra.addChangeListener(e -> checkMargen());
        spnPrecioVenta .addChangeListener(e -> checkMargen());

        gc.gridx = 0; gc.gridy = row; gc.insets = ins(0, 0, 4, 6);
        body.add(spnPrecioCompra, gc);
        gc.gridx = 1; gc.insets = ins(0, 6, 4, 0);
        body.add(spnPrecioVenta, gc);
        row++;

        lblMargenWarning = new JLabel(" ");
        lblMargenWarning.setFont(FONT_SMALL);
        lblMargenWarning.setForeground(WARNING_CLR);
        gc.gridx = 0; gc.gridwidth = 2; gc.gridy = row; gc.insets = ins(0, 0, 4, 0);
        body.add(lblMargenWarning, gc);

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        return scroll;
    }

    private JPanel buildFooter() {
        JButton btnCancel = buildCancelButton();
        JButton btnSave   = buildSuccessButton("Registrar Compra");
        btnCancel.addActionListener(e -> onCancel());
        btnSave  .addActionListener(e -> doSave());
        return buildStandardFooter(btnCancel, btnSave);
    }

    private void loadClientes() {
        new SwingWorker<Void, Void>() {
            List<Cliente> clientes;
            List<Employee> employees;
            @Override protected Void doInBackground() throws Exception {
                clientes = clienteService.getAll();
                employees = employeeService.findAll();
                return null;
            }
            @Override protected void done() {
                try {
                    get();
                    if (clientes != null) clientes.forEach(cmbCliente::addItem);
                    if (employees != null) employees.forEach(cmbEmpleado::addItem);
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void checkMargen() {
        double compra = (double) spnPrecioCompra.getValue();
        double venta  = (double) spnPrecioVenta.getValue();
        if (compra > 0 && venta > 0 && compra >= venta) {
            lblMargenWarning.setText("⚠ El precio de compra es igual o mayor al precio de venta.");
        } else {
            lblMargenWarning.setText(" ");
        }
    }

    private void doSave() {
        if (txtNombreArticulo.getText().isBlank()) {
            showValidationError("El nombre del artículo es obligatorio."); return;
        }
        double compra = (double) spnPrecioCompra.getValue();
        double venta  = (double) spnPrecioVenta.getValue();
        if (compra <= 0) { showValidationError("El precio de compra debe ser mayor a $0."); return; }
        if (venta  <= 0) { showValidationError("El precio de venta debe ser mayor a $0."); return; }

        if (compra >= venta) {
            int ok = JOptionPane.showConfirmDialog(this,
                    "El precio de compra es igual o mayor al precio de venta.\n¿Confirmar de todas formas?",
                    "Advertencia de margen", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (ok != JOptionPane.YES_OPTION) return;
        }

        Article article = new Article(
                0,
                txtNombreArticulo.getText().trim(),
                txtDescription.getText().trim(),
                1,
                BigDecimal.valueOf(venta),
                (ArticleCategory) cmbCategoria.getSelectedItem(),
                com.app.Model.Enum.SourceType.COMPRA,
                (ItemState) cmbEstado.getSelectedItem(),
                BigDecimal.valueOf(compra)
        );

        int    clienteId     = 0;
        Cliente clienteRapido = null;

        if (rbClienteExistente.isSelected()) {
            Cliente sel = cmbCliente.getItemAt(cmbCliente.getSelectedIndex());
            if (sel != null) clienteId = sel.getId();
            else { showValidationError("Seleccione un cliente registrado."); return; }
        } else if (rbClienteNuevo.isSelected()) {
            String nombre = txtNombreRapido.getText().trim();
            if (nombre.isBlank()) { showValidationError("El nombre del cliente es obligatorio."); return; }
            clienteRapido = Cliente.createRapido(null, nombre, null, txtTelefonoRapido.getText().trim());
        } else if (rbEmpleado.isSelected()) {
            Employee sel = cmbEmpleado.getItemAt(cmbEmpleado.getSelectedIndex());
            if (sel == null) { showValidationError("Seleccione un empleado."); return; }
            clienteRapido = Cliente.createRapido(null, sel.getFullName(), null, sel.getEmail());
        }

        final int fClienteId     = clienteId;
        final Cliente fClienteRapido = clienteRapido;
        final String notes = txtDescription.getText().trim();

        new SwingWorker<Purchase, Void>() {
            @Override protected Purchase doInBackground() throws Exception {
                return purchaseService.register(article, BigDecimal.valueOf(compra),
                        fClienteId, fClienteRapido, notes);
            }
            @Override protected void done() {
                try {
                    result    = get();
                    confirmed = true;
                    JOptionPane.showMessageDialog(PurchaseDialog.this,
                            "Compra registrada. Artículo añadido al inventario.",
                            "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } catch (ExecutionException ex) {
                    showValidationError("Error: " + ex.getCause().getMessage());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    public boolean isConfirmed()  { return confirmed; }
    public Purchase getResult()   { return result; }
}