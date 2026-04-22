package com.app.Model.domain;

import java.math.BigDecimal;

/**
 * Representa el detalle de una línea de venta (artículo incluido en una venta).
 */
public class SalesDetail {

    private int id;
    private int saleId;
    private int articleId;
    private int amount;
    private BigDecimal unitPrice;   // Precio unitario al momento de la venta

    public SalesDetail() {}

    /**
     * Constructor de creación (sin id).
     */
    public SalesDetail(int saleId, int articleId, int amount, BigDecimal unitPrice) {
        this.saleId    = saleId;
        this.articleId = articleId;
        this.amount    = amount;
        this.unitPrice = unitPrice;
    }

    /**
     * Constructor completo (desde base de datos).
     */
    public SalesDetail(int id, int saleId, int articleId, int amount, BigDecimal unitPrice) {
        this(saleId, articleId, amount, unitPrice);
        this.id = id;
    }

    // ---- Getters / Setters ----------------------------------------

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getSaleId() { return saleId; }
    public void setSaleId(int saleId) { this.saleId = saleId; }

    public int getArticleId() { return articleId; }
    public void setArticleId(int articleId) { this.articleId = articleId; }

    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    // ---- Lógica de negocio ----------------------------------------

    /**
     * Calcula el subtotal de esta línea (precio unitario × cantidad).
     *
     * @return subtotal, o {@link BigDecimal#ZERO} si el precio es null
     */
    public BigDecimal getSubtotal() {
        if (unitPrice == null) return BigDecimal.ZERO;
        return unitPrice.multiply(BigDecimal.valueOf(amount));
    }
}
