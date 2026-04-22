package com.app.Model.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Representa un artículo en el inventario del sistema de compraventa/empeño.
 */
public class Article {

    private int id;
    private int clienteId;
    private String nameArticle;
    private String description;
    private int amount;
    private BigDecimal price;
    private boolean sold;
    private LocalDateTime updatedAt;

    public Article(int id, String name, String desc, int amount, BigDecimal bigDecimal, boolean sold, Object o) {}

    /**
     * Constructor completo (usado al mapear desde la base de datos).
     */
    public Article(String nameArticle, String description,
                   int amount, BigDecimal price, boolean sold) {
        this.id          = id;
        this.clienteId   = clienteId;
        this.nameArticle = nameArticle;
        this.description = description;
        this.amount      = amount;
        this.price       = price;
        this.sold        = sold;
        this.updatedAt   = updatedAt;
    }

    /**
     * Constructor de creación (sin id ni timestamps).
     */
    public Article(int clienteId, String nameArticle, String description,
                   int amount, BigDecimal price, boolean sold) {
        this(nameArticle, description, amount, price, sold);
    }

    // ---- Getters / Setters ----------------------------------------

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    /** @deprecated Use {@link #getClienteId()} */
    @Deprecated
    public int getCliente_id() { return clienteId; }

    public int getClienteId() { return clienteId; }
    public void setClienteId(int clienteId) { this.clienteId = clienteId; }

    /** @deprecated Use {@link #setClienteId(int)} */
    @Deprecated
    public void setCliente_id(int clienteId) { this.clienteId = clienteId; }

    public String getNameArticle() { return nameArticle; }
    public void setNameArticle(String nameArticle) { this.nameArticle = nameArticle; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public boolean isSold() { return sold; }
    public void setSold(boolean sold) { this.sold = sold; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // ---- Lógica de negocio ----------------------------------------

    /** Retorna {@code true} si hay unidades disponibles en inventario. */
    public boolean hasStock() {
        return amount > 0;
    }

    /**
     * Retorna {@code true} si el artículo está marcado para venta Y aún tiene stock.
     * Nota: la lógica original tenía un error semántico (sold = ya fue vendido).
     */
    public boolean canSell() {
        return !sold && amount > 0;
    }

    @Override
    public String toString() {
        return nameArticle;
    }
}