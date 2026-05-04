package com.app.Model.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa una venta realizada en el sistema de compraventa.
 * Una venta tiene un empleado (profile), un cliente, una fecha
 * y una lista de detalles (artículos vendidos).
 */
public class Sale {

    private int id;
    private String profileId;   // UUID del empleado
    private int clienteId;
    private String clienteNombreAnon; // Para ventas anónimas (v6)
    private LocalDateTime saleDate;
    private List<SalesDetail> details;

    public Sale() {
        this.details = new ArrayList<>();
    }

    /**
     * Constructor de creación (sin id).
     */
    public Sale(String profileId, int clienteId, LocalDateTime saleDate) {
        this();
        this.profileId = profileId;
        this.clienteId = clienteId;
        this.saleDate  = saleDate;
    }

    /**
     * Constructor completo (desde base de datos).
     */
    public Sale(int id, String profileId, int clienteId, LocalDateTime saleDate) {
        this(profileId, clienteId, saleDate);
        this.id = id;
    }

    // ---- Getters / Setters ----------------------------------------

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getProfileId() { return profileId; }
    public void setProfileId(String profileId) { this.profileId = profileId; }

    public int getClienteId() { return clienteId; }
    public void setClienteId(int clienteId) { this.clienteId = clienteId; }

    public String getClienteNombreAnon() { return clienteNombreAnon; }
    public void setClienteNombreAnon(String clienteNombreAnon) { this.clienteNombreAnon = clienteNombreAnon; }

    public LocalDateTime getSaleDate() { return saleDate; }
    public void setSaleDate(LocalDateTime saleDate) { this.saleDate = saleDate; }

    public List<SalesDetail> getDetails() { return details; }
    public void setDetails(List<SalesDetail> details) { this.details = details; }

    // ---- Lógica de negocio ----------------------------------------

    /**
     * Agrega un detalle a la venta.
     *
     * @param detail detalle a agregar
     */
    public void addDetail(SalesDetail detail) {
        if (this.details == null) this.details = new ArrayList<>();
        this.details.add(detail);
    }

    /**
     * Calcula el total de la venta sumando todos sus detalles.
     *
     * @return total de la venta
     */
    public BigDecimal getTotal() {
        if (details == null) return BigDecimal.ZERO;
        return details.stream()
                .map(SalesDetail::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
