package com.app.UI.dialogs;

import com.app.Model.domain.Article;
import com.app.Model.domain.Cliente;
import com.app.Model.domain.Sale;
import com.app.Model.domain.SalesDetail;
import com.app.Service.ArticleService;
import com.app.Service.ClienteService;
import com.app.Service.SaleService;
import com.app.Service.exceptions.ServiceException;
import com.app.Utils.CurrencyUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Diálogo completo para registrar una venta.
 * HU-19: Selección de cliente, carrito multi-artículo, reducción atómica de stock.
 */
public class SaleDialog extends JDialog {

    private static final Color HEADER_BG   = new Color(18, 28, 58);
    private static final Color BLUE_ACCENT = new Color(30, 136, 229);
    private static final Color FIELD_BG    = new Color(245, 247, 252);
    private static final Color TEXT_DARK   = new Color(15, 25, 50);
    private static final Color SUCCESS     = new Color(56, 142, 60);
    private static final Color DANGER      = new Color(211, 47, 47);

    private JComboBox<Cliente>  cmbCliente;
    private JComboBox<Article>  cmbArticle;
    private JSpinner            spnQuantity;
    private JLabel              lblArticlePrice;

    private DefaultTableModel   cartModel;
    private JTable              cartTable;
    private JLabel              lblTotal;

    private JButton             btnAdd;
    private JButton             btnRemove;
    private JButton             btnConfirm;

    private Sale                confirmedSale;
    private boolean             confirmed = false;

    private final ArticleService articleService = new ArticleService();
    private final ClienteService clienteService = new ClienteService();
    private final SaleService    saleService    = new SaleService();

    public SaleDialog(JFrame parent) {
        super(parent, true);
        setUndecorated(true);
        setSize(720, 580);
        setLocationRelativeTo(parent);
        setContentPane(buildRoot());
        loadData();
    }

    // ── UI ────────────────────────────────────────────────────────────────────

    private JPanel buildRoot() {
        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
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
        header.setPreferredSize(new Dimension(0, 60));
        header.setBorder(new EmptyBorder(0, 20, 0, 20));

        JLabel lblTitle = new JLabel("💰  Nueva Venta");
        lblTitle.setFont(new Font("Segoe UI Emoji", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);

        JButton btnClose = makeCloseButton();
        header.add(lblTitle, BorderLayout.WEST);
        header.add(btnClose,  BorderLayout.EAST);
        return header;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(16, 20, 8, 20));

        body.add(buildClientePanel(),  BorderLayout.NORTH);
        body.add(buildCartSection(),   BorderLayout.CENTER);
        return body;
    }

    private JPanel buildClientePanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panel.setOpaque(false);

        JLabel lbl = new JLabel("Cliente: *");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(TEXT_DARK);

        cmbCliente = new JComboBox<>();
        cmbCliente.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbCliente.setPreferredSize(new Dimension(300, 36));

        panel.add(lbl);
        panel.add(cmbCliente);
        return panel;
    }

    private JPanel buildCartSection() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        panel.add(buildAddArticleRow(), BorderLayout.NORTH);
        panel.add(buildCartTable(),     BorderLayout.CENTER);
        panel.add(buildTotalRow(),      BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildAddArticleRow() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panel.setBackground(new Color(240, 245, 255));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 220, 240)),
                new EmptyBorder(8, 10, 8, 10)));

        // Artículo
        JLabel lblArt = new JLabel("Artículo:");
        lblArt.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cmbArticle = new JComboBox<>();
        cmbArticle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cmbArticle.setPreferredSize(new Dimension(220, 32));
        cmbArticle.addActionListener(e -> updatePriceLabel());

        // Cantidad
        JLabel lblQty = new JLabel("Cant:");
        lblQty.setFont(new Font("Segoe UI", Font.BOLD, 12));
        spnQuantity = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        spnQuantity.setPreferredSize(new Dimension(65, 32));

        // Precio actual
        lblArticlePrice = new JLabel("Precio: -");
        lblArticlePrice.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblArticlePrice.setForeground(new Color(80, 120, 180));

        btnAdd = new JButton("+ Agregar");
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAdd.setBackground(BLUE_ACCENT);
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setBorderPainted(false);
        btnAdd.setFocusPainted(false);
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
        String[] cols = {"#", "Artículo", "Cantidad", "Precio Unit.", "Subtotal"};
        cartModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        cartTable = new JTable(cartModel);
        cartTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cartTable.setRowHeight(28);
        cartTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cartTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        cartTable.setFillsViewportHeight(true);
        cartTable.getColumnModel().getColumn(0).setMaxWidth(40);

        JScrollPane sp = new JScrollPane(cartTable);
        sp.setPreferredSize(new Dimension(0, 200));
        sp.setBorder(BorderFactory.createLineBorder(new Color(210, 220, 235)));
        return sp;
    }

    private JPanel buildTotalRow() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        btnRemove = new JButton("✕ Quitar seleccionado");
        btnRemove.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnRemove.setForeground(DANGER);
        btnRemove.setBorderPainted(false);
        btnRemove.setContentAreaFilled(false);
        btnRemove.setFocusPainted(false);
        btnRemove.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRemove.addActionListener(e -> removeFromCart());

        lblTotal = new JLabel("Total: $0");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTotal.setForeground(SUCCESS);

        panel.add(btnRemove, BorderLayout.WEST);
        panel.add(lblTotal,  BorderLayout.EAST);
        return panel;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(225, 232, 245)));

        JButton btnCancel = buildFooterBtn("Cancelar", false);
        btnCancel.addActionListener(e -> dispose());

        btnConfirm = buildFooterBtn("Confirmar Venta", true);
        btnConfirm.addActionListener(e -> doConfirm());

        footer.add(btnCancel);
        footer.add(btnConfirm);
        return footer;
    }

    // ── Datos ─────────────────────────────────────────────────────────────────

    private void loadData() {
        new SwingWorker<Void, Void>() {
            List<Cliente>  clientes;
            List<Article>  articles;
            @Override protected Void doInBackground() throws Exception {
                clientes = clienteService.getAll();
                articles = articleService.getAvailableForSaleOrPawn();
                return null;
            }
            @Override protected void done() {
                try {
                    get();
                    clientes.forEach(cmbCliente::addItem);
                    articles.forEach(cmbArticle::addItem);
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
        int alreadyInCart = getCartQuantityForArticle(selected.getId());
        int totalRequested = alreadyInCart + qty;

        if (totalRequested > selected.getAmount()) {
            showError("Stock insuficiente. Disponible: " + selected.getAmount() +
                    ", en carrito: " + alreadyInCart + ".");
            return;
        }

        // Actualizar fila si ya existe, agregar si no
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            int rowArticleId = (int) cartModel.getValueAt(i, 0);
            if (rowArticleId == selected.getId()) {
                int newQty      = (int) cartModel.getValueAt(i, 2) + qty;
                BigDecimal price = selected.getPrice();
                cartModel.setValueAt(newQty, i, 2);
                cartModel.setValueAt(CurrencyUtils.format(price.multiply(BigDecimal.valueOf(newQty))), i, 4);
                refreshTotal();
                return;
            }
        }

        BigDecimal subtotal = selected.getPrice().multiply(BigDecimal.valueOf(qty));
        cartModel.addRow(new Object[]{
                selected.getId(),
                selected.getNameArticle(),
                qty,
                CurrencyUtils.format(selected.getPrice()),
                CurrencyUtils.format(subtotal)
        });
        refreshTotal();
    }

    private void removeFromCart() {
        int row = cartTable.getSelectedRow();
        if (row < 0) { showError("Selecciona un artículo del carrito para quitar."); return; }
        cartModel.removeRow(row);
        refreshTotal();
    }

    private int getCartQuantityForArticle(int articleId) {
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            if ((int) cartModel.getValueAt(i, 0) == articleId) {
                return (int) cartModel.getValueAt(i, 2);
            }
        }
        return 0;
    }

    private void refreshTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            // Reconstruir subtotal desde precio y cantidad
            Article art = findArticleInCombo((int) cartModel.getValueAt(i, 0));
            if (art != null) {
                int qty = (int) cartModel.getValueAt(i, 2);
                total = total.add(art.getPrice().multiply(BigDecimal.valueOf(qty)));
            }
        }
        lblTotal.setText("Total: " + CurrencyUtils.format(total));
    }

    private Article findArticleInCombo(int articleId) {
        for (int i = 0; i < cmbArticle.getItemCount(); i++) {
            Article a = cmbArticle.getItemAt(i);
            if (a.getId() == articleId) return a;
        }
        return null;
    }

    private void updatePriceLabel() {
        Article selected = (Article) cmbArticle.getSelectedItem();
        if (selected != null) {
            lblArticlePrice.setText("Precio: " + CurrencyUtils.format(selected.getPrice()) +
                    " | Stock: " + selected.getAmount());
        } else {
            lblArticlePrice.setText("Precio: -");
        }
    }

    // ── Confirmar ─────────────────────────────────────────────────────────────

    private void doConfirm() {
        if (cmbCliente.getSelectedItem() == null) {
            showError("Selecciona un cliente.");
            return;
        }
        if (cartModel.getRowCount() == 0) {
            showError("El carrito está vacío. Agrega al menos un artículo.");
            return;
        }

        Cliente cliente = (Cliente) cmbCliente.getSelectedItem();
        Sale sale = new Sale(null, cliente.getId(), java.time.LocalDateTime.now());

        for (int i = 0; i < cartModel.getRowCount(); i++) {
            int     articleId = (int) cartModel.getValueAt(i, 0);
            int     qty       = (int) cartModel.getValueAt(i, 2);
            Article art       = findArticleInCombo(articleId);
            if (art != null) {
                sale.addDetail(new SalesDetail(0, articleId, qty, art.getPrice()));
            }
        }

        btnConfirm.setEnabled(false);
        btnConfirm.setText("Procesando...");

        new SwingWorker<Sale, Void>() {
            @Override protected Sale doInBackground() throws Exception {
                return saleService.create(sale);
            }
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

    private JButton buildFooterBtn(String text, boolean primary) {
        Color bg = primary ? BLUE_ACCENT : new Color(240, 242, 248);
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color fill = getModel().isPressed() ? bg.darker()
                        : getModel().isRollover()   ? (primary ? bg.brighter() : new Color(225, 228, 242))
                          : bg;
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                if (!primary) {
                    g2.setColor(new Color(200, 208, 228));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", primary ? Font.BOLD : Font.PLAIN, 13));
        btn.setForeground(primary ? Color.WHITE : new Color(80, 90, 120));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(primary ? 160 : 100, 38));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton makeCloseButton() {
        JButton btn = new JButton("✕");
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setForeground(new Color(180, 200, 230));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> dispose());
        return btn;
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Validación", JOptionPane.WARNING_MESSAGE);
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public boolean isConfirmed()  { return confirmed; }
    public Sale getConfirmedSale() { return confirmedSale; }
}