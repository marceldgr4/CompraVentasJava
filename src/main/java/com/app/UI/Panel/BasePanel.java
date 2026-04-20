package com.app.UI.Panel;

import javax.swing.*;
import java.awt.*;

public abstract class BasePanel extends JPanel {
    protected static final int BORDER_SIZE = 12;
    protected static final int INNIER_BORDER = 16;

    public BasePanel() {
        initComponents();
    }
    protected abstract void initComponents();

    protected void setDefaultsLayout() {
        setLayout(new BorderLayout(0,0));
        setBorder(BorderFactory.createEmptyBorder(BORDER_SIZE, INNIER_BORDER, BORDER_SIZE, INNIER_BORDER));
        setBackground(new Color(245, 247, 250));
    }
    protected void showSuccessMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Exito", JOptionPane.INFORMATION_MESSAGE);
    }
    protected void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    protected void showWarningMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Warning", JOptionPane.WARNING_MESSAGE);
    }
    protected void showInfoMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Info", JOptionPane.INFORMATION_MESSAGE);
        }
        protected boolean showConfirmation(String message, String title) {
        int result = JOptionPane.showConfirmDialog(this, message, title, JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        return result == JOptionPane.YES_OPTION;
        }
        protected  void clearTable(javax.swing.table.DefaultTableModel tableModel){
        tableModel.setRowCount(0);
        }
        protected void setLoading(String message) {
        }

        public abstract void refresh();

}
