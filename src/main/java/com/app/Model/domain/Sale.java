package com.app.Model.domain;

import java.util.Date;

public class Sale {
    int id;
    int profile_id;
    int cliente_id;
    Date date;

    public Sale(){}

    public Sale(int id, int profile_id, int cliente_id, Date date) {
        this.id = id;
        this.profile_id = profile_id;
        this.cliente_id = cliente_id;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProfile_id() {
        return profile_id;
    }

    public void setProfile_id(int profile_id) {
        this.profile_id = profile_id;
    }

    public int getCliente_id() {
        return cliente_id;
    }

    public void setCliente_id(int cliente_id) {
        this.cliente_id = cliente_id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
