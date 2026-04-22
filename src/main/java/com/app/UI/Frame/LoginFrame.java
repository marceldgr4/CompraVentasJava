package com.app.UI.Frame;

import com.app.UI.Components.ModernButton;
import com.app.UI.Components.ModernPasswordField;
import com.app.UI.Components.ModernTextField;
import com.app.Service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.concurrent.ExecutionException;


public class LoginFrame extends JFrame {
    private  static final Logger log = LoggerFactory.getLogger(LoginFrame.class);

    private ModernTextField txtEmail;
    private ModernPasswordField txtPassword;
    private ModernButton btnLogin;
    private JButton btnTogglePassword;
    private JLabel lblError;
    private boolean passwordVisible = false;

    private final AuthService authService = new AuthService();

    public LoginFrame() {
        initComponents();
        setupListeners();
    }
/*
* Diseño del login UI
* */
    private void initComponents() {
        setTitle("CompraVenta — Iniciar sesión");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(450, 650);
        setLocationRelativeTo(null);
        setResizable(false);
        setContentPane(buildMainPanel());
    }
        // Panel Principal con Gradiente Moderno
        private JPanel buildMainPanel() {
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(245, 247, 250), 0, getHeight(), new Color(210, 220, 235));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(50, 60, 50, 60));

        // Título
        JLabel lblTitle = new JLabel("COMPRA VENTA");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 30));
        lblTitle.setForeground(new Color(44, 62, 80));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSubtitle = new JLabel("Gestiona tus ventas fácilmente");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubtitle.setForeground(new Color(127, 140, 141));
        lblSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Inputs
        txtEmail = new ModernTextField("Correo electrónico");
        txtPassword = new ModernPasswordField("Contraseña");
        // Botón
        btnLogin = new ModernButton("Entrar");

        lblError = buildErroLabel();
        /*lblError.setForeground(new Color(231, 76, 60));
        lblError.setAlignmentX(Component.CENTER_ALIGNMENT);*/

        // Agregar al panel con espaciado
        mainPanel.add(lblTitle);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(lblSubtitle);
        mainPanel.add(Box.createVerticalStrut(50));
        mainPanel.add(txtEmail);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(buildPaswordRow());

        mainPanel.add(Box.createVerticalStrut(40));
        mainPanel.add(btnLogin);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(lblError);
        return mainPanel;

       //setContentPane(mainPanel);
    }

    private JPanel buildPaswordRow() {
        JPanel container = new JPanel(null);
        container.setOpaque(false);
        container.setPreferredSize(new Dimension(300, 45));
        container.setMaximumSize(new Dimension(300, 45));
        container.setAlignmentX(Component.CENTER_ALIGNMENT);

        txtPassword = new ModernPasswordField("Contraseña");
        txtPassword.setBounds(0,0,300,45);

        btnTogglePassword = buildToggleButtom();
        btnTogglePassword.setBounds(262,10,30,26);
        container.add(btnTogglePassword);
        container.add(txtPassword);
        return container;
    }

    private JButton buildToggleButtom() {
        JButton btn = new JButton("👁");
        btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setToolTipText("Mostar contraseña");
        return btn;
    }
    private JLabel buildErroLabel() {
        JLabel lblError = new JLabel(" ");
        lblError.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblError.setForeground(new Color(231, 76, 60));
        lblError.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblError.setHorizontalAlignment(SwingConstants.CENTER);
        lblError.setPreferredSize(new Dimension(300, 45));
        return lblError;
    }


       private void setupListeners() {
        btnLogin.addActionListener(e -> doLogin());
        txtPassword.addActionListener(e -> doLogin());
        btnTogglePassword.addActionListener(e -> togglePasswordVisible());
    }
    /*
    * Accion
    * */
    private void togglePasswordVisible() {
        passwordVisible = !passwordVisible;
        if (passwordVisible) {
            txtPassword.setEchoChar((char) 0);
            btnTogglePassword.setText(" 🙈");
            btnTogglePassword.setToolTipText("Ocultar password");
            log.debug("password field set to Visible");
        }else{
            txtPassword.setEchoChar('*');
            btnTogglePassword.setText("👁");
            btnTogglePassword.setToolTipText("Mostrar password");
            log.debug("Passwrod field set to Hidden");
        }
    }

    private void doLogin() {
        String email = txtEmail.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            lblError.setText("Por favor, completa los campos.");
            return;
        }
        lblError.setText("");
        btnLogin.setEnabled(false);
        btnLogin.setText("iniciando...");

        SwingWorker<Void,Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                authService.Login(email,password);
                return null;
            }
            @Override
            protected void done() {
                try{
                    get();
                    log.info("Login OK - completed, email={}.", email);
                    dispose();
                    SwingUtilities.invokeLater(()->new MainFrame().setVisible(true));
              }catch (ExecutionException ex){
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    String userMsg = extractUserMessage(cause);
                    log.error("Login FAILED - email={} | cause={} | msg={}",
                            email, cause.getClass().getSimpleName(), cause.getMessage(), cause);
                   showError(userMsg);
                   setFormEnable(true);
                } catch (InterruptedException ex){
                    Thread.currentThread().interrupt();
                    log.error("Login interrupte- email={}",email, ex);
                    showError("la operacion fue interrumpida");
                    setFormEnable(true);
                }
            }
        };
        worker.execute();
    }
    private String extractUserMessage(Throwable cause) {
        String raw = cause.getMessage();
        if (raw == null || raw.isBlank()) {
            return "Erro desconhecido. verificar tu conexion. ";
        }
        int colon = raw.indexOf(":");
        if (colon > 0) {
            String prefix = raw.substring(0, colon);
            if (!prefix.endsWith(" ")) {
                return raw.substring(colon + 1).trim();
            }
        }
        return raw;
    }
    private void showError(String message) {
        lblError.setText("<html><center>" + message + "</center></html>");
    }
    private void clearError() {
        lblError.setText(" ");
    }
    private void setFormEnable(boolean enabled) {
        btnLogin.setEnabled(enabled);
        btnLogin.setText(enabled ? "Login" : "Login");
        txtEmail.setEnabled(enabled);
        txtPassword.setEnabled(enabled);
        btnTogglePassword.setEnabled(enabled);

    }
}