package com.app.Service;

import Infrastructure.security.SessionManager;
import com.app.Model.Dao.SaleDao;
import com.app.Model.domain.Sale;
import com.app.Model.domain.SalesDetail;
import com.app.Service.exceptions.BusinessException;
import com.app.Service.exceptions.ServiceException;

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
            throw new ServiceException("Error loading all Sale"+e.getMessage(),e);
        }
    }
    public Sale findById(int id) throws ServiceException{
        try{
            return saleDao.findById(id).orElseThrow(()-> new ServiceException("Sale"+id+" not found"));
        }catch (SQLException e){
            throw new ServiceException("Sale"+id+" not found",e);
        }
    }
    public List<Sale> findByCliente(int clienteId) throws ServiceException{
        try{
            return saleDao.findClientes(clienteId);
        }catch (SQLException e){
            throw new ServiceException("Error the Sale not found cliente" + e.getMessage(),e);
        }
    }
    public List<Sale> findByProfile(String profile) throws ServiceException{
        try{
            return saleDao.findByProfile(profile);
        }catch (SQLException e){
            throw new ServiceException("Error the Sale not found profile" + e.getMessage(),e);
        }
    }
       public List<Sale> finByDateRange(LocalDate from, LocalDate to) throws ServiceException{
        if(from == null||to == null|| from.isAfter(to)){
            throw new BusinessException("Error the range from "+from+" to "+to);
        }
        try{
            return saleDao.findByDateRange(from,to);
        }catch (SQLException e){
            throw new ServiceException("Error the Sale not found date"+e.getMessage(),e);
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
            for (SalesDetail detail: sale.getDetails()){
                try{
                    articleService.addStock(detail.getArticleId(),detail.getAmount());
                } catch (ServiceException ignored){}
            }
            throw new ServiceException("Error the persister a Sale not found"+e.getMessage(),e);
        }
    }

    // -------------------------------------------------------
    // DELETE — solo Admin
    // -------------------------------------------------------

    public void delete(int id) throws ServiceException{
        requireAdmin("delect Sale");
        try{
            boolean deletec = saleDao.delete(id);
            if(!deletec){
                throw new ServiceException("Sale"+id+" not found");
            }
        }
        catch (SQLException e){
            throw new ServiceException("Erro the deletec Sale"+id+" not found"+e.getMessage(), e);
        }
    }
    private void requireAdmin(String action) throws ServiceException{
        if(!SessionManager.isAdmin()){
            throw new ServiceException("You are not admin, Only administrator hava "+ action);
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
