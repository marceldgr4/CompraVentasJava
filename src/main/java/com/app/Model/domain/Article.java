package com.app.Model.domain;

import com.app.Model.Enum.ArticleCategory;
import com.app.Model.Enum.ItemState;
import com.app.Model.Enum.SourceType;

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

    //Nuevos
    private SourceType sourceType;
    private ItemState itemState;
    private BigDecimal purchasePrice; // Costo de adquisicion solo apra el admin

    /**
     * Constructor completo para uso general.
     */
    public Article( int clienteId, String nameArticle, String description,
                    int amount, BigDecimal price, ArticleCategory category) {

        this.clienteId = clienteId;
        this.nameArticle = nameArticle;
        this.description = description;
        this.amount = amount;
        this.price = price;
        this.category = category;
        //-------//
        this.sourceType = SourceType.OTRO;
        this.itemState = ItemState.Bueno;
    }

    // Constructor extendido para el módulo de Compras (HU-28)
    public Article(int clienteId, String nameArticle, String description,
                   int amount, BigDecimal price, ArticleCategory category,
                   SourceType sourceType, ItemState itemState, BigDecimal purchasePrice) {
        this(clienteId, nameArticle, description, amount, price, category);
        this.sourceType = sourceType != null ? sourceType : SourceType.OTRO;
        this.itemState = itemState != null ? itemState : ItemState.Bueno;
        this.purchasePrice = purchasePrice;
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

    public SourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }

    public ItemState getItemState() {
        return itemState;
    }

    public void setItemState(ItemState itemState) {
        this.itemState = itemState;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

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