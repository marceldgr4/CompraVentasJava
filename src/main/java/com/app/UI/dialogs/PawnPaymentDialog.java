package com.app.UI.dialogs;

import com.app.Model.domain.Pawn;
import com.app.Service.PawnPaymentService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.util.concurrent.ExecutionException;

/**
 * Diálogo para registrar un pago de cuota o una cuota impagada.
 */
public class PawnPaymentDialog extends BaseDialog {

    public enum Mode { PAYMENT, MISSED_INSTALLMENT }

    private final Pawn pawn;
    private final Mode mode;
    private final PawnPaymentService paymentService = new PawnPaymentService();

    private JSpinner spnAmount;
    private JTextArea txtNotes;
    private JButton btnConfirm;
    private boolean confirmed = false;

    public PawnPaymentDialog(JFrame parent, Pawn pawn, Mode mode) {
        super(parent, mode == Mode.PAYMENT ? "Registrar Pago" : "Cuota Impagada", mode == Mode.PAYMENT ? "💳" : "⚠️");
        this.pawn = pawn;
        this.mode = mode;
        setSize(420, mode == Mode.PAYMENT ? 350 : 280);
        setLocationRelativeTo(parent);
        
        setContentBody(buildBody());
        setFooter(buildFooter());
    }

    private JPanel buildBody() {
        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(16, 20, 8, 20));
        
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        gc.insets = ins(4, 0, 4, 0);
        int row = 0;

        // Info empeño
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 2;
        JLabel lblInfo = new JLabel(
                "<html>Artículo: <b>" + (pawn.getArticleName() != null ? pawn.getArticleName() : "N/A") + "</b> &nbsp;|&nbsp;" +
                        "Cuotas: <b>" + pawn.getInstallmentsPaid() + "/" + pawn.getInstallmentCount() + "</b></html>");
        lblInfo.setFont(FONT_FIELD);
        lblInfo.setForeground(TEXT_MUTED);
        body.add(lblInfo, gc); row++;

        if (mode == Mode.MISSED_INSTALLMENT) {
            gc.gridy = row;
            JLabel warn = new JLabel("<html><b>⚠ Advertencia:</b> Si las cuotas impagadas superan 4, el empeño pasará a estado PERDIDO.</html>");
            warn.setFont(FONT_SMALL);
            warn.setForeground(WARNING_CLR);
            warn.setOpaque(true);
            warn.setBackground(new Color(255, 243, 224));
            warn.setBorder(new EmptyBorder(8, 8, 8, 8));
            body.add(warn, gc); row++;
        }

        if (mode == Mode.PAYMENT) {
            gc.gridy = row; gc.gridwidth = 1; gc.weightx = 0;
            body.add(fieldLabel("Monto del pago: *"), gc);
            gc.gridx = 1; gc.weightx = 1;
            spnAmount = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 9_999_999.0, 1000.0));
            body.add(spnAmount, gc);
            gc.gridx = 0; gc.gridwidth = 2; gc.weightx = 1;
            row++;
        }

        gc.gridy = row;
        body.add(fieldLabel("Notas (opcional):"), gc); row++;
        gc.gridy = row;
        txtNotes = styledTextArea(3);
        body.add(new JScrollPane(txtNotes), gc);

        return body;
    }

    private JPanel buildFooter() {
        JButton btnCancel = buildCancelButton();
        String confirmText = mode == Mode.PAYMENT ? "Registrar Pago" : "Registrar Impagada";
        btnConfirm = mode == Mode.PAYMENT ? buildPrimaryButton(confirmText) : buildDangerButton(confirmText);
        
        btnCancel.addActionListener(e -> onCancel());
        btnConfirm.addActionListener(e -> doConfirm());
        
        return buildStandardFooter(btnCancel, btnConfirm);
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
                    showValidationError(cause.getMessage());
                    btnConfirm.setEnabled(true);
                    btnConfirm.setText(mode == Mode.PAYMENT ? "Registrar Pago" : "Registrar Impagada");
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    public boolean isConfirmed() { return confirmed; }
}