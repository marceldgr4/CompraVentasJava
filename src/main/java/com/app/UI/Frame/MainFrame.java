package com.app.UI.Frame;

import Infrastructure.security.SessionManager;
import com.app.Controllers.AuthController;
import com.app.UI.Components.ButtonFactory;
import com.app.UI.Panel.*;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;

/**
 * MainFrame rediseñado para coincidir con el mockup:
 *
 * SIDEBAR:
 *  - Logo "CompraVenta" + subtítulo "SISTEMA DE GESTIÓN" arriba
 *  - Ítems de navegación con emoji + label, ítem activo fondo azul+barra lateral
 *  - Avatar circular con iniciales en la parte inferior
 *
 * TOPBAR:
 *  - Hamburger "☰" + título de sección activa
 *  - Botones "Actualizar" (azul) y "Cerrar Sesión" (rojo) a la derecha
 *
 * CONTENIDO: CardLayout con los paneles de cada sección
 */
public class MainFrame extends JFrame {

    // ── Paleta exacta del mockup ──────────────────────────────────────────────
    static final Color SIDEBAR_BG    = new Color(18,  28, 58);   // #121C3A
    static final Color SIDEBAR_SEL   = new Color(30, 100, 200);  // azul ítem activo
    static final Color SIDEBAR_HOV   = new Color(28,  42, 80);   // hover
    static final Color ACCENT_LINE   = new Color(100, 181, 246); // barra lateral izquierda
    static final Color TOPBAR_BG     = new Color(30, 100, 200);  // azul topbar
    static final Color CONTENT_BG    = new Color(240, 244, 250); // fondo contenido
    static final Color LOGOUT_RED    = new Color(220,  55,  55);

    private static final int SIDEBAR_W_EXP  = 262;
    private static final int SIDEBAR_W_COL  = 64;

    // ── Secciones ─────────────────────────────────────────────────────────────
    private static final String[] PANEL_IDS   = {"Dashboard","Articles","Pawns","Sales","Purchases","Clients","Profiles"};
    private static final String[] NAV_ICONS   = {"🏠","📦","🤝","💰","🛒","👤","👔"};
    private static final String[] NAV_LABELS  = {"Dashboard","Artículos","Empeños","Ventas","Compras","Clientes","Empleados"};

    // ── Estado ────────────────────────────────────────────────────────────────
    private JPanel           sidebar;
    private JPanel           contentPanel;
    private JLabel           lblTopTitle;
    private CardLayout       cardLayout;
    private boolean          expanded = true;
    private String           activePanel = "Dashboard";

    private final List<NavItem> navItems = new ArrayList<>();
    private final AuthController authController = new AuthController();

    public MainFrame() {
        setTitle("CompraVenta — Sistema de Gestión");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1240, 760);
        setMinimumSize(new Dimension(1000, 640));
        setLocationRelativeTo(null);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(buildTopBar(), BorderLayout.NORTH);
        getContentPane().add(buildSidebar(), BorderLayout.WEST);
        getContentPane().add(buildContent(), BorderLayout.CENTER);

        selectPanel("Dashboard");
    }

    // ── TOP BAR ───────────────────────────────────────────────────────────────

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(TOPBAR_BG);
        bar.setPreferredSize(new Dimension(0, 56));
        bar.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 20));

        // Izquierda: hamburger + título de sección
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);

        JButton btnHamburger = new JButton("☰") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(new Color(255, 255, 255, 30));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnHamburger.setFont(new Font("Segoe UI Emoji", Font.BOLD, 20));
        btnHamburger.setForeground(Color.WHITE);
        btnHamburger.setContentAreaFilled(false);
        btnHamburger.setBorderPainted(false);
        btnHamburger.setFocusPainted(false);
        btnHamburger.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnHamburger.addActionListener(e -> toggleSidebar());

        lblTopTitle = new JLabel("Dashboard");
        lblTopTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTopTitle.setForeground(Color.WHITE);

        left.add(btnHamburger);
        left.add(lblTopTitle);

        // Derecha: botones Actualizar + Cerrar Sesión
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        JButton btnRefresh = buildTopBarButton("⟳  Actualizar", new Color(60, 130, 220));
        JButton btnLogout  = buildTopBarButton("Cerrar Sesión", LOGOUT_RED);
        btnLogout.addActionListener(e -> doLogout());

        right.add(btnRefresh);
        right.add(btnLogout);

        bar.add(left,  BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JButton buildTopBarButton(String text, Color bg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color fill = getModel().isPressed() ? bg.darker() : getModel().isRollover() ? bg.brighter() : bg;
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setMargin(new Insets(6, 16, 6, 16));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ── SIDEBAR ───────────────────────────────────────────────────────────────

    private JPanel buildSidebar() {
        sidebar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(SIDEBAR_BG);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        sidebar.setLayout(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(SIDEBAR_W_EXP, 0));

        // ── Header del sidebar ────────────────────────────────────────────
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(BorderFactory.createEmptyBorder(24, 20, 16, 20));

        JLabel lblLogo = new JLabel("CompraVenta");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblLogo.setForeground(Color.WHITE);
        lblLogo.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lblSub = new JLabel("SISTEMA DE GESTIÓN");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblSub.setForeground(new Color(255, 255, 255, 100));
        lblSub.setAlignmentX(LEFT_ALIGNMENT);
        lblSub.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));

        header.add(lblLogo);
        header.add(lblSub);

        // Separador
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(255, 255, 255, 20));
        sep.setBackground(new Color(255, 255, 255, 20));

        // ── Ítems de navegación ───────────────────────────────────────────
        JPanel navPanel = new JPanel();
        navPanel.setOpaque(false);
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));

        boolean isAdmin = SessionManager.isAdmin();
        for (int i = 0; i < PANEL_IDS.length; i++) {
            if (PANEL_IDS[i].equals("Profiles") && !isAdmin) continue;

            NavItem item = new NavItem(NAV_ICONS[i], NAV_LABELS[i], PANEL_IDS[i]);
            item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
            item.addActionListener(e -> selectPanel(item.panelId));
            navItems.add(item);
            navPanel.add(item);
        }

        // ── Avatar / usuario ──────────────────────────────────────────────
        JPanel avatarPanel = buildAvatarPanel();

        // Composición del sidebar
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(header, BorderLayout.NORTH);
        top.add(sep,    BorderLayout.CENTER);

        sidebar.add(top,        BorderLayout.NORTH);
        sidebar.add(navPanel,   BorderLayout.CENTER);
        sidebar.add(avatarPanel,BorderLayout.SOUTH);

        return sidebar;
    }

    private JPanel buildAvatarPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(255,255,255,20)),
                BorderFactory.createEmptyBorder(14, 10, 14, 10)));

        // Círculo con iniciales
        String name = "";
        try { name = SessionManager.getFullName(); } catch (Exception ignored) {}
        String initials = getInitials(name);
        String role = SessionManager.isAdmin() ? "Administradora" : "Empleado";

        AvatarCircle avatar = new AvatarCircle(initials, new Color(30, 100, 200), 38);

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel lblName = new JLabel(name.isEmpty() ? "Usuario" : name);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblName.setForeground(Color.WHITE);

        JLabel lblRole = new JLabel(role);
        lblRole.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblRole.setForeground(new Color(255, 255, 255, 140));

        info.add(lblName);
        info.add(lblRole);

        panel.add(avatar);
        panel.add(info);
        return panel;
    }

    // ── CONTENT ───────────────────────────────────────────────────────────────

    private JPanel buildContent() {
        cardLayout   = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(CONTENT_BG);

        contentPanel.add(new DashboardPanel(), "Dashboard");
        contentPanel.add(new ArticlePanel(),   "Articles");
        contentPanel.add(new PawnPanel(),      "Pawns");
        contentPanel.add(new SalePanel(),      "Sales");
        contentPanel.add(new PurchasePanel(),  "Purchases");
        contentPanel.add(new ClientePanel(),   "Clients");

        if (SessionManager.isAdmin()) {
            contentPanel.add(new ProfilePanel(), "Profiles");
        }
        return contentPanel;
    }

    private JLabel buildPlaceholder(String text) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.ITALIC, 20));
        lbl.setForeground(new Color(180, 190, 210));
        lbl.setBackground(CONTENT_BG);
        lbl.setOpaque(true);
        return lbl;
    }

    // ── Acciones ──────────────────────────────────────────────────────────────

    private void selectPanel(String panelId) {
        activePanel = panelId;
        cardLayout.show(contentPanel, panelId);

        // Actualizar label del topbar
        int idx = indexOf(PANEL_IDS, panelId);
        if (idx >= 0) lblTopTitle.setText(NAV_LABELS[idx]);

        // Actualizar estado visual de los nav items
        navItems.forEach(ni -> ni.setSelected(ni.panelId.equals(panelId)));
    }

    private void toggleSidebar() {
        expanded = !expanded;
        sidebar.setPreferredSize(new Dimension(expanded ? SIDEBAR_W_EXP : SIDEBAR_W_COL, 0));
        navItems.forEach(ni -> ni.setExpanded(expanded));
        sidebar.revalidate();
        sidebar.repaint();
    }

    private void doLogout() {
        authController.logout(this, () -> {
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        });
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String getInitials(String name) {
        if (name == null || name.isBlank()) return "?";
        String[] parts = name.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(2, parts.length); i++)
            if (!parts[i].isEmpty()) sb.append(parts[i].charAt(0));
        return sb.toString().toUpperCase();
    }

    private int indexOf(String[] arr, String val) {
        for (int i = 0; i < arr.length; i++) if (arr[i].equals(val)) return i;
        return -1;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Componentes internos
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Ítem de navegación del sidebar con emoji icon, label, barra lateral
     * de acento cuando está seleccionado, y efecto hover.
     */
    static class NavItem extends JToggleButton {
        final String panelId;
        private final String icon;
        private final String label;
        private boolean expanded = true;

        NavItem(String icon, String label, String panelId) {
            this.icon    = icon;
            this.label   = label;
            this.panelId = panelId;
            applyText(true);

            setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
            setForeground(new Color(200, 210, 230));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setHorizontalAlignment(SwingConstants.LEFT);
            setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(Integer.MAX_VALUE, 50));
        }

        void setExpanded(boolean exp) {
            this.expanded = exp;
            applyText(exp);
            setHorizontalAlignment(exp ? SwingConstants.LEFT : SwingConstants.CENTER);
            setBorder(exp
                    ? BorderFactory.createEmptyBorder(0, 20, 0, 20)
                    : BorderFactory.createEmptyBorder(0, 0, 0, 0));
        }

        private void applyText(boolean exp) {
            setText(exp ? icon + "  " + label : icon);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (isSelected()) {
                g2.setColor(SIDEBAR_SEL);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Barra lateral de acento
                g2.setColor(ACCENT_LINE);
                g2.fillRoundRect(0, 0, 4, getHeight(), 4, 4);
                setForeground(Color.WHITE);
            } else if (getModel().isRollover()) {
                g2.setColor(SIDEBAR_HOV);
                g2.fillRect(0, 0, getWidth(), getHeight());
                setForeground(Color.WHITE);
            } else {
                g2.setColor(SIDEBAR_BG);
                g2.fillRect(0, 0, getWidth(), getHeight());
                setForeground(new Color(180, 195, 220));
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Círculo con iniciales para el avatar del sidebar.
     */
    static class AvatarCircle extends JPanel {
        private final String initials;
        private final Color  bg;
        private final int    size;

        AvatarCircle(String initials, Color bg, int size) {
            this.initials = initials;
            this.bg       = bg;
            this.size     = size;
            setOpaque(false);
            setPreferredSize(new Dimension(size, size));
            setMaximumSize (new Dimension(size, size));
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fill(new Ellipse2D.Double(0, 0, size, size));
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, size / 3));
            FontMetrics fm = g2.getFontMetrics();
            int tx = (size - fm.stringWidth(initials)) / 2;
            int ty = (size - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(initials, tx, ty);
            g2.dispose();
        }
    }
}