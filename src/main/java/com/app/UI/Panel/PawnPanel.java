package com.app.UI.Panel;

import Infrastructure.security.SessionManager;
import com.app.Service.ArticleService;
import com.app.Service.ClienteService;
import com.app.Service.PawnService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;

/**
 * Panel para gestión de los artículos empeñados.
 * Incluye filtros por estado, operaciones CRUD y visualización de estados con colores.
 */

public class PawnPanel extends JPanel {
    private static final String[] COLUMNS = {
            "ID", " NOMBRE DEL CLIENTE", "NOMBRE DEL ARTICULO", "CANTIDAD", "PRECIO UNIT", "TOTAL", "FECHA INGRESO", "FECHA LIMITE", "ESTADO", "EMPLEADO QUE REGISTRO EL ARTICULO"
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

    private JButton btnMarkReturned;
    private JButton btnMarkExpired;

    private JLabel lblStatus;
    private JLabel lblTotal;

    private final PawnService pawnService = new PawnService();
    private final ArticleService articleService = new ArticleService();
    private final ClienteService clienteService = new ClienteService();

    public PawnPanel() {
        initComponents();
        ConfigurePermission();
        loadTable("Todos");
    }

    private void ConfigurePermission() {
    }

    private void initComponents() {
        setLayout(new BoxLayout(0, 0));
        setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        setBackground(new Color(245, 247, 250));

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
        JPanel topBar = new JPanel(new BorderLayout(10, 0));
        topBar.setOpaque(false);
        topBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        JPanel filterPanel = createFilterPanel();
        JPanel btnPanel = createButtonPanel();
        topBar.add(filterPanel, BorderLayout.WEST);
        topBar.add(btnPanel, BorderLayout.EAST);
        return topBar;
    }


    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panel.setOpaque(false);
        JLabel lblFilter = new JLabel("FILTRO");
        lblFilter.setFont(new Font("Tahoma", Font.BOLD, 12));
        cmbFilter = new JComboBox<>(new String[]{"Todos", "Activo", "Vencidos", "devuelto", "Expirados"});
        cmbFilter.setFont(new Font("Tahoma", Font.PLAIN, 12));
        cmbFilter.addActionListener(e -> loadTable((String) cmbFilter.getSelectedItem()));
        panel.add(lblFilter);
        panel.add(cmbFilter);
        return panel;
    }

    private void loadTable(String selectedItem) {
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panel.setOpaque(false);

        btnNew = createBtn("+ Nuevo Empeño", new Color(30, 136, 229));
        btnMarkReturned = createBtn("Marcar Devuelto", new Color(56, 142, 60));
        btnMarkExpired = createBtn("Marcar Expirado", new Color(245, 124, 0));
        btnProcessOverdue = createBtn("Procesar Vencidos", new Color(183, 28, 28));
        btnEdit = createBtn() "Editat", new Color(176, 174, 71)
        btnDelete = createBtn("Eliminar", new Color(211, 47, 47));
        btnRefresh = createBtn("↻ Actualizar", new Color(97, 97, 97));


        return panel;
    }
}
