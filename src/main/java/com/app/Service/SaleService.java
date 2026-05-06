package com.app.Service;

import Infrastructure.DataBase.ConnectionPool;
import Infrastructure.security.SessionManager;
import com.app.Model.Dao.SaleDao;
import com.app.Model.domain.Sale;
import com.app.Model.domain.SalesDetail;
import com.app.Service.exceptions.BusinessException;
import com.app.Service.exceptions.ServiceException;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;

public class SaleService {

    private final SaleDao saleDao = new SaleDao();

    // ── READ ──────────────────────────────────────────────────────────────────

    public List<Sale> getAllSales() throws ServiceException {
        try {
            return saleDao.findAll();
        } catch (SQLException e) {
            throw new ServiceException("Error al cargar ventas: " + e.getMessage(), e);
        }
    }

    public Sale findById(int id) throws ServiceException {
        try {
            return saleDao.findById(id)
                    .orElseThrow(() -> new ServiceException("Venta " + id + " no encontrada."));
        } catch (SQLException e) {
            throw new ServiceException("Error al buscar venta: " + e.getMessage(), e);
        }
    }

    public List<Sale> findByCliente(int clienteId) throws ServiceException {
        try {
            return saleDao.findClientes(clienteId);
        } catch (SQLException e) {
            throw new ServiceException("Error al buscar ventas del cliente: " + e.getMessage(), e);
        }
    }

    public List<Sale> findByProfile(String profileId) throws ServiceException {
        try {
            return saleDao.findByProfile(profileId);
        } catch (SQLException e) {
            throw new ServiceException("Error al buscar ventas del empleado: " + e.getMessage(), e);
        }
    }

    public List<Sale> findByDateRange(LocalDate from, LocalDate to) throws ServiceException {
        if (from == null || to == null || from.isAfter(to)) {
            throw new BusinessException("El rango de fechas es inválido: de " + from + " a " + to + ".");
        }
        try {
            return saleDao.findByDateRange(from, to);
        } catch (SQLException e) {
            throw new ServiceException("Error al buscar ventas por fecha: " + e.getMessage(), e);
        }
    }

    // ── CREATE ────────────────────────────────────────────────────────────────

    /**
     * Registra una venta de forma atómica usando {@code register_sale()}.
     */
    public Sale create(Sale sale) throws ServiceException {
        validateSale(sale);

        if (sale.getProfileId() == null || sale.getProfileId().isEmpty()) {
            sale.setProfileId(SessionManager.getProfileId());
        }
        if (sale.getSaleDate() == null) {
            sale.setSaleDate(LocalDate.now().atStartOfDay());
        }

        try {
            return saveViaStoredProcedure(sale);
        } catch (SQLException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "";
            if (msg.contains("CV001") || msg.contains("Stock insuficiente") || msg.contains("stock")) {
                throw new BusinessException("Stock insuficiente para uno o más artículos.");
            }
            if (msg.contains("CV002") || msg.contains("no existe")) {
                throw new BusinessException("Uno de los artículos ya no existe en el sistema.");
            }
            throw new ServiceException("Error al guardar la venta: " + msg, e);
        }
    }


    private Sale saveViaStoredProcedure(Sale sale) throws SQLException {
        StringBuilder items = new StringBuilder("[");
        List<SalesDetail> details = sale.getDetails();
        for (int i = 0; i < details.size(); i++) {
            SalesDetail d = details.get(i);
            if (i > 0) items.append(",");
            items.append(String.format(
                    "{\"article_id\":%d,\"amount\":%d,\"unit_price\":%s}",
                    d.getArticleId(), d.getAmount(), d.getUnitPrice().toPlainString()));
        }
        items.append("]");

        String sql = "SELECT public.register_sale(?::uuid, ?, ?, ?::jsonb)";
        try (Connection con = ConnectionPool.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, sale.getProfileId());


            if (sale.getClienteId() > 0) {
                ps.setInt(2, sale.getClienteId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }

            ps.setString(3, sale.getClienteNombreAnon());
            ps.setString(4, items.toString());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) sale.setId(rs.getInt(1));
            }
        }
        return sale;
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    public void delete(int id) throws ServiceException {
        requireAdmin("eliminar venta");
        try {
            boolean deleted = saleDao.delete(id);
            if (!deleted) throw new ServiceException("Venta " + id + " no encontrada.");
        } catch (SQLException e) {
            throw new ServiceException("Error al eliminar la venta " + id + ": " + e.getMessage(), e);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void requireAdmin(String action) throws ServiceException {
        if (!SessionManager.isAdmin()) {
            throw new ServiceException("Solo el administrador puede: " + action + ".");
        }
    }


    private void validateSale(Sale sale) {
        if (sale.getDetails() == null || sale.getDetails().isEmpty()) {
            throw new BusinessException("La venta debe tener al menos un artículo.");
        }
        for (SalesDetail detail : sale.getDetails()) {
            if (detail.getArticleId() <= 0) {
                throw new BusinessException("Cada detalle debe referenciar un artículo válido.");
            }
            if (detail.getAmount() <= 0) {
                throw new BusinessException("La cantidad de cada artículo debe ser mayor a 0.");
            }
            if (detail.getUnitPrice() == null || detail.getUnitPrice().signum() <= 0) {
                throw new BusinessException("El precio de cada artículo debe ser mayor a $0.");
            }
        }
    }
}