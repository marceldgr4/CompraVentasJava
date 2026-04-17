package com.app.View;

import com.app.Service.PawnService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;

import static com.sun.java.swing.ui.CommonUI.createButton;
import static sun.net.www.MimeTable.loadTable;

/**
 * Panel para gestión de los artículos empeñados.
 * Incluye filtros por estado, operaciones CRUD y visualización de estados con colores.
 */

public class PawnPanel extends JPanel {
    private static final String[] COLUMNS ={
            "ID", " NOMBRE DEL CLIENTE", "NOMBRE DEL ARTICULO", "CANTIDAD", "PRECIO UNIT", "TOTAL", "FECHA INGRESO", "FECHA LIMITE","ESTADO","EMPLEADO QUE REGISTRO EL ARTICULO"
    };
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<String> cmbFilter;
    private JButton btnNew;
    private JButton btnEdit;
    private JButton btnDelete;
    private JButton btnExpire;
    private JButton btnRefresh;
    private JButton btnProcessOverdue;
    private JButton btnReturn;

    private JLabel lblStatus;
    private JLabel lblTotal;

    private final PawnService pawnService = new PawnService();

    public PawnPanel() {
        initComponents();
        ConfigurePermission();
        loadTable();
    }
    private void initComponents() {
        setLayout(new BoxLayout(0,0));
        setBorder(BorderFactory.createEmptyBorder(12,16,12,16));
        setBackground(new Color(245,247,250));

        JPanel topBar = createTopBar();
        JScrollPane scrollPane = createTable();
        JPanel bottomBar = createBottomBar();

        add(topBar, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomBar, BorderLayout.SOUTH);

    }
    /**
     * Crea la barra superior con filtros y botones.
     */
    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout(10,0));
        topBar.setOpaque(false);
        topBar.setBorder(BorderFactory.createEmptyBorder(0,0,12,0));

        JPanel filterPanel = createFilterPanel();
        JPanel btnPanel = createButtonPanel();
        topBar.add(filterPanel, BorderLayout.WEST);
        topBar.add(btnPanel, BorderLayout.EAST);
        return topBar;
    }



    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        panel.setOpaque(false);
        JLabel lblFilter = new JLabel("FILTRO");
        lblFilter.setFont(new Font("Tahoma", Font.BOLD, 12));
        cmbFilter = new JComboBox<>(new String[] {"Todos","Activo","Vencidos", "devuelto", "Expirados"});
        cmbFilter.setFont(new Font("Tahoma", Font.PLAIN, 12));
        cmbFilter.addActionListener(e -> applyFilter());
        panel.add(lblFilter);
        panel.add(cmbFilter);
        return panel;
    }
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        panel.setOpaque(false);
        btnNew = createButton("+ New Pawn", new Color(30,163,229));
        btnNew.addActionListener(e -> openNewDialog());


    }

    private void ConfigurePermission() {

    }


}
