package com.app.UI.dialogs;

import com.app.Model.domain.Article;
import com.app.Model.domain.Cliente;
import com.app.Model.domain.Employee;
import com.app.Service.ClienteService;
import com.app.Service.EmployeeService;
import com.app.Model.Enum.ArticleCategory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Diálogo para crear o editar un artículo del inventario.
 */
public class ArticleDialog extends BaseDialog {

    private JRadioButton rbCliente;
    private JRadioButton rbEmpleado;
    private JComboBox<Cliente> cmbCliente;
    private JComboBox<Employee> cmbEmpleado;
    private JPanel pnlPropietario;

    private StyledField txtName;
    private StyledField txtDescription;
    private StyledField txtAmount;
    private StyledField txtPrice;
    private JComboBox<ArticleCategory> cmbCategory;

    private boolean confirmed = false;

    private final ClienteService clienteService = new ClienteService();
    private final EmployeeService employeeService = new EmployeeService();

    public ArticleDialog(JFrame parent, Article article) {

        super(parent, article == null ? "Nuevo Artículo" : "Editar Artículo", "📦");
        setSize(540, 600);
        setLocationRelativeTo(parent);
        
        setContentBody(buildBody());
        setFooter(buildFooter());
        
        loadData();
        if (article != null) fillFields(article);
    }

    private JPanel buildBody() {
        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(20, 24, 12, 24));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        int row = 0;

        // ── Sección Propietario ───────────────────────────────────────────────
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 2; gc.insets = ins(0, 0, 4, 0);
        body.add(sectionLabel("Propietario del artículo *"), gc); row++;

        rbCliente  = new JRadioButton("Cliente registrado");
        rbEmpleado = new JRadioButton("Empleado");
        rbCliente.setSelected(true);
        rbCliente.setOpaque(false);
        rbEmpleado.setOpaque(false);
        rbCliente.setFont(FONT_FIELD);
        rbEmpleado.setFont(FONT_FIELD);

        ButtonGroup grp = new ButtonGroup();
        grp.add(rbCliente);
        grp.add(rbEmpleado);

        JPanel radioRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        radioRow.setOpaque(false);
        radioRow.add(rbCliente);
        radioRow.add(rbEmpleado);
        gc.gridy = row; gc.insets = ins(0, 0, 4, 0);
        body.add(radioRow, gc); row++;

        pnlPropietario = new JPanel(new CardLayout());
        pnlPropietario.setOpaque(false);

        cmbCliente  = styledCombo();
        cmbEmpleado = styledCombo();

        pnlPropietario.add(cmbCliente,  "CLIENTE");
        pnlPropietario.add(cmbEmpleado, "EMPLEADO");

        CardLayout cl = (CardLayout) pnlPropietario.getLayout();
        rbCliente .addActionListener(e -> cl.show(pnlPropietario, "CLIENTE"));
        rbEmpleado.addActionListener(e -> cl.show(pnlPropietario, "EMPLEADO"));

        gc.gridy = row; gc.insets = ins(0, 0, 14, 0);
        body.add(pnlPropietario, gc); row++;

        // ── Sección Artículo ─────────────────────────────────────────────────
        gc.gridy = row; gc.insets = ins(4, 0, 4, 0);
        body.add(sectionLabel("Datos del artículo"), gc); row++;

        gc.gridy = row; gc.insets = ins(0, 0, 4, 0);
        body.add(fieldLabel("Nombre del artículo *"), gc); row++;
        txtName = styledField("Nombre del artículo");
        gc.gridy = row; gc.insets = ins(0, 0, 12, 0);
        body.add(txtName, gc); row++;

        gc.gridy = row; gc.insets = ins(0, 0, 4, 0);
        body.add(fieldLabel("Categoría *"), gc); row++;
        cmbCategory = styledCombo();
        for (ArticleCategory cat : ArticleCategory.values()) cmbCategory.addItem(cat);
        gc.gridy = row; gc.insets = ins(0, 0, 12, 0);
        body.add(cmbCategory, gc); row++;

        gc.gridy = row; gc.insets = ins(0, 0, 4, 0);
        body.add(fieldLabel("Descripción"), gc); row++;
        txtDescription = styledField("Opcional...");
        gc.gridy = row; gc.insets = ins(0, 0, 12, 0);
        body.add(txtDescription, gc); row++;

        gc.gridwidth = 1;
        gc.gridx = 0; gc.gridy = row; gc.insets = ins(0, 0, 4, 6);
        body.add(fieldLabel("Cantidad *"), gc);
        gc.gridx = 1; gc.insets = ins(0, 6, 4, 0);
        body.add(fieldLabel("Precio de venta *"), gc);
        row++;
        
        txtAmount = styledField("1");
        txtPrice  = styledField("0.00");
        gc.gridx = 0; gc.gridy = row; gc.insets = ins(0, 0, 6, 6);
        body.add(txtAmount, gc);
        gc.gridx = 1; gc.insets = ins(0, 6, 6, 0);
        body.add(txtPrice, gc);

        return body;
    }

    private JPanel buildFooter() {
        JButton btnCancel = buildCancelButton();
        JButton btnSave   = buildPrimaryButton("Guardar");
        btnCancel.addActionListener(e -> onCancel());
        btnSave  .addActionListener(e -> doSave());
        return buildStandardFooter(btnCancel, btnSave);
    }

    private void loadData() {
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

    private void fillFields(Article a) {
        txtName       .setText(a.getNameArticle());
        txtDescription.setText(a.getDescription() != null ? a.getDescription() : "");
        txtAmount     .setText(String.valueOf(a.getAmount()));
        txtPrice      .setText(a.getPrice() != null ? a.getPrice().toPlainString() : "");
        if (a.getCategory() != null) cmbCategory.setSelectedItem(a.getCategory());
    }

    private void doSave() {
        if (rbCliente.isSelected() && cmbCliente.getSelectedItem() == null) {
            showValidationError("Selecciona un cliente propietario."); return;
        }
        if (rbEmpleado.isSelected() && cmbEmpleado.getSelectedItem() == null) {
            showValidationError("Selecciona un empleado."); return;
        }
        if (txtName.getText().isBlank()) {
            showValidationError("El nombre del artículo es obligatorio."); return;
        }
        try {
            int amount = Integer.parseInt(txtAmount.getText().trim());
            if (amount < 0) throw new NumberFormatException();
            new BigDecimal(txtPrice.getText().trim());
        } catch (NumberFormatException ex) {
            showValidationError("Cantidad y precio deben ser números válidos."); return;
        }
        confirmed = true;
        dispose();
    }

    public boolean isConfirmed() { return confirmed; }

    public Article getArticle() {
        int clienteId = 0;
        if (rbCliente.isSelected()) {
            Cliente c = (Cliente) cmbCliente.getSelectedItem();
            if (c != null) clienteId = c.getId();
        }
        ArticleCategory cat = (ArticleCategory) cmbCategory.getSelectedItem();
        return new Article(
                clienteId,
                txtName.getText().trim(),
                txtDescription.getText().trim(),
                Integer.parseInt(txtAmount.getText().trim()),
                new BigDecimal(txtPrice.getText().trim()),
                cat
        );
    }
}