package com.app.Model.domain;

import com.app.Model.Enum.RolUser;

public class Employee {
    private String id;
    private String email;
    private String fullName;
    private RolUser rol;
    private boolean active;

    public Employee() {}

    public Employee(String id, String fullName, RolUser rol, boolean active) {
        this.id = id;
        this.fullName = fullName;
        this.rol = rol;
        this.active = active;
    }

    public Employee(String id, String email, String fullName, RolUser rol, boolean active){
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.rol = rol;
        this.active = active;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public RolUser getRol() {
        return rol;
    }

    public void setRol(RolUser rol) {
        this.rol = rol;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
