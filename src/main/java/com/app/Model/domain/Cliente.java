package com.app.Model.domain;

import com.app.Model.Enum.ClienteStatus;
import com.app.Model.Enum.RegistrationType;

import java.sql.Timestamp;

/**
 * Representa un cliente del sistema de compraventa/empeño.
 */
public class Cliente {

    private int id;
    private String cedula;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private String city;
    private ClienteStatus status;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    //---nuevos----
    private RegistrationType registrationType;

    /**
     * Constructor completo (usado al mapear desde la base de datos).
     */
    public Cliente(int id, String cedula, String firstName, String lastName,
                   String email, String phone, String address, String city,
                   ClienteStatus status, RegistrationType registrationType,
                   Timestamp createdAt, Timestamp updatedAt) {
        this.id        = id;
        this.cedula    = validateCedula(cedula);
        this.firstName = firstName;
        this.lastName  = lastName;
        this.email     = email;
        this.phone     = phone;
        this.address   = address;
        this.city      = city;

        this.status    = status != null ? status : ClienteStatus.Activo;
        this.registrationType = registrationType != null ? registrationType : RegistrationType.COMPLETO;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Constructor para crear cliente COMPLETO desde formulario
    public Cliente(String cedula, String firstName, String lastName, String email, String phone, String address, String city) {
        this(0, cedula, firstName, lastName, email, phone, address, city,
                ClienteStatus.Activo, RegistrationType.COMPLETO, null, null);
    }

    // Constructor para crear cliente RAPIDO (HU-25) — solo nombre obligatorio
    public static Cliente createRapido(String cedula, String firstName, String lastName, String phone) {
        return new Cliente(0, cedula, firstName, lastName, null, phone, null, null,
                ClienteStatus.Activo, RegistrationType.RAPIDO, null, null);
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

    public RegistrationType getRegistrationType() {
        return registrationType;
    }

    public void setRegistrationType(RegistrationType registrationType) {
        this.registrationType = registrationType;
    }

    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

    public String getCedula() {
        return cedula;
    }

    public void setCedula(String cedula) {
        this.cedula = validateCedula(cedula);
    }

    private String validateCedula(String val) {
        if (val == null) return null;
        if (!val.matches("^[0-9]+$")) {
            throw new IllegalArgumentException("La cédula solo permite números: " + val);
        }
        return val;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
    // ---- Utilidades -----------------------------------------------

    public boolean isActive() {
        return status == ClienteStatus.Activo;
    }

    public boolean isRegistrationComplete() { return registrationType == RegistrationType.COMPLETO; }

    public String getFullName() {
        if (lastName == null || lastName.isBlank()) return firstName;
        return lastName + ", " + firstName;
    }

    @Override
    public String toString() {
        return getFullName();
    }

}
