package com.app.UI.dialogs;

import com.app.Model.Enum.ArticleCategory;
import com.app.Model.Enum.ItemState;
import com.app.Model.domain.Article;
import com.app.Model.domain.Cliente;
import com.app.Model.domain.Purchase;
import com.app.Service.ClienteService;
import com.app.Service.PurchaseService;
import com.app.Service.exceptions.ServiceException;
import com.app.UI.Components.ButtonFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Diálogo para registrar la compra de un producto usado al negocio (HU-28).
 * Soporta cliente existente o registro rápido embebido.
 */
public class PurchaseDialog extends JDialog {

    private static final Color HEADER_BG   = new Color(18, 28, 58);
    private static final Color BLUE_ACCENT = new Color(30, 136, 229);
    private static final Color FIELD_BG    = new Color(245, 247, 252);
    private static final Color TEXT_DARK   = new Color(15, 25, 50);
    private static final Color WARNING_CLR = new Color(245, 124, 0);

    // Modo cliente
    private JRadioButton rbClienteExistente;
    private JRadioButton rbClienteNuevo;
    private JComboBox<Cliente> cmbCliente;
    private JPanel pnlClienteRapido;
    private JTextField txtNombreRapido;
    private JTextField txtTelefonoRapido;

    // Artículo
    private JTextField           txtNombreArticulo;
    private JComboBox<ArticleCategory> cmbCategoria;
    private JComboBox<ItemState> cmbEstado;
    private JTextArea            txtDescripcion;

    // Precios
    private JSpinner spnPrecioCompra;
    private JSpinner spnPrecioVenta;
    private JLabel   lblMargenWarning;

    // Estado
    private boolean confirmed = false;
    private Purchase result;

    private final PurchaseService purchaseService = new PurchaseService();
    private final ClienteService  clienteService  = new ClienteService();

    public PurchaseDialog(JFrame parent) {
        super(parent, true);
        setUndecorated(true);
        setSize(600, 620);
        setLocationRelativeTo(parent);
        setContentPane(buildRoot());
        loadClientes();
    }

    // ── Build UI ──────────────────────────────────────────────────────────────

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
        header.setPreferredSize(new Dimension(0, 58));
        header.setBorder(new EmptyBorder(0, 20, 0, 20));

        JLabel lbl = new JLabel("🛒  Registrar Compra de Producto");
        lbl.setFont(new Font("Segoe UI Emoji", Font.BOLD, 15));
        lbl.setForeground(Color.WHITE);

        JButton btnClose = closeBtn();
        header.add(lbl,      BorderLayout.WEST);
        header.add(btnClose, BorderLayout.EAST);
        return header;
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

        // Radio buttons
        rbClienteExistente = new JRadioButton("Cliente registrado");
        rbClienteNuevo     = new JRadioButton("Cliente nuevo");
        rbClienteExistente.setSelected(true);
        rbClienteExistente.setOpaque(false);
        rbClienteNuevo.setOpaque(false);
        ButtonGroup grp = new ButtonGroup();
        grp.add(rbClienteExistente);
        grp.add(rbClienteNuevo);

        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        radioPanel.setOpaque(false);
        radioPanel.add(rbClienteExistente);
        radioPanel.add(rbClienteNuevo);
        gc.gridy = row; gc.insets = ins(0, 0, 6, 0);
        body.add(radioPanel, gc); row++;

        // Combo cliente existente
        cmbCliente = new JComboBox<>();
        cmbCliente.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gc.gridy = row; gc.insets = ins(0, 0, 12, 0);
        body.add(cmbCliente, gc); row++;

        // Panel cliente rápido
        pnlClienteRapido = new JPanel(new GridLayout(1, 2, 8, 0));
        pnlClienteRapido.setOpaque(false);
        txtNombreRapido   = styledField("Nombre completo *");
        txtTelefonoRapido = styledField("Teléfono (opcional)");
        pnlClienteRapido.add(txtNombreRapido);
        pnlClienteRapido.add(txtTelefonoRapido);
        pnlClienteRapido.setVisible(false);
        gc.gridy = row; gc.insets = ins(0, 0, 12, 0);
        body.add(pnlClienteRapido, gc); row++;

        // Toggle visibilidad
        rbClienteExistente.addActionListener(e -> {
            cmbCliente.setVisible(true);
            pnlClienteRapido.setVisible(false);
        });
        rbClienteNuevo.addActionListener(e -> {
            cmbCliente.setVisible(false);
            pnlClienteRapido.setVisible(true);
        });

        // ── Sección Artículo ──────────────────────────────────────────────────
        gc.gridy = row; gc.insets = ins(4, 0, 6, 0);
        body.add(sectionLabel("📦  Datos del Artículo"), gc); row++;

        gc.gridy = row; gc.insets = ins(0, 0, 4, 0);
        body.add(fieldLabel("Nombre del artículo *"), gc); row++;
        txtNombreArticulo = styledField("");
        gc.gridy = row; gc.insets = ins(0, 0, 10, 0);
        body.add(txtNombreArticulo, gc); row++;

        // Categoría + Estado lado a lado
        gc.gridwidth = 1; gc.weightx = 1;
        gc.gridx = 0; gc.gridy = row; gc.insets = ins(0, 0, 4, 6);
        body.add(fieldLabel("Categoría *"), gc);
        gc.gridx = 1; gc.insets = ins(0, 6, 4, 0);
        body.add(fieldLabel("Estado del producto"), gc);
        row++;

        cmbCategoria = new JComboBox<>(ArticleCategory.values());
        cmbCategoria.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbEstado = new JComboBox<>(ItemState.values());
        cmbEstado.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        gc.gridx = 0; gc.gridy = row; gc.insets = ins(0, 0, 10, 6);
        body.add(cmbCategoria, gc);
        gc.gridx = 1; gc.insets = ins(0, 6, 10, 0);
        body.add(cmbEstado, gc);
        row++;

        // Descripción
        gc.gridx = 0; gc.gridwidth = 2; gc.gridy = row; gc.insets = ins(0, 0, 4, 0);
        body.add(fieldLabel("Descripción / Observaciones"), gc); row++;
        txtDescripcion = new JTextArea(2, 20);
        txtDescripcion.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);
        txtDescripcion.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 220, 235)),
                new EmptyBorder(6, 8, 6, 8)));
        gc.gridy = row; gc.insets = ins(0, 0, 12, 0);
        body.add(new JScrollPane(txtDescripcion), gc); row++;

        // ── Sección Precios ───────────────────────────────────────────────────
        gc.gridy = row; gc.insets = ins(4, 0, 6, 0);
        body.add(sectionLabel("💰  Precios"), gc); row++;

        gc.gridwidth = 1;
        gc.gridx = 0; gc.gridy = row; gc.insets = ins(0, 0, 4, 6);
        body.add(fieldLabel("Precio de compra (lo que pagamos) *"), gc);
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
        lblMargenWarning.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblMargenWarning.setForeground(WARNING_CLR);
        gc.gridx = 0; gc.gridwidth = 2; gc.gridy = row; gc.insets = ins(0, 0, 4, 0);
        body.add(lblMargenWarning, gc);

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        return scroll;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(225, 232, 245)));

        JButton btnCancel  = ButtonFactory.createNeutralButton("Cancelar");
        JButton btnConfirm = ButtonFactory.createSuccessButton("Registrar Compra");
        btnCancel .addActionListener(e -> dispose());
        btnConfirm.addActionListener(e -> doSave());

        footer.add(btnCancel);
        footer.add(btnConfirm);
        return footer;
    }

    // ── Operaciones ───────────────────────────────────────────────────────────

    private void loadClientes() {
        new SwingWorker<List<Cliente>, Void>() {
            @Override protected List<Cliente> doInBackground() throws Exception {
                return clienteService.getAll();
            }
            @Override protected void done() {
                try {
                    get().forEach(cmbCliente::addItem);
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
        // Validaciones UI básicas
        if (txtNombreArticulo.getText().isBlank()) {
            showError("El nombre del artículo es obligatorio."); return;
        }
        double compra = (double) spnPrecioCompra.getValue();
        double venta  = (double) spnPrecioVenta.getValue();
        if (compra <= 0) { showError("El precio de compra debe ser mayor a $0."); return; }
        if (venta  <= 0) { showError("El precio de venta debe ser mayor a $0."); return; }

        // Confirmación de margen negativo
        if (compra >= venta) {
            int ok = JOptionPane.showConfirmDialog(this,
                    "El precio de compra es igual o mayor al precio de venta.\n¿Confirmar de todas formas?",
                    "Advertencia de margen", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (ok != JOptionPane.YES_OPTION) return;
        }

        // Construir artículo
        Article article = new Article(
                0,
                txtNombreArticulo.getText().trim(),
                txtDescripcion.getText().trim(),
                1,
                BigDecimal.valueOf(venta),
                (ArticleCategory) cmbCategoria.getSelectedItem(),
                com.app.Model.Enum.SourceType.COMPRA,
                (ItemState) cmbEstado.getSelectedItem(),
                BigDecimal.valueOf(compra)
        );

        // Resolver cliente
        int    clienteId     = 0;
        Cliente clienteRapido = null;

        if (rbClienteExistente.isSelected()) {
            Cliente sel = (Cliente) cmbCliente.getSelectedItem();
            if (sel != null) clienteId = sel.getId();
        } else {
            String nombre = txtNombreRapido.getText().trim();
            if (nombre.isBlank()) { showError("El nombre del cliente es obligatorio."); return; }
            clienteRapido = Cliente.createRapido(null, nombre,
                    null, txtTelefonoRapido.getText().trim());
        }

        final int fClienteId     = clienteId;
        final Cliente fClienteRapido = clienteRapido;
        final String notes = txtDescripcion.getText().trim();

        new SwingWorker<Purchase, Void>() {
            @Override protected Purchase doInBackground() throws Exception {
                return purchaseService.Register(article, BigDecimal.valueOf(compra),
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
                    showError("Error: " + ex.getCause().getMessage());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private JTextField styledField(String placeholder) {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.putClientProperty("JTextField.placeholderText", placeholder);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 220, 235)),
                new EmptyBorder(8, 10, 8, 10)));
        tf.setPreferredSize(new Dimension(200, 38));
        return tf;
    }

    private JLabel sectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI Emoji", Font.BOLD, 13));
        lbl.setForeground(new Color(30, 80, 160));
        lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(210, 220, 235)));
        return lbl;
    }

    private JLabel fieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(TEXT_DARK);
        return lbl;
    }

    private Insets ins(int t, int l, int b, int r) {
        return new Insets(t, l, b, r);
    }

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

    public boolean isConfirmed()  { return confirmed; }
    public Purchase getResult()   { return result; }
}