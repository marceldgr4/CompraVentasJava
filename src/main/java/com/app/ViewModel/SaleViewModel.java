package com.app.ViewModel;

import Infrastructure.security.SessionManager;
import com.app.Model.domain.Sale;
import com.app.Model.domain.SalesDetail;
import com.app.Service.SaleService;
import com.app.Service.exceptions.ServiceException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SaleViewModel extends BaseViewModel {
    // ---- Eventos que se notifican a los observers ----
    public static final String EVENT_SALES_LOADED    = "Sales_Loaded";
    public static final String EVENT_SALE_CREATED    = "Sale_Created";
    public static final String EVENT_SALE_DELETED    = "Sale_Deleted";
    public static final String EVENT_CART_UPDATED    = "Cart_Updated";
    public static final String EVENT_CART_CLEARED    = "Cart_Cleared";
    public static final String EVENT_ERROR           = "Sale_Error";

    private final SaleService saleService;

    private List<Sale> sales = new ArrayList<>();
    private final List<SalesDetail> cart = new ArrayList<>();
    public SaleViewModel(){
        this.saleService = new SaleService();
    }

    public SaleViewModel(SaleService saleService) {
        this.saleService = saleService;
    }
    public void loadAll() throws ServiceException {
        sales = saleService.getAllSales();
        notifyObservers(EVENT_SALES_LOADED,new ArrayList<>(sales));
    }
    public void loadByDateRange(LocalDate from, LocalDate to) throws ServiceException {
        sales = saleService.findByDateRange(from,to);
        notifyObservers(EVENT_SALES_LOADED,new ArrayList<>(sales));
    }
    public void loadByCliente(int clienteId) throws ServiceException {
        sales = saleService.findByCliente(clienteId);
        notifyObservers(EVENT_SALES_LOADED, new ArrayList<>(sales));
    }
    // -------------------------------------------------------
    // CARRITO — gestión del carrito temporal
    // -------------------------------------------------------

    public void addToCard(SalesDetail detail){
        cart.stream()
                .filter(d -> d.getArticleId() == detail.getArticleId())
                .findFirst()
                .ifPresentOrElse(
                        existing -> existing.setAmount(existing.getAmount()+ detail.getAmount()),
                                ()-> cart.add(detail));
                        notifyObservers(EVENT_CART_UPDATED, getCardTotal());
    }
    public BigDecimal getCardTotal(){
        return cart.stream()
                .map(SalesDetail::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    public void removeFromCart(int index){
        if(index >= 0 && index < cart.size()){
            cart.remove(index);
            notifyObservers(EVENT_CART_CLEARED, getCardTotal());
        }
    }
    public void clearCart(){
        cart.clear();
        notifyObservers(EVENT_CART_CLEARED, null);
    }

    // -------------------------------------------------------
    // CREATE — confirmar venta
    // -------------------------------------------------------

    public Sale confirmSale(int clineteId) throws ServiceException {
        if (cart.isEmpty()){
            throw new ServiceException("Cart is empty, add article ");
        }
        Sale sale = new Sale(
                SessionManager.getEmployeeId(),
                clineteId, LocalDateTime.now()
        );

        cart.forEach(sale::addDetail);
        Sale created = saleService.create(sale);
        sales.add(created);
        clearCart();
        notifyObservers(EVENT_SALE_CREATED, created);
        return created;
    }
    // -------------------------------------------------------
    // DELETE — solo Admin
    // -------------------------------------------------------

    public void deleteSale(int saleId) throws ServiceException {
        saleService.delete(saleId);
        sales.removeIf(sale -> sale.getId() == saleId);
        notifyObservers(EVENT_SALE_DELETED, saleId);
    }
    // -------------------------------------------------------
    // Accessors
    // -------------------------------------------------------

    /** Retorna una copia defensiva de la lista de ventas. */
    public List<Sale> getSales() {
        return new ArrayList<>(sales);
    }

    /** Retorna una copia defensiva del carrito actual. */
    public List<SalesDetail> getCart() {
        return new ArrayList<>(cart);
    }

    /** @return {@code true} si el carrito tiene al menos un artículo */
    public boolean isCartEmpty() {
        return cart.isEmpty();
    }
}
