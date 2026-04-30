package com.app.Model.domain;

import com.app.Model.Enum.ArticleCategory;

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

    private ArticleCategory category;

    private int amount;
    private BigDecimal price;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Constructor completo para uso general.
     */
    public Article( String nameArticle, String description,
                    int amount, BigDecimal price) {

        this.clienteId = clienteId;
        this.nameArticle = nameArticle;
        this.description = description;
        this.amount = amount;
        this.category = category;
        this.price = price;
    }



    // ---- Getters / Setters ----------------------------------------


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

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

    public ArticleCategory getCategory() {
        return category;
    }

    public void setCategory(ArticleCategory category) {
        this.category = category;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // ---- Lógica de negocio ----------------------------------------

    /** Retorna {@code true} si hay unidades disponibles en inventario. */
    public boolean hasStock() {
        return amount > 0;
    }

    public String getStockStatus() {
        return hasStock() ? "Disponible" : "Sin Stock";
    }

    public boolean requireWeigthForPawn(){
        return category != null && category.requiresWeight();
    }

    @Override
    public String toString() {
        return nameArticle;
    }
}