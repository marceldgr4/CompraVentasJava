package com.app.Model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Pawn {
   private int id;
   private String profile_id;
    private int article_id;

    private String client;
    private int amount;


    private BigDecimal price;
    private LocalDate pawn_date;
    private LocalDate return_date;
    private boolean expired;
    private boolean returnd;
    private LocalDateTime updated_at;

    public Pawn() {}

    public Pawn(String profile_id, int article_id,
                String client, int amout,
                BigDecimal price, LocalDate pawn_date,
                LocalDate return_date, boolean expired,
                boolean returnd) {

        this.profile_id = profile_id;
        this.article_id = article_id;

        this.client = client;
        this.amount = amout;
        this.price = price;
        this.pawn_date = LocalDate.now();
        this.return_date = return_date;
        this.expired = false;
        this.returnd = false;

    }

    public Pawn(int id, String profile_id,
                int article_id, String client,
                int amount, BigDecimal price, LocalDate pawn_date,
                LocalDate return_date, boolean expired,
                boolean returnd, LocalDateTime updated_at)
    {
        this.id = id;
        this.profile_id = profile_id;
        this.article_id = article_id;
        this.client = client;
        this.amount = amount;
        this.price = price;
        this.pawn_date = pawn_date;
        this.return_date = return_date;
        this.expired = expired;
        this.returnd = returnd;
        this.updated_at = updated_at;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProfile_id() {
        return profile_id;
    }

    public void setProfile_id(String profile_id) {
        this.profile_id = profile_id;
    }

    public int getArticle_id() {
        return article_id;
    }

    public void setArticle_id(int article_id) {
        this.article_id = article_id;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
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

    public LocalDate getPawn_date() {
        return pawn_date;
    }

    public void setPawn_date(LocalDate pawn_date) {
        this.pawn_date = pawn_date;
    }

    public LocalDate getReturn_date() {
        return return_date;
    }

    public void setReturn_date(LocalDate return_date) {
        this.return_date = return_date;
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    public boolean isReturnd() {
        return returnd;
    }

    public void setReturnd(boolean returnd) {
        this.returnd = returnd;
    }

    public LocalDateTime getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(LocalDateTime updated_at) {
        this.updated_at = updated_at;
    }
}
