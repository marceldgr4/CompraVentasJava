package com.app.Model.domain;

import com.app.Model.Enum.ClienteStatus;

import java.sql.Timestamp;

/**
 * Representa un cliente del sistema de compraventa/empeño.
 */
public class Cliente {

    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private ClienteStatus status;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Cliente() {}

    /**
     * Constructor completo (usado al mapear desde la base de datos).
     */
    public Cliente(int id, String firstName, String lastName,
                   String email, String phone, ClienteStatus status, Timestamp createdAt, Timestamp updatedAt) {
        this.id        = id;
        this.firstName = firstName;
        this.lastName  = lastName;
        this.email     = email;
        this.phone     = phone;
        this.status    = status != null ? status: ClienteStatus.Activo;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    /**
     * Constructor de creación (sin id ni timestamp).
     */
    public Cliente(String firstName, String lastName, String email, String phone) {
        this(0, firstName, lastName, email, phone, ClienteStatus.Activo,null, null);
    }

    // ---- Getters / Setters ----------------------------------------

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public ClienteStatus getStatus() {
        return status;
    }

    public void setStatus(ClienteStatus status) {
        this.status = status;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
// ---- Utilidades -----------------------------------------------

    public boolean isActive() {
        return status == ClienteStatus.Activo;
    }
    /** Nombre completo concatenado (útil para tablas y combos). */
    public String getFullName() {
        return lastName + ", " + firstName;
    }

    @Override
    public String toString() {
        return getFullName();
    }
}
