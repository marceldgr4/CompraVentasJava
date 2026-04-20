package com.app.Model.domain;

public class SalesDetail {
    int id;
    int sale_id;
    int article_id;
    int amount;
    int unit_price;

    public SalesDetail(){}

    public SalesDetail(int id, int sale_id, int article_id, int amount, int unit_price) {
        this.id = id;
        this.sale_id = sale_id;
        this.article_id = article_id;
        this.amount = amount;
        this.unit_price = unit_price;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSale_id() {
        return sale_id;
    }

    public void setSale_id(int sale_id) {
        this.sale_id = sale_id;
    }

    public int getArticle_id() {
        return article_id;
    }

    public void setArticle_id(int article_id) {
        this.article_id = article_id;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getUnit_price() {
        return unit_price;
    }

    public void setUnit_price(int unit_price) {
        this.unit_price = unit_price;
    }
}
