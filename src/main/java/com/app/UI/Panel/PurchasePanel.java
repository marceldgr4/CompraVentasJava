package com.app.UI.Panel;

import Infrastructure.security.SessionManager;
import com.app.Model.domain.Purchase;
import com.app.Service.PurchaseService;
import com.app.UI.Components.ButtonFactory;
import com.app.UI.dialogs.PurchaseDialog;
import com.app.Utils.CurrencyUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;

public class PurchasePanel extends JPanel {
    private static final String[] COLUMNS_ADMIN ={"ID", "Empleado", "Cliente", "Artículo", "Precio Compra", "Precio Venta", "Fecha", "Notas"};
    private static final String[] COLUMNS_EMP    = {"ID", "Cliente", "Artículo", "Fecha", "Notas"};
    private static final DateTimeFormatter FMT   = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel lblStatus;

    private final PurchaseService purchaseService = new PurchaseService();
    private final boolean isAdmin = SessionManager.isAdmin();

    public PurchasePanel() {
        initComponets();
        loadData();
    }
    private void initComponets() {
        setLayout(new BorderLayout(0,0));
        setBorder(BorderFactory.createEmptyBorder(12,16,12, 16));
        setBackground(new Color(245, 247, 250));

        add(buildTooBar(),BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);
        add(buildStatusBar(),BorderLayout.SOUTH);
    }
    private JPanel buildTooBar() {
        JPanel bar = new JPanel(new BorderLayout(10,0));
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(0,0,12,0));

        JLabel lblTitulo = new JLabel("Historial de Compras");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setForeground(new Color(30, 42, 74));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));
        btnPanel.setOpaque(false);

        JButton btnNew = ButtonFactory.createSuccessButton("+ Registrar");
        JButton btnRefresh = ButtonFactory.createNeutralButton("Actualizar");
        btnNew.addActionListener(e -> openNewDialog());
        btnRefresh.addActionListener(e -> loadData());

        btnPanel.add(btnNew);
        btnPanel.add(btnRefresh);

        bar.add(lblTitulo,BorderLayout.WEST);
        bar.add(btnPanel,BorderLayout.EAST);
        return bar;
    }
    private JScrollPane buildTable() {
        String[] columns = isAdmin ? COLUMNS_ADMIN : COLUMNS_EMP;
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(28);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.setGridColor(new Color(230, 230, 230));
        table.setFillsViewportHeight(true);
        table.getColumnModel().getColumn(0).setPreferredWidth(55);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));
        return sp;
    }
    private JLabel buildStatusBar() {
        lblStatus = new JLabel("Cargando...");
        lblStatus.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblStatus.setForeground(Color.DARK_GRAY);
        lblStatus.setBorder(BorderFactory.createEmptyBorder(6,0,0,0));
        return lblStatus;
    }
    private void loadData() {
        lblStatus.setText("Cargando...");
        tableModel.setRowCount(0);
        new SwingWorker<List<Purchase>,Void>(){
            @Override protected List<Purchase> doInBackground() throws Exception{
                return purchaseService.getAll();
            }
            @Override protected void done() {
                try{
                    List<Purchase> list = get();
                    populateTable(list);
                    lblStatus.setText(list.size()+ "compra(s) encontrado(s)");
                }catch (ExecutionException ex){
                    lblStatus.setText("Error al cargar los datos"+ ex.getMessage());

                }catch (InterruptedException ex){
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }
    private void openNewDialog() {
        PurchaseDialog dialog = new PurchaseDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
        if(dialog.isConfirmed()) loadData();

    }
    private void populateTable(List<Purchase> list) {
        tableModel.setRowCount(0);
        for (Purchase p : list) {
            String fecha = p.getPurchaseDate() != null ? p.getPurchaseDate().format(FMT) : "N/A";
            String cliente = p.getClienteName() != null ? p.getClienteName() : "Sin Cliente";
            String articulo = p.getArticleName() != null ? p.getArticleName() : "N/A";
            if (isAdmin) {
                tableModel.addRow(new Object[]{
                        p.getId(),
                        p.getProfileName() != null ? p.getProfileName() : "N/A",
                        cliente, articulo,
                        CurrencyUtils.format(p.getPurchasePrice()), "Ver articulos",
                        fecha, p.getNotes() != null ? p.getNotes() : " "
                });
            } else {
                tableModel.addRow(new Object[]{
                        p.getId(), cliente, articulo, fecha, p.getNotes() != null ? p.getNotes() : " "
                });
            }
        }
    }
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    public void refresh() {
        loadData();
    }
}
