package com.app.Model.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Article {

    private int id;
    private int cliente_id;
    private String nameArticle;
    private String description;
    private int amount;
    private BigDecimal price;
    private boolean sold;
    private LocalDateTime updatedAt;

    public Article(String trim, String trimmed, int i, BigDecimal bigDecimal, boolean selected) {
    }

    public Article(int id, int cliente_id, String nameArticle, String description, int amount, BigDecimal price, boolean sold, LocalDateTime updatedAt) {
        this.id = id;
        this.cliente_id = cliente_id;
        this.nameArticle = nameArticle;
        this.description = description;
        this.amount = amount;
        this.price = price;
        this.sold = sold;
        this.updatedAt = updatedAt;
    }

    public Article(int id, String nameArticle, String description, int amount, BigDecimal price, boolean sold, LocalDateTime updatedAt) {
        this(0, 0, nameArticle, description, amount, price, sold, null);
    }

    public Article(int cliente_id, String nameArticle, String description, int amount,
                   BigDecimal price, boolean sold) {
        this(0, cliente_id, nameArticle, description, amount, price, sold, null);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCliente_id() {
        return cliente_id;
    }

    public void setCliente_id(int cliente_id) {
        this.cliente_id = cliente_id;
    }

    public String getNameArticle() {
        return nameArticle;
    }

    public void setNameArticle(String nameArticle) {
        this.nameArticle = nameArticle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public boolean isSold() {
        return sold;
    }

    public void setSold(boolean sold) {
        this.sold = sold;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean hasStock() {
        return amount > 0;
    }
    public  boolean canSell(){
        return sold && amount > 0;
    }
    @Override
    public String toString() {
        return nameArticle;
    }
}