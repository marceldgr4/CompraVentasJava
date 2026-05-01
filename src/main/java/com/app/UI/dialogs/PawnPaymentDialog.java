package com.app.UI.dialogs;

import com.app.Model.domain.Pawn;
import com.app.Service.PawnPaymentService;
import com.app.Service.exceptions.ServiceException;
import com.app.Utils.CurrencyUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.util.concurrent.ExecutionException;

/**
 * Diálogo para registrar un pago de cuota o una cuota impagada.
 * HU-16: Empleado o Admin registra pago exitoso.
 * HU-17: Solo Admin registra cuota impagada.
 */
public class PawnPaymentDialog extends JDialog {

    public enum Mode { PAYMENT, MISSED_INSTALLMENT }

    private static final Color HEADER_BG   = new Color(18, 28, 58);
    private static final Color BLUE_ACCENT = new Color(30, 136, 229);
    private static final Color WARNING_BG  = new Color(255, 243, 224);

    private final Pawn                pawn;
    private final Mode                mode;
    private final PawnPaymentService  paymentService = new PawnPaymentService();

    private JSpinner   spnAmount;
    private JTextArea  txtNotes;
    private JButton    btnConfirm;
    private boolean    confirmed = false;

    public PawnPaymentDialog(JFrame parent, Pawn pawn, Mode mode) {
        super(parent, true);
        this.pawn = pawn;
        this.mode = mode;
        setUndecorated(true);
        setSize(420, mode == Mode.PAYMENT ? 320 : 260);
        setLocationRelativeTo(parent);
        setContentPane(buildRoot());
    }

    private JPanel buildRoot() {
        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
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
                g2.fillRoundRect(0, 0, getWidth(), getHeight() + 12, 12, 12);
                g2.fillRect(0, getHeight() / 2, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setPreferredSize(new Dimension(0, 55));
        header.setBorder(new EmptyBorder(0, 16, 0, 16));

        String title = mode == Mode.PAYMENT
                ? "💳  Registrar Pago — Empeño #" + pawn.getId()
                : "⚠️  Registrar Cuota Impagada — Empeño #" + pawn.getId();
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI Emoji", Font.BOLD, 13));
        lbl.setForeground(Color.WHITE);

        JButton btnClose = new JButton("✕");
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnClose.setForeground(new Color(180, 200, 230));
        btnClose.setContentAreaFilled(false);
        btnClose.setBorderPainted(false);
        btnClose.setFocusPainted(false);
        btnClose.addActionListener(e -> dispose());

        header.add(lbl,      BorderLayout.WEST);
        header.add(btnClose, BorderLayout.EAST);
        return header;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(16, 20, 8, 20));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        gc.insets = new Insets(4, 0, 4, 0);
        int row = 0;

        // Info empeño
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 2;
        JLabel lblInfo = new JLabel(
                "<html>Artículo: <b>" + (pawn.getArticleName() != null ? pawn.getArticleName() : "N/A") + "</b> &nbsp;|&nbsp;" +
                        "Cuotas: <b>" + pawn.getInstallmentsPaid() + "/" + pawn.getInstallmentCount() + "</b></html>");
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblInfo.setForeground(new Color(80, 90, 120));
        body.add(lblInfo, gc); row++;

        if (mode == Mode.MISSED_INSTALLMENT) {
            gc.gridy = row;
            JLabel warn = new JLabel("<html><b>⚠ Advertencia:</b> Si las cuotas impagadas superan 4, el empeño pasará a estado PERDIDO.</html>");
            warn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            warn.setForeground(new Color(180, 100, 0));
            warn.setOpaque(true);
            warn.setBackground(WARNING_BG);
            warn.setBorder(new EmptyBorder(8, 8, 8, 8));
            body.add(warn, gc); row++;
        }

        if (mode == Mode.PAYMENT) {
            gc.gridy = row; gc.gridwidth = 1; gc.weightx = 0;
            body.add(label("Monto del pago: *"), gc);
            gc.gridx = 1; gc.weightx = 1;
            spnAmount = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 9_999_999.0, 1000.0));
            body.add(spnAmount, gc);
            gc.gridx = 0; gc.gridwidth = 2; gc.weightx = 1;
            row++;
        }

        gc.gridy = row;
        body.add(label("Notas (opcional):"), gc); row++;
        gc.gridy = row;
        txtNotes = new JTextArea(3, 20);
        txtNotes.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtNotes.setLineWrap(true);
        txtNotes.setWrapStyleWord(true);
        txtNotes.setBorder(BorderFactory.createLineBorder(new Color(210, 220, 235)));
        body.add(new JScrollPane(txtNotes), gc);

        return body;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(225, 232, 245)));

        JButton btnCancel = new JButton("Cancelar");
        btnCancel.addActionListener(e -> dispose());

        String confirmText = mode == Mode.PAYMENT ? "Registrar Pago" : "Registrar Impagada";
        btnConfirm = new JButton(confirmText);
        btnConfirm.setBackground(mode == Mode.PAYMENT ? BLUE_ACCENT : new Color(230, 81, 0));
        btnConfirm.setForeground(Color.WHITE);
        btnConfirm.setBorderPainted(false);
        btnConfirm.setFocusPainted(false);
        btnConfirm.addActionListener(e -> doConfirm());

        footer.add(btnCancel);
        footer.add(btnConfirm);
        return footer;
    }

    private void doConfirm() {
        btnConfirm.setEnabled(false);
        btnConfirm.setText("Procesando...");

        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                if (mode == Mode.PAYMENT) {
                    double val = (double) spnAmount.getValue();
                    if (val <= 0) throw new IllegalArgumentException("El monto debe ser mayor a $0.");
                    paymentService.registerPayment(pawn.getId(), BigDecimal.valueOf(val),
                            txtNotes.getText().trim());
                } else {
                    paymentService.registerMissedInstallment(pawn.getId(), txtNotes.getText().trim());
                }
                return null;
            }
            @Override protected void done() {
                try {
                    get();
                    confirmed = true;
                    dispose();
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    JOptionPane.showMessageDialog(PawnPaymentDialog.this,
                            cause.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    btnConfirm.setEnabled(true);
                    btnConfirm.setText(mode == Mode.PAYMENT ? "Registrar Pago" : "Registrar Impagada");
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    private JLabel label(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(15, 25, 50));
        return lbl;
    }

    public boolean isConfirmed() { return confirmed; }
}