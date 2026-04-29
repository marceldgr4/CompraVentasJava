package com.app.Service;

import Infrastructure.DataBase.ConnectionPool;
import Infrastructure.security.SessionManager;
import com.app.Model.Dao.SaleDao;
import com.app.Model.domain.Sale;
import com.app.Model.domain.SalesDetail;
import com.app.Service.exceptions.BusinessException;
import com.app.Service.exceptions.ServiceException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class SaleService {
    private final SaleDao saleDao = new SaleDao();
    private final ArticleService articleService = new ArticleService();

    public List<Sale> getAllSales() throws ServiceException {
        try{
            return saleDao.findAll();

        }catch(SQLException e){
            throw new ServiceException("Error al cargar todas las ventas: "+e.getMessage(),e);
        }
    }
    public Sale findById(int id) throws ServiceException{
        try{
            return saleDao.findById(id).orElseThrow(()-> new ServiceException("Venta "+id+" no encontrada"));
        }catch (SQLException e){
            throw new ServiceException("Venta "+id+" no encontrada",e);
        }
    }
    public List<Sale> findByCliente(int clienteId) throws ServiceException{
        try{
            return saleDao.findClientes(clienteId);
        }catch (SQLException e){
            throw new ServiceException("Error: cliente de la venta no encontrado: " + e.getMessage(),e);
        }
    }
    public List<Sale> findByProfile(String profile) throws ServiceException{
        try{
            return saleDao.findByProfile(profile);
        }catch (SQLException e){
            throw new ServiceException("Error: perfil de la venta no encontrado: " + e.getMessage(),e);
        }
    }
       public List<Sale> finByDateRange(LocalDate from, LocalDate to) throws ServiceException{
        if(from == null||to == null|| from.isAfter(to)){
            throw new BusinessException("Error en el rango de fechas de "+from+" a "+to);
        }
        try{
            return saleDao.findByDateRange(from,to);
        }catch (SQLException e){
            throw new ServiceException("Error al buscar ventas por fecha: "+e.getMessage(),e);
        }
    }
    // -------------------------------------------------------
    // CREATE — transaccional con validación de stock
    // -------------------------------------------------------
    public Sale create(Sale sale) throws ServiceException{
        validateSale(sale);
        if(sale.getProfileId() == null || sale.getProfileId().isEmpty()){
            sale.setProfileId(SessionManager.getProfileId());
        }
        if (sale.getSaleDate()==null){
            sale.setSaleDate(LocalDate.now().atStartOfDay());
        }

        for(SalesDetail detail : sale.getDetails()){
            articleService.removeStock(detail.getArticleId(),detail.getAmount());
        }
        try{
            return saleDao.save(sale);
        }catch (SQLException e){
            String msg = e.getMessage();
            if(msg != null && msg.contains("No existe el producto con el id"))
                throw new BusinessException("Stock insuficientes");
            throw new ServiceException("Error: no se pudo guardar la venta: "+ msg,e);
        }

    }

    public Sale save(Sale sale) throws SQLException{
        StringBuilder items = new StringBuilder("[");
        for(int i =0; i < sale.getDetails().size(); i++){
            SalesDetail d = sale.getDetails().get(i);
            if(i> 0) items.append(", ");
            items.append(String.format(
                    "{\"article_id\":%d,\"amount\":%d,\"unit_price\":%s}",
                    d.getArticleId(), d.getAmount(), d.getUnitPrice().toPlainString()));
        }
        items.append("]");

        String sql ="SELECT public.register_sale(?::uuid. ?, ?:: jsonb)";
        try(Connection con = ConnectionPool.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)){
            ps.setString(1, sale.getProfileId());
            ps.setInt(2,sale.getClienteId());
            ps.setString(3,items.toString());
            try(ResultSet rs = ps.executeQuery()){
                if(rs.next()) sale.setId(rs.getInt(1));
            }
        }
        return sale;
    }

    // -------------------------------------------------------
    // DELETE — solo Admin
    // -------------------------------------------------------

    public void delete(int id) throws ServiceException{
        requireAdmin("eliminar venta");
        try{
            boolean deletec = saleDao.delete(id);
            if(!deletec){
                throw new ServiceException("Venta "+id+" no encontrada");
            }
        }
        catch (SQLException e){
            throw new ServiceException("Error al eliminar la venta "+id+": "+e.getMessage(), e);
        }
    }
    private void requireAdmin(String action) throws ServiceException{
        if(!SessionManager.isAdmin()){
            throw new ServiceException("No es administrador. Solo el administrador tiene permiso para "+ action);
        }
    }
    private void validateSale(Sale sale) throws ServiceException{
        if(sale.getSaleDate()==null ||sale.getDetails().isEmpty() ){
            throw new BusinessException("La venta debe tener al menus un articulo");
        }
        if(sale.getClienteId()<=0){
            throw new BusinessException("La venta debe tener un Cliente valido");
        }
        for (SalesDetail detail : sale.getDetails()){
            if(detail.getArticleId()<=0){
                throw new BusinessException("La detalle  debe tener un Article valido");
            }
            if(detail.getAmount()<=0){
                throw new BusinessException("La cantinda de cada articulo debe ser mayor 0,");
            }
            if(detail.getUnitPrice() == null || detail.getUnitPrice().signum()<=0 ){
                throw new BusinessException("el precio de cada articulo debe ser mayor a $ 0");
            }
        }
    }
}
