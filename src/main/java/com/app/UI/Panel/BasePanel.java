package com.app.UI.Panel;

import com.app.UI.Components.ButtonFactory;
import com.app.UI.Components.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * BasePanel centraliza la lógica de UI común para todos los paneles de la aplicación.
 * Proporciona helpers para construir headers, buscadores y tablas con un estilo uniforme.
 */
public abstract class BasePanel extends JPanel {
    
    protected static final Color HEADER_FG = new Color(30, 42, 74);
    protected static final Color SUBTITLE_FG = new Color(120, 130, 155);

    public BasePanel() {
        setDefaultsLayout();
        initComponents();
    }

    protected abstract void initComponents();

    /** Configuración base del panel (padding, fondo). */
    protected void setDefaultsLayout() {
        setLayout(new BorderLayout(0, UIConstants.SPACING));
        setBorder(new EmptyBorder(20, 24, 20, 24));
        setBackground(UIConstants.BG_SECONDARY);
    }

    /** Construye un header profesional con título e ícono/subtítulo. */
    protected JPanel buildHeader(String title, String subtitle) {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);

        JPanel titleArea = new JPanel();
        titleArea.setOpaque(false);
        titleArea.setLayout(new BoxLayout(titleArea, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI Semibold", Font.BOLD, 22));
        lblTitle.setForeground(HEADER_FG);

        JLabel lblSub = new JLabel(subtitle);
        lblSub.setFont(UIConstants.FONT_SMALL);
        lblSub.setForeground(SUBTITLE_FG);

        titleArea.add(lblTitle);
        titleArea.add(Box.createVerticalStrut(4));
        titleArea.add(lblSub);

        header.add(titleArea, BorderLayout.WEST);
        return header;
    }

    /** Construye un panel de búsqueda estándar. */
    protected JPanel buildSearchPanel(String placeholder, JTextField searchField, ActionListener searchAction) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panel.setOpaque(false);

        searchField.setPreferredSize(new Dimension(220, 36));
        searchField.setFont(UIConstants.FONT_REGULAR);
        searchField.putClientProperty("JTextField.placeholderText", placeholder);
        searchField.addActionListener(searchAction);

        JButton btnSearch = ButtonFactory.createPrimaryButton("Buscar", "search");
        btnSearch.setPreferredSize(new Dimension(120, 36));
        btnSearch.addActionListener(searchAction);

        panel.add(searchField);
        panel.add(btnSearch);
        return panel;
    }

    /** Aplica un estilo moderno y uniforme a una JTable. */
    protected void styleTable(JTable table) {
        table.setFont(UIConstants.FONT_REGULAR);
        table.setRowHeight(32);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setFont(UIConstants.FONT_HEADER);
        table.getTableHeader().setBackground(Color.WHITE);
        table.getTableHeader().setForeground(HEADER_FG);
        table.setGridColor(new Color(230, 235, 240));
        table.setShowGrid(true);
        table.setFillsViewportHeight(true);
        table.setBorder(null);
    }

    /** Crea un JScrollPane con borde uniforme para la tabla. */
    protected JScrollPane createTableScroll(JTable table) {
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 220)));
        scroll.getViewport().setBackground(Color.WHITE);
        return scroll;
    }

    /** Helper para mostrar mensajes de confirmación. */
    protected boolean showConfirmation(String message, String title) {
        return JOptionPane.showConfirmDialog(this, message, title, 
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    protected void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    protected void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Advertencia", JOptionPane.WARNING_MESSAGE);
    }

    protected void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public abstract void refresh();
}
