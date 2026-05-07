package com.app.UI.Frame;

import com.app.Controllers.AuthController;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.concurrent.ExecutionException;

/**
 * LoginFrame rediseñado para coincidir exactamente con el mockup:
 * - Panel izquierdo: gradiente azul oscuro, logo "Compra Venta" grande, tagline
 * - Panel derecho: blanco, título "Iniciar Sesión", campos redondeados con borde azul, botón azul grande
 * - Ventana sin decoración nativa, bordes redondeados
 */
public class LoginFrame extends JFrame {

    // ── Colores del diseño ────────────────────────────────────────────────────
    private static final Color GRAD_TOP    = new Color(13,  42,  93);   // #0D2A5D azul muy oscuro
    private static final Color GRAD_BOT    = new Color(30,  90, 180);   // #1E5AB4 azul medio
    private static final Color BLUE_FIELD  = new Color(30, 136, 229);   // borde activo campo
    private static final Color FIELD_BG    = new Color(245, 247, 250);  // fondo campo inactivo
    private static final Color BTN_BLUE    = new Color(30, 136, 229);
    private static final Color TEXT_DARK   = new Color(15,  25,  50);
    private static final Color TEXT_MUTED  = new Color(110, 120, 140);
    private static final Color DOT_ACTIVE  = new Color(100, 181, 246);
    private static final Color DOT_INACT   = new Color(255, 255, 255, 80);

    // ── Campos ───────────────────────────────────────────────────────────────
    private RoundedField     txtEmail;
    private RoundedPassField txtPassword;
    private JButton          btnLogin;
    private JLabel           lblError;

    private final AuthController authController = new AuthController();
    private JPanel rootPanel;

    public LoginFrame() {
        setUndecorated(true);                    // sin barra nativa de Windows
        setSize(860, 620);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Contenedor principal con borde redondeado
        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };
        root.setOpaque(false);
        root.add(buildLeftPanel(),  BorderLayout.WEST);
        root.add(buildRightPanel(), BorderLayout.CENTER);

        // Fondo real de la ventana (para el recorte redondeado)
        this.rootPanel = root;
        setContentPane(root);
        setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));
    }

    // ── Panel izquierdo ───────────────────────────────────────────────────────

    private JPanel buildLeftPanel() {
        JPanel panel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Gradiente diagonal azul oscuro → azul medio
                GradientPaint gp = new GradientPaint(
                        0, 0,           GRAD_TOP,
                        getWidth(), getHeight(), GRAD_BOT);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth() + 20, getHeight(), 20, 20);
                g2.dispose();
            }
        };
        panel.setPreferredSize(new Dimension(340, 620));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(60, 40, 50, 40));

        // "SISTEMA DE GESTIÓN"
        JLabel lblTag = new JLabel("SISTEMA DE GESTIÓN");
        lblTag.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTag.setForeground(new Color(255, 255, 255, 140));
        lblTag.setAlignmentX(CENTER_ALIGNMENT);
        lblTag.setBorder(new EmptyBorder(0, 0, 20, 0));

        // Logo grande "Compra Venta"
        JLabel lblLogo = new JLabel("<html><center>Compra<br>Venta</center></html>");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 52));
        lblLogo.setForeground(Color.WHITE);
        lblLogo.setAlignmentX(CENTER_ALIGNMENT);
        lblLogo.setHorizontalAlignment(SwingConstants.CENTER);

        // Tagline
        JLabel lblTag2 = new JLabel(
                "<html><center>Gestiona compras, ventas y empeños<br>en un solo lugar.</center></html>");
        lblTag2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblTag2.setForeground(new Color(255, 255, 255, 180));
        lblTag2.setAlignmentX(CENTER_ALIGNMENT);
        lblTag2.setHorizontalAlignment(SwingConstants.CENTER);
        lblTag2.setBorder(new EmptyBorder(20, 0, 40, 0));

        // Puntos decorativos
        JPanel dots = buildDots();
        dots.setOpaque(false);
        dots.setAlignmentX(CENTER_ALIGNMENT);

        panel.add(Box.createVerticalGlue());
        panel.add(lblTag);
        panel.add(lblLogo);
        panel.add(lblTag2);
        panel.add(dots);
        panel.add(Box.createVerticalGlue());

        // Arrastrar ventana desde el panel izquierdo
        addDragBehavior(panel);
        return panel;
    }

    private JPanel buildDots() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        p.setOpaque(false);
        for (int i = 0; i < 3; i++) {
            final int idx = i;
            JPanel dot = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(idx == 0 ? DOT_ACTIVE : DOT_INACT);
                    g2.fillOval(0, 0, 10, 10);
                    g2.dispose();
                }
                @Override public Dimension getPreferredSize() { return new Dimension(10, 10); }
            };
            dot.setOpaque(false);
            p.add(dot);
        }
        return p;
    }

    // ── Panel derecho ─────────────────────────────────────────────────────────

    private JPanel buildRightPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(80, 60, 80, 60));

        JLabel lblTitle = new JLabel("Iniciar Sesión");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(TEXT_DARK);
        lblTitle.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lblSub = new JLabel("Ingresa tus credenciales para continuar");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSub.setForeground(TEXT_MUTED);
        lblSub.setAlignmentX(LEFT_ALIGNMENT);
        lblSub.setBorder(new EmptyBorder(6, 0, 36, 0));

        // Campo email
        JLabel lblEmail = fieldLabel("Correo electrónico");
        txtEmail = new RoundedField("admin@compraventa.com", false);
        txtEmail.setAlignmentX(LEFT_ALIGNMENT);

        // Campo contraseña
        JLabel lblPass = fieldLabel("Contraseña");
        lblPass.setBorder(new EmptyBorder(16, 0, 6, 0));
        txtPassword = new RoundedPassField("••••••••••••");
        txtPassword.setAlignmentX(LEFT_ALIGNMENT);

        // Botón
        btnLogin = buildLoginButton();
        btnLogin.setAlignmentX(LEFT_ALIGNMENT);

        // Error label
        lblError = new JLabel(" ");
        lblError.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblError.setForeground(new Color(220, 50, 50));
        lblError.setAlignmentX(LEFT_ALIGNMENT);
        lblError.setBorder(new EmptyBorder(8, 0, 0, 0));

        panel.add(Box.createVerticalGlue());
        panel.add(lblTitle);
        panel.add(lblSub);
        panel.add(lblEmail);
        panel.add(txtEmail);
        panel.add(lblPass);
        panel.add(txtPassword);
        panel.add(Box.createRigidArea(new Dimension(0, 28)));
        panel.add(btnLogin);
        panel.add(lblError);
        panel.add(Box.createVerticalGlue());

        txtPassword.addActionListener(e -> doLogin());
        txtEmail   .addActionListener(e -> doLogin());
        return panel;
    }

    private JLabel fieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(TEXT_DARK);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        lbl.setBorder(new EmptyBorder(0, 0, 6, 0));
        return lbl;
    }

    private JButton buildLoginButton() {
        JButton btn = new JButton("Entrar al Sistema") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color fill = getModel().isPressed()   ? BTN_BLUE.darker()
                        : getModel().isRollover()  ? BTN_BLUE.brighter()
                          : BTN_BLUE;
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setPreferredSize(new Dimension(340, 52));
        btn.setMaximumSize (new Dimension(Integer.MAX_VALUE, 52));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> doLogin());
        return btn;
    }

    // ── Drag window ───────────────────────────────────────────────────────────

    private void addDragBehavior(JPanel panel) {
        final Point[] start = {null};
        panel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent e) { start[0] = e.getPoint(); }
        });
        panel.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent e) {
                if (start[0] != null) {
                    Point loc = getLocation();
                    setLocation(loc.x + e.getX() - start[0].x,
                            loc.y + e.getY() - start[0].y);
                }
            }
        });
    }

    // ── Lógica de login ───────────────────────────────────────────────────────

    private void doLogin() {
        String email = txtEmail.getText().trim();
        String pass = new String(txtPassword.getPassword());

        if (email.isEmpty() || pass.isEmpty()) {
            lblError.setText("Correo y contraseña obligatorios");
            animateErrorLabel();
            return;
        }

        setLoading(true);
        lblError.setText("");

        authController.login(email, pass, rootPanel,
            () -> {
                setLoading(false);
                dispose();
                com.app.UI.Frame.MainFrame.getInstance().setVisible(true);
            },
            (msg, ex) -> {
                setLoading(false);
                lblError.setText(msg);
                animateErrorLabel();
            }
        );
    }

    private void setLoading(boolean loading) {
        btnLogin.setEnabled(!loading);
        btnLogin.setText(loading ? "Iniciando..." : "Iniciar Sesión");
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Componentes personalizados internos
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Campo de texto con bordes redondeados y borde azul al enfocar.
     */
    private static class RoundedField extends JTextField {
        private static final int R = 10;
        private boolean focused = false;

        RoundedField(String placeholder, boolean isPass) {
            setText(placeholder);
            setOpaque(false);
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setForeground(new Color(100, 110, 130));
            setBorder(new EmptyBorder(12, 14, 12, 14));
            setPreferredSize(new Dimension(340, 50));
            setMaximumSize  (new Dimension(Integer.MAX_VALUE, 50));

            addFocusListener(new java.awt.event.FocusAdapter() {
                public void focusGained(java.awt.event.FocusEvent e) {
                    if (getText().equals(placeholder)) { setText(""); setForeground(new Color(15, 25, 50)); }
                    focused = true; repaint();
                }
                public void focusLost(java.awt.event.FocusEvent e) {
                    if (getText().isEmpty()) { setText(placeholder); setForeground(new Color(100, 110, 130)); }
                    focused = false; repaint();
                }
            });
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(FIELD_BG);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), R*2, R*2);
            g2.setColor(focused ? BLUE_FIELD : new Color(210, 218, 230));
            g2.setStroke(new BasicStroke(focused ? 2f : 1.5f));
            g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, R*2, R*2);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Campo de contraseña con el mismo estilo redondeado. */
    private static class RoundedPassField extends JPasswordField {
        private static final int R = 10;
        private boolean focused = false;
        private final String placeholder;

        RoundedPassField(String placeholder) {
            this.placeholder = placeholder;
            setEchoChar('•');
            setOpaque(false);
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setBorder(new EmptyBorder(12, 14, 12, 14));
            setPreferredSize(new Dimension(340, 50));
            setMaximumSize  (new Dimension(Integer.MAX_VALUE, 50));

            addFocusListener(new java.awt.event.FocusAdapter() {
                public void focusGained(java.awt.event.FocusEvent e) { focused = true;  repaint(); }
                public void focusLost  (java.awt.event.FocusEvent e) { focused = false; repaint(); }
            });
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(FIELD_BG);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), R*2, R*2);
            g2.setColor(focused ? BLUE_FIELD : new Color(210, 218, 230));
            g2.setStroke(new BasicStroke(focused ? 2f : 1.5f));
            g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, R*2, R*2);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}