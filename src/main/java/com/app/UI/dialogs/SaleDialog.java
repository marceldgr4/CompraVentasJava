package com.app.UI.dialogs;

import com.app.Model.domain.Article;
import com.app.Model.domain.Cliente;
import com.app.Model.domain.Profile;
import com.app.Model.domain.Sale;
import com.app.Model.domain.SalesDetail;
import com.app.Service.ArticleService;
import com.app.Service.ClienteService;
import com.app.Service.ProfileService;
import com.app.Service.SaleService;
import com.app.UI.Components.ButtonFactory;
import com.app.Utils.CurrencyUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Diálogo para registrar una venta (HU-19 / HU-27).
 */
public class SaleDialog extends JDialog {

    private static final Color HEADER_BG   = new Color(18, 28, 58);
    private static final Color BLUE_ACCENT = new Color(30, 136, 229);
    private static final Color FIELD_BG    = new Color(245, 247, 252);
    private static final Color TEXT_DARK   = new Color(15, 25, 50);
    private static final Color SUCCESS_CLR = new Color(56, 142, 60);
    private static final Color DANGER_CLR  = new Color(211, 47, 47);

    // Modo cliente
    private JRadioButton rbSinCliente;
    private JRadioButton rbClienteExistente;
    private JRadioButton rbNombreLibre;
    private JRadioButton rbEmpleado;
    private JComboBox<Cliente> cmbCliente;
    private JComboBox<Profile> cmbEmpleado;
    private JTextField txtNombreLibre;

    // Artículo
    private JComboBox<Article> cmbArticle;
    private JSpinner           spnQuantity;
    private JLabel             lblArticlePrice;

    // Carrito
    private DefaultTableModel cartModel;
    private JTable            cartTable;
    private JLabel            lblTotal;

    private JButton btnConfirm;

    private Sale    confirmedSale;
    private boolean confirmed = false;

    private final ArticleService articleService = new ArticleService();
    private final ClienteService clienteService = new ClienteService();
    private final SaleService    saleService    = new SaleService();
    private final ProfileService profileService = new ProfileService();

    public SaleDialog(JFrame parent) {
        super(parent, true);
        setUndecorated(true);
        setSize(740, 600);
        setLocationRelativeTo(parent);
        setContentPane(buildRoot());
        loadData();
    }

    // ── Build UI ──────────────────────────────────────────────────────────────

    private JPanel buildRoot() {
        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(new Color(210, 220, 235));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
            }
        };
        root.setOpaque(false);
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildBody(),   BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);
        return root;
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(HEADER_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight() + 14, 14, 14);
                g2.fillRect(0, getHeight() / 2, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setPreferredSize(new Dimension(0, 58));
        header.setBorder(new EmptyBorder(0, 20, 0, 20));

        JLabel lblTitle = new JLabel("💰  Nueva Venta");
        lblTitle.setFont(new Font("Segoe UI Emoji", Font.BOLD, 15));
        lblTitle.setForeground(Color.WHITE);

        JButton btnClose = closeBtn();
        header.add(lblTitle, BorderLayout.WEST);
        header.add(btnClose, BorderLayout.EAST);
        return header;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(0, 10));
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(14, 20, 8, 20));
        body.add(buildClienteSection(), BorderLayout.NORTH);
        body.add(buildCartSection(),    BorderLayout.CENTER);
        return body;
    }

    // ── Sección cliente con 3 modos ───────────────────────────────────────────

    private JPanel buildClienteSection() {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);

        JLabel lbl = new JLabel("Cliente");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(TEXT_DARK);

        // Radio buttons
        rbSinCliente      = new JRadioButton("Sin cliente (anónima)");
        rbClienteExistente= new JRadioButton("Cliente registrado");
        rbNombreLibre     = new JRadioButton("Nombre libre");
        rbEmpleado        = new JRadioButton("Empleado");
        rbSinCliente.setSelected(true);
        rbSinCliente.setOpaque(false);
        rbClienteExistente.setOpaque(false);
        rbNombreLibre.setOpaque(false);
        rbEmpleado.setOpaque(false);
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

        // Input según modo
        JPanel inputRow = new JPanel(new CardLayout());
        inputRow.setOpaque(false);

        cmbCliente = new JComboBox<>();
        cmbCliente.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbCliente.setPreferredSize(new Dimension(320, 34));

        cmbEmpleado = new JComboBox<>();
        cmbEmpleado.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbEmpleado.setPreferredSize(new Dimension(320, 34));

        txtNombreLibre = new JTextField();
        txtNombreLibre.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtNombreLibre.putClientProperty("JTextField.placeholderText", "Nombre del comprador...");
        txtNombreLibre.setPreferredSize(new Dimension(320, 34));

        inputRow.add(new JLabel(" "), "SIN_CLIENTE");
        inputRow.add(cmbCliente, "CLIENTE");
        inputRow.add(txtNombreLibre, "NOMBRE_LIBRE");
        inputRow.add(cmbEmpleado, "EMPLEADO");

        CardLayout cl = (CardLayout) inputRow.getLayout();
        // Toggle
        rbSinCliente.addActionListener(e -> cl.show(inputRow, "SIN_CLIENTE"));
        rbClienteExistente.addActionListener(e -> cl.show(inputRow, "CLIENTE"));
        rbNombreLibre.addActionListener(e -> cl.show(inputRow, "NOMBRE_LIBRE"));
        rbEmpleado.addActionListener(e -> cl.show(inputRow, "EMPLEADO"));

        panel.add(lbl,      BorderLayout.NORTH);
        panel.add(radioRow, BorderLayout.CENTER);
        panel.add(inputRow, BorderLayout.SOUTH);
        return panel;
    }

    // ── Carrito ───────────────────────────────────────────────────────────────

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
                BorderFactory.createLineBorder(new Color(210, 220, 240)),
                new EmptyBorder(4, 8, 4, 8)));

        JLabel lblArt = new JLabel("Artículo:");
        lblArt.setFont(new Font("Segoe UI", Font.BOLD, 12));

        cmbArticle = new JComboBox<>();
        cmbArticle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cmbArticle.setPreferredSize(new Dimension(220, 32));
        cmbArticle.addActionListener(e -> updatePriceLabel());

        JLabel lblQty = new JLabel("Cant:");
        lblQty.setFont(new Font("Segoe UI", Font.BOLD, 12));

        spnQuantity = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        spnQuantity.setPreferredSize(new Dimension(65, 32));

        lblArticlePrice = new JLabel("Precio: -");
        lblArticlePrice.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblArticlePrice.setForeground(new Color(60, 100, 180));

        // BUG FIX: botón usando ButtonFactory para color correcto
        JButton btnAdd = ButtonFactory.createPrimaryButton("+ Agregar");
        btnAdd.addActionListener(e -> addToCart());

        panel.add(lblArt);
        panel.add(cmbArticle);
        panel.add(lblQty);
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
        cartTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cartTable.setRowHeight(28);
        cartTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cartTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        cartTable.setFillsViewportHeight(true);
        cartTable.getColumnModel().getColumn(0).setMaxWidth(45);

        JScrollPane sp = new JScrollPane(cartTable);
        sp.setPreferredSize(new Dimension(0, 200));
        sp.setBorder(BorderFactory.createLineBorder(new Color(210, 220, 235)));
        return sp;
    }

    private JPanel buildTotalRow() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);


        JButton btnRemove = ButtonFactory.createDangerButton("✕ Quitar");
        btnRemove.addActionListener(e -> removeFromCart());

        lblTotal = new JLabel("Total: $0");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTotal.setForeground(SUCCESS_CLR);

        panel.add(btnRemove, BorderLayout.WEST);
        panel.add(lblTotal,  BorderLayout.EAST);
        return panel;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(225, 232, 245)));

        // BUG FIX: ambos botones con ButtonFactory
        JButton btnCancel  = ButtonFactory.createNeutralButton("Cancelar");
        btnConfirm = ButtonFactory.createSuccessButton("Confirmar Venta");
        btnCancel .addActionListener(e -> dispose());
        btnConfirm.addActionListener(e -> doConfirm());

        // Tamaño mínimo para que el texto del botón no quede cortado
        btnConfirm.setPreferredSize(new Dimension(150, 38));

        footer.add(btnCancel);
        footer.add(btnConfirm);
        return footer;
    }

    // ── Datos ─────────────────────────────────────────────────────────────────

    private void loadData() {
        new SwingWorker<Void, Void>() {
            List<Cliente> clientes;
            List<Article> articles;
            List<Profile> profiles;
            @Override protected Void doInBackground() throws Exception {
                clientes = clienteService.getAll();
                articles = articleService.getAvailableForSaleOrPawn();
                profiles = profileService.findAll();
                return null;
            }
            @Override protected void done() {
                try {
                    get();
                    if (clientes != null) clientes.forEach(cmbCliente::addItem);
                    if (articles != null) articles.forEach(cmbArticle::addItem);
                    if (profiles != null) profiles.forEach(cmbEmpleado::addItem);
                    updatePriceLabel();
                } catch (ExecutionException ex) {
                    showError("Error al cargar datos: " + ex.getCause().getMessage());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    // ── Carrito ───────────────────────────────────────────────────────────────

    private void addToCart() {
        Article selected = (Article) cmbArticle.getSelectedItem();
        if (selected == null) { showError("Selecciona un artículo."); return; }
        int qty = (int) spnQuantity.getValue();
        int inCart = getCartQtyFor(selected.getId());
        if (inCart + qty > selected.getAmount()) {
            showError("Stock insuficiente. Disponible: " + selected.getAmount() + ", en carrito: " + inCart);
            return;
        }
        // Actualizar si ya existe
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
        if (row < 0) { showError("Selecciona un artículo del carrito."); return; }
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
            Article art = findInCombo((int) cartModel.getValueAt(i, 0));
            if (art != null) {
                total = total.add(art.getPrice().multiply(BigDecimal.valueOf((int) cartModel.getValueAt(i, 2))));
            }
        }
        lblTotal.setText("Total: " + CurrencyUtils.format(total));
    }

    private Article findInCombo(int id) {
        for (int i = 0; i < cmbArticle.getItemCount(); i++) {
            Article a = cmbArticle.getItemAt(i);
            if (a.getId() == id) return a;
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

    // ── Confirmar ─────────────────────────────────────────────────────────────

    private void doConfirm() {
        if (cartModel.getRowCount() == 0) {
            showError("El carrito está vacío. Agrega al menos un artículo."); return;
        }

        // Resolver cliente según modo
        int clienteId = 0;
        String nombreAnon = null;

        if (rbClienteExistente.isSelected()) {
            Cliente sel = (Cliente) cmbCliente.getSelectedItem();
            if (sel == null) { showError("Selecciona un cliente de la lista."); return; }
            clienteId = sel.getId();
        } else if (rbNombreLibre.isSelected()) {
            String nombre = txtNombreLibre.getText().trim();
            if (nombre.isBlank()) { showError("Ingresa el nombre del comprador."); return; }
            nombreAnon = nombre;
        } else if (rbEmpleado.isSelected()) {
            Profile sel = (Profile) cmbEmpleado.getSelectedItem();
            if (sel == null) { showError("Seleccione un empleado de la lista."); return; }
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
                    showError("Error al registrar venta: " + ex.getCause().getMessage());
                    btnConfirm.setEnabled(true);
                    btnConfirm.setText("Confirmar Venta");
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private JButton closeBtn() {
        JButton btn = new JButton("✕");
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(new Color(180, 200, 230));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.addActionListener(e -> dispose());
        return btn;
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Validación", JOptionPane.WARNING_MESSAGE);
    }

    public boolean isConfirmed()   { return confirmed; }
    public Sale getConfirmedSale() { return confirmedSale; }
}