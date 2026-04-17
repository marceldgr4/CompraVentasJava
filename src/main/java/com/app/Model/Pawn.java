package com.app.Model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Pawn {
    private int id;
    private String profile_id;
    private int article_id;
    private int cliente_id;
    private int amount;
    private BigDecimal price;
    private LocalDate pawn_date;
    private LocalDate return_date;
    private boolean expired;
    private boolean returned;
    private LocalDateTime updated_at;

    // Campos adicionales para los JOINs desde la Base de Datos
    private String profile_name;
    private String article_name;
    private String cliente_name;

    public Pawn() {}

    // Constructor general sin ID ni timestamps (para crear)
    public Pawn(String profile_id, int article_id, int cliente_id, int amount,
                BigDecimal price, LocalDate pawn_date, LocalDate return_date, 
                boolean expired, boolean returned) {
        this.profile_id = profile_id;
        this.article_id = article_id;
        this.cliente_id = cliente_id;
        this.amount = amount;
        this.price = price;
        this.pawn_date = pawn_date;
        this.return_date = return_date;
        this.expired = expired;
        this.returned = returned;
    }

    // Constructor completo mapeado desde la base de datos
    public Pawn(int id, String profile_id, int article_id, int cliente_id,
                int amount, BigDecimal price, LocalDate pawn_date,
                LocalDate return_date, boolean expired, boolean returned, 
                LocalDateTime updated_at) {
        this.id = id;
        this.profile_id = profile_id;
        this.article_id = article_id;
        this.cliente_id = cliente_id;
        this.amount = amount;
        this.price = price;
        this.pawn_date = pawn_date;
        this.return_date = return_date;
        this.expired = expired;
        this.returned = returned;
        this.updated_at = updated_at;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getProfile_id() { return profile_id; }
    public void setProfile_id(String profile_id) { this.profile_id = profile_id; }

    public int getArticle_id() { return article_id; }
    public void setArticle_id(int article_id) { this.article_id = article_id; }

    public int getCliente_id() { return cliente_id; }
    public void setCliente_id(int cliente_id) { this.cliente_id = cliente_id; }

    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public LocalDate getPawn_date() { return pawn_date; }
    public void setPawn_date(LocalDate pawn_date) { this.pawn_date = pawn_date; }

    public LocalDate getReturn_date() { return return_date; }
    public void setReturn_date(LocalDate return_date) { this.return_date = return_date; }

    public boolean isExpired() { return expired; }
    public void setExpired(boolean expired) { this.expired = expired; }

    public boolean isReturned() { return returned; }
    public void setReturned(boolean returned) { this.returned = returned; }

    public LocalDateTime getUpdated_at() { return updated_at; }
    public void setUpdated_at(LocalDateTime updated_at) { this.updated_at = updated_at; }

    public String getProfile_name() { return profile_name; }
    public void setProfile_name(String profile_name) { this.profile_name = profile_name; }

    public String getArticle_name() { return article_name; }
    public void setArticle_name(String article_name) { this.article_name = article_name; }

    public String getCliente_name() { return cliente_name; }
    public void setCliente_name(String cliente_name) { this.cliente_name = cliente_name; }


    public boolean isActive(){
        return !expired && !returned;
    }

    public BigDecimal getTotal() {
        return price.multiply(BigDecimal.valueOf(amount));
    }
    public String getStatus() {
        if(returned) return "Returned";
        if (expired) return "Expired";
        if (isOverdue()) return "Overdue";
        return "activo";
    }

    private boolean isOverdue() {
        return LocalDate.now().isAfter(return_date) && !returned;
    }
}
