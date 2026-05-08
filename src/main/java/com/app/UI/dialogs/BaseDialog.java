package com.app.UI.dialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Clase base para todos los diálogos de la aplicación.
 * Garantiza un estilo visual unificado: cabecera azul oscura, cuerpo blanco
 * con esquinas redondeadas y sombra exterior para distinguirse del fondo.
 */
public abstract class BaseDialog extends JDialog {

    // ── Paleta centralizada ──────────────────────────────────────────────────
    protected static final Color HEADER_BG    = new Color(18, 28, 58);
    protected static final Color HEADER_BTN   = new Color(100, 130, 200);
    protected static final Color BLUE_ACCENT  = new Color(30, 136, 229);
    protected static final Color FIELD_BG     = new Color(245, 247, 252);
    protected static final Color TEXT_DARK    = new Color(15, 25, 50);
    protected static final Color TEXT_MUTED   = new Color(110, 120, 140);
    protected static final Color BORDER_COLOR = new Color(210, 220, 235);
    protected static final Color SUCCESS_CLR  = new Color(56, 142, 60);
    protected static final Color DANGER_CLR   = new Color(211, 47, 47);
    protected static final Color WARNING_CLR  = new Color(245, 124, 0);
    protected static final Color SECTION_CLR  = new Color(30, 80, 160);

    // Fuentes
    protected static final Font FONT_TITLE   = new Font("Segoe UI Emoji", Font.BOLD, 15);
    protected static final Font FONT_LABEL   = new Font("Segoe UI", Font.BOLD, 12);
    protected static final Font FONT_FIELD   = new Font("Segoe UI", Font.PLAIN, 13);
    protected static final Font FONT_SECTION = new Font("Segoe UI Emoji", Font.BOLD, 13);
    protected static final Font FONT_SMALL   = new Font("Segoe UI", Font.ITALIC, 11);

    private final JPanel bodyContainer;
    private final JPanel footerContainer;
    protected boolean confirmed = false;

    /**
     * @param parent ventana padre
     * @param title  título mostrado en la cabecera
     * @param icon   emoji o texto del icono de cabecera
     */
    protected BaseDialog(Window parent, String title, String icon) {
        super(parent, ModalityType.APPLICATION_MODAL);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        // Panel raíz con sombra y esquinas redondeadas
        JPanel root = new ShadowPanel();
        root.setLayout(new BorderLayout());
        root.setBorder(new EmptyBorder(6, 7, 6, 7)); // espacio para la sombra

        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
            }
        };
        card.setOpaque(false);

        bodyContainer   = new JPanel(new BorderLayout());
        bodyContainer.setBackground(Color.WHITE);

        footerContainer = new JPanel(new BorderLayout());
        footerContainer.setBackground(Color.WHITE);

        card.add(buildHeader(icon, title), BorderLayout.NORTH);
        card.add(bodyContainer,            BorderLayout.CENTER);
        card.add(footerContainer,          BorderLayout.SOUTH);

        root.add(card, BorderLayout.CENTER);
        setContentPane(root);

        // ESC cierra el diálogo
        getRootPane().registerKeyboardAction(
                e -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    // ── Cabecera ─────────────────────────────────────────────────────────────

    private JPanel buildHeader(String icon, String title) {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(HEADER_BG);
                // Rellena esquinas superiores redondeadas, inferiores cuadradas
                g2.fillRoundRect(0, 0, getWidth(), getHeight() + 14, 14, 14);
                g2.fillRect(0, getHeight() / 2, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setPreferredSize(new Dimension(0, 58));
        header.setBorder(new EmptyBorder(0, 20, 0, 16));

        JLabel lblTitle = new JLabel(icon + "  " + title);
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(Color.WHITE);

        JButton btnClose = buildCloseButton();

        header.add(lblTitle,  BorderLayout.WEST);
        header.add(btnClose,  BorderLayout.EAST);
        return header;
    }

    private JButton buildCloseButton() {
        JButton btn = new JButton("✕") {
            @Override protected void paintComponent(Graphics g) {
                if (getModel().isRollover()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(new Color(255, 255, 255, 40));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(HEADER_BTN);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> onCancel());
        return btn;
    }

    // ── API para subclases ────────────────────────────────────────────────────

    /**
     * Establece el panel de contenido principal (cuerpo del diálogo).
     */
    protected void setContentBody(JComponent body) {
        bodyContainer.removeAll();
        bodyContainer.add(body, BorderLayout.CENTER);
    }

    /**
     * Establece el panel de pie de página (botones de acción).
     */
    protected void setFooter(JComponent footer) {
        footerContainer.removeAll();
        footerContainer.add(footer, BorderLayout.CENTER);
    }

    /**
     * Acción ejecutada al presionar ESC o el botón ✕.
     * Por defecto llama a {@link #dispose()}.
     */
    protected void onCancel() {
        dispose();
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    // ── Helpers de construcción ───────────────────────────────────────────────

    /** Crea un label de sección con línea inferior. */
    protected JLabel sectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_SECTION);
        lbl.setForeground(SECTION_CLR);
        lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        return lbl;
    }

    /** Crea un label de campo en negrita. */
    protected JLabel fieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(TEXT_DARK);
        return lbl;
    }

    /** Crea un campo de texto con estilo. */
    protected StyledField styledField(String placeholder) {
        return new StyledField(placeholder);
    }

    /** Crea un campo de contraseña con estilo. */
    protected StyledPasswordField styledPasswordField(String placeholder) {
        return new StyledPasswordField(placeholder);
    }

    /** Crea un combo con estilo. */
    protected <T> StyledCombo<T> styledCombo() {
        return new StyledCombo<>();
    }

    /** Crea un área de texto con estilo. */
    protected JTextArea styledTextArea(int rows) {
        JTextArea ta = new JTextArea(rows, 20);
        ta.setFont(FONT_FIELD);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(6, 8, 6, 8)
        ));
        return ta;
    }

    /** Panel footer estándar con botones alineados a la derecha. */
    protected JPanel buildStandardFooter(JButton... buttons) {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(225, 232, 245)));
        for (JButton btn : buttons) footer.add(btn);
        return footer;
    }

    /** Crea botón de acción principal (azul). */
    protected JButton buildPrimaryButton(String text) {
        return buildStyledButton(text, BLUE_ACCENT, Color.WHITE, true);
    }

    /** Crea botón de acción exitosa (verde). */
    protected JButton buildSuccessButton(String text) {
        return buildStyledButton(text, SUCCESS_CLR, Color.WHITE, true);
    }

    /** Crea botón de acción peligrosa (rojo). */
    protected JButton buildDangerButton(String text) {
        return buildStyledButton(text, DANGER_CLR, Color.WHITE, true);
    }

    /** Crea botón de cancelar (neutro). */
    protected JButton buildCancelButton() {
        return buildStyledButton("Cancelar", new Color(245, 248, 255), TEXT_MUTED, false);
    }

    private JButton buildStyledButton(String text, Color bg, Color fg, boolean filled) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color fill = filled
                        ? (getModel().isPressed() ? bg.darker() : getModel().isRollover() ? bg.brighter() : bg)
                        : (getModel().isRollover() ? new Color(230, 235, 248) : bg);
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                if (!filled) {
                    g2.setColor(BORDER_COLOR);
                    g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", filled ? Font.BOLD : Font.PLAIN, 13));
        btn.setForeground(fg);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(110, 38));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    protected void showValidationError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Validación", JOptionPane.WARNING_MESSAGE);
    }

    /** Helper para Insets. */
    protected Insets ins(int t, int l, int b, int r) {
        return new Insets(t, l, b, r);
    }

    // ── Componentes internos ──────────────────────────────────────────────────

    /**
     * Campo de texto estilizado con borde azul al enfocar.
     */
    public static class StyledField extends JTextField {
        private boolean focused = false;

        public StyledField(String placeholder) {
            setOpaque(false);
            setFont(FONT_FIELD);
            setForeground(TEXT_DARK);
            setBorder(new EmptyBorder(10, 12, 10, 12));
            setPreferredSize(new Dimension(200, 42));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
            putClientProperty("JTextField.placeholderText", placeholder);
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
            g2.setColor(focused ? BLUE_ACCENT : BORDER_COLOR);
            g2.setStroke(new BasicStroke(focused ? 2f : 1.5f));
            g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 10, 10);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Campo de contraseña estilizado.
     */
    public static class StyledPasswordField extends JPasswordField {
        private boolean focused = false;

        public StyledPasswordField(String placeholder) {
            setOpaque(false);
            setFont(FONT_FIELD);
            setForeground(TEXT_DARK);
            setBorder(new EmptyBorder(10, 12, 10, 12));
            setPreferredSize(new Dimension(200, 42));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
            putClientProperty("JTextField.placeholderText", placeholder);
            setEchoChar('●');
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
            g2.setColor(focused ? BLUE_ACCENT : BORDER_COLOR);
            g2.setStroke(new BasicStroke(focused ? 2f : 1.5f));
            g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 10, 10);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Combo estilizado.
     */
    public static class StyledCombo<T> extends JComboBox<T> {
        public StyledCombo() {
            setFont(FONT_FIELD);
            setBackground(FIELD_BG);
            setPreferredSize(new Dimension(200, 42));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        }
    }

    /**
     * Panel con efecto de sombra exterior para destacar el diálogo sobre el fondo blanco.
     */
    private static class ShadowPanel extends JPanel {
        private static final int SHADOW_SIZE = 8;
        private static final Color SHADOW_COLOR = new Color(0, 0, 0, 55);

        ShadowPanel() {
            setOpaque(false);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Sombra difuminada en capas
            for (int i = 0; i < SHADOW_SIZE; i++) {
                float alpha = 0.04f * (SHADOW_SIZE - i);
                g2.setColor(new Color(0, 0, 0, (int)(alpha * 255)));
                g2.fillRoundRect(
                        i + 2, i + 4,
                        getWidth()  - i * 2 - 4,
                        getHeight() - i * 2 - 2,
                        18, 18
                );
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
