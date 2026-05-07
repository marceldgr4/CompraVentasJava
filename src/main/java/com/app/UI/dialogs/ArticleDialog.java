package com.app.UI.dialogs;

import com.app.Model.domain.Article;
import com.app.Model.domain.Cliente;
import com.app.Service.ClienteService;
import com.app.Model.Enum.ArticleCategory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class ArticleDialog extends JDialog {

    private static final Color HEADER_BG   = new Color(18, 28, 58);
    private static final Color BLUE_BORDER = new Color(30, 136, 229);
    private static final Color FIELD_BG    = new Color(245, 247, 252);
    private static final Color TEXT_DARK   = new Color(15, 25, 50);
    private static final Color TEXT_MUTED  = new Color(110, 120, 140);

    private StyledCombo<Cliente> cmbCliente;
    private StyledField          txtName;
    private StyledField          txtDescription;
    private StyledField          txtAmount;
    private StyledField          txtPrice;
    private StyledCombo<ArticleCategory> cmbCategory;
   // private JCheckBox            chkSold;
    private boolean              confirmed = false;

    public ArticleDialog(JFrame parent, Article article) {
        super(parent, true);
        setUndecorated(true);
        setSize(520, 520);
        setLocationRelativeTo(parent);

        JPanel root = buildRoot(article == null ? "Nuevo Artículo" : "Editar Artículo");
        setContentPane(root);
        if (article != null) fillFields(article);
    }

    private JPanel buildRoot(String title) {
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
        root.setBackground(Color.WHITE);
        root.add(buildHeader(title), BorderLayout.NORTH);
        root.add(buildBody(),        BorderLayout.CENTER);
        root.add(buildFooter(),      BorderLayout.SOUTH);
        return root;
    }

    private JPanel buildHeader(String title) {
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

        JLabel lblTitle = new JLabel("📦  " + title);
        lblTitle.setFont(new Font("Segoe UI Emoji", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);

        JButton btnClose = new JButton("✕");
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnClose.setForeground(new Color(180, 200, 230));
        btnClose.setContentAreaFilled(false);
        btnClose.setBorderPainted(false);
        btnClose.setFocusPainted(false);
        btnClose.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dispose());

        header.add(lblTitle, BorderLayout.WEST);
        header.add(btnClose, BorderLayout.EAST);
        return header;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(20, 24, 12, 24));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        int row = 0;

        // Cliente
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 2; gc.insets = new Insets(0,0,4,0);
        body.add(fieldLabel("Cliente propietario *"), gc); row++;
        cmbCliente = new StyledCombo<>();
        loadClientes();
        gc.gridy = row; gc.insets = new Insets(0,0,14,0);
        body.add(cmbCliente, gc); row++;

        // Nombre
        gc.insets = new Insets(0,0,4,0);
        gc.gridy = row; body.add(fieldLabel("Nombre del artículo *"), gc); row++;
        txtName = new StyledField("");
        gc.gridy = row; gc.insets = new Insets(0,0,14,0);
        body.add(txtName, gc); row++;

        // Categoría
        gc.insets = new Insets(0,0,4,0);
        gc.gridy = row; body.add(fieldLabel("Categoría *"), gc); row++;
        cmbCategory = new StyledCombo<>();
        for (ArticleCategory cat : ArticleCategory.values()) {
            cmbCategory.addItem(cat);
        }
        gc.gridy = row; gc.insets = new Insets(0,0,14,0);
        body.add(cmbCategory, gc); row++;

        // Descripcion
        gc.insets = new Insets(0,0,4,0);
        gc.gridy = row; body.add(fieldLabel("Descripción"), gc); row++;
        txtDescription = new StyledField("");
        gc.gridy = row; gc.insets = new Insets(0,0,14,0);
        body.add(txtDescription, gc); row++;

        // Cantidad + Precio (2 cols)
        gc.gridwidth = 1;
        gc.gridx = 0; gc.gridy = row; gc.weightx = 1; gc.insets = new Insets(0,0,4,6);
        body.add(fieldLabel("Cantidad *"), gc);
        gc.gridx = 1; gc.insets = new Insets(0,6,4,0);
        body.add(fieldLabel("Precio *"), gc);
        row++;
        txtAmount = new StyledField("0");
        txtPrice  = new StyledField("");
        gc.gridx = 0; gc.gridy = row; gc.insets = new Insets(0,0,16,6);
        body.add(txtAmount, gc);
        gc.gridx = 1; gc.insets = new Insets(0,6,16,0);
        body.add(txtPrice, gc);
        row++;

        /*/ Checkbox
        chkSold = new JCheckBox("¿Disponible para venta?");
        chkSold.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        chkSold.setForeground(TEXT_DARK);
        chkSold.setOpaque(false);
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 2; gc.insets = new Insets(0,0,0,0);
        body.add(chkSold, gc);
*/
        return body;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1,0,0,0, new Color(230,235,245)));

        JButton btnCancel = buildFooterButton("Cancelar", false);
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = buildFooterButton("Guardar", true);
        btnSave.addActionListener(e -> doSave());

        footer.add(btnCancel);
        footer.add(btnSave);
        return footer;
    }

    private JButton buildFooterButton(String text, boolean primary) {
        Color bgColor = primary ? BLUE_BORDER : new Color(245,248,255);
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color fill = getModel().isPressed() ? bgColor.darker()
                        : getModel().isRollover() ? (primary ? bgColor.brighter() : new Color(230,235,248))
                          : bgColor;
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                if (!primary) {
                    g2.setColor(new Color(200, 210, 230));
                    g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", primary ? Font.BOLD : Font.PLAIN, 13));
        btn.setForeground(primary ? Color.WHITE : TEXT_MUTED);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(100, 38));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void loadClientes() {
        try {
            List<Cliente> list = new ClienteService().getAll();
            cmbCliente.removeAllItems();
            list.forEach(cmbCliente::addItem);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudieron cargar los clientes:\n" + ex.getMessage(),
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void fillFields(Article a) {
        txtName       .setText(a.getNameArticle());
        txtDescription.setText(a.getDescription() != null ? a.getDescription() : "");
        txtAmount     .setText(String.valueOf(a.getAmount()));
        txtPrice      .setText(a.getPrice() != null ? a.getPrice().toPlainString() : "");
        if (a.getCategory() != null) {
            cmbCategory.setSelectedItem(a.getCategory());
        }

        if (a.getClienteId() > 0) {
            for (int i = 0; i < cmbCliente.getItemCount(); i++) {
                if (cmbCliente.getItemAt(i).getId() == a.getClienteId()) {
                    cmbCliente.setSelectedIndex(i); break;
                }
            }
        }
    }

    private void doSave() {
        if (cmbCliente.getSelectedItem() == null) { showError("Selecciona un cliente propietario."); return; }
        if (txtName.getText().isBlank())           { showError("El nombre es obligatorio."); return; }
        try {
            int amount = Integer.parseInt(txtAmount.getText().trim());
            if (amount < 0) throw new NumberFormatException();
            new BigDecimal(txtPrice.getText().trim());
        } catch (NumberFormatException ex) {
            showError("Cantidad y precio deben ser números válidos."); return;
        }
        confirmed = true;
        dispose();
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Validación", JOptionPane.WARNING_MESSAGE);
    }

    public boolean isConfirmed() { return confirmed; }

    public Article getArticle() {
        Cliente c = (Cliente) cmbCliente.getSelectedItem();
        ArticleCategory cat = (ArticleCategory) cmbCategory.getSelectedItem();
        int cId = (c != null) ? c.getId() : 0;
        
        Article a = new Article(
                cId,
                txtName.getText().trim(), 
                txtDescription.getText().trim(),
                Integer.parseInt(txtAmount.getText().trim()),
                new BigDecimal(txtPrice.getText().trim()),
                cat
        );
        return a;
    }

    private JLabel fieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(TEXT_DARK);
        return lbl;
    }

    static class StyledField extends JTextField {
        private boolean focused = false;
        StyledField(String text) {
            setText(text);
            setOpaque(false);
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setForeground(TEXT_DARK);
            setBorder(new EmptyBorder(10, 12, 10, 12));
            setPreferredSize(new Dimension(200, 42));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
            addFocusListener(new java.awt.event.FocusAdapter() {
                public void focusGained(java.awt.event.FocusEvent e) { focused = true;  repaint(); }
                public void focusLost  (java.awt.event.FocusEvent e) { focused = false; repaint(); }
            });
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(FIELD_BG);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g2.setColor(focused ? BLUE_BORDER : new Color(210, 220, 235));
            g2.setStroke(new BasicStroke(focused ? 2f : 1.5f));
            g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 10, 10);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    static class StyledCombo<T> extends JComboBox<T> {
        StyledCombo() {
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setBackground(FIELD_BG);
            setPreferredSize(new Dimension(200, 42));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        }
    }
}