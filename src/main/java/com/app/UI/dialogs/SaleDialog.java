package com.app.UI.dialogs;

import com.app.Model.domain.*;
import com.app.Service.ArticleService;
import com.app.Service.ClienteService;
import com.app.Service.EmployeeService;
import com.app.Service.SaleService;
import com.app.Utils.CurrencyUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Diálogo para registrar una venta.
 */
public class SaleDialog extends BaseDialog {

    private JRadioButton rbSinCliente;
    private JRadioButton rbClienteExistente;
    private JRadioButton rbNombreLibre;
    private JRadioButton rbEmpleado;
    private StyledCombo<Cliente> cmbCliente;
    private StyledCombo<Employee> cmbEmpleado;
    private StyledField txtNombreLibre;

    private StyledCombo<Article> cmbArticle;
    private JSpinner           spnQuantity;
    private JLabel             lblArticlePrice;

    private DefaultTableModel cartModel;
    private JTable            cartTable;
    private JLabel            lblTotal;

    private JButton btnConfirm;

    private Sale    confirmedSale;
    private boolean confirmed = false;

    private final ArticleService articleService = new ArticleService();
    private final ClienteService clienteService = new ClienteService();
    private final SaleService    saleService    = new SaleService();
    private final EmployeeService employeeService = new EmployeeService();

    public SaleDialog(JFrame parent) {
        super(parent, "Nueva Venta", "💰");
        setSize(760, 620);
        setLocationRelativeTo(parent);
        
        setContentBody(buildBody());
        setFooter(buildFooter());
        
        loadData();
    }

    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(0, 10));
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(14, 20, 8, 20));
        body.add(buildClienteSection(), BorderLayout.NORTH);
        body.add(buildCartSection(),    BorderLayout.CENTER);
        return body;
    }

    private JPanel buildClienteSection() {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);

        panel.add(fieldLabel("Vendido a:"), BorderLayout.NORTH);

        rbSinCliente      = new JRadioButton("Anónima");
        rbClienteExistente= new JRadioButton("Cliente");
        rbNombreLibre     = new JRadioButton("Nombre");
        rbEmpleado        = new JRadioButton("Empleado");
        rbSinCliente.setSelected(true);
        rbSinCliente.setOpaque(false);
        rbClienteExistente.setOpaque(false);
        rbNombreLibre.setOpaque(false);
        rbEmpleado.setOpaque(false);
        rbSinCliente.setFont(FONT_FIELD);
        rbClienteExistente.setFont(FONT_FIELD);
        rbNombreLibre.setFont(FONT_FIELD);
        rbEmpleado.setFont(FONT_FIELD);

        ButtonGroup grp = new ButtonGroup();
        grp.add(rbSinCliente);
        grp.add(rbClienteExistente);
        grp.add(rbNombreLibre);
        grp.add(rbEmpleado);

        JPanel radioRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        radioRow.setOpaque(false);
        radioRow.add(rbSinCliente);
        radioRow.add(rbClienteExistente);
        radioRow.add(rbNombreLibre);
        radioRow.add(rbEmpleado);

        JPanel inputRow = new JPanel(new CardLayout());
        inputRow.setOpaque(false);

        cmbCliente = styledCombo();
        cmbEmpleado = styledCombo();
        txtNombreLibre = styledField("Nombre del comprador...");

        inputRow.add(new JLabel(" "), "SIN_CLIENTE");
        inputRow.add(cmbCliente, "CLIENTE");
        inputRow.add(txtNombreLibre, "NOMBRE_LIBRE");
        inputRow.add(cmbEmpleado, "EMPLEADO");

        CardLayout cl = (CardLayout) inputRow.getLayout();
        rbSinCliente.addActionListener(e -> cl.show(inputRow, "SIN_CLIENTE"));
        rbClienteExistente.addActionListener(e -> cl.show(inputRow, "CLIENTE"));
        rbNombreLibre.addActionListener(e -> cl.show(inputRow, "NOMBRE_LIBRE"));
        rbEmpleado.addActionListener(e -> cl.show(inputRow, "EMPLEADO"));

        panel.add(radioRow, BorderLayout.CENTER);
        panel.add(inputRow, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildCartSection() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);
        panel.add(buildAddRow(),   BorderLayout.NORTH);
        panel.add(buildCartTable(),BorderLayout.CENTER);
        panel.add(buildTotalRow(), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildAddRow() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        panel.setBackground(new Color(240, 245, 255));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(4, 8, 4, 8)));

        cmbArticle = styledCombo();
        cmbArticle.setPreferredSize(new Dimension(250, 34));
        cmbArticle.addActionListener(e -> updatePriceLabel());

        spnQuantity = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        spnQuantity.setPreferredSize(new Dimension(65, 34));

        lblArticlePrice = new JLabel("Precio: -");
        lblArticlePrice.setFont(FONT_SMALL);
        lblArticlePrice.setForeground(BLUE_ACCENT);

        JButton btnAdd = buildPrimaryButton("+ Agregar");
        btnAdd.setPreferredSize(new Dimension(100, 34));
        btnAdd.addActionListener(e -> addToCart());

        panel.add(fieldLabel("Artículo:"));
        panel.add(cmbArticle);
        panel.add(fieldLabel("Cant:"));
        panel.add(spnQuantity);
        panel.add(lblArticlePrice);
        panel.add(btnAdd);
        return panel;
    }

    private JScrollPane buildCartTable() {
        String[] cols = {"ID", "Artículo", "Cant.", "Precio Unit.", "Subtotal"};
        cartModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        cartTable = new JTable(cartModel);
        cartTable.setFont(FONT_FIELD);
        cartTable.setRowHeight(32);
        cartTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cartTable.getTableHeader().setFont(FONT_LABEL);
        cartTable.setFillsViewportHeight(true);
        cartTable.getColumnModel().getColumn(0).setMaxWidth(45);

        JScrollPane sp = new JScrollPane(cartTable);
        sp.setPreferredSize(new Dimension(0, 200));
        sp.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        return sp;
    }

    private JPanel buildTotalRow() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JButton btnRemove = buildDangerButton("✕ Quitar");
        btnRemove.setPreferredSize(new Dimension(100, 36));
        btnRemove.addActionListener(e -> removeFromCart());

        lblTotal = new JLabel("Total: $0");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTotal.setForeground(SUCCESS_CLR);

        panel.add(btnRemove, BorderLayout.WEST);
        panel.add(lblTotal,  BorderLayout.EAST);
        return panel;
    }

    private JPanel buildFooter() {
        JButton btnCancel = buildCancelButton();
        btnConfirm = buildSuccessButton("Confirmar Venta");
        btnConfirm.setPreferredSize(new Dimension(160, 38));
        
        btnCancel.addActionListener(e -> onCancel());
        btnConfirm.addActionListener(e -> doConfirm());
        
        return buildStandardFooter(btnCancel, btnConfirm);
    }

    private void loadData() {
        new SwingWorker<Void, Void>() {
            List<Cliente> clientes;
            List<Article> articles;
            List<Employee> employees;

            @Override protected Void doInBackground() throws Exception {
                clientes = clienteService.getAll();
                articles = articleService.getAvailableForSaleOrPawn();
                employees = employeeService.findAll();
                return null;
            }

            @Override protected void done() {
                try {
                    get();
                    if (clientes != null) clientes.forEach(cmbCliente::addItem);
                    if (articles != null) articles.forEach(cmbArticle::addItem);
                    if (employees != null) employees.forEach(cmbEmpleado::addItem);
                    updatePriceLabel();
                } catch (Exception ex) {
                    showValidationError("Error al cargar datos: " + ex.getMessage());
                }
            }

        }.execute();
    }

    private void addToCart() {
        Article selected = (Article) cmbArticle.getSelectedItem();
        if (selected == null) { showValidationError("Selecciona un artículo."); return; }
        int qty = (int) spnQuantity.getValue();
        int inCart = getCartQtyFor(selected.getId());
        if (inCart + qty > selected.getAmount()) {
            showValidationError("Stock insuficiente. Disponible: " + selected.getAmount());
            return;
        }
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            if ((int) cartModel.getValueAt(i, 0) == selected.getId()) {
                int nq = (int) cartModel.getValueAt(i, 2) + qty;
                cartModel.setValueAt(nq, i, 2);
                cartModel.setValueAt(CurrencyUtils.format(selected.getPrice().multiply(BigDecimal.valueOf(nq))), i, 4);
                refreshTotal();
                return;
            }
        }
        cartModel.addRow(new Object[]{
                selected.getId(), selected.getNameArticle(), qty,
                CurrencyUtils.format(selected.getPrice()),
                CurrencyUtils.format(selected.getPrice().multiply(BigDecimal.valueOf(qty)))
        });
        refreshTotal();
    }

    private void removeFromCart() {
        int row = cartTable.getSelectedRow();
        if (row < 0) { showValidationError("Selecciona un artículo del carrito."); return; }
        cartModel.removeRow(row);
        refreshTotal();
    }

    private int getCartQtyFor(int articleId) {
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            if ((int) cartModel.getValueAt(i, 0) == articleId) return (int) cartModel.getValueAt(i, 2);
        }
        return 0;
    }

    private void refreshTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            int articleId = (int) cartModel.getValueAt(i, 0);
            Article art = findInCombo(articleId);
            if (art != null) {
                total = total.add(art.getPrice().multiply(BigDecimal.valueOf((int) cartModel.getValueAt(i, 2))));
            }
        }
        lblTotal.setText("Total: " + CurrencyUtils.format(total));
    }

    private Article findInCombo(int id) {
        for (int i = 0; i < cmbArticle.getItemCount(); i++) {
            Article a = cmbArticle.getItemAt(i);
            if (a != null && a.getId() == id) return a;
        }
        return null;
    }

    private void updatePriceLabel() {
        Article sel = (Article) cmbArticle.getSelectedItem();
        if (sel != null) {
            lblArticlePrice.setText("Precio: " + CurrencyUtils.format(sel.getPrice()) + " | Stock: " + sel.getAmount());
        } else {
            lblArticlePrice.setText("Precio: -");
        }
    }

    private void doConfirm() {
        if (cartModel.getRowCount() == 0) {
            showValidationError("El carrito está vacío."); return;
        }

        int clienteId = 0;
        String nombreAnon = null;

        if (rbClienteExistente.isSelected()) {
            Cliente sel = (Cliente) cmbCliente.getSelectedItem();
            if (sel == null) { showValidationError("Selecciona un cliente."); return; }
            clienteId = sel.getId();
        } else if (rbNombreLibre.isSelected()) {
            nombreAnon = txtNombreLibre.getText().trim();
            if (nombreAnon.isBlank()) { showValidationError("Ingresa el nombre."); return; }
        } else if (rbEmpleado.isSelected()) {
           Employee sel = (Employee) cmbEmpleado.getSelectedItem();
            if (sel == null) { showValidationError("Selecciona un empleado."); return; }
            nombreAnon = sel.getFullName();
        }

        Sale sale = new Sale(null, clienteId, java.time.LocalDateTime.now());
        sale.setClienteNombreAnon(nombreAnon);

        for (int i = 0; i < cartModel.getRowCount(); i++) {
            int articleId = (int) cartModel.getValueAt(i, 0);
            int qty       = (int) cartModel.getValueAt(i, 2);
            Article art   = findInCombo(articleId);
            if (art != null) sale.addDetail(new SalesDetail(0, articleId, qty, art.getPrice()));
        }

        btnConfirm.setEnabled(false);
        btnConfirm.setText("Procesando...");

        new SwingWorker<Sale, Void>() {
            @Override protected Sale doInBackground() throws Exception { return saleService.create(sale); }
            @Override protected void done() {
                try {
                    confirmedSale = get();
                    confirmed     = true;
                    dispose();
                } catch (ExecutionException ex) {
                    showValidationError("Error: " + ex.getCause().getMessage());
                    btnConfirm.setEnabled(true);
                    btnConfirm.setText("Confirmar Venta");
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    public boolean isConfirmed()   { return confirmed; }
    public Sale getConfirmedSale() { return confirmedSale; }
}